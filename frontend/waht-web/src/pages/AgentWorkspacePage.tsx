import { agentApi, type DecideAgentApprovalParams } from '@/api/agent';
import { MarkdownContent } from '@/components/MarkdownContent';
import { DraftApprovalPanel } from '@/features/agent/DraftApprovalPanel';
import type { AgentApprovalDecision, AgentDraft, AgentMessage } from '@/types/api';
import { formatDate } from '@/utils/date';
import { getErrorMessage } from '@/utils/error';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Bot, FileText, Loader2, MessageSquarePlus, Send, Sparkles } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';

type SendMessageVariables = {
  conversationId: string;
  message: string;
};

type ApprovalVariables = {
  approvalId: string;
  params: DecideAgentApprovalParams;
};

type ApprovalPanelPayload = {
  decision: AgentApprovalDecision;
  draft?: AgentDraft;
};

// AgentWorkspacePage 是登录后的作者学习助手，统一管理会话、回答来源和草稿审批。
export function AgentWorkspacePage() {
  const queryClient = useQueryClient();
  const [selectedConversationId, setSelectedConversationId] = useState<string | null>(null);
  const [message, setMessage] = useState('');
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const conversationsQuery = useQuery({
    queryKey: ['agent', 'conversations'],
    queryFn: agentApi.listConversations,
  });

  useEffect(() => {
    if (!selectedConversationId && conversationsQuery.data?.length) {
      setSelectedConversationId(conversationsQuery.data[0].id);
    }
  }, [conversationsQuery.data, selectedConversationId]);

  const conversationQuery = useQuery({
    queryKey: ['agent', 'conversation', selectedConversationId],
    queryFn: () => agentApi.getConversation(selectedConversationId as string),
    enabled: Boolean(selectedConversationId),
  });

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ block: 'end' });
  }, [conversationQuery.data?.messages.length]);

  const createMutation = useMutation({
    mutationFn: () => agentApi.createConversation(),
    async onSuccess(created) {
      setSelectedConversationId(created.id);
      await queryClient.invalidateQueries({ queryKey: ['agent', 'conversations'] });
    },
  });

  const sendMutation = useMutation({
    mutationFn: ({ conversationId, message: content }: SendMessageVariables) =>
      agentApi.sendMessage(conversationId, content),
    async onSuccess(result) {
      setMessage('');
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['agent', 'conversations'] }),
        queryClient.invalidateQueries({ queryKey: ['agent', 'conversation', result.conversationId] }),
      ]);
    },
  });

  const approvalMutation = useMutation({
    mutationFn: ({ approvalId, params }: ApprovalVariables) => agentApi.decideApproval(approvalId, params),
    async onSuccess(result) {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['agent', 'conversations'] }),
        queryClient.invalidateQueries({ queryKey: ['agent', 'conversation', result.conversationId] }),
        queryClient.invalidateQueries({ queryKey: ['my-notes'] }),
      ]);
    },
  });

  function submitMessage(): void {
    const content = message.trim();
    if (!selectedConversationId || !content || conversationQuery.data?.pendingApproval) {
      return;
    }
    sendMutation.mutate({ conversationId: selectedConversationId, message: content });
  }

  function decideApproval(payload: ApprovalPanelPayload): void {
    const approval = conversationQuery.data?.pendingApproval;
    if (!approval) {
      return;
    }
    approvalMutation.mutate({ approvalId: approval.id, params: payload });
  }

  const actionError = createMutation.error ?? sendMutation.error ?? approvalMutation.error;
  const hasPendingApproval = Boolean(conversationQuery.data?.pendingApproval);

  return (
    <div className="pb-10">
      <div className="mb-5 flex flex-col gap-3 border-b border-black/10 pb-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="flex items-center gap-2 text-xs font-semibold text-vermilion">
            <Sparkles size={14} />
            <span>作者工具</span>
          </div>
          <h1 className="mt-2 text-2xl font-semibold text-ink">学习助手</h1>
          <p className="mt-1 text-sm text-ink/60">基于你的笔记回答问题，写入操作必须由你确认。</p>
        </div>
        <Link to="/workspace/notes" className="text-sm font-medium text-ink/65 hover:text-vermilion">
          返回写作台
        </Link>
      </div>

      {actionError ? (
        <div className="mb-4 border border-vermilion/30 bg-vermilion/5 px-4 py-3 text-sm text-vermilion">
          {getErrorMessage(actionError)}
        </div>
      ) : null}

      <div className="grid min-h-[620px] overflow-hidden border border-black/10 bg-white lg:grid-cols-[260px_minmax(0,1fr)]">
        <aside className="border-b border-black/10 bg-black/[0.025] lg:border-b-0 lg:border-r">
          <div className="flex h-14 items-center justify-between border-b border-black/10 px-4">
            <span className="text-sm font-semibold text-ink">学习会话</span>
            <button
              type="button"
              title="新建会话"
              onClick={() => createMutation.mutate()}
              disabled={createMutation.isPending}
              className="focus-ring inline-flex size-9 items-center justify-center text-ink/60 hover:bg-black/5 hover:text-vermilion disabled:opacity-50"
            >
              {createMutation.isPending ? <Loader2 className="animate-spin" size={17} /> : <MessageSquarePlus size={17} />}
            </button>
          </div>

          <div className="max-h-52 overflow-y-auto p-2 lg:max-h-[565px]">
            {conversationsQuery.isLoading ? (
              <div className="flex items-center gap-2 px-3 py-4 text-xs text-ink/50">
                <Loader2 className="animate-spin" size={15} />
                <span>正在加载会话</span>
              </div>
            ) : null}
            {conversationsQuery.data?.map((conversation) => (
              <button
                key={conversation.id}
                type="button"
                onClick={() => setSelectedConversationId(conversation.id)}
                className={[
                  'focus-ring mb-1 block w-full border-l-2 px-3 py-3 text-left transition',
                  selectedConversationId === conversation.id
                    ? 'border-vermilion bg-white text-ink'
                    : 'border-transparent text-ink/60 hover:bg-white/70 hover:text-ink',
                ].join(' ')}
              >
                <span className="block truncate text-sm font-medium">{conversation.title}</span>
                <span className="mt-1 block text-[11px] text-ink/40">{formatDate(conversation.updatedAt)}</span>
              </button>
            ))}
            {conversationsQuery.data?.length === 0 ? (
              <p className="px-3 py-5 text-xs leading-5 text-ink/50">还没有会话，从右上角新建一个学习主题。</p>
            ) : null}
          </div>
        </aside>

        <section className="flex min-w-0 flex-col">
          <div className="h-[500px] overflow-y-auto px-4 py-5 sm:px-6">
            {!selectedConversationId ? (
              <div className="flex h-full flex-col items-center justify-center text-center">
                <Bot size={30} className="text-vermilion" />
                <p className="mt-3 text-sm font-medium text-ink">创建一个会话开始学习</p>
                <p className="mt-1 max-w-sm text-xs leading-5 text-ink/50">可以让助手查找旧笔记、解释知识，或整理成待确认草稿。</p>
              </div>
            ) : null}

            {conversationQuery.isLoading ? (
              <div className="flex h-full items-center justify-center gap-2 text-sm text-ink/50">
                <Loader2 className="animate-spin" size={18} />
                <span>正在加载对话</span>
              </div>
            ) : null}

            {conversationQuery.isError ? (
              <div className="border border-vermilion/30 bg-vermilion/5 p-4 text-sm text-vermilion">
                {getErrorMessage(conversationQuery.error)}
              </div>
            ) : null}

            {conversationQuery.data?.messages.length === 0 ? (
              <div className="py-10 text-center text-sm text-ink/50">说说你现在想学习或整理什么。</div>
            ) : null}

            <div className="space-y-6">
              {conversationQuery.data?.messages.map((item: AgentMessage) => (
                <article key={item.id} className={item.role === 'USER' ? 'flex justify-end' : 'flex gap-3'}>
                  {item.role === 'ASSISTANT' ? (
                    <div className="mt-1 flex size-8 shrink-0 items-center justify-center bg-vermilion/10 text-vermilion">
                      <Bot size={17} />
                    </div>
                  ) : null}
                  <div
                    className={
                      item.role === 'USER'
                        ? 'max-w-[82%] bg-ink px-4 py-3 text-sm leading-6 text-white'
                        : 'min-w-0 max-w-3xl flex-1 pt-1'
                    }
                  >
                    {item.role === 'USER' ? <p className="whitespace-pre-wrap">{item.content}</p> : <MarkdownContent content={item.content} />}
                    {item.sources.length ? (
                      <div className="mt-4 border-t border-black/10 pt-3">
                        <div className="mb-2 text-[11px] font-semibold text-ink/45">参考笔记</div>
                        <div className="flex flex-wrap gap-2">
                          {item.sources.map((source) => (
                            <Link
                              key={source.noteId}
                              to={source.status === 'PUBLISHED' ? `/notes/${source.slug}` : `/workspace/notes/${source.noteId}/edit`}
                              className="inline-flex min-h-8 items-center gap-2 border border-black/10 px-2 text-xs text-ink/65 hover:border-vermilion/40 hover:text-vermilion"
                            >
                              <FileText size={13} />
                              <span>{source.title}</span>
                            </Link>
                          ))}
                        </div>
                      </div>
                    ) : null}
                  </div>
                </article>
              ))}
            </div>

            {conversationQuery.data?.pendingApproval ? (
              <DraftApprovalPanel
                approval={conversationQuery.data.pendingApproval}
                isPending={approvalMutation.isPending}
                onDecision={decideApproval}
              />
            ) : null}
            <div ref={messagesEndRef} />
          </div>

          <div className="mt-auto border-t border-black/10 bg-black/[0.015] p-3 sm:p-4">
            {hasPendingApproval ? (
              <p className="mb-2 text-xs text-gold">请先处理上方草稿，再继续发送消息。</p>
            ) : null}
            <div className="flex items-end gap-2">
              <textarea
                rows={2}
                maxLength={8000}
                value={message}
                onChange={(event) => setMessage(event.target.value)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
                    event.preventDefault();
                    submitMessage();
                  }
                }}
                disabled={!selectedConversationId || hasPendingApproval || sendMutation.isPending}
                placeholder="询问知识，或让助手整理一份学习笔记草稿"
                className="focus-ring min-h-16 flex-1 resize-none border border-black/10 bg-white px-3 py-2 text-sm leading-6 text-ink disabled:bg-black/5"
              />
              <button
                type="button"
                title="发送消息"
                onClick={submitMessage}
                disabled={!selectedConversationId || !message.trim() || hasPendingApproval || sendMutation.isPending}
                className="focus-ring inline-flex size-11 shrink-0 items-center justify-center bg-vermilion text-white hover:bg-vermilion/90 disabled:cursor-not-allowed disabled:opacity-40"
              >
                {sendMutation.isPending ? <Loader2 className="animate-spin" size={18} /> : <Send size={18} />}
              </button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}
