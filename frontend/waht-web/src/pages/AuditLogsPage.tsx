import { auditActions, listAuditEvents, type AuditFilters } from '@/api/audit';
import { SectionHeader } from '@/components/SectionHeader';
import { useAuth } from '@/features/auth/AuthProvider';
import { useQuery } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';

const empty: AuditFilters = { username: '', action: '', outcome: '', requestId: '', from: '', to: '' };
const outcomes = { SUCCESS: '成功', FAILURE: '失败', UNKNOWN: '结果未知' };
const states: Record<string, string> = {
  DRAFT: '草稿', PUBLISHED: '已发布', PENDING: '待确认', WAITING_APPROVAL: '待确认',
  APPROVED: '已批准', EDITED: '修改后批准', REJECTED: '已拒绝', COMPLETED: '已完成',
  FAILED: '执行失败', CANCELLED: '已取消', RUNNING: '执行中', CREATED: '已创建',
};
const control = 'focus-ring mt-1 w-full rounded-md border border-black/15 bg-white px-3 py-2 text-sm';
const button = 'focus-ring rounded-md border border-black/15 px-3 py-2 text-sm disabled:opacity-40';

export function AuditLogsPage() {
  const { user } = useAuth();
  const [draft, setDraft] = useState(empty);
  const [filters, setFilters] = useState(empty);
  const [page, setPage] = useState(1);
  const [validationError, setValidationError] = useState('');
  const query = useQuery({
    queryKey: ['audit', user?.id, filters, page],
    queryFn: ({ signal }) => listAuditEvents(filters, page, signal),
    enabled: user?.role === 'ADMIN',
    retry: false,
  });

  if (user?.role !== 'ADMIN') {
    return <div className="panel p-6" role="alert">仅管理员可以查看操作审计记录。</div>;
  }

  function update(key: keyof AuditFilters, value: string) {
    setDraft((previous) => ({ ...previous, [key]: value }));
  }
  function search(event: FormEvent) {
    event.preventDefault();
    if ((draft.from && !Number.isFinite(Date.parse(draft.from))) || (draft.to && !Number.isFinite(Date.parse(draft.to)))
      || (draft.from && draft.to && new Date(draft.from) > new Date(draft.to))) {
      setValidationError('请输入有效时间，开始时间不能晚于结束时间。');
      return;
    }
    setValidationError('');
    setFilters({ ...draft });
    setPage(1);
  }

  return (
    <div className="space-y-5">
      <SectionHeader title="操作审计" description="查看登录、笔记维护与 AI 审批的操作痕迹。记录只读，时间按当前浏览器时区显示。" />
      <form onSubmit={search} className="panel space-y-4 p-5">
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <label className="text-sm">操作者用户名
            <input className={control} maxLength={50} value={draft.username} onChange={(e) => update('username', e.target.value)} placeholder="精确匹配用户名" />
          </label>
          <label className="text-sm">操作类型
            <select className={control} value={draft.action} onChange={(e) => update('action', e.target.value)}>
              <option value="">全部操作</option>
              {Object.entries(auditActions).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>
          <label className="text-sm">接口处理结果
            <select className={control} value={draft.outcome} onChange={(e) => update('outcome', e.target.value)}>
              <option value="">全部结果</option>
              {Object.entries(outcomes).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>
          <label className="text-sm">请求编号
            <input className={control} maxLength={64} value={draft.requestId} onChange={(e) => update('requestId', e.target.value)} placeholder="精确匹配请求编号" />
          </label>
          <label className="text-sm">开始时间
            <input type="datetime-local" className={control} value={draft.from} onChange={(e) => update('from', e.target.value)} />
          </label>
          <label className="text-sm">结束时间
            <input type="datetime-local" className={control} value={draft.to} onChange={(e) => update('to', e.target.value)} />
          </label>
        </div>
        {validationError && <p role="alert" className="text-sm text-vermilion">{validationError}</p>}
        <div className="flex flex-wrap gap-2">
          <button type="submit" className={`${button} bg-ink text-white`}>查询</button>
          <button type="button" className={button} onClick={() => { setDraft(empty); setFilters(empty); setPage(1); setValidationError(''); }}>重置</button>
          <button type="button" className={button} disabled={query.isFetching} onClick={() => void query.refetch()}>刷新</button>
        </div>
      </form>
      <p className="text-sm leading-6 text-ink/65">“成功”表示接口已处理；AI 草稿是否写入请结合业务状态查看。认证失败的请求显示为“未认证”。服务异常或远程超时显示“结果未知”，需结合请求编号核查。</p>
      {query.isPending && <p role="status" className="panel p-5">正在加载审计记录…</p>}
      {query.isError && <div role="alert" className="panel p-5 text-vermilion">
        <p>审计记录加载失败：{query.error.message}</p>
        <button className={`${button} mt-3`} onClick={() => void query.refetch()}>重试</button>
      </div>}
      {!query.isError && query.data && <>
        {query.data.items.length === 0 ? <p className="panel p-6">当前条件下没有操作记录。</p> : <div className="panel overflow-x-auto">
          <table className="w-full min-w-[840px] text-left text-sm">
            <caption className="sr-only">操作审计列表</caption>
            <thead className="border-b bg-black/[0.025]"><tr>
              {['时间', '操作者', '操作 / 对象', '处理结果', '连接地址', '详情'].map((label) => <th key={label} scope="col" className="px-4 py-3 font-medium">{label}</th>)}
            </tr></thead>
            <tbody>{query.data.items.map((item) => <tr key={item.id} className="border-b last:border-0 align-top">
              <td className="whitespace-nowrap px-4 py-4">{new Date(`${item.occurredAt}Z`).toLocaleString('zh-CN', { hour12: false })}</td>
              <td className="px-4 py-4">{item.username || '未认证'}<div className="text-xs text-ink/50">{item.userId == null ? '身份未确认' : `用户 #${item.userId}`}</div></td>
              <td className="px-4 py-4">{auditActions[item.action] || item.action}<div className="mt-1 max-w-44 break-all text-xs text-ink/55">{item.resourceId ? `对象 ${item.resourceId}` : '—'}</div></td>
              <td className="px-4 py-4"><span className={item.outcome === 'SUCCESS' ? 'text-jade' : item.outcome === 'FAILURE' ? 'text-vermilion' : 'text-amber-700'}>{outcomes[item.outcome]}</span><div className="mt-1 text-xs text-ink/55">{item.operationState ? states[item.operationState] || item.operationState : '—'}</div></td>
              <td className="px-4 py-4 font-mono text-xs">{item.clientIp || '—'}</td>
              <td className="px-4 py-4"><details><summary className="focus-ring cursor-pointer whitespace-nowrap">查看详情</summary>
                <dl className="mt-3 max-w-80 space-y-2 break-all text-xs">
                  <div><dt className="text-ink/55">请求编号</dt><dd className="select-all">{item.requestId}</dd></div>
                  <div><dt className="text-ink/55">事件编号</dt><dd className="select-all">{item.id}</dd></div>
                  <div><dt className="text-ink/55">接口</dt><dd>{item.method} {item.route}</dd></div>
                  <div><dt className="text-ink/55">状态与耗时</dt><dd>HTTP {item.httpStatus} · 业务码 {item.businessCode ?? '未知'} · {item.durationMs} ms</dd></div>
                </dl>
              </details></td>
            </tr>)}</tbody>
          </table>
        </div>}
        <div className="flex flex-wrap items-center justify-between gap-3 text-sm" aria-live="polite">
          <span>共 {query.data.total} 条 · 第 {query.data.page} 页{query.isFetching ? ' · 更新中…' : ''}</span>
          <div className="flex gap-2">
            <button className={button} disabled={!query.data.hasPrevious || query.isFetching} onClick={() => setPage(query.data.page - 1)}>上一页</button>
            <button className={button} disabled={!query.data.hasNext || query.isFetching} onClick={() => setPage(query.data.page + 1)}>下一页</button>
          </div>
        </div>
      </>}
    </div>
  );
}
