import { notesApi } from '@/api/notes';
import { SectionHeader } from '@/components/SectionHeader';
import { formatDate } from '@/utils/date';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';

// NoteDetailPage 展示单篇公开笔记详情，对接 /api/notes/{slug}。
export function NoteDetailPage() {
  const { slug } = useParams();
  const noteQuery = useQuery({
    queryKey: ['notes', slug],
    queryFn: () => notesApi.getNote(slug!),
    enabled: Boolean(slug),
  });

  if (noteQuery.isLoading) {
    return (
      <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
        <Loader2 className="animate-spin" size={18} />
        <span>正在加载笔记详情</span>
      </div>
    );
  }

  if (noteQuery.isError) {
    return (
      <div className="panel border-vermilion/30 bg-vermilion/5 p-5 text-sm text-vermilion">
        {(noteQuery.error as Error).message}
      </div>
    );
  }

  const note = noteQuery.data;
  if (!note) {
    return <div className="panel p-5 text-sm text-ink/60">笔记不存在。</div>;
  }

  return (
    <article className="space-y-4">
      <Link to="/notes" className="focus-ring inline-flex h-10 items-center gap-2 rounded-md px-2 text-sm text-ink/70 hover:bg-black/5">
        <ArrowLeft size={16} />
        <span>返回笔记列表</span>
      </Link>

      <div className="panel p-5">
        <SectionHeader title={note.title} description={note.summary ?? undefined} />
        <div className="flex flex-wrap items-center gap-2 text-xs text-ink/55">
          <span>{note.category?.name ?? '未分类'}</span>
          <span>·</span>
          <span>{formatDate(note.publishedAt)}</span>
          <span>·</span>
          <span>{note.viewCount ?? 0} 次浏览</span>
        </div>
        <div className="mt-3 flex flex-wrap gap-2">
          {note.tags.map((tag) => (
            <span
              key={tag.id}
              className="rounded-md border border-black/10 px-2 py-1 text-xs text-ink/70"
              style={tag.color ? { borderColor: `${tag.color}55`, color: tag.color } : undefined}
            >
              {tag.name}
            </span>
          ))}
        </div>
      </div>

      <div className="panel p-5">
        <div className="whitespace-pre-wrap text-sm leading-7 text-ink/80">{note.content}</div>
      </div>
    </article>
  );
}
