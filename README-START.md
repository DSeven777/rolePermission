# 项目启动指南

本项目已重构为 Spring Cloud Alibaba 微服务架构。

## 1. 环境准备

在启动服务之前，必须确保以下基础设施已运行：

*   **Nacos Server (2.x)**
    *   地址: `localhost:8848`
    *   作用: 服务注册与发现、配置中心
*   **Redis**
    *   地址: `localhost:6379`
    *   作用: 缓存、Token 存储
*   **MySQL**
    *   地址: `localhost:3306`
    *   数据库: `rolePermission`
    *   **注意**: 请确保修改各服务 `src/main/resources/application.yml` 中的数据库密码。

### 快速启动基础设施 (Docker)
如果你安装了 Docker，可以直接运行：
```bash
docker-compose up -d
```
这将启动 Nacos 和 Redis。

## 2. 启动服务

### 方法 A: 使用 IDE (推荐用于开发)
在 IDE (如 IntelliJ IDEA, Trae, Eclipse) 中，找到以下类并依次启动：

1.  `gateway-service`: `com.dseven.rolepermission.gateway.GatewayServiceApplication`
2.  `user-service`: `com.dseven.rolepermission.user.UserServiceApplication`
3.  `permission-service`: `com.dseven.rolepermission.permission.PermissionServiceApplication`
4.  `auth-service`: `com.dseven.rolepermission.auth.AuthServiceApplication`
5.  `notification-service`: `com.dseven.rolepermission.notification.NotificationServiceApplication`

### 方法 B: 使用脚本 (推荐用于预览)
在项目根目录下，使用 PowerShell 运行：
```powershell
./startup.ps1
```
该脚本会自动编译项目并弹出 5 个窗口分别运行各个服务。

## 3. 验证

*   **Nacos 控制台**: [http://localhost:8848/nacos](http://localhost:8848/nacos) (默认账号密码: nacos/nacos)
    *   查看 "服务管理 -> 服务列表"，应能看到所有服务均已注册。
*   **API 访问**: 所有请求通过网关访问
    *   地址: `http://localhost:8080`
    *   例如登录: `POST http://localhost:8080/auth/login`

## 4. 常见问题

*   **启动报错 "Connection refused"**: 检查 Nacos 或 Redis 是否已启动。
*   **启动报错 "Access denied for user"**: 检查 `application.yml` 中的 MySQL 密码是否正确。
*   **服务注册失败**: 确保 Nacos 版本匹配 (推荐 2.2+)，且端口 8848/9848 未被防火墙拦截。
