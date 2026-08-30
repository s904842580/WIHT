import { notesApi, type NoteSaveParams } from '@/api/notes';
import { MarkdownEditor } from '@/components/MarkdownEditor';
import { SectionHeader } from '@/components/SectionHeader';
import { useAuth } from '@/features/auth/AuthProvider';
import type { NoteEditor } from '@/types/api';
import { getErrorMessage } from '@/utils/error';
import {
  createNoteDraftKey,
  isSameNoteDraft,
  readNoteDraft,
  removeNoteDraft,
  writeNoteDraft,
} from '@/utils/noteDraft';
import type { NoteDraftForm, NoteDraftSnapshot } from '@/utils/noteDraft';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Eye, History, Loader2, Save, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';

type NoteFormState = NoteDraftForm;
type NoteFormUpdater = (current: NoteFormState) => NoteFormState;

type SaveAction = {
  publish: boolean;
};

const SLUG_PATTERN = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;

const emptyForm: NoteFormState = {
  title: '',
  slug: '',
  summary: '',
  content: '',
  categoryId: '',
  tagIds: [],
};

function toFormState(note: NoteEditor): NoteFormState {
  return {
    title: note.title,
    slug: note.slug,
    summary: note.summary ?? '',
    content: note.content,
    categoryId: String(note.categoryId),
    tagIds: note.tagIds,
  };
}

