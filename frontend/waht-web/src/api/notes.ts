import { request } from '@/api/http';
import type {
  ManagedNoteSummary,
  NoteCategory,
  NoteDetail,
  NoteEditor,
  NoteSummary,
  NoteTag,
  PageResult,
} from '@/types/api';

// 笔记编辑保存参数，和 Java NoteSaveRequest 保持一致。
export type NoteSaveParams = {
  title: string;
  slug?: string;
  summary?: string;
  content: string;
  categoryId: number;
  tagIds: number[];
};

// 公开笔记分页查询参数，所有筛选字段都对应后端同名 query 参数。
export type NoteListParams = {
  page: number;
  pageSize: number;
  keyword?: string;
  categoryId?: number;
  tagId?: number;
};

function buildNoteListQuery(params: NoteListParams): string {
  const searchParams = new URLSearchParams({
    page: String(params.page),
    pageSize: String(params.pageSize),
  });

  if (params.keyword) {
    searchParams.set('keyword', params.keyword);
  }
  if (params.categoryId !== undefined) {
    searchParams.set('categoryId', String(params.categoryId));
  }
  if (params.tagId !== undefined) {
    searchParams.set('tagId', String(params.tagId));
  }

  return searchParams.toString();
}

// notesApi 集中维护公开阅读和当前作者工作台接口。
export const notesApi = {
  listNotes(params: NoteListParams) {
    return request<PageResult<NoteSummary>>(`/notes?${buildNoteListQuery(params)}`, {
      auth: false,
    });
  },

  getNote(slug: string) {
    return request<NoteDetail>(`/notes/${slug}`, {
      auth: false,
    });
  },

  listCategories() {
    return request<NoteCategory[]>('/note-categories', {
      auth: false,
    });
  },

  listTags() {
    return request<NoteTag[]>('/note-tags', {
      auth: false,
    });
  },

  listMyNotes() {
    return request<ManagedNoteSummary[]>('/my/notes');
  },

  getMyNote(noteId: number) {
    return request<NoteEditor>(`/my/notes/${noteId}`);
  },

  createNote(params: NoteSaveParams) {
    return request<NoteEditor>('/my/notes', {
      method: 'POST',
      body: JSON.stringify(params),
    });
  },

  updateNote(noteId: number, params: NoteSaveParams) {
    return request<NoteEditor>(`/my/notes/${noteId}`, {
      method: 'PUT',
      body: JSON.stringify(params),
    });
  },

  publishNote(noteId: number) {
    return request<NoteEditor>(`/my/notes/${noteId}/publish`, {
      method: 'POST',
    });
  },

  moveNoteToDraft(noteId: number) {
    return request<NoteEditor>(`/my/notes/${noteId}/draft`, {
      method: 'POST',
    });
  },

  deleteNote(noteId: number) {
    return request<void>(`/my/notes/${noteId}`, {
      method: 'DELETE',
    });
  },
};
