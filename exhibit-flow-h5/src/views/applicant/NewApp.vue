<template>
  <div class="eh-new">
    <!-- breadcrumb -->
    <div class="eh-new__bar">
      <button class="eh-new__back" @click="onBack">
        <Ic n="arrowLeft" :size="12" />返回
      </button>
      <Ic n="chevronRight" :size="12" color="var(--t-text3)" />
      <span class="eh-new__title">新建参观申请</span>
    </div>

    <div class="eh-new__card">
      <StepBar :steps="STEPS" :cur="step" />

      <!-- Step 1：基本信息 -->
      <div v-if="step === 1" class="eh-new__grid eh-new__grid--col">
        <FancyInput label="会议主题" v-model="form.title" placeholder="本次参观的主题" required />

        <div class="eh-new__grid eh-new__grid--2col">
          <FancyInput label="来访单位" v-model="form.unit" placeholder="单位全称" required />
          <FancySelect label="所属行业" v-model="form.industry" :options="INDUSTRIES" />
        </div>

        <FancyInput label="参观日期" required hint="点击选择日期，不可早于今天">
          <div
            class="eh-new__date-field"
            role="button"
            tabindex="0"
            aria-label="选择参观日期"
            @click="openDatePicker"
            @keydown.enter.prevent="openDatePicker"
            @keydown.space.prevent="openDatePicker"
          >
            <div class="eh-new__date-inner">
              <span :class="form.startDate ? 'eh-new__date-val' : 'eh-new__date-empty'">
                {{ displayDateLabel }}
              </span>
              <div class="eh-new__date-meta">
                <span class="eh-new__date-icon-row" aria-hidden="true">
                  <Ic n="calendar" :size="15" color="var(--t-text3)" />
                  <Ic n="chevronDown" :size="12" color="var(--t-text3)" />
                </span>
              </div>
            </div>
            <input
              ref="dateInputRef"
              type="date"
              tabindex="-1"
              v-model="form.startDate"
              class="eh-new__date-input"
              :min="minSelectableDate"
            />
          </div>
        </FancyInput>

        <FancyInput label="使用时段" required hint="选择占用展厅的时间段">
          <div class="eh-new__slots">
            <button
              v-for="s in TIME_SLOTS"
              :key="s.key"
              type="button"
              class="eh-new__slot"
              :class="{
                'eh-new__slot--on':       form.timeSlot === s.key && conflictState === 'idle' && !checking,
                'eh-new__slot--checking': form.timeSlot === s.key && checking,
                'eh-new__slot--ok':       form.timeSlot === s.key && conflictState === 'ok',
                'eh-new__slot--err':      form.timeSlot === s.key && conflictState === 'error',
              }"
              @click="selectSlot(s)"
            >
              <span class="eh-new__slot-name">{{ s.name }}</span>
              <span class="eh-new__slot-range">{{ s.range }}</span>
            </button>
          </div>
        </FancyInput>

        <!-- 冲突弹窗 -->
        <Transition name="eh-modal">
          <div v-if="conflictModal" class="eh-new__modal-mask" @click.self="conflictModal = false">
            <div class="eh-new__modal">
              <div class="eh-new__modal-icon">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color:var(--t-danger)"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              </div>
              <div class="eh-new__modal-title">时段冲突</div>
              <div class="eh-new__modal-desc">所选时段已被以下申请占用，请更换日期或时段后重新提交。</div>
              <div class="eh-new__modal-detail">
                <div class="eh-new__modal-detail-row">
                  <span class="eh-new__modal-detail-key">占用申请</span>
                  <span class="eh-new__modal-detail-val">{{ form.conflictResult }}</span>
                </div>
                <div class="eh-new__modal-detail-row">
                  <span class="eh-new__modal-detail-key">冲突时段</span>
                  <span class="eh-new__modal-detail-val">{{ form.startDate }} {{ form.startHour }}:00 — {{ form.endHour }}:00</span>
                </div>
              </div>
              <button class="eh-new__modal-btn" @click="conflictModal = false">我知道了</button>
            </div>
          </div>
        </Transition>

        <div class="eh-new__grid eh-new__grid--2col">
          <FancyInput label="最高陪同领导" v-model="form.leader" placeholder="领导姓名及职务" />
          <FancyInput label="预计人数" v-model="form.headCount" placeholder="总人数" type="number" />
        </div>

        <div class="eh-new__grid eh-new__grid--2col">
          <FancySelect label="所属区县" v-model="form.district" :options="DISTRICTS" required />
          <FancyInput label="申请部门" v-model="form.dept" placeholder="所在部门全称" required />
        </div>

        <div class="eh-new__grid eh-new__grid--2col">
          <FancyInput label="申请人姓名" v-model="form.applicant" required />
          <FancyInput label="联系电话" v-model="form.phone" required />
        </div>

        <FancyInput label="简要议程" type="textarea" v-model="form.agenda" placeholder="本次参观主要议程和目的" />
      </div>

      <!-- Step 2：确认提交 -->
      <div v-else-if="step === 2" class="eh-new__grid eh-new__grid--col">
        <div class="eh-new__success">
          <Ic n="checkCircle" :size="14" color="var(--t-success)" />
          <span>信息确认无误后点击提交，将自动发起审批流程</span>
        </div>
        <div class="eh-new__summary">
          <div
            v-for="(r, i) in summary"
            :key="r[0]"
            class="eh-new__summary-row"
            :class="{ 'eh-new__summary-row--alt': i % 2 === 0 }"
          >
            <span class="eh-new__summary-key">{{ r[0] }}</span>
            <span class="eh-new__summary-val">{{ r[1] }}</span>
          </div>
        </div>
        <div class="eh-new__hint">
          <Ic n="info" :size="14" color="var(--t-warning)" />
          <span><b>审批链：</b>{{ approvalChainText }}</span>
        </div>
        <FancyInput label="备注" type="textarea" v-model="form.remark" placeholder="其他特殊需求或补充说明" />
      </div>

      <!-- Footer -->
      <div class="eh-new__footer">
        <Btn variant="ghost" icon="arrowLeft" @click="onPrev">{{ step === 1 ? '取消' : '上一步' }}</Btn>
        <div class="eh-new__actions">
          <Btn v-if="step < 2" variant="ghost" @click="onSaveDraft">存草稿</Btn>
          <Btn v-if="step < 2" variant="primary" icon="chevronRight" @click="onNext">下一步</Btn>
          <Btn v-else variant="orange" icon="send" :disabled="submitting" @click="onSubmit">
            {{ submitting ? '提交中…' : '提交申请' }}
          </Btn>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <Transition name="eh-toast">
      <div v-if="toast" class="eh-toast" :data-type="toast.type">
        <Ic :n="toastIcon" :size="16" :color="toastColor" />
        <span>{{ toast.msg }}</span>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { FancyInput, FancySelect, Btn, Ic, StepBar } from '../../components/eh';
