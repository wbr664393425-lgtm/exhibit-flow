#!/bin/bash
# Exhibit Flow 接口自动化测试脚本
# 用法: bash test_runner.sh
# 依赖: curl, jq, docker (mysql57容器运行中)

set -uo pipefail

BASE="http://localhost:9999/admin"
DB_PWD="sdbs"
DB_USER="root"
DB_NAME="exhibit_flow"
PASS=0; FAIL=0; SKIP=0

# ── 颜色 ──────────────────────────────────────────────
GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

pass() { echo -e "  ${GREEN}✓ PASS${RESET}  $1"; PASS=$((PASS+1)); }
fail() { echo -e "  ${RED}✗ FAIL${RESET}  $1"; FAIL=$((FAIL+1)); }
skip() { echo -e "  ${YELLOW}- SKIP${RESET}  $1"; SKIP=$((SKIP+1)); }
info() { echo -e "  ${CYAN}→${RESET} $1"; }
section() { echo -e "\n${BOLD}${CYAN}═══ $1 ═══${RESET}"; }

# ── 工具函数 ──────────────────────────────────────────
db_query() {
  docker exec -e MYSQL_PWD="$DB_PWD" mysql57 mysql -u "$DB_USER" -D "$DB_NAME" --batch --silent -e "$1" 2>/dev/null
}

http_post() {
  local url="$1"; local body="$2"
  curl -s -X POST "${BASE}${url}" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$body"
}

http_put() {
  local url="$1"; local body="${2:-{}}"
  curl -s -X PUT "${BASE}${url}" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$body"
}

http_get() {
  local url="$1"
  curl -s -X GET "${BASE}${url}" \
    -H "Authorization: Bearer ${TOKEN}"
}

http_delete() {
  local url="$1"; local body="$2"
  curl -s -X DELETE "${BASE}${url}" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$body"
}

assert_code() {
  local resp="$1"; local expected_code="${2:-0}"; local label="$3"
  local code; code=$(echo "$resp" | jq -r '.code // "null"' 2>/dev/null)
  if [[ "$code" == "$expected_code" ]]; then
    pass "$label"
  else
    fail "$label (code=$code, resp=$(echo "$resp" | head -c 200))"
  fi
}

assert_data_true() {
  local resp="$1"; local label="$2"
  local data; data=$(echo "$resp" | jq -r '.data // false' 2>/dev/null)
  if [[ "$data" == "true" ]]; then
    pass "$label"
  else
    fail "$label (data=$data)"
  fi
}

assert_data_null() {
  local resp="$1"; local label="$2"
  local data; data=$(echo "$resp" | jq -r '.data' 2>/dev/null)
  if [[ "$data" == "null" ]]; then
    pass "$label"
  else
    fail "$label (data 非 null: $data)"
  fi
}

assert_data_notnull() {
  local resp="$1"; local label="$2"
  local data; data=$(echo "$resp" | jq '.data' 2>/dev/null)
  if [[ "$data" != "null" && -n "$data" ]]; then
    pass "$label"
  else
    fail "$label (data 为空)"
  fi
}

assert_db() {
  local sql="$1"; local expected="$2"; local label="$3"
  local result; result=$(db_query "$sql")
  if [[ "$result" == "$expected" ]]; then
    pass "DB: $label"
  else
    fail "DB: $label (期望='$expected', 实际='$result')"
  fi
}

# ── 清理测试数据 ───────────────────────────────────────
cleanup() {
  db_query "DELETE FROM eh_change_log WHERE apply_id IN (SELECT id FROM eh_apply WHERE subject LIKE 'TC%');" 2>/dev/null || true
  db_query "DELETE FROM eh_notification WHERE apply_id IN (SELECT id FROM eh_apply WHERE subject LIKE 'TC%');" 2>/dev/null || true
  db_query "DELETE FROM eh_approval_node WHERE apply_id IN (SELECT id FROM eh_apply WHERE subject LIKE 'TC%');" 2>/dev/null || true
  db_query "DELETE FROM eh_apply_visitor WHERE apply_id IN (SELECT id FROM eh_apply WHERE subject LIKE 'TC%');" 2>/dev/null || true
  db_query "DELETE FROM eh_visit_photo WHERE visit_id IN (SELECT vr.id FROM eh_visit_record vr JOIN eh_apply a ON a.id=vr.apply_id WHERE a.subject LIKE 'TC%');" 2>/dev/null || true
  db_query "DELETE FROM eh_visit_record WHERE apply_id IN (SELECT id FROM eh_apply WHERE subject LIKE 'TC%');" 2>/dev/null || true
  db_query "DELETE FROM eh_apply WHERE subject LIKE 'TC%';" 2>/dev/null || true
}

# ══════════════════════════════════════════════════════
section "初始化：清理遗留数据 & 获取 Token"
# ══════════════════════════════════════════════════════
cleanup
info "清理 TC% 测试数据完成"

