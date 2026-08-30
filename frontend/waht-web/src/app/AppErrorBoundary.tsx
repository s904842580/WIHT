import { RefreshCw, TriangleAlert } from 'lucide-react';
import { Component, type ErrorInfo, type ReactNode } from 'react';

type AppErrorBoundaryProps = {
  children: ReactNode;
};

type AppErrorBoundaryState = {
  hasError: boolean;
};

// AppErrorBoundary 捕获组件渲染异常，避免用户只看到没有任何信息的空白页面。
export class AppErrorBoundary extends Component<AppErrorBoundaryProps, AppErrorBoundaryState> {
  state: AppErrorBoundaryState = {
    hasError: false,
  };

  static getDerivedStateFromError(): AppErrorBoundaryState {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    console.error('WAHT frontend render error', error, errorInfo);
  }

  private reloadPage = (): void => {
    window.location.reload();
  };

  render(): ReactNode {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <main className="flex min-h-screen items-center justify-center bg-paper px-4">
        <section className="panel w-full max-w-lg p-6 text-center">
          <TriangleAlert className="mx-auto text-vermilion" size={30} />
          <h1 className="mt-4 text-lg font-semibold text-ink">页面暂时无法显示</h1>
          <p className="mt-2 text-sm leading-6 text-ink/60">请刷新页面；如果问题持续出现，请查看浏览器控制台。</p>
          <button
            type="button"
            onClick={this.reloadPage}
            className="focus-ring mt-5 inline-flex h-10 items-center justify-center gap-2 rounded-md bg-vermilion px-4 text-sm font-semibold text-white hover:bg-vermilion/90"
          >
            <RefreshCw size={17} />
            <span>刷新页面</span>
          </button>
        </section>
      </main>
    );
  }
}
