# 角色权限管理系统 (Role Permission System)

## 项目简介

这是一个基于 Spring Boot 3.x + MyBatis-Plus 的角色权限管理系统，提供了完整的用户认证、授权和权限管理功能。系统采用前后端分离架构，支持 JWT 令牌认证，使用 RBAC（基于角色的访问控制）模型进行权限管理。

## 技术栈

### 后端技术
- **Spring Boot 3.5.8** - 基础框架
- **MyBatis-Plus 3.5.15** - ORM 框架
- **Spring Security** - 安全框架
- **JWT 0.12.3** - 令牌认证
- **Redis** - 缓存和会话管理
- **MySQL 8.0** - 数据库
- **HikariCP** - 数据库连接池
- **Lombok** - 简化 Java 代码
- **Swagger/OpenAPI 3.0** - API 文档

### 开发工具
- **Maven** - 项目管理
- **Spring Boot Actuator** - 应用监控
- **JaCoCo** - 测试覆盖率

## 系统架构

### 核心模块
1. **用户管理 (User Management)**
   - 用户注册、登录、注销
   - 用户信息管理
   - 密码加密（BCrypt）

2. **角色管理 (Role Management)**
   - 角色定义和分配
   - 角色权限关联
   - 角色数据权限

3. **权限管理 (Permission Management)**
   - 菜单权限
   - 按钮权限
   - 数据权限

4. **部门管理 (Department Management)**
   - 组织架构管理
   - 部门层级关系
   - 数据权限范围

## 数据库设计

### 核心表结构

- **sys_user** - 用户表
- **sys_role** - 角色表
- **sys_permission** - 权限表
- **sys_dept** - 部门表
- **sys_user_role** - 用户角色关联表
- **sys_role_permission** - 角色权限关联表
- **sys_role_dept** - 角色部门数据权限表

### 特点
- 支持逻辑删除
- 自动填充创建时间和更新时间
- 支持乐观锁
- 使用雪花算法生成 ID

## 安全特性

### 密码安全
- 使用 BCrypt 算法加密存储
- 每次加密生成随机盐值
- 防止彩虹表攻击

### JWT 认证
- 访问令牌（Access Token）：1小时有效期
- 刷新令牌（Refresh Token）：7天有效期
- 令牌黑名单机制

### CORS 支持
- 支持跨域请求
- 可配置允许的源、方法和头部

## API 接口

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出
- `POST /api/auth/refresh` - 刷新令牌
- `GET /api/auth/verify` - 验证令牌

### Swagger 文档
访问地址：`http://localhost:8080/swagger-ui.html`

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/DSeven777/rolePermission.git
cd role-permission
```

2. **配置数据库**
   - 创建数据库：`rolePermission`
   - 执行 `init.sql` 初始化表结构

3. **修改配置**
   - 复制 `application-dev.yml.example` 为 `application-dev.yml`
   - 修改数据库连接信息和 Redis 配置

4. **启动项目**
```bash
mvn spring-boot:run
```

5. **访问系统**
- Swagger 文档：http://localhost:8080/swagger-ui.html


## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rolePermission
    username: your-username
    password: your-password
```

### JWT 配置
```yaml
app:
  jwt:
    secret: your-secret-key
    expiration: 3600000  # 1小时
    refresh-expiration: 604800000  # 7天
```

### MyBatis-Plus 配置
- 开启驼峰命名转换
- 支持分页插件
- 支持乐观锁
- 逻辑删除配置

## 开发规范

### 代码规范
- 使用 Lombok 简化代码
- 统一使用 RESTful API 风格
- 统一使用 Result 返回格式
- 所有 API 接口添加 Swagger 注解

### 分包结构
```
com.dseven.rolepermission
├── common          # 公共类
├── config          # 配置类
├── controller      # 控制器
├── entity          # 实体类
├── mapper          # Mapper 接口
├── service         # 服务层
├── sso            # 单点登录模块
├── utils          # 工具类
└── vo            # 视图对象
```

## 性能优化

### 数据库优化
- 使用 HikariCP 连接池
- 开启 MyBatis-Plus 二级缓存
- 合理使用索引

### 缓存策略
- Redis 缓存用户权限信息
- JWT 令牌缓存机制

## 监控与运维

### 健康检查
系统提供多种健康检查端点：
- 应用状态
- 数据库连接
- Redis 连接

### 日志配置
- 使用 SLF4J + Logback
- 支持彩色日志输出
- 可配置日志级别


### 集成测试
使用 Testcontainers 进行数据库集成测试

## 后续计划

- [ ] 添加 OAuth2 支持
- [ ] 实现多租户功能
- [ ] 添加操作日志记录
- [ ] 实现动态权限配置
- [ ] 添加数据权限注解支持
- [ ] 集成 Spring Security OAuth2 Resource Server
- [ ] 支持社交登录（微信、QQ等）
- [ ] 添加图形验证码
- [ ] 实现接口限流