import { INDUSTRIES, DISTRICTS, DEPTS } from '../../mock/applications';
import { checkTimeConflict, submitApplication } from '../../api/eh/apply';

const router = useRouter();

const dateInputRef = ref<HTMLInputElement | null>(null);

function parseLocalYmd(ymd: string): Date | null {
  if (!ymd || !/^\d{4}-\d{2}-\d{2}$/.test(ymd)) return null;
  const [y, m, d] = ymd.split('-').map(Number);
  return new Date(y, m - 1, d);
}

const displayDateLabel = computed(() => form.startDate || '选择参观日期');

const dateWeekday = computed(() => {
  const dt = parseLocalYmd(form.startDate);
  if (!dt) return '';
  return new Intl.DateTimeFormat('zh-CN', { weekday: 'short' }).format(dt);
});

/** 禁止选择过去的日期（本地日界线） */
const minSelectableDate = computed(() => {
  const t = new Date();
  const y = t.getFullYear();
  const m = String(t.getMonth() + 1).padStart(2, '0');
  const d = String(t.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
});

function openDatePicker() {
  const el = dateInputRef.value;
  if (!el) return;
  if (typeof el.showPicker === 'function') {
    try {
      el.showPicker();
      return;
    } catch {
      /* 部分浏览器在无用户手势时抛错，退回 click */
    }
  }
  el.focus();
  el.click();
}

const STEPS = ['基本信息', '确认提交'];
const step = ref(1);
const submitting = ref(false);
const checking = ref(false);

const TIME_SLOTS = [
  { key: 'morning',   name: '上午', range: '09:00 - 12:00', startHour: '09', endHour: '12' },
  { key: 'afternoon', name: '下午', range: '14:00 - 17:00', startHour: '14', endHour: '17' },
  { key: 'fullday',   name: '全天', range: '09:00 - 17:00', startHour: '09', endHour: '17' },
] as const;

type SlotKey = typeof TIME_SLOTS[number]['key'];

const form = reactive({
  title: '',
  unit: '',
  industry: INDUSTRIES[0],
  district: DISTRICTS[0],
  applicant: '',
  phone: '',
  dept: '',
  startDate: '',
  timeSlot: '' as SlotKey | '',
  startHour: '',
  endHour: '',
  leader: '',
  headCount: '',
  agenda: '',
  remark: '',
  conflictChecked: false,
  conflictResult: null as string | null,
});

const conflictModal = ref(false);

function selectSlot(s: typeof TIME_SLOTS[number]) {
  // 点击已冲突的 chip → 重新弹出详情，不重置
  if (form.timeSlot === s.key && conflictState.value === 'error') {
    conflictModal.value = true;
    return;
  }
  form.timeSlot        = s.key;
  form.startHour       = s.startHour;
  form.endHour         = s.endHour;
  form.conflictChecked = false;
  form.conflictResult  = null;
}

const conflictState = computed(() => {
  if (!form.conflictChecked) return 'idle';
  return form.conflictResult ? 'error' : 'ok';
});

// 选完日期 + 时段后自动检测
watch(
  () => [form.startDate, form.timeSlot] as const,
  async ([date, slot]) => {
    if (!date || !slot) {
      form.conflictChecked = false;
      form.conflictResult  = null;
      return;
    }
    checking.value = true;
    form.conflictChecked = false;
    form.conflictResult  = null;
    form.conflictResult  = await checkTimeConflict(date, form.startHour, form.endHour);
    form.conflictChecked = true;
    checking.value = false;

    if (!form.conflictResult) {
      // 无冲突：chip 变绿，无需额外操作
    } else {
      // 有冲突：自动弹窗
      conflictModal.value = true;
    }
  }
);

const approvalChainText = computed(() => {
  if (form.leader === '总经理') return '部门领导 → 展厅主管 → 总经理（三级）';
  if (form.leader === '分管副总') return '部门领导 → 展厅主管 → 分管副总（三级）';
  return '部门领导 → 展厅主管（二级）';
});

const selectedSlot = computed(() => TIME_SLOTS.find(s => s.key === form.timeSlot));

const summary = computed<[string, string][]>(() => [
  ['会议主题', form.title],
  ['来访单位', form.unit],
  ['所属行业', form.industry],
  ['参观日期', form.startDate],
  ['使用时段', selectedSlot.value ? `${selectedSlot.value.name}（${selectedSlot.value.range}）` : '—'],
  ['最高领导', form.leader || '—'],
  ['预计人数', form.headCount || '—'],
  ['申请部门', form.dept],
  ['申请人', `${form.applicant}（${form.district}）`],
  ['联系电话', form.phone],
  ['简要议程', form.agenda || '—'],
]);

interface Toast { msg: string; type: 'success' | 'info' | 'error' }
const toast = ref<Toast | null>(null);
const toastIcon = computed(() => {
  if (!toast.value) return 'info';
  if (toast.value.type === 'success') return 'checkCircle';
  if (toast.value.type === 'error') return 'alertTri';
  return 'info';
});
const toastColor = computed(() => {
  if (!toast.value) return 'var(--t-text2)';
  if (toast.value.type === 'success') return 'var(--t-success)';
  if (toast.value.type === 'error') return 'var(--t-danger)';
  return '#1d4ed8';
});
function notify(msg: string, type: Toast['type'] = 'success') {
  toast.value = { msg, type };
  setTimeout(() => (toast.value = null), 2600);
}

function onBack() { router.back(); }
function onPrev() {
  if (step.value === 1) router.back();
  else step.value -= 1;
}
function onNext() {
  if (!form.title || !form.unit || !form.startDate || !form.timeSlot || !form.applicant || !form.phone || !form.district) {
    notify('请填写所有必填项', 'error');
    return;
  }
  step.value += 1;
}
function onSaveDraft() { notify('草稿已保存', 'info'); }
async function onSubmit() {
  submitting.value = true;
  try {
    const app = await submitApplication({ ...form });
    notify(`申请「${app.title}」已提交，等待审批`, 'success');
    setTimeout(() => router.replace('/mine'), 600);
  } catch (e) {
    notify('提交失败，请重试', 'error');
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.eh-new {
  padding: 16px;
  position: relative;
}
.eh-new__bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
}
.eh-new__back {
  border: 1px solid var(--t-border);
  background: var(--t-surface);
  cursor: pointer;
  border-radius: 4px;
  padding: 5px 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--t-text2);
  font-family: inherit;
  font-weight: 500;
}
.eh-new__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--t-text1);
}
.eh-new__card {
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: 8px;
  padding: 20px 16px;
}

