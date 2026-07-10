import { request } from '@/api/http';
import type { NoteCategory, NoteDetail, NoteSummary, NoteTag } from '@/types/api';

// notesApi 只放学习笔记公开查询接口。
export const notesApi = {
  listNotes() {
    return request<NoteSummary[]>('/notes', {
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
};
