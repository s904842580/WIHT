import { request } from '@/api/http';
import type {
  AgentApprovalDecision,
  AgentConversationDetail,
  AgentConversationSummary,
  AgentDraft,
  AgentTurn,
} from '@/types/api';

// 审批参数：EDIT 会提交用户修改后的完整草稿，其他决定无需 draft。
export type DecideAgentApprovalParams = {
  decision: AgentApprovalDecision;
  draft?: AgentDraft;
};

// agentApi 只调用 Java Gateway，浏览器不会直接访问 Python 服务。
export const agentApi = {
  listConversations() {
    return request<AgentConversationSummary[]>('/agent/conversations');
  },

  createConversation(title?: string) {
    return request<AgentConversationSummary>('/agent/conversations', {
      method: 'POST',
      body: JSON.stringify({ title }),
    });
  },

  getConversation(conversationId: string) {
    return request<AgentConversationDetail>(`/agent/conversations/${conversationId}`);
  },

  sendMessage(conversationId: string, message: string) {
    return request<AgentTurn>(`/agent/conversations/${conversationId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ message }),
    });
  },

  decideApproval(approvalId: string, params: DecideAgentApprovalParams) {
    return request<AgentTurn>(`/agent/approvals/${approvalId}`, {
      method: 'POST',
      body: JSON.stringify(params),
    });
  },
};
