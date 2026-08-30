// 把 TanStack Query 的 unknown 错误安全转换成可以展示的文字，避免页面重复类型断言。
export function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return '请求失败，请稍后重试';
}