ENC_PWD=$(node -e "
const c=require('crypto');
const k=Buffer.from('thanks,pig4cloud');
const ci=c.createCipheriv('aes-128-cfb',k,k);
let e=ci.update('admin888','utf8');
console.log(Buffer.concat([e,ci.final()]).toString('base64'));
" 2>/dev/null)

TOKEN_RESP=$(curl -s -X POST "${BASE}/oauth2/token" \
  -H "Authorization: Basic cGlnOnBpZw==" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "grant_type=password" \
  --data-urlencode "username=admin" \
  --data-urlencode "password=${ENC_PWD}" \
  --data-urlencode "scope=server")

TOKEN=$(echo "$TOKEN_RESP" | jq -r '.access_token // ""' 2>/dev/null)
if [[ -z "$TOKEN" || "$TOKEN" == "null" ]]; then
  echo -e "${RED}Token 获取失败，终止测试${RESET}"
  echo "$TOKEN_RESP" | head -c 300
  exit 1
fi
pass "Token 获取成功 (${TOKEN:0:20}...)"

# ══════════════════════════════════════════════════════
section "模块一：展厅申请"
# ══════════════════════════════════════════════════════

# ── TC-001 保存草稿 ────────────────────────────────────
echo -e "\n${BOLD}TC-001 保存申请草稿${RESET}"
DRAFT_BODY='{
  "title":"TC001-草稿申请","startDate":"2026-12-01","startHour":"9","endHour":"11",
  "unit":"TC测试单位A","industry":"互联网","headCount":5,"leader":"副总裁",
  "applicant":"测试员张三","dept":"研发部","phone":"13800000001","district":"西湖区",
  "agenda":"产品演示","services":["茶水"],
  "visitors":[{"name":"李四","title":"总监","unit":"TC测试单位A","isStrategic":false,"strategicLevel":""}]
}'
R=$(http_post "/eh/apply/draft" "$DRAFT_BODY")
assert_data_true "$R" "TC-001 响应 data=true"

DRAFT_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC001-草稿申请' ORDER BY id DESC LIMIT 1")
if [[ -n "$DRAFT_ID" ]]; then
  assert_db "SELECT status FROM eh_apply WHERE id=$DRAFT_ID" "0" "草稿 status=0"
  assert_db "SELECT COUNT(*) FROM eh_approval_node WHERE apply_id=$DRAFT_ID" "0" "草稿无审批节点"
  assert_db "SELECT COUNT(*) FROM eh_apply_visitor WHERE apply_id=$DRAFT_ID" "1" "来访客户有1条"
else
  fail "DB: 草稿申请未写入"
fi

# ── TC-002 直接提交（2节点）──────────────────────────────
echo -e "\n${BOLD}TC-002 直接提交申请（副总裁→2节点）${RESET}"
SUBMIT_BODY='{
  "title":"TC002-直接提交申请","startDate":"2026-12-02","startHour":"9","endHour":"11",
  "unit":"TC测试单位B","industry":"医疗","headCount":8,"leader":"副总裁",
  "applicant":"测试员李四","dept":"销售部","phone":"13800000002","district":"滨江区",
  "agenda":"合作洽谈","services":[],
  "visitors":[{"name":"王五","title":"CEO","unit":"TC测试单位B","isStrategic":false,"strategicLevel":""}]
}'
R=$(http_post "/eh/apply/submit" "$SUBMIT_BODY")
assert_data_true "$R" "TC-002 响应 data=true"

APPLY2_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC002-直接提交申请' ORDER BY id DESC LIMIT 1")
if [[ -n "$APPLY2_ID" ]]; then
  assert_db "SELECT status FROM eh_apply WHERE id=$APPLY2_ID" "1" "TC-002 status=1(待审批)"
  assert_db "SELECT COUNT(*) FROM eh_approval_node WHERE apply_id=$APPLY2_ID" "2" "TC-002 审批节点=2"
  assert_db "SELECT status FROM eh_approval_node WHERE apply_id=$APPLY2_ID AND node_level=1" "pending" "TC-002 节点1 status=pending"
  assert_db "SELECT status FROM eh_approval_node WHERE apply_id=$APPLY2_ID AND node_level=2" "waiting" "TC-002 节点2 status=waiting"
else
  fail "DB: TC-002 申请未写入"
fi

# ── TC-003 三节点提交（含总经理）───────────────────────────
echo -e "\n${BOLD}TC-003 提交申请（总经理→3节点）${RESET}"
SUBMIT3_BODY='{
  "title":"TC003-三节点申请","startDate":"2026-12-03","startHour":"14","endHour":"16",
  "unit":"TC测试单位C","industry":"党政","headCount":20,"leader":"总经理",
  "applicant":"测试员王五","dept":"市场部","phone":"13800000003","district":"上城区",
  "agenda":"重要接待","services":[],
  "visitors":[{"name":"赵六","title":"书记","unit":"TC测试单位C","isStrategic":true,"strategicLevel":"S"}]
}'
R=$(http_post "/eh/apply/submit" "$SUBMIT3_BODY")
assert_data_true "$R" "TC-003 响应 data=true"

APPLY3_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC003-三节点申请' ORDER BY id DESC LIMIT 1")
if [[ -n "$APPLY3_ID" ]]; then
  assert_db "SELECT COUNT(*) FROM eh_approval_node WHERE apply_id=$APPLY3_ID" "3" "TC-003 审批节点=3（含总经理）"
  assert_db "SELECT approver FROM eh_approval_node WHERE apply_id=$APPLY3_ID AND node_level=3" "总经理" "TC-003 节点3审批人=总经理"
else
  fail "DB: TC-003 申请未写入"
fi

