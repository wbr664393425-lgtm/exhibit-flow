<template>
  <div class="eh-detail" v-if="app">
    <div class="eh-detail__shell">
    <div class="eh-detail__bar">
      <h2 class="eh-detail__title">{{ app.title }}</h2>
      <button class="eh-detail__close" @click="router.back()" aria-label="关闭详情">
        <Ic n="x" :size="14" color="var(--t-text3)" />
      </button>
    </div>

    <div class="eh-detail__meta">
      <span class="eh-detail__meta-id">{{ app.id }}</span>
      <Badge :status="app.status" />
      <span class="eh-detail__meta-company">{{ app.unit }}</span>
      <MonoLabel>创建于 {{ app.created }}</MonoLabel>
    </div>

    <div class="eh-detail__tabs-wrap">
    <div class="eh-detail__tabs">
      <button
        v-for="t in tabs"
        :key="t.key"
        class="eh-detail__tab"
        :class="{ 'eh-detail__tab--active': tab === t.key }"
        @click="tab = t.key"
      >{{ t.label }}</button>
    </div>
      <span class="eh-detail__status-chip"><span class="eh-detail__status-dot"></span>{{ statusText }}</span>
    </div>

    <!-- 信息 -->
    <div v-if="tab === 'info'" class="eh-detail__info-card">
      <div class="eh-detail__grid">
        <div v-for="r in infoRows" :key="r.key" class="eh-detail__cell" :class="{ 'eh-detail__cell--full': r.full }">
          <div class="eh-detail__cell-label">
            <Ic :n="r.icon" :size="13" color="var(--t-text3)" />
            <MonoLabel>{{ r.key }}</MonoLabel>
          </div>
          <div class="eh-detail__cell-val">{{ r.value }}</div>
        </div>
      </div>

      <template v-if="app.agenda">
        <div class="eh-detail__divider"></div>
        <div class="eh-detail__agenda">
          <div class="eh-detail__cell-label">
            <Ic n="fileText" :size="13" color="var(--t-text3)" />
            <MonoLabel>简要议程</MonoLabel>
          </div>
          <p>{{ app.agenda }}</p>
        </div>
      </template>
    </div>

    <!-- 审批 -->
    <div v-else-if="tab === 'approval'" class="eh-detail__section">
      <ApprovalTimeline :nodes="app.approvalNodes" />
    </div>

    <!-- 审批历史 -->
    <div v-else-if="tab === 'history'" class="eh-detail__history">
      <div v-if="!app.history || app.history.length === 0" class="eh-detail__history-empty">
        <Ic n="clock" :size="32" color="var(--t-text3)" />
        <p>暂无历史记录</p>
      </div>
      <div v-else class="eh-detail__timeline">
        <div
          v-for="(evt, idx) in app.history"
          :key="idx"
          class="eh-detail__tl-item"
        >
          <div class="eh-detail__tl-left">
            <div class="eh-detail__tl-dot" :data-type="evt.eventType"></div>
            <div v-if="idx < app.history.length - 1" class="eh-detail__tl-line"></div>
          </div>
          <div class="eh-detail__tl-body">
            <div class="eh-detail__tl-head">
              <span class="eh-detail__tl-tag" :data-type="evt.eventType">{{ historyLabel(evt.eventType) }}</span>
              <MonoLabel>{{ evt.eventTime }}</MonoLabel>
            </div>
            <div class="eh-detail__tl-desc">{{ evt.eventDesc }}</div>
            <div v-if="evt.operator" class="eh-detail__tl-meta">操作人：{{ evt.operator }}</div>
            <div v-if="evt.remark" class="eh-detail__tl-remark">{{ evt.remark }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div v-if="canAct || app.status === 'rejected'" class="eh-detail__action-bar">
      <Btn v-if="app.status === 'rejected'" variant="primary" size="md" icon="send" @click="router.push(`/apply/edit/${app.id}`)">
        重新提交申请
      </Btn>
      <template v-if="canAct">
        <Btn v-if="app.status === 'pending' || app.status === 'approved'" variant="outline" size="md" icon="rotate" @click="showReschedule = true">
          申请改期
        </Btn>
        <Btn variant="ghost" size="md" icon="xCircle" :style="{ color: 'var(--t-danger)', borderColor: '#c41c1c44' }" @click="showCancel = true">
          取消申请
        </Btn>
      </template>
    </div>

    <!-- 取消弹层 -->
    <div v-if="showCancel" class="eh-modal" @click.self="showCancel = false">
      <div class="eh-modal__body">
        <div class="eh-modal__head">
          <span>取消申请</span>
          <button class="eh-modal__x" @click="showCancel = false"><Ic n="x" :size="14" /></button>
        </div>
        <div class="eh-modal__content">
          <div class="eh-modal__alert eh-modal__alert--danger">
            <Ic n="alertTri" :size="14" color="var(--t-danger)" />
            <span>取消后排期将释放，已审批的记录保留存档</span>
          </div>
          <div class="eh-modal__info">
            <b>{{ app.title }}</b>
            <div>会议正式开始时间：{{ appStartLabel(app) }}</div>
          </div>
          <FancyInput label="取消原因" required>
            <textarea v-model="cancelReason" placeholder="请说明取消原因（必填）" class="eh-input__field eh-input__field--textarea" rows="3" />
          </FancyInput>
          <div class="eh-modal__foot">
            <Btn variant="ghost" @click="showCancel = false">保留申请</Btn>
            <Btn variant="danger" :disabled="!cancelReason.trim()" @click="onCancel">确认取消</Btn>
          </div>
        </div>
      </div>
    </div>

    <!-- 改期弹层 -->
    <div v-if="showReschedule" class="eh-modal" @click.self="showReschedule = false">
      <div class="eh-modal__body">
        <div class="eh-modal__head">
          <span>申请改期</span>
          <button class="eh-modal__x" @click="showReschedule = false"><Ic n="x" :size="14" /></button>
        </div>
        <div class="eh-modal__content">
          <div class="eh-modal__info">
            <MonoLabel :style="{ display: 'block', marginBottom: '4px' }">原会议正式开始时间</MonoLabel>
            <span>{{ appStartLabel(app) }}</span>
          </div>
          <FancyInput label="新参观日期" required>
            <input type="date" v-model="rescheduleForm.newDate" class="eh-input__field" :min="todayYmd" />
          </FancyInput>
          <FancyInput label="新会议正式开始时间" required>
            <input type="time" v-model="rescheduleForm.newTime" class="eh-input__field" />
          </FancyInput>
          <div v-if="rescheduleConflictState === 'error' && rescheduleConflictMsg" class="eh-modal__alert eh-modal__alert--danger">
            <Ic n="alertTri" :size="13" color="var(--t-danger)" />
            <span>所选开始时间冲突：{{ rescheduleConflictMsg }}</span>
          </div>
          <div class="eh-modal__alert eh-modal__alert--accent">
            <Ic n="info" :size="13" color="var(--t-accent)" />
            <span>改期将保留原申请编号，需重新走审批流程</span>
          </div>
          <div class="eh-modal__foot">
            <Btn variant="ghost" @click="showReschedule = false">取消</Btn>
            <Btn variant="orange" icon="rotate" :disabled="!canSubmitReschedule" @click="onReschedule">确认改期</Btn>
          </div>
        </div>
      </div>
    </div>

    <!-- toast -->
    <Transition name="eh-toast">
      <div v-if="toast" class="eh-toast" :data-type="toast.type">
        <Ic :n="toast.type === 'success' ? 'checkCircle' : 'info'" :size="16" :color="toast.type === 'success' ? 'var(--t-success)' : 'var(--t-accent)'" />
        <span>{{ toast.msg }}</span>
      </div>
    </Transition>
    </div>
  </div>

  <div v-else class="eh-detail__empty">
    <Ic n="clipboard" :size="40" color="var(--t-text3)" />
    <p>未找到该申请</p>
    <Btn variant="outline" icon="arrowLeft" @click="router.back()">返回</Btn>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Badge, Ic, MonoLabel, FancyInput, ApprovalTimeline, Btn } from '../../components/eh';
