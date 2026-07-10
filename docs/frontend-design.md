# 前端设计草案

当前版本：V0.2  
当前范围：`waht-web` 已开始实现，管理后台仍只做规划。

## 前端定位

WAHT 前端建议分成两个应用：

```text
frontend/
  waht-web/       # 用户前台，优先做
  waht-admin/     # 管理后台，后续做
```

当前优先级：

1. `waht-web`：已搭建基础工程，当前支持登录、注册、笔记列表和笔记详情。
2. `waht-admin`：登录、内容管理、素材管理。

原因：

- 当前 Java 后端刚完成登录基础能力，业务接口还没完整。
- 用户前台更适合先验证内容结构。
- 管理后台依赖更多 CRUD 接口，可以晚一点做。

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
- `code === 401`：清理 token，跳转登录页。
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
```

### 首页

内容：

- 最近学习笔记。
- 推荐项目。
- 技术栈概览。
- 游戏科普入口。

第一版数据可以先用静态假数据，等后端接口补齐后替换。

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

不建议现在一次性做完整后台，因为后端 CRUD 接口还没完成。

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

### Step 4：后端补后台管理接口

```text
POST /api/admin/notes
PUT /api/admin/notes/{id}
DELETE /api/admin/notes/{id}
```

前端对应：

- 管理后台笔记 CRUD。

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
