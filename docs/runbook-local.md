# 本地运行说明

本文档说明如何在 Windows 上从数据库初始化到前后端联调完整启动 WAHT。

## 环境要求

- JDK 17。
- Maven 3.9 或兼容版本。
- Node.js 18 或更高版本。
- npm。
- Python 3.11 或更高版本。
- MySQL 8.x 与 MySQL CLI。
- 项目目录：`E:\projects\WAHT`。

先执行环境预检：

```powershell
cd E:\projects\WAHT
.\scripts\check-local.ps1
```

## Maven 本地仓库

后端通过 `backend/waht-java/.mvn/maven.config` 固定使用：

```text
G:/maven/repository
```

从 `backend/waht-java` 执行 Maven 命令时会自动加载该配置。

## 后端环境变量

字段样例见 `backend/waht-java/.env.example`。Spring Boot 不会自动读取这个样例文件，需要在 IDEA Run Configuration、当前 PowerShell 会话或 Windows 环境变量中设置。

当前 PowerShell 会话示例：

```powershell
$env:WAHT_DB_USERNAME = 'root'
$env:WAHT_DB_PASSWORD = '你的本地密码'
$env:WAHT_JWT_SECRET = '至少32位且仅用于当前环境的随机字符串'
```

重要规则：

- 在“高级系统设置”中修改环境变量后，已经运行的 IDEA、终端和 Java 进程不会自动获得新值。
- 修改完成后需要完全关闭并重新打开 IDEA 或终端，再启动项目。
- 不要把真实密码写入 `.env.example`、YAML、脚本或 Git。

可选变量：

```text
WAHT_SERVER_PORT
WAHT_DB_URL
WAHT_DB_USERNAME
WAHT_DB_PASSWORD
WAHT_JWT_SECRET
WAHT_JWT_EXPIRATION_MINUTES
WAHT_AGENT_DELEGATION_EXPIRATION_MINUTES
WAHT_AGENT_BASE_URL
WAHT_AGENT_SERVICE_TOKEN
WAHT_AGENT_CONNECT_TIMEOUT_MILLIS
WAHT_AGENT_READ_TIMEOUT_MILLIS
WAHT_CORS_ALLOWED_ORIGINS
```

## 初始化 MySQL

首次安装按顺序执行初始化脚本：

```powershell
mysql --default-character-set=utf8mb4 --user=root --password -e "source E:/projects/WAHT/database/mysql/init/001_init_schema.sql"
mysql --default-character-set=utf8mb4 --user=root --password -e "source E:/projects/WAHT/database/mysql/init/002_init_admin_user.sql"
mysql --default-character-set=utf8mb4 --user=root --password -e "source E:/projects/WAHT/database/mysql/init/003_init_note_demo_data.sql"
mysql --default-character-set=utf8mb4 --user=root --password -e "source E:/projects/WAHT/database/mysql/init/004_init_project_demo_data.sql"
mysql --default-character-set=utf8mb4 --user=root --password -e "source E:/projects/WAHT/database/mysql/init/005_init_agent_schema.sql"
```

已经初始化过数据库时，只执行尚未执行的 `database/mysql/migration` 脚本。不要重复修改并重新执行旧迁移文件。

启用 Agent 时，现有 `waht` 数据库还需要执行：

```powershell
mysql --default-character-set=utf8mb4 --user=root --password -e "source E:/projects/WAHT/database/mysql/migration/20260830_001_add_agent_note_draft_request.sql"
```

## 启动后端

推荐使用脚本：

```powershell
cd E:\projects\WAHT
.\scripts\start-backend.ps1 -Port 8080
```

也可以直接执行：

```powershell
cd E:\projects\WAHT\backend\waht-java
mvn spring-boot:run
```

检查地址：

```text
http://localhost:8080/api/health
http://localhost:8080/actuator/health
```

`/api/health` 检查 Web 应用是否响应；`/actuator/health` 还会汇总 Spring Boot 注册的组件健康状态。

## 启动 AI 学习助手

首次安装 Python 依赖：

```powershell
cd E:\projects\WAHT\backend\ai-service
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -e ".[dev]"
```

Java 和 Python 必须配置相同的共享令牌。以下变量设置在各自的启动终端中：

```powershell
$env:WAHT_AGENT_SERVICE_TOKEN = '本地随机共享令牌'
$env:WAHT_AGENT_DB_URL = 'mysql+pymysql://root:你的密码@127.0.0.1:3306/waht_ai?charset=utf8mb4'
$env:WAHT_CORE_BASE_URL = 'http://127.0.0.1:8080'
$env:OPENAI_API_KEY = '你的模型服务密钥'
$env:WAHT_AI_MODEL = '你要使用的模型名称'
```

启动服务：

```powershell
cd E:\projects\WAHT
.\scripts\start-agent.ps1 -Port 8000
```

检查地址：

```text
http://127.0.0.1:8000/health
http://127.0.0.1:5173/workspace/agent
```

没有配置 `OPENAI_API_KEY` 或 `WAHT_AI_MODEL` 时，会话查询仍可使用，发送消息明确返回 503。

## 启动前端

首次安装依赖：

```powershell
cd E:\projects\WAHT\frontend\waht-web
npm install
```

启动开发服务器：

```powershell
cd E:\projects\WAHT
.\scripts\start-frontend.ps1 -Port 5173 -ApiProxyTarget http://localhost:8080
```

默认页面：

```text
http://127.0.0.1:5173/login
http://127.0.0.1:5173/notes
http://127.0.0.1:5173/workspace/notes
http://127.0.0.1:5173/workspace/agent
```

## 构建验证

提交代码前执行：

```powershell
cd E:\projects\WAHT
.\scripts\verify-project.ps1
```

该脚本依次运行 Java 测试、前端生产构建和 Python Agent 测试。

## 常见问题

### 数据库连接失败

先重新运行 `scripts/check-local.ps1`。重点确认 MySQL 服务已启动、密码环境变量存在，并且启动 Java 的 IDEA/终端是在配置变量后重新打开的。

### 访问 `/api` 返回 401

`/api` 不是页面，也不是公开接口。登录页在前端 `/login`，后端登录接口为 `POST /api/auth/login`。

### 前端端口被占用

Vite 使用严格端口模式，会直接提示端口冲突。关闭占用进程，或使用：

```powershell
.\scripts\start-frontend.ps1 -Port 5174 -ApiProxyTarget http://localhost:8080
```

### 跨域请求被拦截

开发环境推荐使用 Vite `/api` 代理。必须跨域直连时，把实际前端来源加入 `WAHT_CORS_ALLOWED_ORIGINS`，多个地址使用逗号分隔，然后重启后端。
