import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

type MarkdownContentProps = {
  content: string;
};

// MarkdownContent 为公开正文和编辑器预览提供同一套 Markdown/GFM 渲染结果。
export function MarkdownContent({ content }: MarkdownContentProps) {
  return (
    <div className="markdown-body">
      <ReactMarkdown remarkPlugins={[remarkGfm]}>{content}</ReactMarkdown>
    </div>
  );
}
