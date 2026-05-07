<template>
  <div class="eh-new">
    <div class="eh-new__card">
      <StepBar :steps="STEPS" :cur="step" />

      <!-- Step 1：基本信息 -->
      <div v-if="step === 1" class="eh-new__grid eh-new__grid--col">
        <FancySelect label="会议主题" v-model="form.title" :options="MEETING_TOPICS" required />

        <div class="eh-new__grid eh-new__grid--2col">
          <FancySelect label="会议性质" v-model="form.meetingNature" :options="MEETING_NATURE_OPTIONS" required />
          <FancyInput label="来访单位" v-model="form.unit" placeholder="单位全称" required />
        </div>

        <FancySelect label="所属行业" v-model="form.industry" :options="INDUSTRIES" />

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

        <FancyInput label="会议正式开始时间" required hint="选择具体开始时间点">
          <div
            class="eh-new__time-field"
            role="button"
            tabindex="0"
            aria-label="选择会议正式开始时间"
            @click="openTimePicker"
            @keydown.enter.prevent="openTimePicker"
            @keydown.space.prevent="openTimePicker"
          >
            <div class="eh-new__time-inner">
              <span :class="form.meetingTime ? 'eh-new__time-val' : 'eh-new__time-empty'">
                {{ displayTimeLabel }}
              </span>
              <span class="eh-new__time-icon-row" aria-hidden="true">
                <Ic n="clock" :size="15" color="var(--t-text3)" />
                <Ic n="chevronDown" :size="12" color="var(--t-text3)" />
              </span>
            </div>
            <input
              ref="timeInputRef"
              type="time"
              tabindex="-1"
              v-model="form.meetingTime"
              class="eh-new__time-input"
            />
          </div>
        </FancyInput>

        <div v-if="dayScheduleHint" class="eh-new__notice">
          <Ic n="info" :size="13" color="var(--t-warning)" />
          <span>{{ dayScheduleHint }}</span>
        </div>

        <div class="eh-new__grid eh-new__grid--2col">
          <FancyInput label="我公司陪同领导">
            <div class="eh-leader-combo">
              <input
                v-model="form.leader"
                class="eh-input__field"
                placeholder="请输入"
                autocomplete="off"
                @focus="leaderOpen = true"
                @blur="leaderOpen = false"
              />
              <Transition name="eh-leader-drop">
                <div v-if="leaderOpen" class="eh-leader-combo__drop">
                  <div v-for="opt in ['总经理', '分管副总']" :key="opt"
                    class="eh-leader-combo__opt"
                    @mousedown.prevent="form.leader = opt; leaderOpen = false"
                  >{{ opt }}</div>
                </div>
              </Transition>
            </div>
          </FancyInput>
          <FancyInput label="客户人数" v-model="form.customerCount" placeholder="客户到场人数" type="number" />
        </div>

        <div class="eh-new__grid eh-new__grid--2col">
          <FancyInput label="所属区县/部门" required>
            <div class="eh-leader-combo">
              <input
                v-model="form.district"
                class="eh-input__field"
                placeholder="请输入内部部门名称"
                autocomplete="off"
                @focus="districtOpen = true"
                @blur="districtOpen = false"
              />
              <Transition name="eh-leader-drop">
                <div v-if="districtOpen && districts.length" class="eh-leader-combo__drop">
                  <div
                    v-for="opt in districts"
                    :key="opt"
                    class="eh-leader-combo__opt"
                    @mousedown.prevent="form.district = opt; districtOpen = false"
                  >
                    {{ opt }}
                  </div>
                </div>
              </Transition>
            </div>
          </FancyInput>
          <FancyInput label="自有人员人数" v-model="form.internalCount" placeholder="我方到场人数" type="number" />
        </div>
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
        <FancyInput label="备注" type="textarea" v-model="form.remark" placeholder="其他特殊需求或补充说明" />
      </div>

      <!-- Footer -->
      <div class="eh-new__footer">
        <Btn variant="ghost" icon="arrowLeft" @click="onPrev">{{ step === 1 ? '取消' : '上一步' }}</Btn>
        <div class="eh-new__actions">
          <Btn v-if="step < 2 && !isEdit" variant="ghost" @click="onSaveDraft">存草稿</Btn>
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
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { FancyInput, FancySelect, Btn, Ic, StepBar } from '../../components/eh';
import { INDUSTRIES } from '../../mock/applications';
import request from '../../api/request';
import { fetchDaySchedules, saveDraftApplication, submitApplication, resubmitApplication, fetchApplication } from '../../api/eh/apply';