# ── TC-004 草稿转提交 ─────────────────────────────────────
echo -e "\n${BOLD}TC-004 草稿转提交${RESET}"
if [[ -n "$DRAFT_ID" ]]; then
  R=$(http_put "/eh/apply/${DRAFT_ID}/submit" "$DRAFT_BODY")
  assert_data_true "$R" "TC-004 响应 data=true"
  assert_db "SELECT status FROM eh_apply WHERE id=$DRAFT_ID" "1" "TC-004 status 由0变1"
  assert_db "SELECT COUNT(*) FROM eh_approval_node WHERE apply_id=$DRAFT_ID" "2" "TC-004 审批节点已创建"
else
  skip "TC-004 草稿ID不存在，跳过"
fi

# ── TC-005 分页查询 ───────────────────────────────────────
echo -e "\n${BOLD}TC-005 申请分页查询${RESET}"
R=$(http_get "/eh/apply/page?current=1&size=10")
assert_code "$R" "0" "TC-005 分页查询 code=0"
TOTAL=$(echo "$R" | jq -r '.data.total // 0')
info "当前申请总数: $TOTAL"

# ── TC-006 聚合详情 ───────────────────────────────────────
echo -e "\n${BOLD}TC-006 获取申请聚合详情${RESET}"
if [[ -n "$APPLY2_ID" ]]; then
  R=$(http_get "/eh/apply/details/${APPLY2_ID}")
  assert_code "$R" "0" "TC-006 code=0"
  assert_data_notnull "$R" "TC-006 data 非空"
  VISITOR_COUNT=$(echo "$R" | jq '.data.visitors | length' 2>/dev/null)
  NODE_COUNT=$(echo "$R" | jq '.data.approvalNodes | length' 2>/dev/null)
  [[ "${VISITOR_COUNT:-0}" -ge 1 ]] && pass "TC-006 visitors 有数据" || fail "TC-006 visitors 为空"
  [[ "${NODE_COUNT:-0}" -ge 2 ]] && pass "TC-006 approvalNodes 有2个节点" || fail "TC-006 approvalNodes 节点数($NODE_COUNT)不足"
else
  skip "TC-006 apply_id 不存在"
fi

# ── TC-007 详情不存在 ─────────────────────────────────────
echo -e "\n${BOLD}TC-007 申请详情-ID不存在${RESET}"
R=$(http_get "/eh/apply/details/999999999")
assert_code "$R" "0" "TC-007 code=0"
assert_data_null "$R" "TC-007 data=null"

# ── TC-008 聚合列表（按status过滤）─────────────────────────
echo -e "\n${BOLD}TC-008 管理端聚合列表（status=1）${RESET}"
R=$(http_get "/eh/apply/page/aggregate?status=1")
assert_code "$R" "0" "TC-008 code=0"

# ── TC-009 时段冲突检测（无冲突）─────────────────────────────
echo -e "\n${BOLD}TC-009 时段冲突检测（无冲突）${RESET}"
R=$(http_get "/eh/apply/conflict?date=2026-11-30&startHour=9&endHour=11")
assert_code "$R" "0" "TC-009 code=0"
assert_data_null "$R" "TC-009 无冲突返回 null"

# ── TC-010 时段冲突检测（有冲突）─────────────────────────────
echo -e "\n${BOLD}TC-010 时段冲突检测（有冲突）${RESET}"
# TC-002 的申请 2026-12-02 09:00-11:00，status=1，应被检测到
R=$(http_get "/eh/apply/conflict?date=2026-12-02&startHour=10&endHour=12")
assert_code "$R" "0" "TC-010 code=0"
DATA=$(echo "$R" | jq -r '.data' 2>/dev/null)
if [[ -n "$DATA" && "$DATA" != "null" ]]; then
  pass "TC-010 返回冲突描述: $DATA"
else
  fail "TC-010 期望返回冲突描述，实际 data=$DATA"
fi

# ── TC-011 相邻时段不冲突 ─────────────────────────────────
echo -e "\n${BOLD}TC-011 时段冲突检测（相邻不冲突）${RESET}"
R=$(http_get "/eh/apply/conflict?date=2026-12-02&startHour=11&endHour=13")
assert_data_null "$R" "TC-011 相邻时段 data=null（不冲突）"

# ── TC-013 排期日历 ───────────────────────────────────────
echo -e "\n${BOLD}TC-013 排期日历查询${RESET}"
R=$(http_get "/eh/apply/calendar?start=2026-12-01&end=2026-12-31")
assert_code "$R" "0" "TC-013 code=0"
CAL_COUNT=$(echo "$R" | jq '.data | length' 2>/dev/null)
info "日历事件数: ${CAL_COUNT:-0}"