.eh-new__grid {
  display: grid;
  gap: 14px;
}
.eh-new__grid--col {
  grid-template-columns: 1fr;
}
.eh-new__grid--2col {
  grid-template-columns: 1fr 1fr;
}

/* 参观日期：背景/圆角/描边与 FancyInput、FancySelect 触发器一致 */
.eh-new__date-field {
  position: relative;
  border: 1px solid var(--t-border);
  border-radius: 4px;
  background: var(--t-surface);
  box-sizing: border-box;
  transition: border-color 0.15s ease;
  outline: none;
}
.eh-new__date-field:hover {
  border-color: var(--t-border-dark);
}
.eh-new__date-field:focus-within {
  border-color: var(--t-text1);
}
.eh-new__date-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 35px;
  padding: 8px 10px;
  pointer-events: none;
  user-select: none;
  line-height: 1.2;
}
.eh-new__date-val {
  font-size: 13px;
  font-weight: 400;
  color: var(--t-text1);
}
.eh-new__date-empty {
  font-size: 13px;
  font-weight: 400;
  color: var(--t-text3);
}
.eh-new__date-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.eh-new__date-week {
  font-family: var(--t-font-mono);
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--t-text2);
  background: var(--t-bg);
  border: 1px solid var(--t-border);
  border-radius: 4px;
  padding: 3px 6px;
  line-height: 1;
}
.eh-new__date-icon-row {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0.92;
}
/* 原生 date：整块可点，尽量不露出系统控件样式 */
.eh-new__date-input {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  border: 0;
  opacity: 0;
  cursor: pointer;
  font-size: 16px; /* iOS 缩放输入框用 */
  color: transparent;
  background: transparent;
  box-sizing: border-box;
  -webkit-appearance: none;
  appearance: none;
  z-index: 1;
}
.eh-new__date-input::-webkit-calendar-picker-indicator {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  opacity: 0;
  cursor: pointer;
}
.eh-new__date-input:focus {
  outline: none;
}

