import type { BaseResponse } from '@/types/api';

// token 的本地存储 key，集中定义后续更容易替换。
export const TOKEN_STORAGE_KEY = 'WAHT_ACCESS_TOKEN';

// 401 时通知 AuthProvider 同步清理内存登录态。
export const AUTH_EXPIRED_EVENT = 'waht:auth-expired';

// 默认走 Vite 代理：/api -> http://localhost:8080。
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '');

type RequestOptions = RequestInit & {
  auth?: boolean;
};

// 带业务码和 HTTP 状态的请求异常，页面仍可像普通 Error 一样展示 message。
export class HttpError extends Error {
  readonly code: number;
  readonly status: number;

  constructor(message: string, code: number, status: number) {
    super(message);
    this.name = 'HttpError';
    this.code = code;
    this.status = status;
  }
}

// 从 localStorage 读取 token；服务端渲染场景下 window 可能不存在，所以做保护。
export function getStoredToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return window.localStorage.getItem(TOKEN_STORAGE_KEY);
}

export function setStoredToken(token: string): void {
  window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearStoredToken(): void {
  window.localStorage.removeItem(TOKEN_STORAGE_KEY);
}

// 统一请求函数：负责拼接地址、加 token、处理 Java 后端统一响应。
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
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

  let payload: BaseResponse<T>;
  try {
    payload = (await response.json()) as BaseResponse<T>;
  } catch {
    throw new HttpError('服务器返回了无法解析的响应', response.status, response.status);
  }

  if (!response.ok || payload.code !== 0) {
    if (payload.code === 401) {
      clearStoredToken();
      window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
    }
    throw new HttpError(payload.message || '请求失败', payload.code, response.status);
  }

  return payload.data;
}