# ── TC-014 取消申请 ───────────────────────────────────────
echo -e "\n${BOLD}TC-014 取消申请${RESET}"
# 单独创建一个申请用于取消
CANCEL_BODY='{
  "title":"TC014-取消申请","startDate":"2026-12-04","startHour":"9","endHour":"11",
  "unit":"TC测试单位D","industry":"教育","headCount":3,"leader":"副总裁",
  "applicant":"测试员","dept":"测试部","phone":"13800000004","district":"萧山区",
  "agenda":"测试取消","services":[],"visitors":[]
}'
http_post "/eh/apply/submit" "$CANCEL_BODY" > /dev/null
CANCEL_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC014-取消申请' ORDER BY id DESC LIMIT 1")
if [[ -n "$CANCEL_ID" ]]; then
  R=$(http_put "/eh/apply/${CANCEL_ID}/cancel" '{"reason":"访客临时有事，取消本次参观"}')
  assert_data_true "$R" "TC-014 取消响应 data=true"
  assert_db "SELECT status FROM eh_apply WHERE id=$CANCEL_ID" "4" "TC-014 status=4(已取消)"
  CHANGE_COUNT=$(db_query "SELECT COUNT(*) FROM eh_change_log WHERE apply_id=$CANCEL_ID AND change_type='cancel'")
  [[ "$CHANGE_COUNT" -ge 1 ]] && pass "DB: TC-014 change_log 有记录" || fail "DB: TC-014 change_log 无记录"
  NOTIFY_COUNT=$(db_query "SELECT COUNT(*) FROM eh_notification WHERE apply_id=$CANCEL_ID AND title='申请已取消'")
  [[ "$NOTIFY_COUNT" -ge 1 ]] && pass "DB: TC-014 通知已发送" || fail "DB: TC-014 通知未发送"
else
  fail "TC-014 取消申请未创建"
fi

# ── TC-015 改期申请 ───────────────────────────────────────
echo -e "\n${BOLD}TC-015 改期申请${RESET}"
RESCHED_BODY='{
  "title":"TC015-改期申请","startDate":"2026-12-05","startHour":"9","endHour":"11",
  "unit":"TC测试单位E","industry":"交通","headCount":4,"leader":"副总裁",
  "applicant":"测试员","dept":"测试部","phone":"13800000005","district":"余杭区",
  "agenda":"测试改期","services":[],"visitors":[]
}'
http_post "/eh/apply/submit" "$RESCHED_BODY" > /dev/null
RESCHED_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC015-改期申请' ORDER BY id DESC LIMIT 1")
if [[ -n "$RESCHED_ID" ]]; then
  R=$(http_put "/eh/apply/${RESCHED_ID}/reschedule" '{"newDate":"2026-12-20","newSH":"14","newEH":"16","reason":"访客调整行程"}')
  assert_data_true "$R" "TC-015 改期响应 data=true"
  assert_db "SELECT status FROM eh_apply WHERE id=$RESCHED_ID" "5" "TC-015 status=5(已改期)"
  NEW_START=$(db_query "SELECT DATE_FORMAT(start_time,'%Y-%m-%d %H:%i') FROM eh_apply WHERE id=$RESCHED_ID")
  [[ "$NEW_START" == "2026-12-20 14:00" ]] && pass "DB: TC-015 start_time 更新为 2026-12-20 14:00" || fail "DB: TC-015 start_time=$NEW_START"
  CL_COUNT=$(db_query "SELECT COUNT(*) FROM eh_change_log WHERE apply_id=$RESCHED_ID AND change_type='reschedule'")
  [[ "$CL_COUNT" -ge 1 ]] && pass "DB: TC-015 change_log 改期记录存在" || fail "DB: TC-015 change_log 无改期记录"
else
  fail "TC-015 改期申请未创建"
fi

# ── TC-016 取消不存在的申请 ───────────────────────────────
echo -e "\n${BOLD}TC-016 取消不存在的申请${RESET}"
R=$(http_put "/eh/apply/999999999/cancel" '{"reason":"测试"}')
DATA=$(echo "$R" | jq '.data' 2>/dev/null)
[[ "$DATA" == "false" ]] && pass "TC-016 返回 false" || fail "TC-016 期望 false，实际=$DATA"

# ── TC-017 删除申请 ───────────────────────────────────────
echo -e "\n${BOLD}TC-017 删除申请（草稿）${RESET}"
DEL_BODY='{"title":"TC017-删除申请","startDate":"2026-12-06","startHour":"9","endHour":"10","unit":"TC测试单位F","industry":"其他","headCount":1,"leader":"","applicant":"测试员","dept":"测试部","phone":"13800000006","district":"临安区","agenda":"","services":[],"visitors":[]}'
http_post "/eh/apply/draft" "$DEL_BODY" > /dev/null
DEL_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC017-删除申请' ORDER BY id DESC LIMIT 1")
if [[ -n "$DEL_ID" ]]; then
  R=$(http_delete "/eh/apply" "[${DEL_ID}]")
  assert_data_true "$R" "TC-017 删除响应 data=true"
  assert_db "SELECT del_flag FROM eh_apply WHERE id=$DEL_ID" "1" "TC-017 del_flag=1(逻辑删除)"
else
  fail "TC-017 待删除申请未创建"
fi

# ══════════════════════════════════════════════════════
section "模块二：审批流"
# ══════════════════════════════════════════════════════

# 获取TC-002的审批节点
if [[ -z "$APPLY2_ID" ]]; then
  fail "模块二：apply_id 不存在，跳过审批流测试"