function formatBackupTime(isoTime: string): string {
  return new Date(isoTime).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

// NoteEditorPage 负责数据库保存、发布流程和本地恢复；Markdown 操作由 MarkdownEditor 组件处理。
export function NoteEditorPage() {
  const { noteId: noteIdParam } = useParams();
  const parsedNoteId = noteIdParam ? Number(noteIdParam) : null;
  const isEditing = parsedNoteId !== null && Number.isInteger(parsedNoteId) && parsedNoteId > 0;
  const hasInvalidNoteId = noteIdParam !== undefined && !isEditing;
  const noteId = isEditing ? parsedNoteId : null;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const draftKey = user ? createNoteDraftKey(user.id, noteId) : null;

  const [form, setForm] = useState<NoteFormState>(emptyForm);
  const [localError, setLocalError] = useState<string>('');
  const [initializedDraftKey, setInitializedDraftKey] = useState<string | null>(null);
  const [hasUserEdited, setHasUserEdited] = useState<boolean>(false);
  const [localBackupAt, setLocalBackupAt] = useState<string | null>(null);
  const [backupFailed, setBackupFailed] = useState<boolean>(false);
  const [recoveryDraft, setRecoveryDraft] = useState<NoteDraftSnapshot | null>(null);

  const noteQuery = useQuery({
    queryKey: ['my-notes', noteId],
    queryFn: () => notesApi.getMyNote(noteId ?? 0),
    enabled: isEditing,
  });
  const categoriesQuery = useQuery({
    queryKey: ['note-categories'],
    queryFn: notesApi.listCategories,
  });
  const tagsQuery = useQuery({
    queryKey: ['note-tags'],
    queryFn: notesApi.listTags,
  });

  // 初始化顺序必须先拿到数据库内容，再判断本地草稿是否值得提示恢复。
  useEffect(() => {
    if (!draftKey || initializedDraftKey === draftKey || hasInvalidNoteId) {
      return;
    }
    if (isEditing && !noteQuery.data) {
      return;
    }
    if (!isEditing && categoriesQuery.isLoading) {
      return;
    }

    const initialForm = isEditing && noteQuery.data
      ? toFormState(noteQuery.data)
      : {
          ...emptyForm,
          categoryId: categoriesQuery.data?.[0] ? String(categoriesQuery.data[0].id) : '',
        };

    const storedDraft = readNoteDraft(draftKey);
    setForm(initialForm);
    setRecoveryDraft(storedDraft && !isSameNoteDraft(storedDraft.form, initialForm) ? storedDraft : null);
    setHasUserEdited(false);
    setLocalBackupAt(null);
    setBackupFailed(false);
    setInitializedDraftKey(draftKey);
  }, [
    categoriesQuery.data,
    categoriesQuery.isLoading,
    draftKey,
    hasInvalidNoteId,
    initializedDraftKey,
    isEditing,
    noteQuery.data,
  ]);

  // 编辑停止 800ms 后写入本地恢复点，不会代替数据库中的正式保存。
  useEffect(() => {
    if (
      !draftKey ||
      initializedDraftKey !== draftKey ||
      !hasUserEdited ||
      recoveryDraft
    ) {
      return;
    }

    const timer = window.setTimeout(() => {
      const savedAt = new Date().toISOString();
      const succeeded = writeNoteDraft(draftKey, form, savedAt);
      setBackupFailed(!succeeded);
      setLocalBackupAt(succeeded ? savedAt : null);
    }, 800);

    return () => window.clearTimeout(timer);
  }, [draftKey, form, hasUserEdited, initializedDraftKey, recoveryDraft]);

  useEffect(() => {
    if (!hasUserEdited) {
      return;
    }

    function warnBeforeUnload(event: BeforeUnloadEvent): void {
      event.preventDefault();
      event.returnValue = '';
    }

    window.addEventListener('beforeunload', warnBeforeUnload);
    return () => window.removeEventListener('beforeunload', warnBeforeUnload);
  }, [hasUserEdited]);

  const saveMutation = useMutation({
    async mutationFn({ publish }: SaveAction): Promise<NoteEditor> {
      const params: NoteSaveParams = {
        title: form.title,
        slug: form.slug || undefined,
        summary: form.summary || undefined,
        content: form.content,
        categoryId: Number(form.categoryId),
        tagIds: form.tagIds,
      };
      const saved = isEditing && noteId
        ? await notesApi.updateNote(noteId, params)
        : await notesApi.createNote(params);
      return publish ? notesApi.publishNote(saved.id) : saved;
    },
    async onSuccess() {
      if (draftKey) {
        removeNoteDraft(draftKey);
      }
      setHasUserEdited(false);
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['my-notes'] }),
        queryClient.invalidateQueries({ queryKey: ['notes'] }),
      ]);
      navigate('/workspace/notes');
    },
  });

  function updateForm(updater: NoteFormUpdater): void {
    setForm((current: NoteFormState) => updater(current));
    setHasUserEdited(true);
    setLocalBackupAt(null);
    setBackupFailed(false);
    setLocalError('');
  }

  function submit(publish: boolean): void {
    setLocalError('');
    if (saveMutation.isPending) {
      return;
    }
    if (!form.title.trim()) {
      setLocalError('请输入笔记标题');
      return;
    }
    if (!form.categoryId) {
      setLocalError('请选择笔记分类');
      return;
    }
    if (form.slug && !SLUG_PATTERN.test(form.slug)) {
      setLocalError('访问标识只能包含小写字母、数字和连字符');
      return;
    }
    if (publish && !form.content.trim()) {
      setLocalError('正文不能为空，无法发布');
      return;
    }
    saveMutation.mutate({ publish });
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    submit(false);
  }

  function toggleTag(tagId: number): void {
    if (!form.tagIds.includes(tagId) && form.tagIds.length >= 10) {
      setLocalError('最多选择 10 个标签');
      return;
    }
    updateForm((current: NoteFormState) => ({
      ...current,
      tagIds: current.tagIds.includes(tagId)
        ? current.tagIds.filter((currentTagId: number) => currentTagId !== tagId)
        : [...current.tagIds, tagId],
    }));
  }

  function restoreLocalDraft(): void {
    if (!recoveryDraft) {
      return;
    }
    setForm(recoveryDraft.form);
    setRecoveryDraft(null);
    setHasUserEdited(true);
    setLocalBackupAt(null);
    setBackupFailed(false);
  }

  function discardLocalDraft(): void {
    if (draftKey) {
      removeNoteDraft(draftKey);
    }
    setRecoveryDraft(null);
    setLocalBackupAt(null);
  }

  if (hasInvalidNoteId) {
    return <div className="panel p-5 text-sm text-vermilion">笔记 ID 无效，请返回写作台重新选择。</div>;
  }

  if (isEditing && noteQuery.isLoading) {
    return (
      <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
        <Loader2 className="animate-spin" size={18} />
        <span>正在加载笔记</span>
      </div>
    );
  }

  if (isEditing && noteQuery.isError) {
    return (
      <div className="panel border-vermilion/30 bg-vermilion/5 p-5 text-sm text-vermilion">
        {getErrorMessage(noteQuery.error)}
      </div>
    );
  }

  const referenceError = categoriesQuery.error ?? tagsQuery.error;
  const errorMessage =
    localError ||
    (saveMutation.isError ? getErrorMessage(saveMutation.error) : '') ||
    (referenceError ? getErrorMessage(referenceError) : '');
  const isPublished = noteQuery.data?.status === 'PUBLISHED';
  const saveStatus = saveMutation.isPending
    ? '正在保存到 MySQL'
    : backupFailed
      ? '本地备份失败，请尽快手动保存'
      : localBackupAt
        ? `已于 ${formatBackupTime(localBackupAt)} 备份到本机`
        : hasUserEdited
          ? '有未保存修改'
          : '内容已同步';

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <Link
            to="/workspace/notes"
            className="focus-ring mb-2 inline-flex h-9 items-center gap-2 rounded-md px-2 text-sm text-ink/65 hover:bg-black/5"
          >
            <ArrowLeft size={16} />
            <span>返回我的笔记</span>
          </Link>
          <SectionHeader
            title={isEditing ? '编辑笔记' : '新建笔记'}
            description={isPublished ? '当前内容已公开，保存后会直接更新公开文章。' : '内容将先保存为草稿。'}
          />
        </div>

        <div className="flex shrink-0 flex-wrap gap-2">
          <button
            type="submit"
            disabled={saveMutation.isPending}
            className="focus-ring inline-flex h-10 items-center gap-2 rounded-md border border-black/10 bg-white px-4 text-sm font-medium text-ink hover:bg-black/5 disabled:opacity-50"
          >
            <Save size={17} />
            <span>{saveMutation.isPending ? '保存中' : '保存草稿'}</span>
          </button>
          {!isPublished ? (
            <button
              type="button"
              disabled={saveMutation.isPending}
              onClick={() => submit(true)}
              className="focus-ring inline-flex h-10 items-center gap-2 rounded-md bg-vermilion px-4 text-sm font-semibold text-white hover:bg-vermilion/90 disabled:opacity-50"
            >
              <Eye size={17} />
              <span>保存并发布</span>
            </button>
          ) : null}
        </div>
      </div>

      {recoveryDraft ? (
        <div className="flex flex-col gap-3 rounded-md border border-gold/35 bg-gold/10 px-4 py-3 text-sm text-ink/75 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-start gap-2">
            <History className="mt-0.5 shrink-0 text-gold" size={17} />
            <span>发现 {formatBackupTime(recoveryDraft.savedAt)} 的本地备份，恢复后仍需手动保存到数据库。</span>
          </div>
          <div className="flex shrink-0 gap-2">
            <button
              type="button"
              onClick={restoreLocalDraft}
              className="focus-ring inline-flex h-9 items-center rounded-md bg-ink px-3 font-medium text-white hover:bg-ink/90"
            >
              恢复
            </button>
            <button
              type="button"
              title="丢弃本地备份"
              aria-label="丢弃本地备份"
              onClick={discardLocalDraft}
              className="focus-ring inline-flex size-9 items-center justify-center rounded-md border border-black/10 text-ink/60 hover:bg-black/5"
            >
              <Trash2 size={16} />
            </button>
          </div>
        </div>
      ) : null}

      {errorMessage ? (
        <div className="rounded-md border border-vermilion/30 bg-vermilion/5 px-3 py-2 text-sm text-vermilion">
          {errorMessage}
        </div>
      ) : null}

      <section className="border-y border-black/10 bg-white px-4 py-5 sm:px-5">
        <div className="grid gap-4 lg:grid-cols-2">
          <label className="block lg:col-span-2">
            <span className="text-sm font-medium text-ink">标题</span>
            <input
              required
              maxLength={120}
              value={form.title}
              onChange={(event) => updateForm((current: NoteFormState) => ({ ...current, title: event.target.value }))}
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
            />
          </label>

          <label className="block">
            <span className="text-sm font-medium text-ink">分类</span>
            <select
              required
              value={form.categoryId}
              onChange={(event) => updateForm((current: NoteFormState) => ({ ...current, categoryId: event.target.value }))}
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
            >
              <option value="">请选择分类</option>
              {categoriesQuery.data?.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>

          <label className="block">
            <span className="text-sm font-medium text-ink">访问标识</span>
            <input
              maxLength={150}
              pattern="[a-z0-9]+(?:-[a-z0-9]+)*"
              value={form.slug}
              onChange={(event) => updateForm((current: NoteFormState) => ({ ...current, slug: event.target.value }))}
              placeholder="留空时自动生成"
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
            />
          </label>

          <label className="block lg:col-span-2">
            <span className="text-sm font-medium text-ink">摘要</span>
            <textarea
              maxLength={500}
              rows={3}
              value={form.summary}
              onChange={(event) => updateForm((current: NoteFormState) => ({ ...current, summary: event.target.value }))}
              className="focus-ring mt-2 w-full resize-y rounded-md border border-black/10 bg-white px-3 py-2 text-sm leading-6 text-ink"
            />
          </label>

          <fieldset className="lg:col-span-2">
            <legend className="text-sm font-medium text-ink">标签</legend>
            <div className="mt-2 flex flex-wrap gap-3">
              {tagsQuery.data?.map((tag) => (
                <label
                  key={tag.id}
                  className="inline-flex h-9 cursor-pointer items-center gap-2 rounded-md border border-black/10 px-3 text-sm text-ink/75"
                >
                  <input
                    type="checkbox"
                    checked={form.tagIds.includes(tag.id)}
                    onChange={() => toggleTag(tag.id)}
                    className="size-4 accent-vermilion"
                  />
                  <span>{tag.name}</span>
                </label>
              ))}
            </div>
          </fieldset>
        </div>
      </section>

      <MarkdownEditor
        value={form.content}
        onChange={(content: string) => updateForm((current: NoteFormState) => ({ ...current, content }))}
        onSave={() => submit(false)}
        isSaving={saveMutation.isPending}
        saveStatus={saveStatus}
      />
    </form>
  );
}