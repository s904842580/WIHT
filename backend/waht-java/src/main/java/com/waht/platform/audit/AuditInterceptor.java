package com.waht.platform.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waht.platform.common.security.AuthConstants;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.security.AgentDelegation;
import com.waht.platform.common.web.RequestIdFilter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditInterceptor implements HandlerInterceptor {
    static final String CONTEXT = AuditInterceptor.class.getName();
    private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);
    private final AuditWriter writer;
    private final ObjectMapper json;
    private final Counter writeFailures;

    static final class Context {
        final AuditEvent event = new AuditEvent();
        final long started = System.nanoTime();
    }

    public AuditInterceptor(AuditWriter writer, ObjectMapper json, MeterRegistry metrics) {
        this.writer = writer;
        this.json = json;
        this.writeFailures = metrics.counter("waht.audit.write.failures");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod()) || !(handler instanceof HandlerMethod method)) return true;
        Audited operation = method.getMethodAnnotation(Audited.class);
        if (operation == null) return true;
        Context context = new Context();
        AuditEvent event = context.event;
        event.id = UUID.randomUUID().toString();
        event.occurredAt = LocalDateTime.now(ZoneOffset.UTC);
        event.module = operation.module();
        event.action = operation.action();
        event.method = request.getMethod();
        event.route = clean(String.valueOf(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)), 200);
        // Do not trust caller-controlled proxy headers.
        ServletRequest peer = request;
        while (peer instanceof ServletRequestWrapper wrapper) peer = wrapper.getRequest();
        event.clientIp = clean(peer.getRemoteAddr(), 64);
        Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables instanceof Map<?, ?> map && map.get(operation.resource()) instanceof String id) {
            event.resourceId = id.matches("[A-Za-z0-9_-]{1,64}") ? id : "INVALID_ID";
        }
        request.setAttribute(CONTEXT, context);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(request.getAttribute(CONTEXT) instanceof Context context)) return;
        request.removeAttribute(CONTEXT);
        AuditEvent event = context.event;
        Object actor = request.getAttribute(AuthConstants.CURRENT_USER_ATTRIBUTE);
        if (actor instanceof CurrentUser user) {
            event.userId = user.userId();
            event.username = clean(user.username(), 50);
        } else if (request.getAttribute(AuthConstants.AGENT_DELEGATION_ATTRIBUTE) instanceof AgentDelegation user) {
            event.userId = user.userId();
            event.username = clean(user.username(), 50);
        }
        event.httpStatus = response.getStatus();
        String requestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        event.requestId = requestId != null && requestId.matches("[A-Za-z0-9._-]{1,64}") ? requestId : event.id;
        event.durationMs = Math.max(0, (System.nanoTime() - context.started) / 1_000_000);
        // A server/transport failure cannot prove whether a remote operation committed.
        if (ex != null || event.httpStatus >= 500 || (event.businessCode != null && event.businessCode >= 500 && event.businessCode < 600)) {
            event.outcome = "UNKNOWN";
        } else if (event.httpStatus >= 400 || (event.businessCode != null && event.businessCode != 0)) {
            event.outcome = "FAILURE";
        } else {
            event.outcome = Integer.valueOf(0).equals(event.businessCode) ? "SUCCESS" : "UNKNOWN";
        }
        try {
            writer.append(event);
        } catch (Exception failure) {
            writeFailures.increment();
            // Allowlisted metadata only; JDBC exception text may contain sensitive SQL/data.
            try {
                log.error("AUDIT_WRITE_FAILED event={}", json.writeValueAsString(event));
            } catch (Exception encodingFailure) {
                log.error("AUDIT_WRITE_FAILED id={} requestId={}", event.id, event.requestId);
            }
        }
    }

    static String clean(String value, int maxLength) {
        if (value == null) return null;
        String safe = value.replaceAll("[\\p{Cntrl}]", " ");
        return safe.substring(0, Math.min(safe.length(), maxLength));
    }
}
