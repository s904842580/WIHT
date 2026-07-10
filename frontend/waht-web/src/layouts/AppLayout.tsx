import { useAuth } from '@/features/auth/AuthProvider';
import { BookOpen, Boxes, Gamepad2, Home, Layers3, LogIn, LogOut, UserPlus } from 'lucide-react';
import { NavLink, Outlet } from 'react-router-dom';

const navItems = [
  { to: '/', label: '首页', icon: Home },
  { to: '/notes', label: '学习笔记', icon: BookOpen },
  { to: '/projects', label: '项目展示', icon: Layers3 },
  { to: '/games', label: '游戏科普', icon: Gamepad2 },
  { to: '/assets', label: '素材展示', icon: Boxes },
];

// AppLayout 是全站布局：顶部导航、登录入口和页面内容出口。
export function AppLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-paper">
      <header className="border-b border-black/10 bg-white">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-4 sm:px-6 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <div className="text-xl font-semibold tracking-normal text-ink">WAHT</div>
            <div className="text-sm text-ink/60">个人学习与项目展示平台</div>
          </div>

          <nav className="flex flex-wrap items-center gap-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    [
                      'focus-ring inline-flex h-10 items-center gap-2 rounded-md px-3 text-sm font-medium transition',
                      isActive ? 'bg-ink text-white' : 'text-ink/70 hover:bg-black/5 hover:text-ink',
                    ].join(' ')
                  }
                >
                  <Icon size={16} />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>

          <div className="flex items-center gap-2">
            {user ? (
              <>
                <span className="max-w-40 truncate text-sm text-ink/70">{user.nickname || user.username}</span>
                <button
                  type="button"
                  onClick={logout}
                  className="focus-ring inline-flex h-10 items-center gap-2 rounded-md border border-black/10 px-3 text-sm font-medium text-ink hover:bg-black/5"
                >
                  <LogOut size={16} />
                  <span>退出</span>
                </button>
              </>
            ) : (
              <>
                <NavLink
                  to="/login"
                  className="focus-ring inline-flex h-10 items-center gap-2 rounded-md border border-black/10 px-3 text-sm font-medium text-ink hover:bg-black/5"
                >
                  <LogIn size={16} />
                  <span>登录</span>
                </NavLink>
                <NavLink
                  to="/register"
                  className="focus-ring inline-flex h-10 items-center gap-2 rounded-md bg-vermilion px-3 text-sm font-medium text-white hover:bg-vermilion/90"
                >
                  <UserPlus size={16} />
                  <span>注册</span>
                </NavLink>
              </>
            )}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6">
        <Outlet />
      </main>
    </div>
  );
}
