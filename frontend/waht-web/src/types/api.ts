// 后端统一响应结构，和 Java 的 BaseResponse 对齐。
export type BaseResponse<T> = {
  code: number;
  message: string;
  data: T;
  timestamp: string;
};

// 通用分页结构，和 Java PageResponse 对齐。
export type PageResult<T> = {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasPrevious: boolean;
  hasNext: boolean;
};

// 登录接口返回的用户信息。
export type UserInfo = {
  id: number;
  username: string;
  nickname: string;
  avatarUrl?: string | null;
  role: 'ADMIN' | 'USER' | string;
};

// 登录接口返回的数据结构。
export type LoginResult = {
  tokenType: 'Bearer' | string;
  token: string;
  expiresIn: number;
  user: UserInfo;
};

// 笔记分类，来自 /api/note-categories。
export type NoteCategory = {
  id: number;
  name: string;
  slug: string;
  description?: string | null;
};

// 笔记标签，来自 /api/note-tags。
export type NoteTag = {
  id: number;
  name: string;
  slug: string;
  color?: string | null;
};

// 笔记列表项，来自 /api/notes。
export type NoteSummary = {
  id: number;
  title: string;
  slug: string;
  summary?: string | null;
  category?: NoteCategory | null;
  tags: NoteTag[];
  viewCount: number;
  publishedAt?: string | null;
};

// 笔记详情，来自 /api/notes/{slug}。
export type NoteDetail = NoteSummary & {
  content: string;
};

// 笔记生命周期状态；草稿仅作者可见，发布后才会进入公开列表。
export type NoteStatus = 'DRAFT' | 'PUBLISHED' | 'HIDDEN';

// 当前用户的笔记列表项，包含写作工作台需要的状态和更新时间。
export type ManagedNoteSummary = {
  id: number;
  title: string;
  slug: string;
  summary?: string | null;
  status: NoteStatus;
  category?: NoteCategory | null;
  tags: NoteTag[];
  publishedAt?: string | null;
  updatedAt?: string | null;
};

// 编辑器完整数据，分类和标签使用 ID 直接回填表单控件。
export type NoteEditor = {
  id: number;
  title: string;
  slug: string;
  summary?: string | null;
  content: string;
  categoryId: number;
  tagIds: number[];
  status: NoteStatus;
  publishedAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

// 项目链接，来自 /api/projects。
export type ProjectLink = {
  id: number;
  linkType: 'REPO' | 'DEMO' | 'DOC' | 'VIDEO' | 'OTHER' | string;
  title: string;
  url: string;
  sortOrder: number;
};

// 技术栈，来自 /api/tech-stacks。
export type TechStack = {
  id: number;
  name: string;
  slug: string;
  techType: 'LANGUAGE' | 'FRAMEWORK' | 'DATABASE' | 'TOOL' | 'OTHER' | string;
  iconUrl?: string | null;
};

// 项目列表项，来自 /api/projects。
export type ProjectSummary = {
  id: number;
  name: string;
  slug: string;
  summary?: string | null;
  coverAssetId?: number | null;
  sortOrder: number;
  startedAt?: string | null;
  endedAt?: string | null;
  techStacks: TechStack[];
  links: ProjectLink[];
};

// 项目详情，来自 /api/projects/{slug}。
export type ProjectDetail = ProjectSummary & {
  description?: string | null;
};

// Agent 运行状态；WAITING_APPROVAL 表示草稿尚未写入笔记表。
export type AgentRunStatus =
  | 'CREATED'
  | 'RUNNING'
  | 'WAITING_APPROVAL'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED';

// Agent 回答引用的现有学习笔记。
export type AgentNoteSource = {
  noteId: number;
  title: string;
  slug: string;
  status: NoteStatus;
};

// Agent 生成的结构化笔记草稿，批准前可继续修改。
export type AgentDraft = {
  title: string;
  summary: string;
  content: string;
  categoryId: number;
  tagIds: number[];
};

// 高风险写操作的审批信息。
export type AgentApproval = {
  id: string;
  status: 'PENDING' | 'APPROVED' | 'EDITED' | 'REJECTED' | string;
  action: 'CREATE_NOTE_DRAFT' | string;
  draft: AgentDraft;
  createdNoteId?: number | null;
};

// Agent 会话中的一条消息。
export type AgentMessage = {
  id: string;
  role: 'USER' | 'ASSISTANT' | string;
  content: string;
  sources: AgentNoteSource[];
  createdAt: string;
};

// 左侧会话列表使用的摘要。
export type AgentConversationSummary = {
  id: string;
  title: string;
  status: 'ACTIVE' | string;
  createdAt: string;
  updatedAt: string;
};

// 会话详情包含消息和最多一个当前待审批草稿。
export type AgentConversationDetail = AgentConversationSummary & {
  messages: AgentMessage[];
  pendingApproval?: AgentApproval | null;
};

// 发送消息或处理审批后的完整结果。
export type AgentTurn = {
  conversationId: string;
  runId: string;
  status: AgentRunStatus;
  answer: string;
  sources: AgentNoteSource[];
  approval?: AgentApproval | null;
};

export type AgentApprovalDecision = 'APPROVE' | 'EDIT' | 'REJECT';
