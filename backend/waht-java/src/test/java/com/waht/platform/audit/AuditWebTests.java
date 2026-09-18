package com.waht.platform.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.common.exception.*;
import com.waht.platform.common.security.*;
import com.waht.platform.common.web.RequestIdFilter;
import com.waht.platform.vo.LoginResponse;
import com.waht.platform.vo.UserInfoResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.filter.ForwardedHeaderFilter;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuditWebTests {
    AuditWriter writer;
    AuditMapper mapper;
    ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    SimpleMeterRegistry metrics;
    AuditInterceptor interceptor;
    MockMvc mvc;

    @BeforeEach void setup() {
        writer = mock(AuditWriter.class);
        mapper = mock(AuditMapper.class);
        metrics = new SimpleMeterRegistry();
        interceptor = new AuditInterceptor(writer, json, metrics);
        HandlerInterceptor authentication = new HandlerInterceptor() {
            @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (request.getRequestURI().equals("/api/test/denied"))
                    throw new ServiceException(ErrorCode.UNAUTHORIZED, "unauthenticated");
                if (request.getRequestURI().equals("/api/test/note/42"))
                    request.setAttribute(AuthConstants.CURRENT_USER_ATTRIBUTE, new CurrentUser(7L, "author", "USER"));
                return true;
            }
        };
        mvc = MockMvcBuilders.standaloneSetup(new Endpoints(), new AuditController(mapper))
                .setControllerAdvice(new GlobalExceptionHandler(), new AuditResponseAdvice())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .addInterceptors(interceptor, authentication)
                .addFilters(new RequestIdFilter(), new ForwardedHeaderFilter()).build();
    }

    @Test void recordsVerifiedIdentityAndOnlyAllowlistedMetadata() throws Exception {
        mvc.perform(post("/api/test/note/42").queryParam("password", "query-secret")
                        .header("Authorization", "Bearer header-secret").header("X-Request-Id", "test-request-42")
                        .header("X-Forwarded-For", "203.0.113.88")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"private-note-secret\"}"))
                .andExpect(status().isOk());
        AuditEvent event = captured();
        assertThat(event.userId).isEqualTo(7L);
        assertThat(event.username).isEqualTo("author");
        assertThat(event.resourceId).isEqualTo("42");
        assertThat(event.route).isEqualTo("/api/test/note/{id}");
        assertThat(event.clientIp).isEqualTo("127.0.0.1");
        assertThat(event.requestId).isEqualTo("test-request-42");
        assertThat(event.outcome).isEqualTo("SUCCESS");
        assertThat(event.durationMs).isNotNegative();
        assertThat(json.writeValueAsString(event)).doesNotContain("query-secret", "header-secret", "private-note-secret", "response-secret");
    }

    @Test void denialBeforeControllerIsAuditedWithoutClaimingIdentity() throws Exception {
        mvc.perform(post("/api/test/denied")).andExpect(status().isUnauthorized());
        AuditEvent event = captured();
        assertThat(event.outcome).isEqualTo("FAILURE");
        assertThat(event.businessCode).isEqualTo(401);
        assertThat(event.userId).isNull();
        assertThat(event.username).isNull();
    }

    @Test void successfulLoginCapturesActorButNeverToken() throws Exception {
        mvc.perform(post("/api/test/login")).andExpect(status().isOk());
        AuditEvent event = captured();
        assertThat(event.username).isEqualTo("alice");
        assertThat(event.userId).isEqualTo(5L);
        assertThat(json.writeValueAsString(event)).doesNotContain("login-token-secret");
    }

    @Test void businessRejectionIsFailureNotUnknown() throws Exception {
        mvc.perform(post("/api/test/reject")).andExpect(status().isBadRequest());
        assertThat(captured().outcome).isEqualTo("FAILURE");
    }

    @Test void upstreamFailureIsUnknownNotProofOfRollback() throws Exception {
        mvc.perform(post("/api/test/unavailable")).andExpect(status().isServiceUnavailable());
        assertThat(captured().outcome).isEqualTo("UNKNOWN");
    }

    @Test void missingResponseEnvelopeDoesNotClaimSuccessAndRecordsOnce() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerMethod handler = new HandlerMethod(new Endpoints(), Endpoints.class.getMethod("login"));
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/test/login");
        interceptor.preHandle(request, response, handler);
        interceptor.afterCompletion(request, response, handler, null);
        interceptor.afterCompletion(request, response, handler, null);
        assertThat(captured().outcome).isEqualTo("UNKNOWN");
    }

    @Test void auditSinkFailureDoesNotChangeBusinessResponse() throws Exception {
        doThrow(new IllegalStateException("sensitive-jdbc-error")).when(writer).append(any());
        mvc.perform(post("/api/test/login")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));
        assertThat(metrics.get("waht.audit.write.failures").counter().count()).isEqualTo(1);
    }

    @Test void auditApiRequiresLogin() throws Exception {
        mvc.perform(get("/api/admin/audit-logs")).andExpect(status().isUnauthorized());
        verifyNoInteractions(mapper);
        assertThat(captured().outcome).isEqualTo("FAILURE");
    }

    @Test void regularUserCannotReadLogs() throws Exception {
        mvc.perform(get("/api/admin/audit-logs").requestAttr(AuthConstants.CURRENT_USER_ATTRIBUTE,
                new CurrentUser(7L, "author", "USER"))).andExpect(status().isForbidden());
        verifyNoInteractions(mapper);
        assertThat(captured().businessCode).isEqualTo(403);
    }

    @Test void malformedAuditTimeIs400Not500() throws Exception {
        mvc.perform(get("/api/admin/audit-logs").param("from", "not-a-date-secret")
                .requestAttr(AuthConstants.CURRENT_USER_ATTRIBUTE, new CurrentUser(1L, "admin", "ADMIN")))
                .andExpect(status().isBadRequest());
        assertThat(captured().outcome).isEqualTo("FAILURE");
        verifyNoInteractions(mapper);
    }

    @Test void unannotatedReadsAreNotRecorded() throws Exception {
        mvc.perform(get("/api/test/public")).andExpect(status().isOk());
        verifyNoInteractions(writer);
    }

    private AuditEvent captured() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(writer).append(captor.capture());
        return captor.getValue();
    }

    @RestController static class Endpoints {
        @PostMapping("/api/test/note/{id}") @Audited(module="NOTE", action="NOTE_UPDATE", resource="id")
        public BaseResponse<String> note(@RequestBody String body) { return BaseResponse.success("response-secret"); }

        @PostMapping("/api/test/denied") @Audited(module="NOTE", action="NOTE_DELETE")
        public BaseResponse<Void> denied() { throw new AssertionError("must not run"); }

        @PostMapping("/api/test/login") @Audited(module="AUTH", action="LOGIN")
        public BaseResponse<LoginResponse> login() {
            UserInfoResponse user = new UserInfoResponse(); user.setId(5L); user.setUsername("alice");
            return BaseResponse.success(new LoginResponse("Bearer", "login-token-secret", 7200L, user));
        }

        @PostMapping("/api/test/reject") @Audited(module="NOTE", action="NOTE_CREATE")
        public BaseResponse<Void> reject() { throw new ServiceException(ErrorCode.BUSINESS_ERROR, "rejected"); }

        @PostMapping("/api/test/unavailable") @Audited(module="AGENT", action="AGENT_MESSAGE")
        public BaseResponse<Void> unavailable() { throw new ServiceException(ErrorCode.SERVICE_UNAVAILABLE, "timeout"); }

        @GetMapping("/api/test/public") public BaseResponse<Void> publicRead() { return BaseResponse.success(); }
    }
}
