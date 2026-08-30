# 前端设计草案

当前版本：V0.5
当前范围：`waht-web` 已实现公开内容、登录注册和个人笔记写作台，独立管理后台仍只做规划。

## 前端定位

WAHT 前端建议分成两个应用：

```text
frontend/
  waht-web/       # 用户前台，优先做
  waht-admin/     # 管理后台，后续做
```

当前优先级：

1. `waht-web`：支持登录、注册、公开笔记、项目展示和个人笔记写作台。
2. `waht-admin`：登录、内容管理、素材管理。

原因：

- 个人写作功能先集成进 `waht-web`，已经可以验证完整内容链路。
- 分类、标签、项目和素材的全局管理以后再放进独立后台。

## 技术选型建议

推荐：

```text
React + TypeScript + Vite + React Router + TanStack Query + Tailwind CSS
```

说明：

- `Vite`：启动快，适合当前学习阶段。
- `React Router`：前端页面路由。
- `TanStack Query`：管理接口请求、缓存、加载状态。
- `Tailwind CSS`：快速写页面样式。
- 暂不使用 Next.js，避免一开始引入 SSR、服务端渲染、部署复杂度。

后续如果要做正式公开站点，再考虑切换或新增 Next.js。

## API 对接约定

Java 后端地址：

```text
http://localhost:8080
```

前端环境变量建议：

```text
VITE_API_BASE_URL=http://localhost:8080
```

统一响应格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-29T10:00:00"
}
```

前端请求封装规则：

- `code === 0`：返回 `data`。
- `code === 401`：清理 token，跳转登录页。网络故障保留 token，并提供重新验证入口。
- 其他错误：显示 `message`。

## 登录设计

### 登录接口

```text
POST /api/auth/login
```

请求：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

响应中的关键字段：

```json
{
  "tokenType": "Bearer",
  "token": "...",
  "expiresIn": 7200,
  "user": {
    "id": 1,
    "username": "admin",
    "nickname": "WAHT Admin",
    "role": "ADMIN"
  }
}
```

前端处理：

- token 第一阶段存 `localStorage`。
- 请求时加请求头：

```text
Authorization: Bearer token
```

### 当前用户接口

```text
GET /api/auth/me
```

用途：

- 刷新页面后恢复登录状态。
- 判断 token 是否仍然有效。
- 获取当前用户信息。

## waht-web 页面设计

`waht-web` 是用户可见前台，不强制登录。

建议路由：

```text
/                    首页
/notes               学习笔记列表
/notes/:slug         学习笔记详情
/projects            项目展示列表
/projects/:slug      项目详情
/games               游戏科普列表
/assets              素材展示
/login               登录页，当前可先作为调试入口
/register            注册页
/workspace/notes     我的笔记，需要登录
/workspace/notes/new 新建笔记，需要登录
/workspace/notes/:id/edit 编辑笔记，需要登录
```

### 首页

内容：

- 最近学习笔记。
- 推荐项目。
- 技术栈概览。
- 游戏科普入口。

笔记和项目展示已经使用后端数据，首页聚合仍可后续补充。

### 学习笔记

列表字段：

- 标题。
- 摘要。
- 分类。
- 标签。
- 发布时间。
- 浏览次数。

详情字段：

- 标题。
- 正文。
- 分类。
- 标签。
- 发布时间。

对应后端表：

- `waht_note`
- `waht_note_category`
- `waht_note_tag`
- `waht_note_tag_rel`

### 项目展示

列表字段：

- 项目名称。
- 摘要。
- 封面。
- 技术栈。

详情字段：

- 项目说明。
- 技术栈。
- 仓库链接。
- 演示链接。
- 文档链接。

对应后端表：

- `waht_project`
- `waht_project_link`
- `waht_tech_stack`
- `waht_project_tech_rel`

### 素材展示

第一阶段只展示 `PUBLIC_SAFE` 素材。

列表字段：

- 素材名称。
- 类型。
- 缩略图。
- 来源说明。
- 授权说明。

对应后端表：

- `waht_asset`

## waht-admin 页面设计

`waht-admin` 是管理后台，需要登录。

建议路由：

```text
/login               登录
/                    控制台
/notes               笔记管理
/notes/new           新建笔记
/notes/:id/edit      编辑笔记
/categories          分类管理
/tags                标签管理
/projects            项目管理
/assets              素材管理
```

第一阶段后台可以先只做：

1. 登录页。
2. 当前用户信息。
3. 笔记列表占位页。

不建议现在重复建设完整后台，因为个人写作闭环已经集成在 waht-web。

## 前端目录建议

以 `waht-web` 为例：

```text
waht-web/
  src/
    app/             # 应用入口、路由
    api/             # 请求封装
    components/      # 通用组件
    features/        # 业务模块
      auth/
      notes/
      projects/
      assets/
    layouts/         # 页面布局
    styles/          # 全局样式
    types/           # TypeScript 类型