else
  NODE1_ID=$(db_query "SELECT id FROM eh_approval_node WHERE apply_id=$APPLY2_ID AND node_level=1")
  NODE2_ID=$(db_query "SELECT id FROM eh_approval_node WHERE apply_id=$APPLY2_ID AND node_level=2")

  # ── TC-101 一级审批通过 ───────────────────────────────
  echo -e "\n${BOLD}TC-101 一级审批通过${RESET}"
  R=$(http_post "/eh/apply/approval/action" "{\"applyId\":${APPLY2_ID},\"nodeId\":${NODE1_ID},\"action\":\"approve\",\"comment\":\"同意，请展厅主管审批\"}")
  assert_code "$R" "0" "TC-101 code=0"
  DATA=$(echo "$R" | jq -r '.data // false')
  [[ "$DATA" == "true" ]] && pass "TC-101 data=true" || fail "TC-101 data=$DATA"
  assert_db "SELECT status FROM eh_approval_node WHERE id=$NODE1_ID" "approved" "TC-101 节点1 status=approved"
  assert_db "SELECT status FROM eh_approval_node WHERE id=$NODE2_ID" "pending" "TC-101 节点2 已激活(pending)"
  assert_db "SELECT status FROM eh_apply WHERE id=$APPLY2_ID" "1" "TC-101 申请仍为审批中(status=1)"
  N_COUNT=$(db_query "SELECT COUNT(*) FROM eh_notification WHERE apply_id=$APPLY2_ID AND title='申请进入下一审批环节'")
  [[ "$N_COUNT" -ge 1 ]] && pass "DB: TC-101 通知已发" || fail "DB: TC-101 通知未发"

  # ── TC-102 二级审批通过（最终通过）────────────────────
  echo -e "\n${BOLD}TC-102 二级审批通过（最终通过）${RESET}"
  R=$(http_post "/eh/apply/approval/action" "{\"applyId\":${APPLY2_ID},\"nodeId\":${NODE2_ID},\"action\":\"approve\",\"comment\":\"同意接待\"}")
  assert_code "$R" "0" "TC-102 code=0"
  assert_db "SELECT status FROM eh_approval_node WHERE id=$NODE2_ID" "approved" "TC-102 节点2 status=approved"
  assert_db "SELECT status FROM eh_apply WHERE id=$APPLY2_ID" "2" "TC-102 申请最终通过(status=2)"
  N2_COUNT=$(db_query "SELECT COUNT(*) FROM eh_notification WHERE apply_id=$APPLY2_ID AND title='申请已通过'")
  [[ "$N2_COUNT" -ge 1 ]] && pass "DB: TC-102 通知'申请已通过'已发" || fail "DB: TC-102 通知未发"

  # ── TC-107 重复审批已处理节点 ─────────────────────────
  echo -e "\n${BOLD}TC-107 重复审批已处理节点${RESET}"
  R=$(http_post "/eh/apply/approval/action" "{\"applyId\":${APPLY2_ID},\"nodeId\":${NODE1_ID},\"action\":\"approve\",\"comment\":\"重复审批\"}")
  CODE=$(echo "$R" | jq -r '.code // 0')
  [[ "$CODE" != "0" ]] && pass "TC-107 重复审批返回错误(code=$CODE)" || fail "TC-107 期望报错，实际 code=0"
fi

# ── TC-103 审批驳回 ───────────────────────────────────────
echo -e "\n${BOLD}TC-103 审批驳回${RESET}"
REJECT_BODY='{
  "title":"TC103-驳回申请","startDate":"2026-12-07","startHour":"9","endHour":"11",
  "unit":"TC测试单位G","industry":"水利","headCount":6,"leader":"副总裁",
  "applicant":"测试员","dept":"测试部","phone":"13800000007","district":"富阳区",
  "agenda":"测试驳回","services":[],"visitors":[]
}'
http_post "/eh/apply/submit" "$REJECT_BODY" > /dev/null
REJ_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC103-驳回申请' ORDER BY id DESC LIMIT 1")
REJ_NODE1=$(db_query "SELECT id FROM eh_approval_node WHERE apply_id=$REJ_ID AND node_level=1")
if [[ -n "$REJ_ID" && -n "$REJ_NODE1" ]]; then
  R=$(http_post "/eh/apply/approval/action" "{\"applyId\":${REJ_ID},\"nodeId\":${REJ_NODE1},\"action\":\"reject\",\"comment\":\"时段不合适\"}")
  assert_code "$R" "0" "TC-103 驳回 code=0"
  assert_db "SELECT status FROM eh_approval_node WHERE id=$REJ_NODE1" "rejected" "TC-103 节点1 status=rejected"
  assert_db "SELECT status FROM eh_apply WHERE id=$REJ_ID" "3" "TC-103 申请 status=3(已驳回)"
  RN=$(db_query "SELECT COUNT(*) FROM eh_notification WHERE apply_id=$REJ_ID AND title='申请已驳回'")
  [[ "$RN" -ge 1 ]] && pass "DB: TC-103 驳回通知已发" || fail "DB: TC-103 驳回通知未发"
else
  fail "TC-103 驳回申请创建失败"
fi

