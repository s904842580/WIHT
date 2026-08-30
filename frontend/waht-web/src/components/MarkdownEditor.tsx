import { MarkdownContent } from '@/components/MarkdownContent';
import {
  Bold,
  Code2,
  Columns2,
  Eye,
  FilePenLine,
  Heading2,
  Image as ImageIcon,
  Italic,
  Link2,
  List,
  ListOrdered,
  Maximize2,
  Minimize2,
  Minus,
  Quote,
  Redo2,
  Save,
  Strikethrough,
  Undo2,
} from 'lucide-react';
import { useMemo, useRef, useState } from 'react';
import type { KeyboardEvent, ReactNode } from 'react';

export type MarkdownEditorMode = 'edit' | 'split' | 'preview';

type MarkdownEditorProps = {
  value: string;
  onChange: (value: string) => void;
  onSave: () => void;
  isSaving: boolean;
  saveStatus: string;
};

type ToolbarButtonProps = {
  label: string;
  onClick: () => void;
  children: ReactNode;
};

// 工具栏按钮统一尺寸和可访问名称，图标含义通过 title 与 aria-label 补充。
function ToolbarButton({ label, onClick, children }: ToolbarButtonProps) {
  return (
    <button
      type="button"
      title={label}
      aria-label={label}
      onClick={onClick}
      className="focus-ring inline-flex size-9 shrink-0 items-center justify-center rounded-md text-ink/65 hover:bg-black/5 hover:text-ink"
    >
      {children}
    </button>
  );
}

