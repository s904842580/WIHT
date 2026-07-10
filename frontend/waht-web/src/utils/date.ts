// formatDate 用于把后端 LocalDateTime 字符串展示成简洁日期。
export function formatDate(value?: string | null) {
  if (!value) {
    return '未发布';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}
