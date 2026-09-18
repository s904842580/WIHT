package com.waht.platform.audit;

import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.vo.LoginResponse;
import com.waht.platform.vo.NoteEditorResponse;
import com.waht.platform.agent.vo.AgentTurnResponse;
import com.waht.platform.agent.vo.AgentConversationSummaryResponse;
import com.waht.platform.agent.vo.AgentCreatedNoteResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import java.util.Set;

@RestControllerAdvice
public class AuditResponseAdvice implements ResponseBodyAdvice<Object> {
    private static final Set<String> SAFE_STATES = Set.of("CREATED", "RUNNING", "WAITING_APPROVAL",
            "COMPLETED", "FAILED", "CANCELLED", "PENDING", "APPROVED", "EDITED", "REJECTED", "DRAFT", "PUBLISHED");

    @Override
    public boolean supports(MethodParameter method, Class<? extends HttpMessageConverter<?>> converter) { return true; }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter method, MediaType type,
                                  Class<? extends HttpMessageConverter<?>> converter,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (request instanceof ServletServerHttpRequest servlet && body instanceof BaseResponse<?> envelope
                && servlet.getServletRequest().getAttribute(AuditInterceptor.CONTEXT) instanceof AuditInterceptor.Context context) {
            AuditEvent event = context.event;
            event.businessCode = envelope.getCode();
            if (envelope.getCode() == 0) {
                Object data = envelope.getData();
                if (data instanceof LoginResponse login && login.getUser() != null) {
                    event.userId = login.getUser().getId();
                    event.username = AuditInterceptor.clean(login.getUser().getUsername(), 50);
                    event.resourceId = String.valueOf(event.userId);
                } else if (data instanceof NoteEditorResponse note) {
                    event.resourceId = String.valueOf(note.getId());
                    event.operationState = safeState(note.getStatus());
                } else if (data instanceof AgentCreatedNoteResponse note) {
                    event.resourceId = String.valueOf(note.id());
                    event.operationState = safeState(note.status());
                } else if (data instanceof AgentConversationSummaryResponse conversation) {
                    event.resourceId = AuditInterceptor.clean(conversation.id(), 64);
                } else if (data instanceof AgentTurnResponse turn) {
                    event.operationState = safeState(turn.approval() == null ? turn.status() : turn.approval().status());
                }
            }
        }
        return body;
    }

    private static String safeState(String state) { return state != null && SAFE_STATES.contains(state) ? state : null; }
}
