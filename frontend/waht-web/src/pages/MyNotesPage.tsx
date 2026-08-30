import { notesApi } from '@/api/notes';
import { SectionHeader } from '@/components/SectionHeader';
import { formatDate } from '@/utils/date';
import { getErrorMessage } from '@/utils/error';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Eye, EyeOff, FilePenLine, Loader2, Plus, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';

type StatusAction = {
  noteId: number;
  publish: boolean;
};

const statusLabels = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  HIDDEN: '已隐藏',
} as const;

// MyNotesPage 是登录用户的写作工作台，集中管理草稿、发布状态和删除操作。
export function MyNotesPage() {
  const queryClient = useQueryClient();
  const notesQuery = useQuery({
    queryKey: ['my-notes'],
    queryFn: notesApi.listMyNotes,
  });

  const statusMutation = useMutation({
    mutationFn: ({ noteId, publish }: StatusAction) =>
      publish ? notesApi.publishNote(noteId) : notesApi.moveNoteToDraft(noteId),
    async onSuccess() {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['my-notes'] }),
        queryClient.invalidateQueries({ queryKey: ['notes'] }),
      ]);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: notesApi.deleteNote,
    async onSuccess() {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['my-notes'] }),
        queryClient.invalidateQueries({ queryKey: ['notes'] }),
      ]);
    },
  });

  function handleDelete(noteId: number, title: string): void {
    if (window.confirm(`确定删除“${title}”吗？该操作无法撤销。`)) {
      deleteMutation.mutate(noteId);
    }
  }

  const actionError = statusMutation.error ?? deleteMutation.error;

  return (
    <div>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <SectionHeader title="我的笔记" description="管理学习笔记的草稿、发布状态和公开文章。" />
        <Link
          to="/workspace/notes/new"
          className="focus-ring inline-flex h-10 shrink-0 items-center justify-center gap-2 rounded-md bg-vermilion px-4 text-sm font-semibold text-white hover:bg-vermilion/90"
        >
          <Plus size={17} />
          <span>新建笔记</span>
        </Link>
      </div>

      {notesQuery.isLoading ? (
        <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
          <Loader2 className="animate-spin" size={18} />
          <span>正在加载我的笔记</span>
        </div>
      ) : null}

      {notesQuery.isError ? (
        <div className="panel border-vermilion/30 bg-vermilion/5 p-5 text-sm text-vermilion">
          {getErrorMessage(notesQuery.error)}
        </div>
      ) : null}

      {actionError ? (
        <div className="mb-4 rounded-md border border-vermilion/30 bg-vermilion/5 px-3 py-2 text-sm text-vermilion">
          {getErrorMessage(actionError)}
        </div>
      ) : null}

      {notesQuery.data?.length === 0 ? (
        <div className="panel p-6 text-center text-sm text-ink/60">
          还没有笔记，先创建第一篇学习记录。
        </div>
      ) : null}

      <div className="grid gap-4">
        {notesQuery.data?.map((note) => {
          const isPublished = note.status === 'PUBLISHED';
          const isActing =
            (statusMutation.isPending && statusMutation.variables?.noteId === note.id) ||
            (deleteMutation.isPending && deleteMutation.variables === note.id);

          return (
            <article key={note.id} className="panel p-5">
              <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h2 className="text-base font-semibold text-ink">{note.title}</h2>
                    <span
                      className={[
                        'rounded-md px-2 py-1 text-xs font-medium',
                        isPublished ? 'bg-jade/10 text-jade' : 'bg-black/5 text-ink/60',
                      ].join(' ')}
                    >
                      {statusLabels[note.status]}
                    </span>
                  </div>
                  <p className="mt-2 max-w-3xl text-sm leading-6 text-ink/60">
                    {note.summary || '暂无摘要'}
                  </p>
                  <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-ink/50">
                    <span>{note.category?.name ?? '未分类'}</span>
                    <span>·</span>
                    <span>更新于 {formatDate(note.updatedAt)}</span>
                    {isPublished ? (
                      <>
                        <span>·</span>
                        <Link to={`/notes/${note.slug}`} className="text-vermilion hover:text-vermilion/80">
                          查看公开文章
                        </Link>
                      </>
                    ) : null}
                  </div>
                </div>

                <div className="flex shrink-0 flex-wrap items-center gap-2">
                  <Link
                    to={`/workspace/notes/${note.id}/edit`}
                    className="focus-ring inline-flex h-9 items-center gap-2 rounded-md border border-black/10 px-3 text-sm text-ink hover:bg-black/5"
                  >
                    <FilePenLine size={16} />
                    <span>编辑</span>
                  </Link>
                  <button
                    type="button"
                    disabled={isActing}
                    onClick={() => statusMutation.mutate({ noteId: note.id, publish: !isPublished })}
                    className="focus-ring inline-flex h-9 items-center gap-2 rounded-md border border-black/10 px-3 text-sm text-ink hover:bg-black/5 disabled:opacity-50"
                  >
                    {isPublished ? <EyeOff size={16} /> : <Eye size={16} />}
                    <span>{isPublished ? '撤回' : '发布'}</span>
                  </button>
                  <button
                    type="button"
                    title="删除笔记"
                    aria-label={`删除${note.title}`}
                    disabled={isActing}
                    onClick={() => handleDelete(note.id, note.title)}
                    className="focus-ring inline-flex size-9 items-center justify-center rounded-md border border-vermilion/20 text-vermilion hover:bg-vermilion/5 disabled:opacity-50"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            </article>
          );
        })}
      </div>
    </div>
  );
}
