# local

本目录是 WAHT 本地运行资料的入口。

当前可执行内容集中放在：

- `docs/runbook-local.md`：环境变量、数据库初始化、前后端启动和常见问题。
- `scripts/check-local.ps1`：开发环境预检。
- `scripts/start-backend.ps1`：启动 Spring Boot。
- `scripts/start-frontend.ps1`：启动 Vite。
- `scripts/verify-project.ps1`：后端测试与前端构建。

MySQL、Redis 和 MinIO 的独立服务配置以后按实际使用情况继续补充，本阶段不依赖 Docker。
