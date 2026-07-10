import { authApi } from '@/api/auth';
import { clearStoredToken, getStoredToken, setStoredToken } from '@/api/http';
import type { LoginResult, UserInfo } from '@/types/api';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { createContext, useContext, useMemo, useState } from 'react';

type AuthContextValue = {
  token: string | null;
  user: UserInfo | null;
  isChecking: boolean;
  login: (result: LoginResult) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

// AuthProvider 管理登录态：读取 token、刷新当前用户、提供登录和退出方法。
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const queryClient = useQueryClient();
  const [token, setToken] = useState(() => getStoredToken());

  const currentUserQuery = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: authApi.me,
    enabled: Boolean(token),
    retry: false,
  });

  const value = useMemo<AuthContextValue>(() => {
    return {
      token,
      user: currentUserQuery.data ?? null,
      isChecking: currentUserQuery.isFetching,
      login(result) {
        setStoredToken(result.token);
        setToken(result.token);
        queryClient.setQueryData(['auth', 'me'], result.user);
      },
      logout() {
        clearStoredToken();
        setToken(null);
        queryClient.removeQueries({ queryKey: ['auth'] });
      },
    };
  }, [currentUserQuery.data, currentUserQuery.isFetching, queryClient, token]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// useAuth 是页面读取登录态的统一入口。
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
