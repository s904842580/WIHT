import { notesApi } from '@/api/notes';
import type { NoteListParams } from '@/api/notes';
import { SectionHeader } from '@/components/SectionHeader';
import { useAuth } from '@/features/auth/AuthProvider';
import { formatDate } from '@/utils/date';
import { getErrorMessage } from '@/utils/error';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { BookOpen, ChevronLeft, ChevronRight, FilePenLine, Loader2, Plus, RotateCcw, Search } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';

const PAGE_SIZE = 6;

type SelectFilterKey = 'categoryId' | 'tagId';

// URL 参数可能被手工修改，因此进入接口查询前统一转换为合法正整数。
function parsePositiveInteger(value: string | null, fallback: number): number {
  if (value === null) {
    return fallback;
  }
  const parsedValue = Number(value);
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : fallback;
}

function parseOptionalId(value: string | null): number | undefined {
  if (value === null) {
    return undefined;
  }
  const parsedValue = Number(value);
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : undefined;
}

// NotesPage 提供公开笔记的关键词、分类、标签筛选和 URL 可恢复分页。
export function NotesPage() {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parsePositiveInteger(searchParams.get('page'), 1);
  const keyword = searchParams.get('keyword')?.trim() ?? '';
  const categoryId = parseOptionalId(searchParams.get('categoryId'));
  const tagId = parseOptionalId(searchParams.get('tagId'));
  const [keywordInput, setKeywordInput] = useState<string>(keyword);

  useEffect(() => {
    setKeywordInput(keyword);
  }, [keyword]);

  const queryParams: NoteListParams = {
    page,
    pageSize: PAGE_SIZE,
    ...(keyword ? { keyword } : {}),
    ...(categoryId !== undefined ? { categoryId } : {}),
    ...(tagId !== undefined ? { tagId } : {}),
  };

  const notesQuery = useQuery({
    queryKey: ['notes', queryParams],
    queryFn: () => notesApi.listNotes(queryParams),
    placeholderData: keepPreviousData,
  });
  const categoriesQuery = useQuery({
    queryKey: ['note-categories'],
    queryFn: notesApi.listCategories,
  });
  const tagsQuery = useQuery({
    queryKey: ['note-tags'],
    queryFn: notesApi.listTags,
  });

  function writeKeyword(nextSearchParams: URLSearchParams): void {
    const normalizedKeyword = keywordInput.trim();
    if (normalizedKeyword) {
      nextSearchParams.set('keyword', normalizedKeyword);
    } else {
      nextSearchParams.delete('keyword');
    }
  }

  function submitKeyword(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    const nextSearchParams = new URLSearchParams(searchParams);
    writeKeyword(nextSearchParams);
    nextSearchParams.delete('page');
    setSearchParams(nextSearchParams);
  }

  function changeSelectFilter(key: SelectFilterKey, value: string): void {
    const nextSearchParams = new URLSearchParams(searchParams);
    writeKeyword(nextSearchParams);
    if (value) {
      nextSearchParams.set(key, value);
    } else {
      nextSearchParams.delete(key);
    }
    nextSearchParams.delete('page');
    setSearchParams(nextSearchParams);
  }

  function resetFilters(): void {
    setKeywordInput('');
    setSearchParams(new URLSearchParams());
  }

  function changePage(nextPage: number): void {
    const nextSearchParams = new URLSearchParams(searchParams);
    if (nextPage <= 1) {
      nextSearchParams.delete('page');
    } else {
      nextSearchParams.set('page', String(nextPage));
    }
    setSearchParams(nextSearchParams);
  }

  const pageResult = notesQuery.data;
  const hasActiveFilters = Boolean(keyword || categoryId !== undefined || tagId !== undefined);

  return (
    <div>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <SectionHeader title="学习笔记" description="记录 Java、前端、数据库与工程实践中的关键结论。" />
        {user ? (
          <div className="flex shrink-0 flex-wrap gap-2">
            <Link
              to="/workspace/notes"
              className="focus-ring inline-flex h-10 items-center gap-2 rounded-md border border-black/10 bg-white px-4 text-sm font-medium text-ink hover:bg-black/5"
            >
              <FilePenLine size={17} />
              <span>管理笔记</span>
            </Link>
            <Link
              to="/workspace/notes/new"
              className="focus-ring inline-flex h-10 items-center gap-2 rounded-md bg-vermilion px-4 text-sm font-semibold text-white hover:bg-vermilion/90"
            >
              <Plus size={17} />
              <span>新建笔记</span>
            </Link>
          </div>
        ) : null}
      </div>

      <form onSubmit={submitKeyword} className="panel mb-5 p-4">
        <div className="grid gap-3 lg:grid-cols-[minmax(0,1fr)_14rem_14rem_auto]">
          <label className="min-w-0">
            <span className="mb-1.5 block text-xs font-medium text-ink/65">关键词</span>
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-ink/45" size={17} />
              <input
                type="search"
                value={keywordInput}
                maxLength={100}
                onChange={(event) => setKeywordInput(event.target.value)}
                placeholder="标题或摘要"
                className="focus-ring h-10 w-full rounded-md border border-black/10 bg-white pl-10 pr-3 text-sm text-ink placeholder:text-ink/35"
              />
            </div>
          </label>

          <label>
            <span className="mb-1.5 block text-xs font-medium text-ink/65">分类</span>
            <select
              value={categoryId?.toString() ?? ''}
              disabled={categoriesQuery.isLoading}
              onChange={(event) => changeSelectFilter('categoryId', event.target.value)}
              className="focus-ring h-10 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink disabled:cursor-wait disabled:opacity-60"
            >
              <option value="">全部分类</option>
              {categoriesQuery.data?.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span className="mb-1.5 block text-xs font-medium text-ink/65">标签</span>
            <select
              value={tagId?.toString() ?? ''}
              disabled={tagsQuery.isLoading}
              onChange={(event) => changeSelectFilter('tagId', event.target.value)}
              className="focus-ring h-10 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink disabled:cursor-wait disabled:opacity-60"
            >
              <option value="">全部标签</option>
              {tagsQuery.data?.map((tag) => (
                <option key={tag.id} value={tag.id}>
                  {tag.name}
                </option>
              ))}
            </select>
          </label>

          <div className="flex items-end gap-2">
            <button
              type="submit"
              disabled={notesQuery.isFetching}
              className="focus-ring inline-flex h-10 items-center gap-2 rounded-md bg-ink px-4 text-sm font-medium text-white hover:bg-ink/90 disabled:cursor-wait disabled:opacity-60"
            >
              {notesQuery.isFetching ? <Loader2 className="animate-spin" size={16} /> : <Search size={16} />}
              <span>查询</span>
            </button>
            <button
              type="button"
              disabled={!hasActiveFilters}
              onClick={resetFilters}
              className="focus-ring inline-flex h-10 items-center gap-2 rounded-md border border-black/10 px-3 text-sm font-medium text-ink hover:bg-black/5 disabled:cursor-not-allowed disabled:opacity-40"
            >
              <RotateCcw size={16} />
              <span>重置</span>
            </button>
          </div>
        </div>

        {categoriesQuery.isError || tagsQuery.isError ? (
          <p className="mt-3 text-xs text-vermilion">部分筛选项加载失败，请刷新页面后重试。</p>
        ) : null}
      </form>

      {notesQuery.isLoading ? (
        <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
          <Loader2 className="animate-spin" size={18} />
          <span>正在加载笔记</span>
        </div>
      ) : null}

      {notesQuery.isError ? (
        <div className="panel border-vermilion/30 bg-vermilion/5 p-5 text-sm text-vermilion">
          {getErrorMessage(notesQuery.error)}
        </div>
      ) : null}

      {pageResult ? (
        <div className="mb-3 flex min-h-6 items-center justify-between gap-3 text-xs text-ink/55">
          <span>共 {pageResult.total} 篇公开笔记</span>
          {pageResult.totalPages > 0 ? (
            <span>
              第 {pageResult.page} / {pageResult.totalPages} 页
            </span>
          ) : null}
        </div>
      ) : null}

      {pageResult?.items.length === 0 ? (
        <div className="panel p-5 text-sm text-ink/60">
          {hasActiveFilters ? '没有符合当前条件的公开笔记。' : '暂时没有已发布笔记。'}
        </div>
      ) : null}

      <div className={`grid gap-4 ${notesQuery.isFetching ? 'opacity-70' : ''}`} aria-busy={notesQuery.isFetching}>
        {pageResult?.items.map((note) => (
          <Link key={note.id} to={`/notes/${note.slug}`} className="focus-ring block rounded-lg">
            <article className="panel p-5 transition hover:-translate-y-0.5 hover:shadow-lg">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div className="flex min-w-0 items-start gap-3">
                  <div className="shrink-0 rounded-md bg-jade/10 p-2 text-jade">
                    <BookOpen size={18} />
                  </div>
                  <div className="min-w-0">
                    <h2 className="break-words text-base font-semibold text-ink">{note.title}</h2>
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

      {pageResult && pageResult.totalPages > 1 ? (
        <nav className="mt-5 flex items-center justify-center gap-3" aria-label="笔记分页">
          <button
            type="button"
            title="上一页"
            aria-label="上一页"
            disabled={!pageResult.hasPrevious || notesQuery.isFetching}
            onClick={() => changePage(pageResult.page - 1)}
            className="focus-ring inline-flex h-10 w-10 items-center justify-center rounded-md border border-black/10 bg-white text-ink hover:bg-black/5 disabled:cursor-not-allowed disabled:opacity-40"
          >
            <ChevronLeft size={18} />
          </button>
          <span className="min-w-24 text-center text-sm text-ink/65">
            {pageResult.page} / {pageResult.totalPages}
          </span>
          <button
            type="button"
            title="下一页"
            aria-label="下一页"
            disabled={!pageResult.hasNext || notesQuery.isFetching}
            onClick={() => changePage(pageResult.page + 1)}
            className="focus-ring inline-flex h-10 w-10 items-center justify-center rounded-md border border-black/10 bg-white text-ink hover:bg-black/5 disabled:cursor-not-allowed disabled:opacity-40"
          >
            <ChevronRight size={18} />
          </button>
        </nav>
      ) : null}
    </div>
  );
}