```

管理后台 `waht-admin` 可以复用同样结构。

## 前后端接口推进顺序

### Step 1：前端基础工程

- Vite React TypeScript。
- 配置 API 地址。
- 封装请求客户端。
- 做登录页。
- 调通 `/api/auth/login` 和 `/api/auth/me`。
- 做注册页。
- 调通 `/api/auth/register`。

### Step 2：后端补笔记查询接口

建议先做公开查询：

```text
GET /api/notes
GET /api/notes/{slug}
GET /api/note-categories
GET /api/note-tags
```

前端对应：

- 笔记列表。
- 笔记详情。
- 分类筛选。
- 标签展示。

当前状态：已完成基础笔记列表和详情对接。

### Step 3：后端补项目展示接口

```text
GET /api/projects
GET /api/projects/{slug}
```

前端对应：

- 项目列表。
- 项目详情。

### Step 4：作者笔记管理接口

```text
GET    /api/my/notes
POST   /api/my/notes
PUT    /api/my/notes/{id}
POST   /api/my/notes/{id}/publish
POST   /api/my/notes/{id}/draft
DELETE /api/my/notes/{id}
```

前端对应：

- 个人写作台笔记 CRUD、状态切换、Markdown 工具栏、全屏预览和本地恢复，当前已完成。

## 当前不建议做

- 不做复杂权限菜单。
- 不做前端状态管理库，例如 Redux。
- 不做 SSR。
- 不做公开部署。
- 不做完整后台。
- 不把素材版权不明确的图片放进公开前台。

## 视觉方向

WAHT 是个人学习平台，不做营销站风格。

建议风格：

- 内容优先。
- 页面密度适中。
- 首页突出学习笔记和项目，而不是大幅宣传页。
- 后台使用清晰、克制的管理界面。
- 素材展示要明确标出公开状态和来源。

## V0.4 写作与登录补充

### 编辑器

- 数据库存储格式继续使用 Markdown 纯文本。
- 格式工具栏只生成 Markdown，不维护第二份富文本状态。
- 本地草稿按“用户 ID + 笔记 ID”隔离。
- 本地草稿只用于崩溃和刷新恢复，MySQL 仍是正式数据源。
- 恢复旧草稿必须由用户确认，不能静默覆盖数据库版本。

### 登录状态

- token 签名和有效期由后端 JWT 校验。
- 每次受保护请求再次查询用户状态。
- 只有明确的 401 才清理 token；网络故障显示重试入口。
- 登录、退出和 token 失效都要清理 my-notes 私有缓存。

### 登录视觉

- 使用暖红、奶油白、梅花、灵蝶和可爱幽灵的原创二次元素材。
- 不引用游戏官方图片、Logo 或现成角色立绘。
- 表单使用高对比浅色底，确保背景图不会影响输入可读性。

## V0.5 个人创作首页与项目导航

### 首页结构

首页从传统工作台调整为个人创作档案：

1. 首屏展示“现在正在欣赏、最近正在整理、持续正在构建”三种状态。
2. 轮播只保留三个小方块，支持点击切换、自动播放、悬停和键盘聚焦暂停。
3. WAHT 只作为左上角的小型个人署名。
4. “关于我”使用真实公开笔记填充最近想法。
5. “作品档案”使用真实项目接口，项目不足三项时显示规划占位。

首页使用独立沉浸式导航；登录、笔记、项目详情等功能页面继续使用原来的工作型导航，避免视觉改造破坏已有操作闭环。

### 两级项目导航

项目访问关系调整为：

```text
首页作品档案
  -> /projects?focus={projectSlug}
  -> 项目总览突出所选项目
  -> /projects/{projectSlug}
  -> 项目详情、文档和演示入口
```

这样首页负责“选择作品”，项目总览负责“理解项目”，详情页负责“查看完整证据”。

### 笔记页面下一步

笔记页不直接复制首页项目卡片，建议采用创作杂志式索引：

- 顶部放一篇置顶笔记，说明当前学习主线。
- 搜索、分类和标签保留，但压缩成单行工具栏。
- 普通笔记改为按日期排列的编辑式行列表。
- 笔记详情保持窄正文阅读区，后续增加目录、关联项目和系列导航。
- 项目详情可以关联笔记，笔记详情也可以反向链接项目。

这一调整作为下一里程碑，本轮没有改动现有笔记查询和编辑功能。

### 笔记编辑入口

- 登录用户可以从公开笔记列表进入个人写作台或新建笔记。
- 公开详情页会通过当前用户的私有笔记列表判断文章归属。
- 只有文章属于当前账号时，详情页才显示编辑这篇笔记，并跳转到 /workspace/notes/{id}/edit。
- 实际更新仍由需要登录的 PUT /api/my/notes/{id} 完成，后端继续校验笔记归属。

### 本地素材

奥黛塔图片与未审核的生成三联画仅用于本地预览，已加入 `.gitignore`。公开部署前必须替换成获得明确授权或审核通过的素材。
