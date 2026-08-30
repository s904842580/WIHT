import { notesApi } from '@/api/notes';
import { MarkdownContent } from '@/components/MarkdownContent';
import { SectionHeader } from '@/components/SectionHeader';
import { useAuth } from '@/features/auth/AuthProvider';
import { formatDate } from '@/utils/date';
import { getErrorMessage } from '@/utils/error';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, FilePenLine, Loader2 } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';

// NoteDetailPage 展示单篇公开笔记详情，对接 /api/notes/{slug}。
export function NoteDetailPage() {
  const { slug } = useParams();
  const { user } = useAuth();
  const noteQuery = useQuery({
    queryKey: ['notes', slug],
    queryFn: () => notesApi.getNote(slug ?? ''),
    enabled: Boolean(slug),
  });
  const myNotesQuery = useQuery({
    queryKey: ['my-notes'],
    queryFn: notesApi.listMyNotes,
    enabled: Boolean(user),
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
        {getErrorMessage(noteQuery.error)}
      </div>
    );
  }

  const note = noteQuery.data;
  if (!note) {
    return <div className="panel p-5 text-sm text-ink/60">笔记不存在。</div>;
  }
  const ownedNote = myNotesQuery.data?.find((managedNote) => managedNote.slug === note.slug);

  return (
    <article className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link to="/notes" className="focus-ring inline-flex h-10 items-center gap-2 rounded-md px-2 text-sm text-ink/70 hover:bg-black/5">
          <ArrowLeft size={16} />
          <span>返回笔记列表</span>
        </Link>
        {ownedNote ? (
          <Link
            to={`/workspace/notes/${ownedNote.id}/edit`}
            className="focus-ring inline-flex h-10 items-center gap-2 rounded-md bg-vermilion px-4 text-sm font-semibold text-white hover:bg-vermilion/90"
          >
            <FilePenLine size={17} />
            <span>编辑这篇笔记</span>
          </Link>
        ) : null}
      </div>

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
        <MarkdownContent content={note.content} />
      </div>
    </article>
  );
}