const router = useRouter();
const route = useRoute();

const editId = computed(() => route.params.id as string | undefined);
const isEdit = computed(() => !!editId.value);

const districts = ref<string[]>([]);
onMounted(async () => {
  const res = await request({ url: '/dict/type/dezhou_district', method: 'get' }).catch(() => null);
  districts.value = (Array.isArray(res) ? res : []).map((item: any) => item.label as string);

  if (isEdit.value) {
    const app = await fetchApplication(editId.value!).catch(() => undefined);
    if (app) {
      const startDate = app.startTime?.split(' ')[0] || '';
      const meetingTime = app.startTime?.split(' ')[1]?.slice(0, 5) || '';

      Object.assign(form, {
        title: app.title,
        meetingNature: app.meetingNature || 'external',
        unit: app.unit,
        industry: app.industry || '',
        district: app.district || districts.value[0] || '',
        startDate,
        meetingTime,
        leader: app.leader === '无' ? '' : app.leader,
        customerCount: String(app.customerCount ?? app.headCount ?? ''),
        internalCount: String(app.internalCount ?? ''),
      });
    }
  }
});

const dateInputRef = ref<HTMLInputElement | null>(null);
const timeInputRef = ref<HTMLInputElement | null>(null);

const displayDateLabel = computed(() => form.startDate || '选择参观日期');
const displayTimeLabel = computed(() => form.meetingTime || '选择开始时间');

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