// MarkdownEditor 提供可预测的 Markdown 编辑命令，不改变后端保存的纯文本格式。
export function MarkdownEditor({ value, onChange, onSave, isSaving, saveStatus }: MarkdownEditorProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [mode, setMode] = useState<MarkdownEditorMode>('split');
  const [isFullscreen, setIsFullscreen] = useState<boolean>(false);

  const statistics = useMemo(() => {
    const chineseCharacters = value.match(/[\u3400-\u9fff]/g)?.length ?? 0;
    const latinWords = value.match(/[A-Za-z0-9]+(?:['-][A-Za-z0-9]+)*/g)?.length ?? 0;
    const wordCount = chineseCharacters + latinWords;
    return {
      characters: Array.from(value).length,
      lines: value ? value.split(/\r?\n/).length : 1,
      wordCount,
      readingMinutes: Math.max(1, Math.ceil(wordCount / 300)),
    };
  }, [value]);

  function commitValue(nextValue: string, selectionStart: number, selectionEnd: number): void {
    onChange(nextValue);
    window.requestAnimationFrame(() => {
      const textarea = textareaRef.current;
      if (!textarea) {
        return;
      }
      textarea.focus();
      textarea.setSelectionRange(selectionStart, selectionEnd);
    });
  }

  function wrapSelection(prefix: string, suffix: string, placeholder: string): void {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = value.slice(start, end) || placeholder;
    const replacement = `${prefix}${selectedText}${suffix}`;
    const nextValue = `${value.slice(0, start)}${replacement}${value.slice(end)}`;
    const nextStart = start + prefix.length;
    commitValue(nextValue, nextStart, nextStart + selectedText.length);
  }

  function prefixSelectedLines(prefixFactory: (index: number) => string): void {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }

    const selectionStart = textarea.selectionStart;
    const selectionEnd = textarea.selectionEnd;
    const lineStart = value.lastIndexOf('\n', Math.max(0, selectionStart - 1)) + 1;
    const nextLineBreak = value.indexOf('\n', selectionEnd);
    const lineEnd = nextLineBreak === -1 ? value.length : nextLineBreak;
    const selectedBlock = value.slice(lineStart, lineEnd);
    const transformedBlock = selectedBlock
      .split('\n')
      .map((line, index) => `${prefixFactory(index)}${line}`)
      .join('\n');
    const nextValue = `${value.slice(0, lineStart)}${transformedBlock}${value.slice(lineEnd)}`;
    commitValue(nextValue, lineStart, lineStart + transformedBlock.length);
  }

  function insertText(text: string): void {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const nextValue = `${value.slice(0, start)}${text}${value.slice(end)}`;
    const nextCursor = start + text.length;
    commitValue(nextValue, nextCursor, nextCursor);
  }

  function insertLink(isImage: boolean): void {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const label = value.slice(start, end) || (isImage ? '图片描述' : '链接文字');
    const prefix = isImage ? '![' : '[';
    const replacement = `${prefix}${label}](https://)`;
    const nextValue = `${value.slice(0, start)}${replacement}${value.slice(end)}`;
    const urlStart = start + prefix.length + label.length + 2;
    commitValue(nextValue, urlStart, urlStart + 8);
  }

  function runNativeHistory(command: 'undo' | 'redo'): void {
    const textarea = textareaRef.current;
    if (!textarea) {
      return;
    }
    textarea.focus();
    document.execCommand(command);
  }

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>): void {
    const hasCommandKey = event.ctrlKey || event.metaKey;
    const key = event.key.toLowerCase();

    if (hasCommandKey && key === 's') {
      event.preventDefault();
      onSave();
      return;
    }
    if (hasCommandKey && key === 'b') {
      event.preventDefault();
      wrapSelection('**', '**', '加粗文字');
      return;
    }
    if (hasCommandKey && key === 'i') {
      event.preventDefault();
      wrapSelection('*', '*', '斜体文字');
      return;
    }
    if (hasCommandKey && key === 'k') {
      event.preventDefault();
      insertLink(false);
      return;
    }
    if (event.key === 'Tab') {
      event.preventDefault();
      insertText('  ');
    }
  }

  const editorVisible = mode !== 'preview';
  const previewVisible = mode !== 'edit';

  return (
    <section
      className={[
        'bg-white',
        isFullscreen
          ? 'fixed inset-0 z-50 flex min-h-0 flex-col'
          : 'min-h-[620px] border-y border-black/10',
      ].join(' ')}
    >
      <div className="flex shrink-0 flex-wrap items-center justify-between gap-2 border-b border-black/10 px-3 py-2">
        <div className="flex min-w-0 flex-wrap items-center gap-0.5">
          <ToolbarButton label="撤销" onClick={() => runNativeHistory('undo')}>
            <Undo2 size={17} />
          </ToolbarButton>
          <ToolbarButton label="重做" onClick={() => runNativeHistory('redo')}>
            <Redo2 size={17} />
          </ToolbarButton>
          <span className="mx-1 h-6 w-px bg-black/10" />
          <ToolbarButton label="二级标题" onClick={() => prefixSelectedLines(() => '## ')}>
            <Heading2 size={17} />
          </ToolbarButton>
          <ToolbarButton label="加粗（Ctrl+B）" onClick={() => wrapSelection('**', '**', '加粗文字')}>
            <Bold size={17} />
          </ToolbarButton>
          <ToolbarButton label="斜体（Ctrl+I）" onClick={() => wrapSelection('*', '*', '斜体文字')}>
            <Italic size={17} />
          </ToolbarButton>
          <ToolbarButton label="删除线" onClick={() => wrapSelection('~~', '~~', '删除文字')}>
            <Strikethrough size={17} />
          </ToolbarButton>
          <ToolbarButton label="行内代码" onClick={() => wrapSelection('`', '`', '代码')}>
            <Code2 size={17} />
          </ToolbarButton>
          <ToolbarButton label="引用" onClick={() => prefixSelectedLines(() => '> ')}>
            <Quote size={17} />
          </ToolbarButton>
          <ToolbarButton label="无序列表" onClick={() => prefixSelectedLines(() => '- ')}>
            <List size={17} />
          </ToolbarButton>
          <ToolbarButton label="有序列表" onClick={() => prefixSelectedLines((index) => `${index + 1}. `)}>
            <ListOrdered size={17} />
          </ToolbarButton>
          <ToolbarButton label="代码块" onClick={() => wrapSelection('```\n', '\n```', '代码')}>
            <Code2 size={17} />
          </ToolbarButton>
          <ToolbarButton label="插入链接（Ctrl+K）" onClick={() => insertLink(false)}>
            <Link2 size={17} />
          </ToolbarButton>
          <ToolbarButton label="插入图片地址" onClick={() => insertLink(true)}>
            <ImageIcon size={17} />
          </ToolbarButton>
          <ToolbarButton label="分隔线" onClick={() => insertText('\n\n---\n\n')}>
            <Minus size={17} />
          </ToolbarButton>
          <span className="mx-1 h-6 w-px bg-black/10" />
          <ToolbarButton label="保存到数据库（Ctrl+S）" onClick={onSave}>
            <Save className={isSaving ? 'animate-pulse' : ''} size={17} />
          </ToolbarButton>
        </div>

        <div className="flex items-center gap-2">
          <div className="inline-flex h-9 items-center rounded-md border border-black/10 bg-paper/50 p-1">
            <button
              type="button"
              onClick={() => setMode('edit')}
              className={`focus-ring inline-flex h-7 items-center gap-1 rounded px-2 text-xs font-medium ${mode === 'edit' ? 'bg-white text-ink shadow-sm' : 'text-ink/55'}`}
            >
              <FilePenLine size={14} />
              <span>编辑</span>
            </button>
            <button
              type="button"
              onClick={() => setMode('split')}
              className={`focus-ring inline-flex h-7 items-center gap-1 rounded px-2 text-xs font-medium ${mode === 'split' ? 'bg-white text-ink shadow-sm' : 'text-ink/55'}`}
            >
              <Columns2 size={14} />
              <span>分栏</span>
            </button>
            <button
              type="button"
              onClick={() => setMode('preview')}
              className={`focus-ring inline-flex h-7 items-center gap-1 rounded px-2 text-xs font-medium ${mode === 'preview' ? 'bg-white text-ink shadow-sm' : 'text-ink/55'}`}
            >
              <Eye size={14} />
              <span>预览</span>
            </button>
          </div>
          <ToolbarButton label={isFullscreen ? '退出全屏' : '全屏编辑'} onClick={() => setIsFullscreen((current) => !current)}>
            {isFullscreen ? <Minimize2 size={17} /> : <Maximize2 size={17} />}
          </ToolbarButton>
        </div>
      </div>

      <div
        className={[
          'grid min-h-[520px] flex-1 overflow-hidden',
          editorVisible && previewVisible ? 'lg:grid-cols-2 lg:divide-x lg:divide-black/10' : 'grid-cols-1',
        ].join(' ')}
      >
        {editorVisible ? (
          <label className="flex min-h-[420px] min-w-0 flex-col p-4 sm:p-5">
            <span className="mb-3 text-sm font-medium text-ink">Markdown 正文</span>
            <textarea
              ref={textareaRef}
              value={value}
              onChange={(event) => onChange(event.target.value)}
              onKeyDown={handleKeyDown}
              spellCheck
              className="focus-ring min-h-[380px] flex-1 resize-none rounded-md border border-black/10 bg-paper/40 p-3 font-mono text-sm leading-7 text-ink"
            />
          </label>
        ) : null}

        {previewVisible ? (
          <div className="min-h-[420px] min-w-0 overflow-y-auto p-4 sm:p-5">
            <div className="mb-3 text-sm font-medium text-ink">实时预览</div>
            {value ? (
              <MarkdownContent content={value} />
            ) : (
              <div className="text-sm text-ink/45">暂无正文内容</div>
            )}
          </div>
        ) : null}
      </div>

      <div className="flex shrink-0 flex-wrap items-center justify-between gap-2 border-t border-black/10 px-4 py-2 text-xs text-ink/50">
        <span>{saveStatus}</span>
        <span>
          {statistics.wordCount} 字词 · {statistics.characters} 字符 · {statistics.lines} 行 · 约 {statistics.readingMinutes} 分钟阅读
        </span>
      </div>
    </section>
  );
}