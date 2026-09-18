import { request } from '@/api/http';
import type { PageResult } from '@/types/api';

export type AuditEvent = {
  id: string;
  occurredAt: string;
  userId: number | null;
  username: string | null;
  module: string;
  action: string;
  resourceId: string | null;
  outcome: 'SUCCESS' | 'FAILURE' | 'UNKNOWN';
  operationState: string | null;
  httpStatus: number;
  businessCode: number | null;
  requestId: string;
  method: string;
  route: string;
  clientIp: string | null;
  durationMs: number;
};

export const auditActions: Record<string, string> = {
  LOGIN: '登录', REGISTER: '注册', NOTE_CREATE: '新增笔记', NOTE_UPDATE: '修改笔记',
  NOTE_DELETE: '删除笔记', NOTE_PUBLISH: '发布笔记', NOTE_UNPUBLISH: '撤回草稿',
  CONVERSATION_CREATE: '新建 AI 会话', AGENT_MESSAGE: '发送 AI 消息',
  AGENT_APPROVAL: '处理 AI 审批', AGENT_DRAFT_CREATE: 'AI 创建草稿', QUERY: '查询审计',
};

export type AuditFilters = {
  username: string; action: string; outcome: string; requestId: string; from: string; to: string;
};

export function listAuditEvents(filters: AuditFilters, page: number, signal?: AbortSignal) {
  const params = new URLSearchParams({ page: String(page), pageSize: '20' });
  for (const [key, value] of Object.entries(filters)) {
    if (value.trim()) params.set(key, key === 'from' || key === 'to' ? new Date(value).toISOString() : value.trim());
  }
  return request<PageResult<AuditEvent>>(`/admin/audit-logs?${params}`, { signal });
}
