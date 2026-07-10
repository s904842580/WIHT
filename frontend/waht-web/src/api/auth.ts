import { request } from '@/api/http';
import type { LoginResult, UserInfo } from '@/types/api';

// 登录请求参数，和 Java 后端 LoginRequest 对齐。
export type LoginParams = {
  username: string;
  password: string;
};

// 注册请求参数，和 Java 后端 RegisterRequest 对齐。
export type RegisterParams = {
  username: string;
  password: string;
  nickname?: string;
};

// authApi 只放认证相关接口，后续用户资料、权限也可以放这里。
export const authApi = {
  login(params: LoginParams) {
    return request<LoginResult>('/auth/login', {
      method: 'POST',
      auth: false,
      body: JSON.stringify(params),
    });
  },

  register(params: RegisterParams) {
    return request<LoginResult>('/auth/register', {
      method: 'POST',
      auth: false,
      body: JSON.stringify(params),
    });
  },

  me() {
    return request<UserInfo>('/auth/me');
  },
};
