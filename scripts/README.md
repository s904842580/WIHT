# scripts

本目录保存 Windows 本地开发脚本，脚本不会向仓库写入密码。

## 当前脚本

- `check-local.ps1`：检查 Java、Maven、Node.js、npm、MySQL 和数据库密码环境变量。
- `start-backend.ps1`：在当前终端启动 Spring Boot，可通过 `-Port` 修改端口。
- `start-frontend.ps1`：在当前终端启动 Vite，可指定前端端口和后端代理地址。
- `verify-project.ps1`：依次执行 Maven 测试和前端生产构建。

## 使用示例

```powershell
.\scripts\check-local.ps1
.\scripts\start-backend.ps1 -Port 8080
.\scripts\start-frontend.ps1 -Port 5173 -ApiProxyTarget http://localhost:8080
.\scripts\verify-project.ps1
```