/* 时段 chip 快选 */
.eh-new__slots {
  display: flex;
  gap: 8px;
}
.eh-new__slot {
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
  transition: border-color 0.15s ease, background 0.15s ease, color 0.15s ease;
}
.eh-new__slot:hover:not(.eh-new__slot--on):not(.eh-new__slot--checking):not(.eh-new__slot--ok):not(.eh-new__slot--err) {
  border-color: var(--t-border-dark);
}
.eh-new__slot:focus-visible {
  outline: 2px solid var(--t-text1);
  outline-offset: 2px;
}
.eh-new__slot-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text1);
}
.eh-new__slot-range {
  font-size: 10px;
  color: var(--t-text3);
  white-space: nowrap;
}
.eh-new__slot--on {
  border-color: var(--t-accent);
  background: var(--t-accent);
}
.eh-new__slot--on .eh-new__slot-name,
.eh-new__slot--on .eh-new__slot-range {
  color: #fff;
}

/* 检测中：边框脉冲 */
@keyframes border-pulse {
  0%, 100% { border-color: var(--t-accent); opacity: 1; }
  50%       { border-color: var(--t-accent); opacity: 0.5; }
}
.eh-new__slot--checking {
  border-color: var(--t-accent);
  background: var(--t-accent);
  animation: border-pulse 1s ease-in-out infinite;
}
.eh-new__slot--checking .eh-new__slot-name,
.eh-new__slot--checking .eh-new__slot-range {
  color: #fff;
}

/* 无冲突：绿色 */
.eh-new__slot--ok {
  border-color: var(--t-success);
  background: var(--t-success);
}
.eh-new__slot--ok .eh-new__slot-name,
.eh-new__slot--ok .eh-new__slot-range {
  color: #fff;
}