# ── TC-104 审批转交 ───────────────────────────────────────
echo -e "\n${BOLD}TC-104 审批转交${RESET}"
FWD_BODY='{
  "title":"TC104-转交申请","startDate":"2026-12-08","startHour":"14","endHour":"16",
  "unit":"TC测试单位H","industry":"金融","headCount":10,"leader":"副总裁",
  "applicant":"测试员","dept":"测试部","phone":"13800000008","district":"建德市",
  "agenda":"测试转交","services":[],"visitors":[]
}'
http_post "/eh/apply/submit" "$FWD_BODY" > /dev/null
FWD_ID=$(db_query "SELECT id FROM eh_apply WHERE subject='TC104-转交申请' ORDER BY id DESC LIMIT 1")
FWD_NODE1=$(db_query "SELECT id FROM eh_approval_node WHERE apply_id=$FWD_ID AND node_level=1")
if [[ -n "$FWD_ID" && -n "$FWD_NODE1" ]]; then
  R=$(http_post "/eh/apply/approval/action" "{\"applyId\":${FWD_ID},\"nodeId\":${FWD_NODE1},\"action\":\"forward\",\"comment\":\"本人出差，转交副部长\",\"targetApprover\":\"副部长陈某\"}")
  assert_code "$R" "0" "TC-104 转交 code=0"
  assert_db "SELECT status FROM eh_approval_node WHERE id=$FWD_NODE1" "transferred" "TC-104 节点1 status=transferred"
  assert_db "SELECT status FROM eh_apply WHERE id=$FWD_ID" "1" "TC-104 申请仍审批中"
  FWD_N2_APPROVER=$(db_query "SELECT approver FROM eh_approval_node WHERE apply_id=$FWD_ID AND node_level=2")
  [[ "$FWD_N2_APPROVER" == "副部长陈某" ]] && pass "DB: TC-104 节点2审批人已更换为副部长陈某" || fail "DB: TC-104 节点2审批人=$FWD_N2_APPROVER"
else
  fail "TC-104 转交申请创建失败"
fi

# ── TC-105 转交缺少 targetApprover ────────────────────────
echo -e "\n${BOLD}TC-105 转交缺少 targetApprover${RESET}"
if [[ -n "$FWD_ID" ]]; then
  R=$(http_post "/eh/apply/approval/action" "{\"applyId\":${FWD_ID},\"action\":\"forward\",\"comment\":\"转交\"}")
  CODE=$(echo "$R" | jq -r '.code // 0')
  [[ "$CODE" != "0" ]] && pass "TC-105 缺少targetApprover 返回错误(code=$CODE)" || fail "TC-105 期望报错，实际 code=0"
else
  skip "TC-105 apply_id 不存在"
fi

# ── TC-106 非法 action ────────────────────────────────────
echo -e "\n${BOLD}TC-106 非法 action 值${RESET}"
if [[ -n "$APPLY2_ID" ]]; then
  R=$(http_post "/eh/apply/approval/action" "{\"applyId\":${APPLY2_ID},\"action\":\"delete\",\"comment\":\"非法\"}")
  CODE=$(echo "$R" | jq -r '.code // 0')
  [[ "$CODE" != "0" ]] && pass "TC-106 非法action 返回错误(code=$CODE)" || fail "TC-106 期望报错，实际 code=0"
else
  skip "TC-106 apply_id 不存在"
fi

# ── TC-108 待审批聚合列表 ──────────────────────────────────
echo -e "\n${BOLD}TC-108 待审批聚合列表${RESET}"
R=$(http_get "/eh/apply/approval/todo")
assert_code "$R" "0" "TC-108 code=0"

# ── TC-109 三节点全流程通过 ───────────────────────────────
echo -e "\n${BOLD}TC-109 三节点全流程审批通过${RESET}"
if [[ -n "$APPLY3_ID" ]]; then
  N3_1=$(db_query "SELECT id FROM eh_approval_node WHERE apply_id=$APPLY3_ID AND node_level=1")
  N3_2=$(db_query "SELECT id FROM eh_approval_node WHERE apply_id=$APPLY3_ID AND node_level=2")
  N3_3=$(db_query "SELECT id FROM eh_approval_node WHERE apply_id=$APPLY3_ID AND node_level=3")
  # 节点1 通过
  http_post "/eh/apply/approval/action" "{\"applyId\":${APPLY3_ID},\"nodeId\":${N3_1},\"action\":\"approve\",\"comment\":\"同意\"}" > /dev/null
  # 节点2 通过
  http_post "/eh/apply/approval/action" "{\"applyId\":${APPLY3_ID},\"nodeId\":${N3_2},\"action\":\"approve\",\"comment\":\"同意\"}" > /dev/null
  # 节点3 通过
  http_post "/eh/apply/approval/action" "{\"applyId\":${APPLY3_ID},\"nodeId\":${N3_3},\"action\":\"approve\",\"comment\":\"同意\"}" > /dev/null
  assert_db "SELECT status FROM eh_apply WHERE id=$APPLY3_ID" "2" "TC-109 三节点全通过后 status=2"
  ALL_APPROVED=$(db_query "SELECT COUNT(*) FROM eh_approval_node WHERE apply_id=$APPLY3_ID AND status='approved'")
  [[ "$ALL_APPROVED" == "3" ]] && pass "DB: TC-109 三个节点均为 approved" || fail "DB: TC-109 通过节点数=$ALL_APPROVED"
else
  skip "TC-109 三节点申请ID不存在"
fi

# ══════════════════════════════════════════════════════
section "模块三：参观留存"
# ══════════════════════════════════════════════════════

