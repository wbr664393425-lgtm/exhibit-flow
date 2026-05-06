# Exhibit Flow 运维部署文档

> 适用范围：展厅访问申请与留存系统三端部署。后续功能变更后，按本文档重新构建、上传、重启和验证。

## 1. 部署现状

| 项 | 值 |
| --- | --- |
| 默认服务器 | `default` / `person` |
| 服务器 IP | `106.12.173.222` |
| 部署根目录 | `/opt/wbr/exhibit-flow` |
| 后端端口 | `1111` |
| 管理后台端口 | `1112` |
| H5 端口 | `1113` |
| 后端 systemd 服务 | `exhibit-flow-backend.service` |
| 后端运行方式 | 宿主机 Java 21 直接运行 |
| Java 21 路径 | `/usr/lib/jvm/java-21-openjdk-21.0.10.0.7-1.el9.alma.1.x86_64/bin/java` |
| nginx 配置 | `/etc/nginx/conf.d/exhibit-flow.conf` |
| 后端外置配置 | `/opt/wbr/exhibit-flow/backend/config/application-prod.yml` |
| 数据库 | MySQL `exhibit_flow` |
| Redis | 服务器本机 `127.0.0.1:6379` |
| 上传目录 | `/opt/wbr/exhibit-flow/upload` |

公网访问：

- 后端健康：`http://106.12.173.222:1111/admin/actuator/health`
- 管理后台：`http://106.12.173.222:1112`
- 移动端 H5：`http://106.12.173.222:1113`

敏感配置来源：

- 服务器生产配置：`/opt/wbr/exhibit-flow/backend/config/application-prod.yml`
- 参考配置：`/Users/wbr/Documents/project/exam-system/exam-backend/pig-boot/src/main/resources/application-prod.yml`

## 2. 项目结构

| 端 | 本地路径 | 技术栈 | 构建产物 |
| --- | --- | --- | --- |
| 后端 | `devolop-master/pig-boot` | Spring Boot 3.5 / Java 21 | `devolop-master/pig-boot/target/pig-boot.jar` |
| 管理后台 | `exhibit-flow-vue` | Vue 3 / Vite / Element Plus | `exhibit-flow-vue/dist/` |
| H5 | `exhibit-flow-h5` | Vue 3 / Vite / Vant | `exhibit-flow-h5/dist/` |

接口约定：

- 后端上下文路径保持 `/admin`。
- 管理后台 nginx 将 `/admin/` 转发到 `http://127.0.0.1:1111/admin/`。
- 管理后台 nginx 将 `/api/` 转发到 `http://127.0.0.1:1111/`。
- H5 nginx 将 `/admin/` 转发到 `http://127.0.0.1:1111/admin/`。

## 3. 标准发布流程

在本地仓库根目录执行：

```bash
cd /Users/wbr/Documents/project/exhibit-flow
```

### 3.1 构建后端

```bash
cd /Users/wbr/Documents/project/exhibit-flow/devolop-master
mvn -pl pig-boot -am clean package -DskipTests
```

上传后端 jar：

```bash
scp /Users/wbr/Documents/project/exhibit-flow/devolop-master/pig-boot/target/pig-boot.jar \
  default:/opt/wbr/exhibit-flow/backend/pig-boot.jar
```

### 3.2 构建管理后台

```bash
cd /Users/wbr/Documents/project/exhibit-flow/exhibit-flow-vue
npm ci
VITE_IS_MICRO=false \
VITE_ADMIN_PROXY_PATH=http://127.0.0.1:1111 \
VITE_PORT=1112 \
VITE_PUBLIC_PATH=/ \
VITE_API_URL= \
VITE_OAUTH2_PASSWORD_CLIENT=pig:pig \
VITE_OAUTH2_MOBILE_CLIENT=pig:pig \
VITE_OAUTH2_SOCIAL_CLIENT=pig:pig \
VITE_PWD_ENC_KEY=thanks,pig4cloud \
VITE_VERIFY_ENABLE=false \
npm run build
```

上传管理后台：

```bash
rsync -az /Users/wbr/Documents/project/exhibit-flow/exhibit-flow-vue/dist/ \
  default:/opt/wbr/exhibit-flow/admin-web/
```

### 3.3 构建 H5

```bash
cd /Users/wbr/Documents/project/exhibit-flow/exhibit-flow-h5
npm ci
VITE_API_BASE=/admin npm run build
```

上传 H5：

```bash
rsync -az /Users/wbr/Documents/project/exhibit-flow/exhibit-flow-h5/dist/ \
  default:/opt/wbr/exhibit-flow/h5-web/
```

### 3.4 重启后端

```bash
ssh default "systemctl restart exhibit-flow-backend.service"
```

nginx 配置未变化时不需要重载。若修改了 `/etc/nginx/conf.d/exhibit-flow.conf`：

```bash
ssh default "nginx -t && systemctl reload nginx"
```

## 4. 数据库同步

业务表定义：表名以 `eh_` 开头的表均视为业务数据表。

非业务表定义：除 `eh_*` 以外的表，例如 `sys_user`、`sys_menu`、`sys_role`、`sys_role_menu`、`sys_dict`、`sys_file` 等。

### 4.1 仅同步非业务数据

本地数据库运行在 Docker 容器 `mysql57`，数据库名 `exhibit_flow`。

导出本地非业务表：

