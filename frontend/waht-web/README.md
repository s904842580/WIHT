# waht-web

WAHT 用户前台，使用 React + TypeScript + Vite。

## 技术栈

- React：组件化 UI。
- TypeScript：给接口数据和组件参数加类型。
- Vite：本地开发和打包。
- React Router：页面路由。
- TanStack Query：接口请求状态和缓存。
- Tailwind CSS：页面样式。

## 本地启动

```powershell
cd E:\projects\WAHT\frontend\waht-web
npm install
npm run dev
```

默认前端地址：

```text
http://localhost:5173
```

默认后端代理：

```text
/api -> http://localhost:8080
```

## 登录联调

后端启动后，使用本地管理员账号：

```text
username: admin
password: admin123
```

登录成功后，前端会把 token 保存到 `localStorage`，后续请求自动携带：

```text
Authorization: Bearer token
```

注册页：

```text
http://localhost:5173/register
```

学习笔记：

```text
http://localhost:5173/notes
http://localhost:5173/notes/spring-boot-auth-flow
```

项目展示：

```text
http://localhost:5173/projects
http://localhost:5173/projects/waht-java-backend
```
