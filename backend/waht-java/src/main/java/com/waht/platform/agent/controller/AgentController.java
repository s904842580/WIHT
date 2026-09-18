package com.waht.platform.agent.controller;

import com.waht.platform.agent.dto.CreateAgentConversationRequest;
import com.waht.platform.agent.dto.DecideAgentApprovalRequest;
import com.waht.platform.agent.dto.SendAgentMessageRequest;
import com.waht.platform.agent.service.AgentGatewayService;
import com.waht.platform.agent.vo.AgentConversationDetailResponse;
import com.waht.platform.agent.vo.AgentConversationSummaryResponse;
import com.waht.platform.agent.vo.AgentTurnResponse;
import com.waht.platform.common.api.BaseResponse;
import com.waht.platform.audit.Audited;
import com.waht.platform.common.security.CurrentUser;
import com.waht.platform.common.security.LoginUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 登录用户的学习助手入口；浏览器只访问 Java，不直接持有 Python 服务凭据。
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentGatewayService agentGatewayService;

    public AgentController(AgentGatewayService agentGatewayService) {
        this.agentGatewayService = agentGatewayService;
    }

    @GetMapping("/conversations")
    public BaseResponse<List<AgentConversationSummaryResponse>> listConversations(
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(agentGatewayService.listConversations(currentUser));
    }

    @PostMapping("/conversations")
    @Audited(module = "AGENT", action = "CONVERSATION_CREATE")
    public BaseResponse<AgentConversationSummaryResponse> createConversation(
            @Valid @RequestBody CreateAgentConversationRequest request,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(agentGatewayService.createConversation(currentUser, request.title()));
    }

    @GetMapping("/conversations/{conversationId}")
    public BaseResponse<AgentConversationDetailResponse> getConversation(
            @PathVariable String conversationId,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(agentGatewayService.getConversation(conversationId, currentUser));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @Audited(module = "AGENT", action = "AGENT_MESSAGE", resource = "conversationId")
    public BaseResponse<AgentTurnResponse> sendMessage(
            @PathVariable String conversationId,
            @Valid @RequestBody SendAgentMessageRequest request,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(agentGatewayService.sendMessage(conversationId, request.message(), currentUser));
    }

    @PostMapping("/approvals/{approvalId}")
    @Audited(module = "AGENT", action = "AGENT_APPROVAL", resource = "approvalId")
    public BaseResponse<AgentTurnResponse> decideApproval(
            @PathVariable String approvalId,
            @Valid @RequestBody DecideAgentApprovalRequest request,
            @LoginUser CurrentUser currentUser) {
        return BaseResponse.success(agentGatewayService.decideApproval(approvalId, request, currentUser));
    }
}