```bash
docker exec -e MYSQL_PWD=sdbs mysql57 mysql -uroot -D exhibit_flow --batch --silent -N \
  -e "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='exhibit_flow' AND TABLE_NAME NOT LIKE 'eh\\_%' ORDER BY TABLE_NAME;" \
  > /tmp/exhibit-flow-non-business-tables.txt

cat /tmp/exhibit-flow-non-business-tables.txt | xargs \
  docker exec -e MYSQL_PWD=sdbs mysql57 mysqldump -uroot --single-transaction --routines --triggers exhibit_flow \
  > /tmp/exhibit-flow-non-business.sql
```

上传 dump：

```bash
scp /tmp/exhibit-flow-non-business.sql \
  default:/opt/wbr/exhibit-flow/sql/exhibit-flow-non-business-from-local.sql
```

服务器导入前先备份完整库：

```bash
ssh default "mkdir -p /opt/wbr/exhibit-flow/backup && \
  mysqldump -uroot -p'见生产配置' --single-transaction --routines --triggers exhibit_flow \
  > /opt/wbr/exhibit-flow/backup/exhibit_flow_before_non_business_sync_\$(date +%Y%m%d%H%M%S).sql"
```

导入非业务表：

```bash
ssh default "mysql -uroot -p'见生产配置' exhibit_flow \
  < /opt/wbr/exhibit-flow/sql/exhibit-flow-non-business-from-local.sql"
```

导入后重启后端：

```bash
ssh default "systemctl restart exhibit-flow-backend.service"
```

### 4.2 精确对比非业务表行数

本地：

```bash
LOCAL_SQL=$(awk '{printf "SELECT '\''%s'\'', COUNT(*) FROM %s UNION ALL ", $1, $1}' /tmp/exhibit-flow-non-business-tables.txt | sed 's/ UNION ALL $//')
docker exec -e MYSQL_PWD=sdbs mysql57 mysql -uroot -D exhibit_flow --batch --silent -e "$LOCAL_SQL"
```

服务器：

```bash
ssh default "mysql -uroot -p'见生产配置' -D exhibit_flow --batch --silent -e \"$LOCAL_SQL\""
```

当前已同步后的关键非业务表计数：

| 表 | 行数 |
| --- | ---: |
| `sys_user` | 53 |
| `sys_menu` | 159 |
| `sys_role` | 7 |
| `sys_role_menu` | 253 |
| `sys_dict` | 28 |
| `sys_dict_item` | 97 |
| `sys_file` | 6 |

## 5. 验证命令

端口检查：

```bash
ssh default "ss -tlnp | grep -E ':(1111|1112|1113)\\b'"
```

后端健康：

```bash
ssh default "curl -s -i http://127.0.0.1:1111/admin/actuator/health | head -20"
```

管理后台首页：

```bash
ssh default "curl -s -I http://127.0.0.1:1112/ | head -20"
curl -s -I http://106.12.173.222:1112/ | head -20
```

H5 首页：

```bash
ssh default "curl -s -I http://127.0.0.1:1113/ | head -20"
curl -s -I http://106.12.173.222:1113/ | head -20
```

管理后台代理后端：

```bash
ssh default "curl -s -I http://127.0.0.1:1112/admin/code/image | head -20"
```

服务状态：

```bash
ssh default "systemctl status exhibit-flow-backend.service --no-pager -n 80"
ssh default "systemctl status nginx --no-pager -n 30"
```

日志：

```bash
ssh default "journalctl -u exhibit-flow-backend.service --no-pager -n 100"
ssh default "tail -100 /opt/wbr/exhibit-flow/backend/logs/service.log"
ssh default "tail -100 /opt/wbr/exhibit-flow/backend/logs/service-error.log"
```

## 6. 回滚

### 6.1 回滚数据库

备份目录：

```bash
/opt/wbr/exhibit-flow/backup
```

查看备份：

```bash
ssh default "ls -lh /opt/wbr/exhibit-flow/backup"
```

恢复某个备份：

```bash
ssh default "mysql -uroot -p'见生产配置' exhibit_flow \
  < /opt/wbr/exhibit-flow/backup/备份文件名.sql"
ssh default "systemctl restart exhibit-flow-backend.service"
```

### 6.2 回滚应用

当前没有自动版本目录。建议每次覆盖前手动备份：

```bash
ssh default "mkdir -p /opt/wbr/exhibit-flow/release-backup/\$(date +%Y%m%d%H%M%S) && \
  cp /opt/wbr/exhibit-flow/backend/pig-boot.jar /opt/wbr/exhibit-flow/release-backup/\$(date +%Y%m%d%H%M%S)-pig-boot.jar"
```

前端可用 `rsync` 重新上传上一版 `dist/`；后端可重新上传上一版 `pig-boot.jar` 后重启：

```bash
ssh default "systemctl restart exhibit-flow-backend.service"
```

## 7. 注意事项

- 不使用 `9999`，服务器该端口已被其他 nginx 配置占用。
- 后端必须用 Java 21 运行；当前通过 systemd 固定 Java 21 绝对路径启动，不依赖 Docker。
- 不要覆盖 `eh_*` 表，除非明确要迁移业务数据。
- 修改 nginx 配置后必须先执行 `nginx -t`，通过后再 `systemctl reload nginx`。
- 修改菜单、角色、用户、字典等非业务数据后，建议重启后端以清理缓存。
- 当前部署不会停止现有 exam 系统、OpenClaw、MySQL、Redis。
