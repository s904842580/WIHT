import { authApi } from '@/api/auth';
import { SectionHeader } from '@/components/SectionHeader';
import { useAuth } from '@/features/auth/AuthProvider';
import { useMutation } from '@tanstack/react-query';
import { LogIn } from 'lucide-react';
import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

// LoginPage 对接 Java 后端 /api/auth/login。
export function LoginPage() {
  const navigate = useNavigate();
  const { login, user } = useAuth();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess(result) {
      login(result);
      navigate('/');
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    loginMutation.mutate({ username, password });
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
      <div>
        <SectionHeader title="登录" description="登录后可以验证当前用户接口，后续管理后台会复用这条认证链路。" />
        {user ? (
          <div className="panel p-4 text-sm text-ink/70">
            当前已登录：<span className="font-medium text-ink">{user.nickname || user.username}</span>
          </div>
        ) : null}
      </div>

      <form onSubmit={handleSubmit} className="panel p-5">
        <div className="space-y-4">
          <label className="block">
            <span className="text-sm font-medium text-ink">用户名</span>
            <input
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
              autoComplete="username"
            />
          </label>

          <label className="block">
            <span className="text-sm font-medium text-ink">密码</span>
            <input
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
              type="password"
              autoComplete="current-password"
            />
          </label>

          {loginMutation.isError ? (
            <div className="rounded-md border border-vermilion/30 bg-vermilion/5 px-3 py-2 text-sm text-vermilion">
              {(loginMutation.error as Error).message}
            </div>
          ) : null}

          <button
            type="submit"
            disabled={loginMutation.isPending}
            className="focus-ring inline-flex h-11 w-full items-center justify-center gap-2 rounded-md bg-vermilion px-4 text-sm font-semibold text-white hover:bg-vermilion/90 disabled:cursor-not-allowed disabled:opacity-60"
          >
            <LogIn size={18} />
            <span>{loginMutation.isPending ? '登录中' : '登录'}</span>
          </button>

          <div className="text-center text-sm text-ink/60">
            还没有账号？
            <Link to="/register" className="ml-1 font-medium text-vermilion hover:text-vermilion/80">
              去注册
            </Link>
          </div>
        </div>
      </form>
    </div>
  );
}
