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
- 统一响应。
- 全局异常。
- MySQL 基础连接。
- MyBatis-Plus 基础 CRUD。
- 登录接口。
- JWT 鉴权拦截器。
- 学习笔记公开查询接口。
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
GET http://localhost:8080/api/notes
GET http://localhost:8080/api/notes/{slug}
GET http://localhost:8080/api/note-categories
GET http://localhost:8080/api/note-tags
```

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

MySQL 连接可通过环境变量覆盖：

```text
WAHT_DB_URL
WAHT_DB_USERNAME
WAHT_DB_PASSWORD
WAHT_SERVER_PORT
WAHT_JWT_SECRET
WAHT_JWT_EXPIRATION_MINUTES
```

## 当前不做

- 不拆微服务。
- 不接 Go。
- 不接 AI。
- 不写 Docker。
