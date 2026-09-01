package com.waht.platform.agent.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waht.platform.agent.config.AgentProperties;
import com.waht.platform.agent.dto.AgentDraftRequest;
import com.waht.platform.agent.vo.AgentConversationDetailResponse;
import com.waht.platform.agent.vo.AgentConversationSummaryResponse;
import com.waht.platform.agent.vo.AgentTurnResponse;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.common.security.CurrentUser;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.function.Supplier;

/**
 * Java Agent Gateway 到 Python 服务的唯一 HTTP 出口，统一处理认证、超时和错误映射。
 */
@Component
public class AgentServiceClient {

    private static final String SERVICE_TOKEN_HEADER = "X-WAHT-Service-Token";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AgentServiceClient(AgentProperties properties, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMillis());
        requestFactory.setReadTimeout(properties.getReadTimeoutMillis());
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(SERVICE_TOKEN_HEADER, properties.getServiceToken())
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = objectMapper;
    }

    public List<AgentConversationSummaryResponse> listConversations(CurrentUser user) {
        AgentServiceEnvelope<List<AgentConversationSummaryResponse>> response = call(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/conversations")
                        .queryParam("userId", user.userId())
                        .queryParam("username", user.username())
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return requireData(response);
    }

    public AgentConversationSummaryResponse createConversation(CurrentUser user, String title) {
        AgentServicePayloads.CreateConversation payload = new AgentServicePayloads.CreateConversation(
                actor(user),
                title
        );
        AgentServiceEnvelope<AgentConversationSummaryResponse> response = call(() -> restClient.post()
                .uri("/internal/v1/conversations")
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return requireData(response);
    }

    public AgentConversationDetailResponse getConversation(String conversationId, CurrentUser user) {
        AgentServiceEnvelope<AgentConversationDetailResponse> response = call(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/v1/conversations/{conversationId}")
                        .queryParam("userId", user.userId())
                        .queryParam("username", user.username())
                        .build(conversationId))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return requireData(response);
    }

    public AgentTurnResponse run(
            String conversationId,
            CurrentUser user,
            String runId,
            String message,
            String delegationToken) {
        AgentServicePayloads.Run payload = new AgentServicePayloads.Run(
                actor(user),
                runId,
                message,
                delegationToken
        );
        AgentServiceEnvelope<AgentTurnResponse> response = call(() -> restClient.post()
                .uri("/internal/v1/conversations/{conversationId}/messages", conversationId)
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return requireData(response);
    }

    public AgentTurnResponse decideApproval(
            String approvalId,
            CurrentUser user,
            String decision,
            AgentDraftRequest draft,
            String delegationToken) {
        AgentServicePayloads.Approval payload = new AgentServicePayloads.Approval(
                actor(user),
                decision,
                delegationToken,
                draft
        );
        AgentServiceEnvelope<AgentTurnResponse> response = call(() -> restClient.post()
                .uri("/internal/v1/approvals/{approvalId}", approvalId)
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }));
        return requireData(response);
    }

    private AgentServicePayloads.Actor actor(CurrentUser user) {
        return new AgentServicePayloads.Actor(user.userId(), user.username());
    }

    private <T> AgentServiceEnvelope<T> call(Supplier<AgentServiceEnvelope<T>> request) {
        try {
            AgentServiceEnvelope<T> response = request.get();
            if (response == null) {
                throw new ServiceException(ErrorCode.SERVICE_UNAVAILABLE, "Agent 服务未返回响应");
            }
            if (response.code() != ErrorCode.SUCCESS.getCode()) {
                throw new ServiceException(response.code(), response.message());
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw mapRemoteError(ex);
        } catch (ResourceAccessException ex) {
            throw new ServiceException(ErrorCode.SERVICE_UNAVAILABLE, "无法连接 AI 学习助手服务");
        }
    }

    private <T> T requireData(AgentServiceEnvelope<T> response) {
        if (response.data() == null) {
            throw new ServiceException(ErrorCode.SERVICE_UNAVAILABLE, "Agent 服务返回数据为空");
        }
        return response.data();
    }

    private ServiceException mapRemoteError(RestClientResponseException ex) {
        try {
            JsonNode body = objectMapper.readTree(ex.getResponseBodyAsString());
            int code = body.path("code").asInt(ex.getStatusCode().value());
            String message = body.path("message").asText("Agent 服务调用失败");
            return new ServiceException(code, message);
        } catch (Exception ignored) {
            return new ServiceException(ErrorCode.SERVICE_UNAVAILABLE, "Agent 服务调用失败");
        }
    }
}
