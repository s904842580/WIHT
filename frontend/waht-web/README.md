# waht-web

WAHT 用户前台，使用 React + TypeScript + Vite。

## 技术栈

- React：组件化 UI。
- TypeScript：给接口数据和组件参数加类型。
- Vite：本地开发和打包。
- React Router：页面路由。
- TanStack Query：接口请求状态和缓存。
- Tailwind CSS：页面样式。
- React Markdown + remark-gfm：Markdown/GFM 正文和实时预览。

## 本地启动

```powershell
cd E:\projects\WAHT\frontend\waht-web
npm install
npm run dev
```

也可以从项目根目录执行：

```powershell
.\scripts\start-frontend.ps1 -Port 5173 -ApiProxyTarget http://localhost:8080
```

默认前端地址：

```text
http://localhost:5173
```

默认后端代理：

```text
/api -> http://localhost:8080
```

环境变量字段见 `.env.example`。Vite 使用严格端口模式，5173 被占用时会直接报错，不会自动切换到其他地址。

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
http://localhost:5173/notes?keyword=Spring&categoryId=1&tagId=2&page=1
http://localhost:5173/notes/spring-boot-auth-flow
```

公开笔记页支持关键词、分类、标签和分页筛选。筛选状态保存在 URL 查询参数中，刷新或分享链接后可以恢复。

作者工作台（需要登录）：

```text
http://localhost:5173/workspace/notes
http://localhost:5173/workspace/notes/new
http://localhost:5173/workspace/notes/{noteId}/edit
```

写作流程：

1. 新建笔记并选择分类、标签。
2. 使用 Markdown 编写正文，右侧同步预览。
3. 只保存时保留为草稿，点击“保存并发布”后进入公开笔记列表。
4. 已发布笔记可以撤回为草稿或继续编辑。

登录后，公开笔记列表会显示管理笔记和新建笔记入口；打开本人发布的笔记详情时，也可以直接点击编辑这篇笔记进入编辑器。公开阅读页本身不直接进入编辑状态，避免访客误操作。

token 失效或用户被禁用时，请求层会清理本地 token，受保护路由会跳回登录页。

根组件使用全局错误边界，组件渲染异常时会显示刷新入口，并把详细错误写入浏览器控制台。

项目展示：

```text
http://localhost:5173/projects
http://localhost:5173/projects/waht-java-backend
```

## Markdown 编辑器

写作台现已支持：

- 标题、摘要、分类和最多 10 个标签。
- 标题、粗体、斜体、删除线、引用、列表、代码、链接、图片地址和分隔线工具。
- Ctrl+B、Ctrl+I、Ctrl+K、Ctrl+S 与 Tab 缩进。
- 编辑、分栏、预览和全屏模式。
- 字词、字符、行数和预计阅读时间统计。
- 800ms 防抖本地备份、刷新恢复提示和未保存离开提醒。

本地备份只用于浏览器恢复，手动点击保存后才会写入 MySQL。

## 登录视觉

登录页使用项目生成的原创二次元背景，采用暖红、奶油白、梅花、灵蝶和可爱幽灵元素。素材文件为 src/assets/plum-spirit-login.jpg，不是游戏官方图片。

## V0.5 首页与项目入口

首页现在使用独立的个人创作档案布局：

- 三个小方块切换“正在欣赏、最近整理、持续构建”三种状态。
- 最近想法读取公开笔记接口。
- 作品档案读取公开项目接口。
- 点击真实作品进入 `/projects?focus={slug}`，项目总览突出所选项目，再进入 `/projects/{slug}` 查看详情。

本地预览素材：

- `src/assets/odette-current.avif`：用户提供的奥黛塔图片，仅本地使用。
- `src/assets/archive-triptych.webp`：首页和项目页三联画，公开使用前需要审核。

两张图片都已加入根目录 `.gitignore`。克隆仓库后若缺少这些本地素材，需要按 `src/assets/README.md` 准备替代图片。