import { fetchApplication, cancelApplication, rescheduleApplication, checkTimeConflict } from '../../api/eh/apply';
import type { Application } from '../../mock/applications';

const route = useRoute();
const router = useRouter();

const app = ref<Application | undefined>();
const tab = ref<'info' | 'approval' | 'history'>('info');
const tabs = [
  { key: 'info' as const, label: '申请信息' },
  { key: 'approval' as const, label: '审批进度' },
  { key: 'history' as const, label: '审批历史' },
];

const showCancel = ref(false);
const showReschedule = ref(false);
const cancelReason = ref('');

const rescheduleForm = reactive({
  newDate: '2026-05-08',
  newTime: '09:00',
});
const rescheduleChecking = ref(false);
const rescheduleConflictChecked = ref(false);
const rescheduleConflictMsg = ref<string | null>(null);
const rescheduleConflictState = computed(() => {
  if (!rescheduleConflictChecked.value) return 'idle';
  return rescheduleConflictMsg.value ? 'error' : 'ok';
});
const canSubmitReschedule = computed(() =>
  !!rescheduleForm.newDate &&
  !!rescheduleForm.newTime &&
  !rescheduleChecking.value &&
  rescheduleConflictChecked.value &&
  !rescheduleConflictMsg.value,
);

const todayYmd = computed(() => {
  const t = new Date();
  const y = t.getFullYear();
  const m = String(t.getMonth() + 1).padStart(2, '0');
  const d = String(t.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
});

watch(
  () => [showReschedule.value, rescheduleForm.newDate, rescheduleForm.newTime] as const,
  async ([open, date, time]) => {
    if (!open || !date || !time) {
      rescheduleConflictChecked.value = false;
      rescheduleConflictMsg.value = null;
      return;
    }
    rescheduleChecking.value = true;
    rescheduleConflictChecked.value = false;
    rescheduleConflictMsg.value = null;
    try {
      rescheduleConflictMsg.value = await checkTimeConflict(date, time, time);
    } finally {
      rescheduleConflictChecked.value = true;
      rescheduleChecking.value = false;
    }
  }
);

onMounted(async () => {
  const id = route.params.id as string;
  app.value = await fetchApplication(id);
});

const infoRows = computed(() => {
  if (!app.value) return [];
  const a = app.value;
  return [
    { key: '申请编号', value: a.id, icon: 'clipboard', full: false },
    { key: '来访单位', value: a.unit, icon: 'building', full: false },
    { key: '会议性质', value: a.meetingNature === 'internal' ? '内部' : '外部', icon: 'clipboard', full: false },
    { key: '会议正式开始时间', value: a.startTime, icon: 'clock', full: false },
    { key: '客户人数', value: `${a.customerCount ?? a.headCount} 人`, icon: 'users', full: false },
    { key: '自有人员人数', value: `${a.internalCount ?? 0} 人`, icon: 'users', full: false },
    { key: '我公司陪同领导', value: a.leader, icon: 'user', full: false },
  ];
});

function appStartLabel(item: Application) {
  return item.startTime || '—';
}

const canAct = computed(() => app.value?.status === 'pending' || app.value?.status === 'approved');
const statusText = computed(() => {
  if (!app.value) return '';
  const map: Record<string, string> = {
    pending: '审批中',
    approved: '已批准',
    rejected: '已驳回',
    cancelled: '已取消',
    rescheduled: '已改期',
  };
  return map[app.value.status] || '处理中';
});

interface Toast { msg: string; type: 'success' | 'info' }
const toast = ref<Toast | null>(null);
function notify(msg: string, type: Toast['type'] = 'success') {
  toast.value = { msg, type };
  setTimeout(() => (toast.value = null), 2400);
}

function historyLabel(eventType: string): string {
  const map: Record<string, string> = {
    submit: '提交申请',
    resubmit: '重新提交',
    approve: '审批通过',
    reject: '审批驳回',
    reschedule: '申请改期',
    cancel: '取消申请',
  };
  return map[eventType] || eventType;
}

async function onCancel() {
  if (!app.value) return;
  await cancelApplication(app.value.id, cancelReason.value);
  showCancel.value = false;
  notify(`申请「${app.value.title}」已取消`, 'success');
  app.value = { ...app.value, status: 'cancelled' };
}

async function onReschedule() {
  if (!app.value || !canSubmitReschedule.value) return;
  const payload = {
    newDate: rescheduleForm.newDate,
    newSH: rescheduleForm.newTime,
    newEH: rescheduleForm.newTime,
  };
  await rescheduleApplication(app.value.id, payload);
  showReschedule.value = false;
  notify(`「${app.value.title}」改期成功，将重新审批`, 'success');
  app.value = {
    ...app.value,
    status: 'rescheduled',
    startTime: `${rescheduleForm.newDate} ${rescheduleForm.newTime}`,
    endTime: `${rescheduleForm.newDate} ${rescheduleForm.newTime}`,
  };
}

</script>

<style scoped>
.eh-detail {
  padding: 8px 4px 24px;
  position: relative;
}
.eh-detail__shell {
  background: var(--t-surface);
  border: 1px solid var(--t-border-dark);
  border-radius: 8px;
  overflow: hidden;
}
.eh-detail__bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--t-border-dark);
  background: var(--t-bg);
}
.eh-detail__close {
  width: 28px;
  height: 28px;
  border: 1px solid var(--t-border-dark);
  border-radius: 6px;
  background: var(--t-bg);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.eh-detail__title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--t-text1);
  line-height: 1.25;
}
.eh-detail__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px 10px;
  padding: 12px 18px 10px;
}
.eh-detail__meta-id {
  font-family: var(--t-font-mono);
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--t-text3);
}
.eh-detail__meta-company {
  font-size: 12px;
  color: var(--t-text2);
}

