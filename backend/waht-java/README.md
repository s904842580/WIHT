# waht-java

`waht-java` 是 WAHT 的 Spring Boot 3 主后端。

## 当前阶段

状态：`[NOW]`

目标：

- 用 IDEA 能直接运行。
- 结构由 WAHT 自己维护。
- 先做单体后端。
- 代码注释尽量完整。
- Maven 依赖走 `G:\maven\repository`。

## 后续建议结构

```text
waht-java/
  .mvn/
    maven.config
  pom.xml
  src/
    main/
      java/
        com/waht/platform/
          common/       # 公共响应、异常、上下文
          config/       # Spring 配置
          controller/   # 接口层
          service/      # 业务层
          mapper/       # MyBatis Mapper
          entity/       # 数据库实体
          dto/          # 请求对象
          vo/           # 响应视图对象
      resources/
        application.yml
```

## 已搭建内容

- 健康检查。
- `BaseResponse<T>` 统一响应。
- `ServiceException` 与全局异常处理。
- MySQL 基础连接。
- MyBatis-Plus 基础 CRUD。
- 登录接口。
- JWT 鉴权拦截器。
- 可配置 CORS 白名单。
- `X-Request-Id` 请求追踪和日志关联。
- 学习笔记公开分页、关键词、分类和标签查询接口。
- 学习笔记作者工作台 CRUD、草稿和发布接口，含正文大小和关联 ID 校验。
- 项目展示公开查询接口。
- Maven 本地仓库配置：`G:\maven\repository`。

## 本地命令

```powershell
cd E:\projects\WAHT\backend\waht-java
mvn test
mvn spring-boot:run
```

健康检查：

```text
GET http://localhost:8080/api/health
```

登录接口：

```text
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

注册接口：

```text
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "new_user",
  "password": "123456",
  "nickname": "新用户"
}
```

当前用户：

```text
GET http://localhost:8080/api/auth/me
Authorization: Bearer 登录返回的token
```

学习笔记公开接口：

```text
GET http://localhost:8080/api/notes?page=1&pageSize=10&keyword=Spring&categoryId=1&tagId=2
GET http://localhost:8080/api/notes/{slug}
GET http://localhost:8080/api/note-categories
GET http://localhost:8080/api/note-tags
```

公开列表查询参数：

- `page`：页码，从 1 开始，默认 1。
- `pageSize`：每页数量，范围 1 到 50，默认 10。
- `keyword`：匹配标题或摘要，最长 100 个字符。
- `categoryId`：分类 ID，可选。
- `tagId`：标签 ID，可选。

列表响应的 `data` 为 `PageResponse`，包含 `items`、`page`、`pageSize`、`total`、`totalPages`、`hasPrevious` 和 `hasNext`。

登录后的“我的笔记”接口：

```text
GET    http://localhost:8080/api/my/notes
GET    http://localhost:8080/api/my/notes/{noteId}
POST   http://localhost:8080/api/my/notes
PUT    http://localhost:8080/api/my/notes/{noteId}
POST   http://localhost:8080/api/my/notes/{noteId}/publish
POST   http://localhost:8080/api/my/notes/{noteId}/draft
DELETE http://localhost:8080/api/my/notes/{noteId}
Authorization: Bearer 登录返回的token
```

新建和编辑请求示例：

```json
{
  "title": "Spring Boot 学习笔记",
  "slug": "spring-boot-study-note",
  "summary": "记录今天完成的登录和笔记功能。",
  "content": "## 今日学习\n\n- JWT 鉴权\n- MyBatis-Plus CRUD",
  "categoryId": 1,
  "tagIds": [1, 4]
}
```

新建固定保存为 `DRAFT`；调用发布接口后才变为 `PUBLISHED` 并出现在公开列表。正文最长 200000 个字符，分类和标签 ID 必须为正整数。

项目展示公开接口：

```text
GET http://localhost:8080/api/projects
GET http://localhost:8080/api/projects/{slug}
GET http://localhost:8080/api/tech-stacks
```

第一阶段鉴权规则：

- 放行：`/api/health`
- 放行：`/api/auth/login`
- 放行：`/api/auth/register`
- 放行：`/api/notes`
- 放行：`/api/notes/**`
- 放行：`/api/note-categories`
- 放行：`/api/note-tags`
- 放行：`/api/projects`
- 放行：`/api/projects/**`
- 放行：`/api/tech-stacks`
- 其他 `/api/**` 默认需要登录

认证补充规则：

- token 校验通过后仍会查询 `waht_user`，账号不存在或状态不是 `ACTIVE` 时返回 401。
- `/api/my/notes/**` 会同时校验 `created_by`，普通用户只能管理自己的笔记。

MySQL 连接可通过环境变量覆盖：

```text
WAHT_DB_URL
WAHT_DB_USERNAME
WAHT_DB_PASSWORD
WAHT_SERVER_PORT
WAHT_JWT_SECRET
WAHT_JWT_EXPIRATION_MINUTES
WAHT_CORS_ALLOWED_ORIGINS
```

环境变量字段样例见 `.env.example`，真实密码不要写入仓库。响应头中的 `X-Request-Id` 可用于查找同一次请求的服务端日志。

## 当前不做

- 不拆微服务。
- 不接 Go。
- 不接 AI。
- 不写 Docker。
