import { notesApi } from '@/api/notes';
import { MarkdownContent } from '@/components/MarkdownContent';
import type { AgentApproval, AgentApprovalDecision, AgentDraft } from '@/types/api';
import { useQuery } from '@tanstack/react-query';
import { Check, Loader2, Pencil, X } from 'lucide-react';
import { useEffect, useState } from 'react';

type ApprovalDecisionPayload = {
  decision: AgentApprovalDecision;
  draft?: AgentDraft;
};

type DraftApprovalPanelProps = {
  approval: AgentApproval;
  isPending: boolean;
  onDecision: (payload: ApprovalDecisionPayload) => void;
};

// DraftApprovalPanel 把 Agent 写操作停在用户确认点，并允许在落库前修改完整草稿。
export function DraftApprovalPanel({ approval, isPending, onDecision }: DraftApprovalPanelProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [draft, setDraft] = useState<AgentDraft>(approval.draft);
  const categoriesQuery = useQuery({ queryKey: ['note-categories'], queryFn: notesApi.listCategories });
  const tagsQuery = useQuery({ queryKey: ['note-tags'], queryFn: notesApi.listTags });

  useEffect(() => {
    setDraft(approval.draft);
    setIsEditing(false);
  }, [approval.id, approval.draft]);

  function updateDraft(updater: (current: AgentDraft) => AgentDraft): void {
    setDraft((current: AgentDraft) => updater(current));
  }

  function toggleTag(tagId: number): void {
    updateDraft((current: AgentDraft) => ({
      ...current,
      tagIds: current.tagIds.includes(tagId)
        ? current.tagIds.filter((id: number) => id !== tagId)
        : [...current.tagIds, tagId],
    }));
  }

  return (
    <section className="mt-5 border border-gold/40 bg-gold/5">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-gold/30 px-4 py-3">
        <div>
          <div className="text-sm font-semibold text-ink">待确认的笔记草稿</div>
          <div className="mt-1 text-xs text-ink/55">批准后才会写入“我的笔记”，默认保持草稿状态。</div>
        </div>
        <button
          type="button"
          title={isEditing ? '取消修改' : '修改草稿'}
          onClick={() => setIsEditing((current: boolean) => !current)}
          disabled={isPending}
          className="focus-ring inline-flex size-9 items-center justify-center border border-black/10 bg-white text-ink/70 hover:text-vermilion disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isEditing ? <X size={16} /> : <Pencil size={16} />}
        </button>
      </div>

      {isEditing ? (
        <div className="grid gap-4 p-4 lg:grid-cols-2">
          <label className="block lg:col-span-2">
            <span className="text-xs font-medium text-ink/70">标题</span>
            <input
              required
              maxLength={120}
              value={draft.title}
              onChange={(event) => updateDraft((current: AgentDraft) => ({ ...current, title: event.target.value }))}
              className="focus-ring mt-1 h-10 w-full border border-black/10 bg-white px-3 text-sm text-ink"
            />
          </label>
          <label className="block">
            <span className="text-xs font-medium text-ink/70">分类</span>
            <select
              value={draft.categoryId}
              onChange={(event) =>
                updateDraft((current: AgentDraft) => ({ ...current, categoryId: Number(event.target.value) }))
              }
              className="focus-ring mt-1 h-10 w-full border border-black/10 bg-white px-3 text-sm text-ink"
            >
              {categoriesQuery.data?.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
          <label className="block">
            <span className="text-xs font-medium text-ink/70">摘要</span>
            <input
              maxLength={500}
              value={draft.summary}
              onChange={(event) => updateDraft((current: AgentDraft) => ({ ...current, summary: event.target.value }))}
              className="focus-ring mt-1 h-10 w-full border border-black/10 bg-white px-3 text-sm text-ink"
            />
          </label>
          <fieldset className="lg:col-span-2">
            <legend className="text-xs font-medium text-ink/70">标签</legend>
            <div className="mt-2 flex flex-wrap gap-2">
              {tagsQuery.data?.map((tag) => (
                <label key={tag.id} className="inline-flex h-8 items-center gap-2 border border-black/10 bg-white px-2 text-xs">
                  <input
                    type="checkbox"
                    checked={draft.tagIds.includes(tag.id)}
                    onChange={() => toggleTag(tag.id)}
                    className="size-4 accent-vermilion"
                  />
                  <span>{tag.name}</span>
                </label>
              ))}
            </div>
          </fieldset>
          <label className="block lg:col-span-2">
            <span className="text-xs font-medium text-ink/70">Markdown 正文</span>
            <textarea
              required
              maxLength={200000}
              rows={14}
              value={draft.content}
              onChange={(event) => updateDraft((current: AgentDraft) => ({ ...current, content: event.target.value }))}
              className="focus-ring mt-1 w-full resize-y border border-black/10 bg-white px-3 py-2 font-mono text-sm leading-6 text-ink"
            />
          </label>
        </div>
      ) : (
        <div className="p-4">
          <h3 className="text-base font-semibold text-ink">{draft.title}</h3>
          {draft.summary ? <p className="mt-2 text-sm leading-6 text-ink/60">{draft.summary}</p> : null}
          <div className="mt-4 max-h-72 overflow-y-auto border-t border-black/10 pt-4">
            <MarkdownContent content={draft.content} />
          </div>
        </div>
      )}

      <div className="flex flex-wrap justify-end gap-2 border-t border-gold/30 px-4 py-3">
        <button
          type="button"
          onClick={() => onDecision({ decision: 'REJECT' })}
          disabled={isPending}
          className="focus-ring inline-flex h-9 items-center gap-2 border border-black/10 bg-white px-3 text-sm text-ink/70 hover:bg-black/5 disabled:opacity-50"
        >
          <X size={15} />
          <span>拒绝</span>
        </button>
        <button
          type="button"
          onClick={() => onDecision(isEditing ? { decision: 'EDIT', draft } : { decision: 'APPROVE' })}
          disabled={isPending || !draft.title.trim() || !draft.content.trim() || draft.categoryId <= 0}
          className="focus-ring inline-flex h-9 items-center gap-2 bg-jade px-3 text-sm font-semibold text-white hover:bg-jade/90 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isPending ? <Loader2 className="animate-spin" size={15} /> : <Check size={15} />}
          <span>{isEditing ? '保存修改并创建' : '批准并创建草稿'}</span>
        </button>
      </div>
    </section>
  );
}
