import type { ApiResponse } from '@/types/api';

// token 的本地存储 key，集中定义后续更容易替换。
export const TOKEN_STORAGE_KEY = 'WAHT_ACCESS_TOKEN';

// 默认走 Vite 代理：/api -> http://localhost:8080。
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

type RequestOptions = RequestInit & {
  auth?: boolean;
};

// 从 localStorage 读取 token；服务端渲染场景下 window 可能不存在，所以做保护。
export function getStoredToken() {
  if (typeof window === 'undefined') {
    return null;
  }
  return window.localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setStoredToken(token: string) {
  window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearStoredToken() {
  window.localStorage.removeItem(TOKEN_STORAGE_KEY);
}

// 统一请求函数：负责拼接地址、加 token、处理 Java 后端统一响应。
export async function request<T>(path: string, options: RequestOptions = {}) {
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (options.auth !== false) {
    const token = getStoredToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok || payload.code !== 0) {
    if (payload.code === 401) {
      clearStoredToken();
    }
    throw new Error(payload.message || '请求失败');
  }

  return payload.data;
}
