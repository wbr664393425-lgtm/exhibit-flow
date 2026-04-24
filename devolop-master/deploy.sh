#!/bin/bash

# Pig4Cloud 一键部署脚本
# 使用方法: ./deploy.sh [start|stop|restart|logs|status]

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker 和 Docker Compose
check_prerequisites() {
    print_info "检查环境依赖..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    print_info "环境检查通过 ✓"
}

# 检查并创建 .env 文件
check_env_file() {
    if [ ! -f .env ]; then
        print_warn ".env 文件不存在，从 .env.example 创建..."
        cp .env.example .env
        print_warn "请编辑 .env 文件配置数据库和Redis密码后再次运行部署"
        exit 0
    fi
}

# 创建必要的目录
create_directories() {
    print_info "创建必要的目录..."
    mkdir -p db/init
    mkdir -p logs
    print_info "目录创建完成 ✓"
}

# 启动服务
start_services() {
    print_info "启动 Pig4Cloud 服务..."
    check_prerequisites
    check_env_file
    create_directories
    
    # 构建并启动服务
    docker-compose up -d --build
    
    print_info "服务启动中，等待健康检查..."
    sleep 5
    
    # 显示服务状态
    docker-compose ps
    
    print_info "部署完成！"
    print_info "访问地址: http://localhost:9999/admin"
    print_info "接口文档: http://localhost:9999/admin/doc.html"
    print_info "查看日志: ./deploy.sh logs"
}

# 停止服务
stop_services() {
    print_info "停止 Pig4Cloud 服务..."
    docker-compose down
    print_info "服务已停止 ✓"
}

# 重启服务
restart_services() {
    print_info "重启 Pig4Cloud 服务..."
    stop_services
    start_services
}

# 查看日志
view_logs() {
    if [ -z "$2" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f "$2"
    fi
}

# 查看服务状态
check_status() {
    print_info "服务状态:"
    docker-compose ps
    echo ""
    print_info "健康检查:"
    docker-compose exec pig-boot curl -s http://localhost:9999/admin/actuator/health || print_warn "应用尚未就绪"
}

# 清理所有数据（包括数据卷）
clean_all() {
    print_warn "警告: 此操作将删除所有数据（包括数据库）！"
    read -p "确认继续? (yes/no): " confirm
    if [ "$confirm" = "yes" ]; then
        print_info "停止并删除所有容器和数据卷..."
        docker-compose down -v
        print_info "清理完成 ✓"
    else
        print_info "操作已取消"
    fi
}

# 主逻辑
case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        view_logs "$@"
        ;;
    status)
        check_status
        ;;
    clean)
        clean_all
        ;;
    *)
        echo "使用方法: $0 {start|stop|restart|logs|status|clean}"
        echo ""
        echo "命令说明:"
        echo "  start   - 启动所有服务"
        echo "  stop    - 停止所有服务"
        echo "  restart - 重启所有服务"
        echo "  logs    - 查看日志 (可选: logs pig-boot)"
        echo "  status  - 查看服务状态"
        echo "  clean   - 清理所有数据（危险操作）"
        exit 1
        ;;
esac
