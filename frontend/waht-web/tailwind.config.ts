import type { Config } from 'tailwindcss';

// Tailwind 配置：扫描 src 和 index.html 中使用到的 class。
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#1f2933',
        paper: '#f7f5f0',
        vermilion: '#b33b2e',
        jade: '#277568',
        gold: '#c58b2b',
      },
      boxShadow: {
        panel: '0 16px 40px rgba(31, 41, 51, 0.10)',
      },
    },
  },
  plugins: [],
} satisfies Config;