/* 有冲突：红色，点击重新查看弹窗 */
.eh-new__slot--err {
  border-color: var(--t-danger);
  background: var(--t-danger);
  cursor: pointer;
}
.eh-new__slot--err .eh-new__slot-name,
.eh-new__slot--err .eh-new__slot-range {
  color: #fff;
}

/* 冲突弹窗遮罩 */
.eh-new__modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 24px;
}
.eh-new__modal {
  background: var(--t-surface);
  border-radius: 12px;
  padding: 28px 22px 22px;
  width: 100%;
  max-width: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.25);
}
.eh-new__modal-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--t-danger-light, #fef2f2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
}
.eh-new__modal-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--t-text1);
}
.eh-new__modal-desc {
  font-size: 12px;
  color: var(--t-text2);
  text-align: center;
  line-height: 1.6;
}
.eh-new__modal-detail {
  width: 100%;
  background: var(--t-bg);
  border-radius: 6px;
  overflow: hidden;
  margin-top: 4px;
}
.eh-new__modal-detail-row {
  display: flex;
  gap: 10px;
  padding: 8px 12px;
  font-size: 12px;
  border-bottom: 1px solid var(--t-border);
}
.eh-new__modal-detail-row:last-child { border-bottom: none; }
.eh-new__modal-detail-key {
  color: var(--t-text3);
  flex-shrink: 0;
  width: 52px;
}
.eh-new__modal-detail-val {
  color: var(--t-text1);
  font-weight: 500;
  word-break: break-all;
}
.eh-new__modal-btn {
  margin-top: 6px;
  width: 100%;
  padding: 11px;
  border: none;
  border-radius: 6px;
  background: var(--t-text1);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
}

/* 弹窗进出动画 */
.eh-modal-enter-active, .eh-modal-leave-active {
  transition: opacity 0.2s;
}
.eh-modal-enter-active .eh-new__modal,
.eh-modal-leave-active .eh-new__modal {
  transition: transform 0.25s cubic-bezier(0.34,1.56,0.64,1), opacity 0.2s;
}
.eh-modal-enter-from, .eh-modal-leave-to {
  opacity: 0;
}
.eh-modal-enter-from .eh-new__modal,
.eh-modal-leave-to .eh-new__modal {
  transform: scale(0.88);
  opacity: 0;
}

.eh-new__hint {
  background: var(--t-warning-light);
  border: 1px solid #92400e33;
  border-radius: 4px;
  padding: 10px 14px;
  font-size: 12px;
  color: var(--t-warning);
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.eh-new__success {
  background: var(--t-success-light);
  border: 1px solid #2c641544;
  border-radius: 4px;
  padding: 10px 14px;
  font-size: 12px;
  color: var(--t-success);
  display: flex;
  gap: 8px;
  align-items: center;
}

.eh-new__summary {
  border: 1px solid var(--t-border);
  border-radius: 6px;
  overflow: hidden;
}
.eh-new__summary-row {
  display: flex;
  gap: 14px;
  padding: 9px 14px;
  background: var(--t-surface);
  font-size: 12px;
}
.eh-new__summary-row--alt {
  background: var(--t-bg);
}
.eh-new__summary-key {
  color: var(--t-text3);
  width: 66px;
  flex-shrink: 0;
  font-weight: 500;
}
.eh-new__summary-val {
  color: var(--t-text1);
  font-weight: 500;
  word-break: break-all;
}

.eh-new__footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--t-border);
  gap: 8px;
}
.eh-new__actions {
  display: flex;
  gap: 8px;
}

.eh-toast {
  position: fixed;
  top: 16px;
  left: 16px;
  right: 16px;
  background: var(--t-surface);
  border-radius: 6px;
  padding: 12px 14px;
  box-shadow: 0 8px 32px rgba(17, 17, 17, 0.14);
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--t-border);
  border-left: 3px solid var(--t-success);
  z-index: 100;
  font-size: 13px;
  color: var(--t-text1);
}
.eh-toast[data-type='error'] { border-left-color: var(--t-danger); }
.eh-toast[data-type='info']  { border-left-color: #1d4ed8; }

.eh-toast-enter-active,
.eh-toast-leave-active {
  transition: transform 0.2s, opacity 0.2s;
}
.eh-toast-enter-from,
.eh-toast-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}
</style>
