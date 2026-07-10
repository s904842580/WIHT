import { notesApi } from '@/api/notes';
import { SectionHeader } from '@/components/SectionHeader';
import { formatDate } from '@/utils/date';
import { useQuery } from '@tanstack/react-query';
import { BookOpen, Loader2 } from 'lucide-react';
import { Link } from 'react-router-dom';

// NotesPage 展示后端 /api/notes 返回的公开笔记列表。
export function NotesPage() {
  const notesQuery = useQuery({
    queryKey: ['notes'],
    queryFn: notesApi.listNotes,
  });

  return (
    <div>
      <SectionHeader title="学习笔记" description="这里展示已经发布的学习笔记，数据来自 Java 后端公开接口。" />

      {notesQuery.isLoading ? (
        <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
          <Loader2 className="animate-spin" size={18} />
          <span>正在加载笔记</span>
        </div>
      ) : null}

      {notesQuery.isError ? (
        <div className="panel border-vermilion/30 bg-vermilion/5 p-5 text-sm text-vermilion">
          {(notesQuery.error as Error).message}
        </div>
      ) : null}

      {notesQuery.data?.length === 0 ? <div className="panel p-5 text-sm text-ink/60">暂时没有已发布笔记。</div> : null}

      <div className="grid gap-4">
        {notesQuery.data?.map((note) => (
          <Link key={note.id} to={`/notes/${note.slug}`} className="focus-ring block rounded-lg">
            <article className="panel p-5 transition hover:-translate-y-0.5 hover:shadow-lg">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div className="flex items-start gap-3">
                  <div className="rounded-md bg-jade/10 p-2 text-jade">
                    <BookOpen size={18} />
                  </div>
                  <div>
                    <h2 className="text-base font-semibold text-ink">{note.title}</h2>
                    <p className="mt-1 max-w-3xl text-sm leading-6 text-ink/60">{note.summary}</p>
                    <div className="mt-3 flex flex-wrap items-center gap-2 text-xs text-ink/55">
                      <span>{note.category?.name ?? '未分类'}</span>
                      <span>·</span>
                      <span>{formatDate(note.publishedAt)}</span>
                      <span>·</span>
                      <span>{note.viewCount ?? 0} 次浏览</span>
                    </div>
                  </div>
                </div>
                <div className="flex flex-wrap gap-2">
                  {note.tags.map((tag) => (
                    <span
                      key={tag.id}
                      className="w-fit rounded-md border border-black/10 px-2 py-1 text-xs text-ink/70"
                      style={tag.color ? { borderColor: `${tag.color}55`, color: tag.color } : undefined}
                    >
                      {tag.name}
                    </span>
                  ))}
                </div>
              </div>
            </article>
          </Link>
        ))}
      </div>
    </div>
  );
}
