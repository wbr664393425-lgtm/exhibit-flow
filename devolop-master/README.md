# Pig4Cloud 单体开发脚手架

基于 pig4cloud 框架的 Spring Boot 3 单体应用开发脚手架，包含用户管理、权限认证、全局异常处理等基础能力，可直接用于新业务模块开发。

## 环境要求

| 工具 | 版本要求 |
|------|----------|
| JDK | 21+ |
| Maven | 3.8+ |
| Docker | 20.10+ (可选) |
| Docker Compose | 2.0+ (可选) |
| MySQL | 8.0+ |
| Redis | 6.0+ |

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.5.6 |
| MyBatis-Plus | 3.x |
| Spring Security + OAuth2 | - |
| Redis | - |
| MySQL | 5.7+ |

## 项目结构

```
├── pig-boot          # 主应用模块（在此添加业务代码）
│   ├── auth/         # OAuth2 认证模块（已就绪）
│   └── config/       # 全局配置（异常处理器等）
├── pig-common        # 公共模块
│   ├── pig-common-core       # 核心工具
│   ├── pig-common-security   # 安全组件
│   ├── pig-common-mybatis    # MyBatis 封装
│   ├── pig-common-swagger    # 接口文档
│   ├── pig-common-excel      # Excel 导出
│   └── pig-common-log        # 操作日志
└── pig-upms          # 用户权限管理（已就绪）
```

## 快速开始

### 方式一：Docker 一键部署（推荐）

**适用场景**: 快速体验、生产部署、团队协作

```bash
# 1. 复制环境配置文件
cp .env.example .env

# 2. 编辑 .env 文件，修改数据库和Redis密码（可选，有默认值）
vim .env

# 3. 将数据库初始化SQL脚本放到 db/init/ 目录（可选）
# 容器启动时会自动执行该目录下的 .sql 文件

# 4. 一键启动所有服务
./deploy.sh start

# 查看服务状态
./deploy.sh status

# 查看日志
./deploy.sh logs

# 停止服务
./deploy.sh stop
```

**访问地址**:
- 应用首页: http://localhost:9999/admin
- 接口文档: http://localhost:9999/admin/doc.html
- MySQL: localhost:33306
- Redis: localhost:36379

### 方式二：本地开发模式

**适用场景**: 开发调试、源码学习

#### 1. 配置环境变量或修改配置文件

**推荐方式**: 使用环境变量（更安全）
```bash
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3306
export DB_NAME=edu_fund
export DB_USERNAME=root
export DB_PASSWORD=your_password
export REDIS_HOST=127.0.0.1
export REDIS_PASSWORD=your_redis_password
```

**或者**: 编辑 `pig-boot/src/main/resources/application-dev.yml`
```yaml
spring:
  datasource:
    username: ${DB_USERNAME:root}
    password: ENC(${DB_PASSWORD:sdbs})  # 使用Jasypt加密
  data:
    redis:
      host: ${REDIS_HOST:127.0.0.1}
      password: ENC(${REDIS_PASSWORD:mypassword})
```

#### 2. 初始化数据库

执行 pig4cloud 基础数据库脚本（sys_user, sys_role, sys_menu 等系统表）。

#### 3. 启动项目

```bash
# 编译项目
mvn clean compile -DskipTests

# 启动应用
mvn spring-boot:run -pl pig-boot
```

**访问地址**:
- 应用首页: http://localhost:9999/admin
- 接口文档: http://localhost:9999/admin/doc.html

## 添加业务模块

在 `pig-boot/src/main/java/com/pig4cloud/pig/` 下创建业务包，例如：

```
pig-boot/src/main/java/com/pig4cloud/pig/
├── config/                    # 全局配置（已有异常处理器）
├── auth/                      # 认证模块（已就绪）
└── your_module/               # ← 新业务模块
    ├── controller/
    ├── service/impl/
    ├── mapper/
    ├── entity/
    └── dto/
```

对应 Mapper XML 放在：
```
pig-boot/src/main/resources/mapper/your_module/
```

## 内置能力

- ✅ OAuth2 密码/短信登录
- ✅ 用户管理（RBAC）
- ✅ 全局异常处理器
- ✅ MyBatis-Plus CRUD 封装
- ✅ Knife4j 接口文档
- ✅ Excel 导出
- ✅ 操作日志
- ✅ Docker 一键部署
- ✅ Jasypt 配置加密
- ✅ 连接池优化配置

## 部署脚本说明

项目提供了 `deploy.sh` 脚本用于简化部署操作：

```bash
./deploy.sh start    # 启动所有服务
./deploy.sh stop     # 停止所有服务
./deploy.sh restart  # 重启所有服务
./deploy.sh logs     # 查看所有日志
./deploy.sh logs pig-boot  # 查看指定服务日志
./deploy.sh status   # 查看服务状态
./deploy.sh clean    # 清理所有数据（危险操作）
```

## 配置加密说明

项目使用 Jasypt 对敏感配置进行加密：

1. **加密密码**: 在 `application-dev.yml` 中使用 `ENC()` 包裹加密后的值
2. **加密密钥**: 通过环境变量 `JASYPT_ENCRYPTOR_PASSWORD` 设置（默认: pig）
3. **环境变量优先**: 配置支持环境变量覆盖，生产环境建议使用环境变量

## 性能优化配置

### 数据库连接池（HikariCP）
- 最大连接数: 10（单体应用适中配置）
- 最小空闲连接: 2（降低资源占用）
- 连接超时: 5秒
- 空闲超时: 10分钟

### Redis连接池（Lettuce）
- 最大活跃连接: 8
- 最大空闲连接: 4
- 最小空闲连接: 1

### Undertow服务器
- IO线程: 4（等于CPU核心数）
- Worker线程: 32

## 常见问题

### 1. Docker部署失败？
- 检查Docker和Docker Compose是否正确安装
- 确认端口 9999、33306、36379 未被占用
- 查看日志: `./deploy.sh logs`

### 2. 数据库连接失败？
- 检查 `.env` 文件中的数据库配置
- 确认MySQL服务已启动
- 检查数据库是否已创建

### 3. Redis连接失败？
- 检查 `.env` 文件中的Redis密码
- 确认Redis服务已启动
- 检查防火墙设置

### 4. 如何修改端口？
编辑 `.env` 文件:
```bash
APP_PORT=8080  # 修改应用端口
```

## 项目结构说明

```
.
├── deploy.sh              # 一键部署脚本
├── docker-compose.yml     # Docker编排配置
├── .env.example           # 环境变量模板
├── db/                    # 数据库相关
│   └── init/              # 数据库初始化脚本目录
├── pig-boot/              # 主应用模块
├── pig-common/            # 公共模块
└── pig-upms/              # 用户权限管理
```

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

Apache License 2.0