.eh-detail__tabs-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 0 18px;
  border-bottom: 1px solid var(--t-border-dark);
}
.eh-detail__tabs {
  display: flex;
  gap: 8px;
  padding: 10px 0;
  flex-wrap: wrap;
}
.eh-detail__tab {
  border: 1px solid transparent;
  background: var(--t-surface-warm);
  color: var(--t-text2);
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  transition: all 0.15s;
}
.eh-detail__tab--active {
  background: var(--t-accent);
  color: #f8fbff;
  border-color: var(--t-accent);
}
.eh-detail__status-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid var(--t-accent-border);
  color: var(--t-accent);
  background: var(--t-accent-light);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}
.eh-detail__status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--t-accent);
}

.eh-detail__info-card {
  background: transparent;
  border: none;
  border-radius: 0;
  padding: 14px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.eh-detail__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.eh-detail__cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: var(--t-surface-warm);
  border-radius: 8px;
  padding: 10px 12px;
}
.eh-detail__cell--full {
  grid-column: 1 / -1;
}
.eh-detail__cell-label {
  display: flex;
  align-items: center;
  gap: 6px;
}
.eh-detail__cell-val {
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text1);
  padding-left: 0;
  line-height: 1.4;
  word-break: break-all;
  white-space: pre-wrap;
}
.eh-detail__divider {
  height: 1px;
  background: var(--t-border);
  margin: 4px 0;
}
.eh-detail__agenda {
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: var(--t-surface-warm);
  border-radius: 8px;
  padding: 10px 12px;
}
.eh-detail__agenda p {
  margin: 0;
  font-size: 13px;
  color: var(--t-text2);
  line-height: 1.5;
  padding-left: 0;
}