# ── TC-201 录入参观留存 ───────────────────────────────────
echo -e "\n${BOLD}TC-201 录入参观留存${RESET}"
if [[ -n "$APPLY2_ID" ]]; then
  R=$(http_post "/eh/visit/upsert" "{\"applyId\":${APPLY2_ID},\"actualVisitTime\":\"2026-12-02 09:30:00\",\"actualVisitCount\":7,\"opportunityCode\":\"OPP-TC-001\",\"ourLeaderLevel\":\"副总裁\",\"remark\":\"参观顺利\"}")
  assert_code "$R" "0" "TC-201 code=0"
  assert_data_true "$R" "TC-201 data=true"
  VISIT_ID=$(db_query "SELECT id FROM eh_visit_record WHERE apply_id=$APPLY2_ID ORDER BY id DESC LIMIT 1")
  if [[ -n "$VISIT_ID" ]]; then
    assert_db "SELECT status FROM eh_visit_record WHERE id=$VISIT_ID" "1" "TC-201 visit_record status=1(已录入)"
    assert_db "SELECT opportunity_code FROM eh_visit_record WHERE id=$VISIT_ID" "OPP-TC-001" "TC-201 opportunity_code 正确"
    assert_db "SELECT actual_count FROM eh_apply WHERE id=$APPLY2_ID" "7" "TC-201 apply.actual_count=7"
  else
    fail "DB: TC-201 visit_record 未写入"
  fi

  # ── TC-202 幂等更新 ─────────────────────────────────────
  echo -e "\n${BOLD}TC-202 更新参观留存（幂等）${RESET}"
  if [[ -n "$VISIT_ID" ]]; then
    R=$(http_post "/eh/visit/upsert" "{\"id\":${VISIT_ID},\"applyId\":${APPLY2_ID},\"actualVisitTime\":\"2026-12-02 09:30:00\",\"actualVisitCount\":9,\"opportunityCode\":\"OPP-TC-001\",\"ourLeaderLevel\":\"副总裁\"}")
    assert_data_true "$R" "TC-202 更新 data=true"
    assert_db "SELECT actual_visit_count FROM eh_visit_record WHERE id=$VISIT_ID" "9" "TC-202 人数更新为9"
    assert_db "SELECT actual_count FROM eh_apply WHERE id=$APPLY2_ID" "9" "TC-202 apply.actual_count=9"
  fi

  # ── TC-203 归还签收 ─────────────────────────────────────
  echo -e "\n${BOLD}TC-203 归还签收${RESET}"
  if [[ -n "$VISIT_ID" ]]; then
    R=$(http_post "/eh/visit/return-sign" "{\"visitRecordId\":${VISIT_ID},\"returnSigner\":\"李主管\",\"returnTime\":\"2026-12-02 12:00:00\"}")
    assert_code "$R" "0" "TC-203 code=0"
    assert_data_true "$R" "TC-203 data=true"
    assert_db "SELECT status FROM eh_visit_record WHERE id=$VISIT_ID" "2" "TC-203 visit_record status=2(已归还)"
    assert_db "SELECT return_signer FROM eh_apply WHERE id=$APPLY2_ID" "李主管" "TC-203 apply.return_signer=李主管"
    RN=$(db_query "SELECT COUNT(*) FROM eh_notification WHERE apply_id=$APPLY2_ID AND title='归还签收已完成'")
    [[ "$RN" -ge 1 ]] && pass "DB: TC-203 归还通知已发" || fail "DB: TC-203 归还通知未发"
  fi
else
  skip "TC-201/202/203 apply_id 不存在"
  VISIT_ID=""
fi

# ── TC-204 归还签收ID不存在 ───────────────────────────────
echo -e "\n${BOLD}TC-204 归还签收（ID不存在）${RESET}"
R=$(http_post "/eh/visit/return-sign" '{"visitRecordId":999999999,"returnSigner":"测试","returnTime":"2026-12-02 12:00:00"}')
DATA=$(echo "$R" | jq '.data' 2>/dev/null)
[[ "$DATA" == "false" ]] && pass "TC-204 返回 false" || fail "TC-204 期望 false，实际=$DATA"

# ══════════════════════════════════════════════════════
section "模块四：现场照片"
# ══════════════════════════════════════════════════════

echo -e "\n${BOLD}TC-301 批量保存现场照片${RESET}"
if [[ -n "${VISIT_ID:-}" && "$VISIT_ID" != "null" ]]; then
  R=$(http_post "/eh/visit/photo/batch" "{\"visitId\":${VISIT_ID},\"photos\":[{\"fileUrl\":\"http://oss.example.com/p1.jpg\",\"fileName\":\"合影.jpg\",\"fileSize\":1024000},{\"fileUrl\":\"http://oss.example.com/p2.jpg\",\"fileName\":\"演示.jpg\",\"fileSize\":2048000}]}")
  assert_data_true "$R" "TC-301 批量照片 data=true"
  assert_db "SELECT COUNT(*) FROM eh_visit_photo WHERE visit_id=$VISIT_ID" "2" "TC-301 照片表有2条记录"

  echo -e "\n${BOLD}TC-302 photos 为空列表${RESET}"
  R=$(http_post "/eh/visit/photo/batch" "{\"visitId\":${VISIT_ID},\"photos\":[]}")
  assert_data_true "$R" "TC-302 空列表返回 true（幂等）"
  assert_db "SELECT COUNT(*) FROM eh_visit_photo WHERE visit_id=$VISIT_ID" "2" "TC-302 照片数量不变"

  echo -e "\n${BOLD}TC-303 fileUrl 为空条目被跳过${RESET}"
  R=$(http_post "/eh/visit/photo/batch" "{\"visitId\":${VISIT_ID},\"photos\":[{\"fileUrl\":\"\",\"fileName\":\"空.jpg\",\"fileSize\":100},{\"fileUrl\":\"http://oss.example.com/valid.jpg\",\"fileName\":\"有效.jpg\",\"fileSize\":500000}]}")
  assert_data_true "$R" "TC-303 data=true"
  assert_db "SELECT COUNT(*) FROM eh_visit_photo WHERE visit_id=$VISIT_ID" "3" "TC-303 只插入1条有效照片(总计3)"

  echo -e "\n${BOLD}TC-304 照片聚合列表${RESET}"
  R=$(http_get "/eh/visit/photo/page/aggregate")
  assert_code "$R" "0" "TC-304 code=0"
