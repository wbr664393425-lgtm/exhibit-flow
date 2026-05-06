#!/usr/bin/env bash

set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${ROOT_DIR}/devolop-master"
ADMIN_DIR="${ROOT_DIR}/exhibit-flow-vue"
H5_DIR="${ROOT_DIR}/exhibit-flow-h5"
RUN_DIR="${ROOT_DIR}/.run/start-all"
LOG_DIR="${ROOT_DIR}/logs/start-all"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_info() {
  echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
  echo -e "${RED}[ERROR]${NC} $1"
}

ensure_dirs() {
  mkdir -p "${RUN_DIR}" "${LOG_DIR}"
}

check_command() {
  local command_name="$1"
  local install_tip="$2"

  if ! command -v "${command_name}" >/dev/null 2>&1; then
    print_error "缺少命令：${command_name}。${install_tip}"
    exit 1
  fi
}

check_prerequisites() {
  check_command npm "请先安装 Node.js 18+"
  check_command mvn "请先安装 Maven 3.8+"
}

install_node_modules_if_needed() {
  local app_name="$1"
  local app_dir="$2"

  if [ ! -d "${app_dir}/node_modules" ]; then
    print_info "${app_name} 安装依赖..."
    (cd "${app_dir}" && npm ci)
  fi
}

is_pid_running() {
  local pid_file="$1"

  [ -f "${pid_file}" ] && kill -0 "$(cat "${pid_file}")" >/dev/null 2>&1
}

start_frontend() {
  local app_name="$1"
  local app_dir="$2"
  local pid_file="$3"
  local log_file="$4"

  if is_pid_running "${pid_file}"; then
    print_warn "${app_name} 已在运行，PID=$(cat "${pid_file}")"
    return
  fi

  install_node_modules_if_needed "${app_name}" "${app_dir}"

  print_info "启动 ${app_name}..."
  (
    cd "${app_dir}"
    nohup npm run dev -- --host 0.0.0.0 > "${log_file}" 2>&1 &
    echo $! > "${pid_file}"
  )

  sleep 1
  if is_pid_running "${pid_file}"; then
    print_info "${app_name} 启动完成，PID=$(cat "${pid_file}")，日志：${log_file}"
  else
    print_error "${app_name} 启动失败，查看日志：${log_file}"
    exit 1
  fi
}

start_backend() {
  local pid_file="${RUN_DIR}/backend.pid"
  local log_file="${LOG_DIR}/backend.log"

  if is_pid_running "${pid_file}"; then
    print_warn "后端已在运行，PID=$(cat "${pid_file}")"
    return
  fi

  print_info "启动后端..."
  (
    cd "${BACKEND_DIR}"
    nohup mvn spring-boot:run -pl pig-boot > "${log_file}" 2>&1 &
    echo $! > "${pid_file}"
  )

  sleep 2
  if is_pid_running "${pid_file}"; then
    print_info "后端启动中，PID=$(cat "${pid_file}")，日志：${log_file}"
  else
    print_error "后端启动失败，查看日志：${log_file}"
    exit 1
  fi
}

stop_frontend() {
  local app_name="$1"
  local pid_file="$2"

  if ! [ -f "${pid_file}" ]; then
    print_warn "${app_name} 未记录 PID，跳过"
    return
  fi

  local pid
  pid="$(cat "${pid_file}")"
  if kill -0 "${pid}" >/dev/null 2>&1; then
    print_info "停止 ${app_name}，PID=${pid}"
    kill "${pid}" >/dev/null 2>&1 || true
  else
    print_warn "${app_name} 进程不存在，清理 PID 文件"
  fi

  rm -f "${pid_file}"
}

start_all() {
  ensure_dirs
  check_prerequisites

  start_backend
  start_frontend "管理后台" "${ADMIN_DIR}" "${RUN_DIR}/admin.pid" "${LOG_DIR}/admin.log"
  start_frontend "移动端 H5" "${H5_DIR}" "${RUN_DIR}/h5.pid" "${LOG_DIR}/h5.log"

  echo ""
  print_info "三端启动完成"
  echo "  后端接口：http://localhost:9999/admin"
  echo "  接口文档：http://localhost:9999/admin/doc.html"
  echo "  管理后台：http://localhost:5173"
  echo "  移动端 H5：http://localhost:3100"
  echo ""
  echo "查看日志：./start-all.sh logs"
  echo "停止服务：./start-all.sh stop"
}

stop_all() {
  ensure_dirs

  stop_frontend "管理后台" "${RUN_DIR}/admin.pid"
  stop_frontend "移动端 H5" "${RUN_DIR}/h5.pid"
  stop_frontend "后端" "${RUN_DIR}/backend.pid"
}

restart_all() {
  stop_all
  start_all
}

print_port_status() {
  local label="$1"
  local port="$2"

  if lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "  ${label}: 运行中，端口 ${port}"
  else
    echo "  ${label}: 未监听，端口 ${port}"
  fi
}

status_all() {
  echo "端口状态："
  print_port_status "后端" "9999"
  print_port_status "管理后台" "5173"
  print_port_status "移动端 H5" "3100"

  echo ""
  echo "本地进程："
  if is_pid_running "${RUN_DIR}/backend.pid"; then
    echo "  后端 PID=$(cat "${RUN_DIR}/backend.pid")"
  else
    echo "  后端未运行"
  fi

  if is_pid_running "${RUN_DIR}/admin.pid"; then
    echo "  管理后台 PID=$(cat "${RUN_DIR}/admin.pid")"
  else
    echo "  管理后台未运行"
  fi

  if is_pid_running "${RUN_DIR}/h5.pid"; then
    echo "  移动端 H5 PID=$(cat "${RUN_DIR}/h5.pid")"
  else
    echo "  移动端 H5 未运行"
  fi
}

tail_logs() {
  ensure_dirs
  touch "${LOG_DIR}/backend.log" "${LOG_DIR}/admin.log" "${LOG_DIR}/h5.log"

  echo "日志文件："
  echo "  后端：${LOG_DIR}/backend.log"
  echo "  管理后台：${LOG_DIR}/admin.log"
  echo "  移动端 H5：${LOG_DIR}/h5.log"
  echo ""

  tail -f "${LOG_DIR}/backend.log" "${LOG_DIR}/admin.log" "${LOG_DIR}/h5.log"
}

case "${1:-start}" in
  start)
    start_all
    ;;
  stop)
    stop_all
    ;;
  restart)
    restart_all
    ;;
  status)
    status_all
    ;;
  logs)
    tail_logs
    ;;
  *)
    echo "使用方法：$0 {start|stop|restart|status|logs}"
    echo ""
    echo "命令说明："
    echo "  start   启动后端、管理后台、移动端 H5"
    echo "  stop    停止三端服务"
    echo "  restart 重启三端服务"
    echo "  status  查看端口和本地进程状态"
    echo "  logs    查看前端实时日志"
    exit 1
    ;;
esac
