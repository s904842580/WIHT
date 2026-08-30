// NoteDraftForm 是写作表单在浏览器本地备份中的稳定结构。
export type NoteDraftForm = {
  title: string;
  slug: string;
  summary: string;
  content: string;
  categoryId: string;
  tagIds: number[];
};

// NoteDraftSnapshot 记录本地备份内容和生成时间，用于刷新页面后的恢复提示。
export type NoteDraftSnapshot = {
  form: NoteDraftForm;
  savedAt: string;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}

function isNumberArray(value: unknown): value is number[] {
  return Array.isArray(value) && value.every((item: unknown) => typeof item === 'number' && Number.isInteger(item));
}

function isNoteDraftForm(value: unknown): value is NoteDraftForm {
  if (!isRecord(value)) {
    return false;
  }

  return (
    typeof value.title === 'string' &&
    typeof value.slug === 'string' &&
    typeof value.summary === 'string' &&
    typeof value.content === 'string' &&
    typeof value.categoryId === 'string' &&
    isNumberArray(value.tagIds)
  );
}

function isNoteDraftSnapshot(value: unknown): value is NoteDraftSnapshot {
  return isRecord(value) && typeof value.savedAt === 'string' && isNoteDraftForm(value.form);
}

// 草稿 key 同时包含用户和笔记 ID，避免不同账号或不同文章互相覆盖。
export function createNoteDraftKey(userId: number, noteId: number | null): string {
  return `waht:note-draft:${userId}:${noteId ?? 'new'}`;
}

export function readNoteDraft(key: string): NoteDraftSnapshot | null {
  try {
    const rawValue = window.localStorage.getItem(key);
    if (!rawValue) {
      return null;
    }
    const parsedValue: unknown = JSON.parse(rawValue);
    return isNoteDraftSnapshot(parsedValue) ? parsedValue : null;
  } catch {
    return null;
  }
}

export function writeNoteDraft(key: string, form: NoteDraftForm, savedAt: string): boolean {
  try {
    const snapshot: NoteDraftSnapshot = { form, savedAt };
    window.localStorage.setItem(key, JSON.stringify(snapshot));
    return true;
  } catch {
    return false;
  }
}

export function removeNoteDraft(key: string): void {
  window.localStorage.removeItem(key);
}

export function isSameNoteDraft(left: NoteDraftForm, right: NoteDraftForm): boolean {
  if (
    left.title !== right.title ||
    left.slug !== right.slug ||
    left.summary !== right.summary ||
    left.content !== right.content ||
    left.categoryId !== right.categoryId ||
    left.tagIds.length !== right.tagIds.length
  ) {
    return false;
  }

  return left.tagIds.every((tagId: number, index: number) => tagId === right.tagIds[index]);
}