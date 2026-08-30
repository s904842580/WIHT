import { useAuth } from '@/features/auth/AuthProvider';
import { getErrorMessage } from '@/utils/error';
import { Loader2, RefreshCw } from 'lucide-react';
import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

type ProtectedRouteProps = {
  children: ReactNode;
};

// ProtectedRoute 统一保护作者工作台，并记录登录完成后需要返回的原始地址。
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { token, user, isChecking, authCheckError, retryAuthCheck } = useAuth();
  const location = useLocation();

  if (isChecking) {
    return (
      <div className="panel flex items-center gap-2 p-5 text-sm text-ink/60">
        <Loader2 className="animate-spin" size={18} />
        <span>正在确认登录状态</span>
      </div>
    );
  }

  // 网络故障不等于 token 失效，保留登录态并允许用户主动重试。
  if (token && !user && authCheckError) {
    return (
      <div className="panel flex flex-col items-start gap-3 p-5 text-sm text-ink/70">
        <div>
          <div className="font-medium text-ink">暂时无法确认登录状态</div>
          <div className="mt-1 text-ink/55">{getErrorMessage(authCheckError)}</div>
        </div>
        <button
          type="button"
          onClick={retryAuthCheck}
          className="focus-ring inline-flex h-10 items-center gap-2 rounded-md border border-black/10 px-3 font-medium text-ink hover:bg-black/5"
        >
          <RefreshCw size={16} />
          <span>重新验证</span>
        </button>
      </div>
    );
  }

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}