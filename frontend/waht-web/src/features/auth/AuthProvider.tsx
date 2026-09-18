import { authApi } from '@/api/auth';
import { AUTH_EXPIRED_EVENT, HttpError, clearStoredToken, getStoredToken, setStoredToken } from '@/api/http';
import type { LoginResult, UserInfo } from '@/types/api';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';

type AuthContextValue = {
  token: string | null;
  user: UserInfo | null;
  isChecking: boolean;
  authCheckError: Error | null;
  retryAuthCheck: () => void;
  login: (result: LoginResult) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

// AuthProvider 管理登录态：读取 token、刷新当前用户、提供登录和退出方法。
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const queryClient = useQueryClient();
  const [token, setToken] = useState(() => getStoredToken());

  // 私有查询必须随用户切换清理，避免短暂展示上一个账号的缓存内容。
  const clearPrivateQueries = useCallback((): void => {
    void queryClient.cancelQueries({ queryKey: ['my-notes'] });
    void queryClient.cancelQueries({ queryKey: ['agent'] });
    void queryClient.cancelQueries({ queryKey: ['audit'] });
    queryClient.removeQueries({ queryKey: ['my-notes'] });
    queryClient.removeQueries({ queryKey: ['agent'] });
    queryClient.removeQueries({ queryKey: ['audit'] });
  }, [queryClient]);

  const currentUserQuery = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: authApi.me,
    enabled: Boolean(token),
    retry: false,
  });

  useEffect(() => {
    function clearExpiredLogin(): void {
      clearStoredToken();
      setToken(null);
      queryClient.removeQueries({ queryKey: ['auth'] });
      clearPrivateQueries();
    }

    window.addEventListener(AUTH_EXPIRED_EVENT, clearExpiredLogin);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, clearExpiredLogin);
  }, [clearPrivateQueries, queryClient]);

  useEffect(() => {
    const error = currentUserQuery.error;
    if (token && error instanceof HttpError && (error.code === 401 || error.status === 401)) {
      clearStoredToken();
      setToken(null);
      clearPrivateQueries();
    }
  }, [clearPrivateQueries, currentUserQuery.error, token]);

  const value = useMemo<AuthContextValue>(() => {
    return {
      token,
      user: token ? (currentUserQuery.data ?? null) : null,
      isChecking: Boolean(token) && (currentUserQuery.isPending || currentUserQuery.isFetching),
      authCheckError: token && currentUserQuery.isError ? currentUserQuery.error : null,
      retryAuthCheck() {
        void currentUserQuery.refetch();
      },
      login(result) {
        clearPrivateQueries();
        setStoredToken(result.token);
        setToken(result.token);
        queryClient.setQueryData(['auth', 'me'], result.user);
      },
      logout() {
        clearStoredToken();
        setToken(null);
        queryClient.removeQueries({ queryKey: ['auth'] });
        clearPrivateQueries();
      },
    };
  }, [
    clearPrivateQueries,
    currentUserQuery.data,
    currentUserQuery.error,
    currentUserQuery.isError,
    currentUserQuery.isFetching,
    currentUserQuery.isPending,
    currentUserQuery.refetch,
    queryClient,
    token,
  ]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// useAuth 是页面读取登录态的统一入口。
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