.eh-detail__section {
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: 8px;
  padding: 16px;
}

.eh-detail__action-bar {
  margin: 8px 18px 16px;
  padding-top: 12px;
  border-top: 1px solid var(--t-border);
  display: flex;
  gap: 8px;
}

.eh-detail__empty {
  padding: 64px 16px;
  text-align: center;
  color: var(--t-text3);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.eh-modal {
  position: fixed;
  inset: 0;
  background: rgba(22, 39, 75, 0.32);
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.eh-modal__body {
  background: var(--t-surface);
  border-radius: 8px;
  width: 100%;
  max-height: 90vh;
  overflow: auto;
  border: 1px solid var(--t-border);
  box-shadow: rgba(47, 103, 216, 0.14) 0px 12px 32px;
}
.eh-modal__head {
  padding: 16px 20px;
  border-bottom: 1px solid var(--t-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 700;
  color: var(--t-text1);
}
.eh-modal__x {
  border: 1px solid var(--t-border);
  background: var(--t-bg);
  cursor: pointer;
  border-radius: 4px;
  padding: 4px 8px;
  display: flex;
  color: var(--t-text2);
}
.eh-modal__content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.eh-modal__alert {
  border-radius: 4px;
  padding: 10px 12px;
  font-size: 12px;
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.eh-modal__alert--danger {
  background: var(--t-danger-light);
  border: 1px solid #c41c1c33;
  color: var(--t-danger);
}
.eh-modal__alert--accent {
  background: var(--t-accent-light);
  border: 1px solid var(--t-accent-border);
  color: var(--t-accent);
}
.eh-modal__info {
  background: var(--t-bg);
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 12px;
  color: var(--t-text2);
}
.eh-modal__info b {
  color: var(--t-text1);
  display: block;
  margin-bottom: 2px;
}
.eh-modal__foot {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.eh-modal__slots {
  display: flex;
  align-items: center;
  gap: 8px;
}
.eh-modal__slot {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 10px 6px;
  border: 1px solid var(--t-border);
  border-radius: 4px;
  background: var(--t-surface);
  cursor: pointer;
  font-family: inherit;
  transition: border-color 0.15s ease, background 0.15s ease;
}
.eh-modal__slot:hover {
  border-color: var(--t-border-dark);
}
.eh-modal__slot--on {
  border-color: var(--t-accent);
  background: var(--t-accent);
}
.eh-modal__slot--checking {
  border-color: var(--t-accent);
  background: var(--t-accent);
  opacity: 0.7;
}
.eh-modal__slot--ok {
  border-color: var(--t-success);
  background: var(--t-success);
}
.eh-modal__slot--err {
  border-color: var(--t-danger);
  background: var(--t-danger);
}
.eh-modal__slot-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text1);
}
.eh-modal__slot-range {
  font-size: 10px;
  color: var(--t-text3);
  white-space: nowrap;
}
.eh-modal__slot--on .eh-modal__slot-name,
.eh-modal__slot--on .eh-modal__slot-range,
.eh-modal__slot--checking .eh-modal__slot-name,
.eh-modal__slot--checking .eh-modal__slot-range,
.eh-modal__slot--ok .eh-modal__slot-name,
.eh-modal__slot--ok .eh-modal__slot-range,
.eh-modal__slot--err .eh-modal__slot-name,
.eh-modal__slot--err .eh-modal__slot-range {
  color: #fff;
}

.eh-toast {
  position: fixed;
  top: 16px;
  left: 16px;
  right: 16px;
  background: var(--t-surface);
  border-radius: 6px;
  padding: 12px 14px;
  box-shadow: 0 8px 32px rgba(47, 103, 216, 0.14);
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--t-border);
  border-left: 3px solid var(--t-success);
  z-index: 300;
  font-size: 13px;
  color: var(--t-text1);
}
.eh-toast[data-type='info'] { border-left-color: var(--t-accent); }
.eh-toast-enter-active,
.eh-toast-leave-active {
  transition: transform 0.2s, opacity 0.2s;
}
.eh-toast-enter-from,
.eh-toast-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}

/* 审批历史 */
.eh-detail__history {
  padding: 14px 18px;
}
.eh-detail__history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 36px 0;
  color: var(--t-text3);
  font-size: 13px;
}
.eh-detail__timeline {
  display: flex;
  flex-direction: column;
}
.eh-detail__tl-item {
  display: flex;
  gap: 12px;
  min-height: 60px;
}
.eh-detail__tl-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 14px;
  padding-top: 3px;
}
.eh-detail__tl-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--t-text3);
  flex-shrink: 0;
  border: 2px solid var(--t-surface);
  box-shadow: 0 0 0 2px var(--t-text3);
}
.eh-detail__tl-dot[data-type='submit'],
.eh-detail__tl-dot[data-type='resubmit'] {
  background: var(--t-accent);
  box-shadow: 0 0 0 2px var(--t-accent);
}
.eh-detail__tl-dot[data-type='approve'] {
  background: var(--t-success);
  box-shadow: 0 0 0 2px var(--t-success);
}
.eh-detail__tl-dot[data-type='reject'] {
  background: var(--t-danger);
  box-shadow: 0 0 0 2px var(--t-danger);
}
.eh-detail__tl-dot[data-type='reschedule'] {
  background: var(--t-warning);
  box-shadow: 0 0 0 2px var(--t-warning);
}
.eh-detail__tl-dot[data-type='cancel'] {
  background: var(--t-text3);
  box-shadow: 0 0 0 2px var(--t-text3);
}
.eh-detail__tl-line {
  flex: 1;
  width: 1px;
  background: var(--t-border-dark);
  margin: 4px 0;
}
.eh-detail__tl-body {
  flex: 1;
  padding-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.eh-detail__tl-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.eh-detail__tl-tag {
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--t-surface-warm);
  color: var(--t-text2);
  border: 1px solid var(--t-border);
}
.eh-detail__tl-tag[data-type='submit'],
.eh-detail__tl-tag[data-type='resubmit'] {
  background: var(--t-accent-light);
  color: var(--t-accent);
  border-color: var(--t-accent-border);
}
.eh-detail__tl-tag[data-type='approve'] {
  background: var(--t-success-light);
  color: var(--t-success);
  border-color: #2c641533;
}
.eh-detail__tl-tag[data-type='reject'] {
  background: var(--t-danger-light);
  color: var(--t-danger);
  border-color: #c41c1c33;
}
.eh-detail__tl-tag[data-type='reschedule'] {
  background: var(--t-warning-light);
  color: var(--t-warning);
  border-color: #92400e33;
}
.eh-detail__tl-desc {
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text1);
}
.eh-detail__tl-meta {
  font-size: 11px;
  color: var(--t-text3);
}
.eh-detail__tl-remark {
  font-size: 12px;
  color: var(--t-text2);
  background: var(--t-bg);
  border-radius: 4px;
  padding: 6px 8px;
  border-left: 2px solid var(--t-border-dark);
  line-height: 1.5;
}
</style>