function openTimePicker() {
  const el = timeInputRef.value;
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
const leaderOpen = ref(false);
const districtOpen = ref(false);
const MEETING_TOPICS = ['党建和创', '方案交流', '合作伙伴', '其他'];
const MEETING_NATURE_OPTIONS = [
  { label: '内部', value: 'internal' },
  { label: '外部', value: 'external' },
];

const form = reactive({
  title: '',
  meetingNature: 'external',
  unit: '',
  industry: '',
  district: districts.value[0] ?? '',
  startDate: '',
  meetingTime: '',
  leader: '',
  customerCount: '',
  internalCount: '',
  remark: '',
});

const daySchedules = ref<string[]>([]);
const dayScheduleHint = computed(() => {
  if (!daySchedules.value.length) return '';
  return `当前已有排期 开始时间为${daySchedules.value.join('、')},仍可继续提交，建议先与负责人联系确认。`;
});

function formatScheduleHint(item: { startTime?: string; endTime?: string; title?: string }) {
  const start = String(item.startTime || '').replace('T', ' ').slice(11, 16);
  return start;
}

watch(
  () => [form.startDate, form.meetingTime] as const,
  async ([date]) => {
    if (!date) {
      daySchedules.value = [];
      return;
    }
    const schedules = await fetchDaySchedules(date).catch(() => []);
    daySchedules.value = schedules
      .filter((item) => !editId.value || item.id !== editId.value)
      .map(formatScheduleHint)
      .filter(Boolean);
  }
);

const summary = computed<[string, string][]>(() => [
  ['会议主题', form.title],
  ['会议性质', form.meetingNature === 'internal' ? '内部' : '外部'],
  ['来访单位', form.unit],
  ['所属行业', form.industry],
  ['参观日期', form.startDate],
  ['会议正式开始时间', form.meetingTime || '—'],
  ['我公司陪同领导', form.leader || '—'],
  ['客户人数', form.customerCount || '0'],
  ['自有人员人数', form.internalCount || '0'],
  ['所属区县/部门', form.district],
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
  return 'var(--t-accent)';
});
function notify(msg: string, type: Toast['type'] = 'success') {
  toast.value = { msg, type };
  setTimeout(() => (toast.value = null), 2600);
}

function onPrev() {
  if (step.value === 1) router.back();
  else step.value -= 1;
}
function onNext() {
  if (!form.title || !form.meetingNature || !form.unit || !form.startDate || !form.meetingTime || !form.district) {
    notify('请填写所有必填项', 'error');
    return;
  }
  step.value += 1;
}
async function onSaveDraft() {
  try {
    await saveDraftApplication({ ...form });
    notify('草稿已保存', 'info');
    setTimeout(() => router.replace('/mine'), 400);
  } catch (e) {
    notify('草稿保存失败，请重试', 'error');
  }
}
async function onSubmit() {
  submitting.value = true;
  try {
    if (isEdit.value) {
      await resubmitApplication(editId.value!, { ...form });
      notify(`申请「${form.title}」已重新提交，等待审批`, 'success');
    } else {
      await submitApplication({ ...form });
      notify(`申请「${form.title}」已提交，等待审批`, 'success');
    }
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
  border: 1px solid var(--t-border-dark);
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
  border: 1px solid var(--t-border-dark);
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
  border-color: var(--t-accent);
  box-shadow: 0 0 0 2px rgba(47, 103, 216, 0.12);
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
  font-size: 16px;
  font-weight: 400;
  color: var(--t-text1);
}
.eh-new__date-empty {
  font-size: 16px;
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
  border: 1px solid var(--t-border-dark);
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

/* 开始时间：规避原生 time 控件在桌面浏览器中的系统样式，视觉与其他表单项保持一致 */
.eh-new__time-field {
  position: relative;
  border: 1px solid var(--t-border-dark);
  border-radius: 4px;
  background: var(--t-surface);
  box-sizing: border-box;
  transition: border-color 0.15s ease;
  outline: none;
}
.eh-new__time-field:hover {
  border-color: var(--t-border-dark);
}
.eh-new__time-field:focus-within {
  border-color: var(--t-accent);
  box-shadow: 0 0 0 2px rgba(47, 103, 216, 0.12);
}
.eh-new__time-inner {
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
.eh-new__time-val {
  font-size: 16px;
  font-weight: 400;
  color: var(--t-text1);
}
.eh-new__time-empty {
  font-size: 16px;
  font-weight: 400;
  color: var(--t-text3);
}
.eh-new__time-icon-row {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  opacity: 0.92;
}
.eh-new__time-input {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  border: 0;
  opacity: 0;
  cursor: pointer;
  font-size: 16px;
  color: transparent;
  background: transparent;
  box-sizing: border-box;
  -webkit-appearance: none;
  appearance: none;
  z-index: 1;
}
.eh-new__time-input::-webkit-calendar-picker-indicator {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  opacity: 0;
  cursor: pointer;
}
.eh-new__time-input:focus {
  outline: none;
}

.eh-new__notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  border: 1px solid #f5d08a;
  background: #fff8eb;
  color: #92400e;
  border-radius: 6px;
  padding: 9px 10px;
  font-size: 12px;
  line-height: 1.5;
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
  outline: 2px solid var(--t-accent);
  outline-offset: 2px;
}
.eh-new__slot-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text1);
}
.eh-new__slot-range {
  font-size: 10px;
  color: var(--t-text2);
  white-space: nowrap;
}
.eh-new__slot--on {
  border-color: var(--t-accent);
  background: var(--t-accent);
}
.eh-new__slot--on .eh-new__slot-name,
.eh-new__slot--on .eh-new__slot-range {
  color: #f8fbff;
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
  color: #f8fbff;
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
  background: rgba(22, 39, 75, 0.32);
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
  box-shadow: rgba(47, 103, 216, 0.14) 0px 12px 32px;
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
  background: var(--t-accent);
  color: #f8fbff;
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
  box-shadow: 0 8px 32px rgba(47, 103, 216, 0.14);
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
.eh-toast[data-type='info']  { border-left-color: var(--t-accent); }

.eh-toast-enter-active,
.eh-toast-leave-active {
  transition: transform 0.2s, opacity 0.2s;
}
.eh-toast-enter-from,
.eh-toast-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}
.eh-leader-combo {
  position: relative;
}
.eh-leader-combo__drop {
  position: absolute;
  top: calc(100% + 2px);
  left: 0;
  right: 0;
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: 4px;
  box-shadow: 0 8px 24px rgba(47, 103, 216, 0.14);
  z-index: 100;
  overflow: hidden;
}
.eh-leader-combo__opt {
  padding: 10px 12px;
  font-size: 13px;
  color: var(--t-text1);
  cursor: pointer;
}
.eh-leader-combo__opt:active {
  background: var(--t-surface-warm);
}
.eh-leader-drop-enter-active {
  transition: opacity 0.18s ease, transform 0.18s cubic-bezier(0.4, 0, 0.2, 1);
}
.eh-leader-drop-leave-active {
  transition: opacity 0.12s ease, transform 0.12s ease;
}
.eh-leader-drop-enter-from,
.eh-leader-drop-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
