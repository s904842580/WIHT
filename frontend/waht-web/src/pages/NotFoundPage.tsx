import { SectionHeader } from '@/components/SectionHeader';
import { Home } from 'lucide-react';
import { Link } from 'react-router-dom';

// NotFoundPage 处理未匹配到的路由。
export function NotFoundPage() {
  return (
    <div className="panel p-5">
      <SectionHeader title="页面不存在" description="当前地址没有对应页面。" />
      <Link
        to="/"
        className="focus-ring inline-flex h-10 items-center gap-2 rounded-md bg-ink px-3 text-sm font-medium text-white hover:bg-ink/90"
      >
        <Home size={16} />
        <span>返回首页</span>
      </Link>
    </div>
  );
}
