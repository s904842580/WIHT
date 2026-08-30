/// <reference types="vite/client" />

// Vite 环境变量类型声明，新增配置时需要同步维护此处和 .env.example。
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string;
  readonly VITE_API_PROXY_TARGET?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
