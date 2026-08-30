import { authApi } from '@/api/auth';
import { SectionHeader } from '@/components/SectionHeader';
import { useAuth } from '@/features/auth/AuthProvider';
import { getErrorMessage } from '@/utils/error';
import { useMutation } from '@tanstack/react-query';
import { UserPlus } from 'lucide-react';
import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

// RegisterPage 对接 Java 后端 /api/auth/register。
export function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [localError, setLocalError] = useState('');

  const registerMutation = useMutation({
    mutationFn: authApi.register,
    onSuccess(result) {
      login(result);
      navigate('/');
    },
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError('');

    if (password !== confirmPassword) {
      setLocalError('两次输入的密码不一致');
      return;
    }

    registerMutation.mutate({
      username,
      password,
      nickname: nickname || undefined,
    });
  }

  const errorMessage = localError || (registerMutation.isError ? getErrorMessage(registerMutation.error) : '');

  return (
    <div className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
      <div>
        <SectionHeader title="注册" description="注册成功后会自动登录，后续可以继续访问需要 token 的接口。" />
        <div className="panel p-4 text-sm leading-6 text-ink/65">
          第一阶段注册只做基础用户创建：默认角色为 <span className="font-medium text-ink">USER</span>，默认状态为{' '}
          <span className="font-medium text-ink">ACTIVE</span>。
        </div>
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
              minLength={3}
              maxLength={50}
              required
            />
          </label>

          <label className="block">
            <span className="text-sm font-medium text-ink">昵称</span>
            <input
              value={nickname}
              onChange={(event) => setNickname(event.target.value)}
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
              maxLength={50}
              placeholder="不填则默认使用用户名"
            />
          </label>

          <label className="block">
            <span className="text-sm font-medium text-ink">密码</span>
            <input
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
              type="password"
              autoComplete="new-password"
              minLength={6}
              maxLength={50}
              required
            />
          </label>

          <label className="block">
            <span className="text-sm font-medium text-ink">确认密码</span>
            <input
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              className="focus-ring mt-2 h-11 w-full rounded-md border border-black/10 bg-white px-3 text-sm text-ink"
              type="password"
              autoComplete="new-password"
              minLength={6}
              maxLength={50}
              required
            />
          </label>

          {errorMessage ? (
            <div className="rounded-md border border-vermilion/30 bg-vermilion/5 px-3 py-2 text-sm text-vermilion">
              {errorMessage}
            </div>
          ) : null}

          <button
            type="submit"
            disabled={registerMutation.isPending}
            className="focus-ring inline-flex h-11 w-full items-center justify-center gap-2 rounded-md bg-vermilion px-4 text-sm font-semibold text-white hover:bg-vermilion/90 disabled:cursor-not-allowed disabled:opacity-60"
          >
            <UserPlus size={18} />
            <span>{registerMutation.isPending ? '注册中' : '注册并登录'}</span>
          </button>

          <div className="text-center text-sm text-ink/60">
            已有账号？
            <Link to="/login" className="ml-1 font-medium text-vermilion hover:text-vermilion/80">
              去登录
            </Link>
          </div>
        </div>
      </form>
    </div>
  );
}
