import loginBackground from '@/assets/plum-spirit-login.jpg';
import { authApi } from '@/api/auth';
import { useAuth } from '@/features/auth/AuthProvider';
import { getErrorMessage } from '@/utils/error';
import { useMutation } from '@tanstack/react-query';
import { ArrowRight, CheckCircle2, Eye, EyeOff, LockKeyhole, LogIn, UserRound } from 'lucide-react';
import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

type LoginLocationState = {
  from?: string;
};

// LoginPage 使用原创梅花灵蝶主题，同时保留完整的登录、返回原页面和账号切换流程。
export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, logout, user } = useAuth();
  const [username, setUsername] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [showPassword, setShowPassword] = useState<boolean>(false);

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess(result) {
      login(result);
      const state = location.state as LoginLocationState | null;
      navigate(state?.from ?? '/workspace/notes', { replace: true });
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>): void {
    event.preventDefault();
    loginMutation.mutate({ username: username.trim(), password });
  }

  return (
    <section className="relative isolate min-h-[680px] overflow-hidden rounded-lg border border-[#d98b7b]/30 bg-[#fff7ed] shadow-panel">
      <img
        src={loginBackground}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 h-full w-full object-cover object-left"
      />
      <div className="absolute left-6 top-6 z-10 max-w-xs text-[#7b2f2d] sm:left-8 sm:top-8">
        <div className="text-2xl font-semibold">梅影入笺</div>
        <div className="mt-1 text-sm text-[#7b2f2d]/70">把今天的灵感，写成明天还能找到的答案。</div>
      </div>

      <div className="relative z-10 flex min-h-[680px] items-end justify-center px-4 pb-5 pt-72 sm:px-6 md:items-center md:justify-end md:p-8">
        <div className="w-full max-w-[410px] rounded-lg border border-[#d98b7b]/35 bg-[#fffaf4]/95 p-5 shadow-[0_20px_55px_rgba(117,47,45,0.16)] backdrop-blur-sm sm:p-7">
          {user ? (
            <div>
              <div className="flex size-11 items-center justify-center rounded-full bg-[#d9564f]/10 text-[#b93d3a]">
                <CheckCircle2 size={22} />
              </div>
              <h1 className="mt-4 text-xl font-semibold text-[#572926]">已经登录</h1>
              <p className="mt-2 text-sm leading-6 text-[#7b4a45]">
                当前账号：{user.nickname || user.username}
              </p>
              <Link
                to="/workspace/notes"
                className="focus-ring mt-5 inline-flex h-11 w-full items-center justify-center gap-2 rounded-md bg-[#b93d3a] px-4 text-sm font-semibold text-white hover:bg-[#a73533]"
              >
                <span>进入写作台</span>
                <ArrowRight size={17} />
              </Link>
              <button
                type="button"
                onClick={logout}
                className="focus-ring mt-3 inline-flex h-10 w-full items-center justify-center rounded-md border border-[#d98b7b]/40 text-sm font-medium text-[#7b3d38] hover:bg-[#f9dfd6]"
              >
                切换账号
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="mb-6">
                <div className="text-xs font-semibold text-[#b45b50]">WAHT WRITING ROOM</div>
                <h1 className="mt-2 text-2xl font-semibold text-[#572926]">欢迎回来</h1>
                <p className="mt-1 text-sm leading-6 text-[#7b4a45]">登录后继续整理你的学习笔记。</p>
              </div>

              <div className="space-y-4">
                <label className="block">
                  <span className="text-sm font-medium text-[#572926]">用户名</span>
                  <div className="relative mt-2">
                    <UserRound
                      className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[#b87870]"
                      size={17}
                    />
                    <input
                      required
                      maxLength={50}
                      value={username}
                      onChange={(event) => setUsername(event.target.value)}
                      className="focus-ring h-11 w-full rounded-md border border-[#d98b7b]/35 bg-white/90 pl-10 pr-3 text-sm text-[#572926] placeholder:text-[#a87b75]"
                      autoComplete="username"
                      placeholder="请输入用户名"
                    />
                  </div>
                </label>

                <label className="block">
                  <span className="text-sm font-medium text-[#572926]">密码</span>
                  <div className="relative mt-2">
                    <LockKeyhole
                      className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-[#b87870]"
                      size={17}
                    />
                    <input
                      required
                      maxLength={72}
                      value={password}
                      onChange={(event) => setPassword(event.target.value)}
                      className="focus-ring h-11 w-full rounded-md border border-[#d98b7b]/35 bg-white/90 pl-10 pr-11 text-sm text-[#572926] placeholder:text-[#a87b75]"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="current-password"
                      placeholder="请输入密码"
                    />
                    <button
                      type="button"
                      title={showPassword ? '隐藏密码' : '显示密码'}
                      aria-label={showPassword ? '隐藏密码' : '显示密码'}
                      onClick={() => setShowPassword((current: boolean) => !current)}
                      className="focus-ring absolute right-1 top-1/2 inline-flex size-9 -translate-y-1/2 items-center justify-center rounded-md text-[#9b655f] hover:bg-[#f8e7df]"
                    >
                      {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                    </button>
                  </div>
                </label>

                {loginMutation.isError ? (
                  <div className="rounded-md border border-[#c54d49]/30 bg-[#fff0ec] px-3 py-2 text-sm text-[#a73533]">
                    {getErrorMessage(loginMutation.error)}
                  </div>
                ) : null}

                <button
                  type="submit"
                  disabled={loginMutation.isPending}
                  className="focus-ring inline-flex h-11 w-full items-center justify-center gap-2 rounded-md bg-[#b93d3a] px-4 text-sm font-semibold text-white hover:bg-[#a73533] disabled:cursor-not-allowed disabled:opacity-60"
                >
                  <LogIn size={18} />
                  <span>{loginMutation.isPending ? '登录中' : '进入写作台'}</span>
                </button>

                <div className="text-center text-sm text-[#7b4a45]">
                  还没有账号？
                  <Link to="/register" className="ml-1 font-medium text-[#b93d3a] hover:text-[#92302f]">
                    去注册
                  </Link>
                </div>
              </div>
            </form>
          )}
        </div>
      </div>
    </section>
  );
}