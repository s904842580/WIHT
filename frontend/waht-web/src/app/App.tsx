import { router } from '@/app/router';
import { AuthProvider } from '@/features/auth/AuthProvider';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router-dom';

// QueryClient 是 TanStack Query 的核心对象，用来管理接口缓存。
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: false,
    },
  },
});

// App 负责挂载全局 Provider：接口缓存、登录态、路由。
export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>
  );
}
