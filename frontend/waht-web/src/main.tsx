import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from '@/app/App';
import { AppErrorBoundary } from '@/app/AppErrorBoundary';
import '@/styles/index.css';

// 前端入口：创建 React 根节点，并渲染应用根组件。
const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Missing #root element in index.html');
}

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <AppErrorBoundary>
      <App />
    </AppErrorBoundary>
  </React.StrictMode>,
);