else
  skip "TC-301/302/303/304 visit_id 不存在，跳过"
fi

# ══════════════════════════════════════════════════════
section "模块五：改期流水"
# ══════════════════════════════════════════════════════

echo -e "\n${BOLD}TC-401 查询改期流水${RESET}"
R=$(http_get "/eh/changelog/page/aggregate")
assert_code "$R" "0" "TC-401 code=0"
CL_COUNT=$(echo "$R" | jq '.data | length' 2>/dev/null)
info "改期流水记录数: ${CL_COUNT:-0}"

# ══════════════════════════════════════════════════════
section "模块六：鉴权"
# ══════════════════════════════════════════════════════

echo -e "\n${BOLD}TC-601 无 Token 访问${RESET}"
R=$(curl -s -o /dev/null -w "%{http_code}" "${BASE}/eh/apply/page")
# pig框架无token返回424（Failed Dependency）
[[ "$R" == "401" || "$R" == "424" ]] && pass "TC-601 返回${R}（未授权）" || fail "TC-601 期望4xx，实际=$R"

# ══════════════════════════════════════════════════════
section "端到端闭环验证"
# ══════════════════════════════════════════════════════

echo -e "\n${BOLD}TC-E2E-001 申请→审批全通过→留存→照片→归还 最终状态断言${RESET}"
if [[ -n "$APPLY2_ID" ]]; then
  FINAL_STATUS=$(db_query "SELECT status FROM eh_apply WHERE id=$APPLY2_ID")
  FINAL_RETURN=$(db_query "SELECT return_signer FROM eh_apply WHERE id=$APPLY2_ID")
  FINAL_COUNT=$(db_query "SELECT actual_count FROM eh_apply WHERE id=$APPLY2_ID")
  ALL_NODES=$(db_query "SELECT COUNT(*) FROM eh_approval_node WHERE apply_id=$APPLY2_ID AND status='approved'")
  VISIT_STATUS=$(db_query "SELECT status FROM eh_visit_record WHERE apply_id=$APPLY2_ID ORDER BY id DESC LIMIT 1")
  PHOTO_CNT=$(db_query "SELECT COUNT(*) FROM eh_visit_photo WHERE visit_id=${VISIT_ID:-0}")

  [[ "$FINAL_STATUS" == "2" ]]  && pass "E2E: apply.status=2(最终通过)" || fail "E2E: apply.status=$FINAL_STATUS"
  [[ "$ALL_NODES" == "2" ]]     && pass "E2E: 2个审批节点均 approved" || fail "E2E: approved节点数=$ALL_NODES"
  [[ "$FINAL_COUNT" == "9" ]]   && pass "E2E: actual_count=9" || fail "E2E: actual_count=$FINAL_COUNT"
  [[ "$FINAL_RETURN" == "李主管" ]] && pass "E2E: return_signer=李主管" || fail "E2E: return_signer=$FINAL_RETURN"
  [[ "$VISIT_STATUS" == "2" ]]  && pass "E2E: visit_record.status=2(已归还)" || fail "E2E: visit_record.status=$VISIT_STATUS"
  [[ "${PHOTO_CNT:-0}" -ge 2 ]] && pass "E2E: 照片≥2张" || fail "E2E: 照片数=${PHOTO_CNT:-0}"
else
  skip "E2E-001 apply_id 不存在"
fi

echo -e "\n${BOLD}TC-E2E-002 驳回闭环验证${RESET}"
if [[ -n "$REJ_ID" ]]; then
  assert_db "SELECT status FROM eh_apply WHERE id=$REJ_ID" "3" "E2E-002 驳回申请 status=3"
  VISIT_AFTER_REJ=$(db_query "SELECT COUNT(*) FROM eh_visit_record WHERE apply_id=$REJ_ID")
  [[ "$VISIT_AFTER_REJ" == "0" ]] && pass "E2E-002 驳回后无参观留存记录" || fail "E2E-002 期望无留存，实际=$VISIT_AFTER_REJ"
else
  skip "E2E-002 驳回申请ID不存在"
fi

# ══════════════════════════════════════════════════════
section "测试结果汇总"
# ══════════════════════════════════════════════════════
TOTAL=$((PASS + FAIL + SKIP))
echo -e ""
echo -e "  总计: ${BOLD}${TOTAL}${RESET}  ${GREEN}通过: ${PASS}${RESET}  ${RED}失败: ${FAIL}${RESET}  ${YELLOW}跳过: ${SKIP}${RESET}"
echo -e ""
if [[ $FAIL -eq 0 ]]; then
  echo -e "  ${GREEN}${BOLD}所有用例通过！${RESET}"
else
  echo -e "  ${RED}${BOLD}有 ${FAIL} 个用例失败，请检查上方日志${RESET}"
fi
echo -e ""
