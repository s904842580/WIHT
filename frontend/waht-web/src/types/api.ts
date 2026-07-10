// 后端统一响应结构，和 Java 的 ApiResponse 对齐。
export type ApiResponse<T> = {
  code: number;
  message: string;
  data: T;
  timestamp: string;
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
