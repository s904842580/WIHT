package com.waht.platform.agent.service;

import com.waht.platform.agent.client.AgentServiceClient;
import com.waht.platform.agent.dto.AgentApprovalDecision;
import com.waht.platform.agent.dto.DecideAgentApprovalRequest;
import com.waht.platform.agent.vo.AgentConversationDetailResponse;
import com.waht.platform.agent.vo.AgentConversationSummaryResponse;
import com.waht.platform.agent.vo.AgentTurnResponse;
import com.waht.platform.common.exception.ErrorCode;
import com.waht.platform.common.exception.ServiceException;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 浏览器访问 Agent 的 Java 门面：复用登录态，并为每次工具调用签发短期最小权限令牌。
 */
@Service
public class AgentGatewayService {

    private static final Set<String> NOTE_READ_SCOPES = Set.of("note:read");
    private static final Set<String> NOTE_DRAFT_SCOPES = Set.of("note:create-draft");

    private final AgentServiceClient agentServiceClient;
    private final JwtTokenProvider jwtTokenProvider;

    public AgentGatewayService(AgentServiceClient agentServiceClient, JwtTokenProvider jwtTokenProvider) {
        this.agentServiceClient = agentServiceClient;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public List<AgentConversationSummaryResponse> listConversations(CurrentUser user) {
        return agentServiceClient.listConversations(user);
    }

    public AgentConversationSummaryResponse createConversation(CurrentUser user, String title) {
        return agentServiceClient.createConversation(user, title);
    }

    public AgentConversationDetailResponse getConversation(String conversationId, CurrentUser user) {
        return agentServiceClient.getConversation(conversationId, user);
    }

    public AgentTurnResponse sendMessage(String conversationId, String message, CurrentUser user) {
        String runId = UUID.randomUUID().toString();
        String delegationToken = jwtTokenProvider.createAgentDelegationToken(user, runId, NOTE_READ_SCOPES);
        return agentServiceClient.run(conversationId, user, runId, message, delegationToken);
    }

    public AgentTurnResponse decideApproval(
            String approvalId,
            DecideAgentApprovalRequest request,
            CurrentUser user) {
        if (request.decision() == AgentApprovalDecision.EDIT && request.draft() == null) {
            throw new ServiceException(ErrorCode.BAD_REQUEST, "修改后确认时必须提交草稿内容");
        }
        String authorizationId = UUID.randomUUID().toString();
        String delegationToken = jwtTokenProvider.createAgentDelegationToken(
                user,
                authorizationId,
                NOTE_DRAFT_SCOPES
        );
        return agentServiceClient.decideApproval(
                approvalId,
                user,
                request.decision().name(),
                request.draft(),
                delegationToken
        );
    }
}
