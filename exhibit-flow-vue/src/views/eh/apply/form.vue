<template>
	<el-dialog
		v-model="visible"
		:title="form.id ? '编辑记录' : '新增记录'"
		width="680"
		:close-on-click-modal="false"
		class="eh-scope eh-form-dlg"
	>
		<div style="display:flex;flex-direction:column;gap:14px">
			<StepBar :steps="STEPS" :cur="step" />

			<!-- Step 1：基本信息 -->
			<div v-if="step === 1" class="eh-new__grid eh-new__grid--col">
				<FancySelect label="会议主题" v-model="form.title" :options="MEETING_TOPICS" required />

				<div class="eh-new__grid eh-new__grid--2col">
					<FancySelect label="会议性质" v-model="form.meetingNature" :options="MEETING_NATURE_OPTIONS" required />
					<FancyInput label="来访单位" v-model="form.unit" placeholder="单位全称" required />
				</div>

				<FancySelect label="所属行业" v-model="form.industry" :options="INDUSTRIES" />

				<FancyInput label="参观日期" required>
					<div
						class="eh-new__date-field"
						role="button"
						tabindex="0"
						@click="openDatePicker"
						@keydown.enter.prevent="openDatePicker"
						@keydown.space.prevent="openDatePicker"
					>
						<div class="eh-new__date-inner">
							<span :class="form.startDate ? 'eh-new__date-val' : 'eh-new__date-empty'">
								{{ form.startDate || '选择参观日期' }}
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
						@click="openTimePicker"
						@keydown.enter.prevent="openTimePicker"
						@keydown.space.prevent="openTimePicker"
					>
						<div class="eh-new__time-inner">
							<span :class="form.meetingTime ? 'eh-new__time-val' : 'eh-new__time-empty'">
								{{ form.meetingTime || '选择开始时间' }}
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

				<FancyInput label="议程内容" type="textarea" v-model="form.agenda" placeholder="补充本次会议议程" />
			</div>

			<!-- Step 2：确认提交 -->
			<div v-else-if="step === 2" class="eh-new__grid eh-new__grid--col">
				<div class="eh-new__success">
					<Ic n="checkCircle" :size="14" color="var(--t-success)" />
					<span>{{ form.id ? '信息确认无误后点击提交，将自动发起审批流程' : '信息确认无误后点击保存，将直接生成已完成记录' }}</span>
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
		</div>

		<template #footer>
			<div style="display:flex;justify-content:space-between;width:100%">
				<Btn variant="ghost" icon="arrowLeft" @click="onPrev">{{ step === 1 ? '取消' : '上一步' }}</Btn>
				<div style="display:flex;gap:8px">
					<Btn v-if="step < 2 && form.id" variant="ghost" @click="onDraft">存草稿</Btn>
					<Btn v-if="step < 2" variant="primary" icon="chevronRight" @click="onNext">下一步</Btn>
					<Btn v-else variant="orange" icon="send" :disabled="saving" @click="onSubmit">
						{{ saving ? '保存中…' : form.id ? '提交申请' : '保存记录' }}
					</Btn>
				</div>
			</div>
		</template>
	</el-dialog>
</template>

<script setup lang="ts" name="ehapplyDialog">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElDialog, ElMessage } from 'element-plus';
import { FancyInput, FancySelect, Btn, Ic, StepBar } from '/@/components/eh';
import { INDUSTRIES } from '/@/components/eh/mock';
import { getDicts } from '/@/api/admin/dict';
import { fetchAggregateList, getObj, saveCompleted, saveDraft, updateAndSubmit, updateDraft } from '/@/api/eh/apply';

const emit = defineEmits<{ (e: 'refresh'): void }>();

const STEPS = ['基本信息', '确认提交'];
const MEETING_TOPICS = ['党建和创', '方案交流', '合作伙伴', '其他'];
const MEETING_NATURE_OPTIONS = [
	{ label: '内部', value: 'internal' },
	{ label: '外部', value: 'external' },
];

const districts = ref<string[]>([]);
onMounted(async () => {
	const res = await getDicts('dezhou_district').catch(() => null);
	districts.value = (res?.data ?? []).map((item: any) => item.label as string);
});

const visible = ref(false);
const step = ref(1);
const saving = ref(false);
const leaderOpen = ref(false);
const districtOpen = ref(false);
const dateInputRef = ref<HTMLInputElement | null>(null);
const timeInputRef = ref<HTMLInputElement | null>(null);
const daySchedules = ref<string[]>([]);

const minSelectableDate = computed(() => {
	const t = new Date();
	return `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`;
});

const form = reactive({
	id: '',
	title: '',
	meetingNature: 'external',
	unit: '',
	industry: '',
	district: '',
	startDate: '',
	meetingTime: '',
	leader: '',
	customerCount: '',
	internalCount: '',
	agenda: '',
	remark: '',
});

const dayScheduleHint = computed(() => {
	if (!daySchedules.value.length) return '';
	return `当前已有排期 开始时间为${daySchedules.value.join('、')},仍可继续提交，建议先与负责人联系确认。`;
});

function formatScheduleHint(item: any) {
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
		try {
			const res: any = await fetchAggregateList();
			daySchedules.value = (res?.data || [])
				.filter((item: any) => String(item.startTime || '').slice(0, 10) === date && (!form.id || String(item.id) !== form.id))
				.map(formatScheduleHint)
				.filter(Boolean);
		} catch {
			daySchedules.value = [];
		}
	}
);

function openDatePicker() {
	const el = dateInputRef.value;
	if (!el) return;
	if (typeof el.showPicker === 'function') {
		try { el.showPicker(); return; } catch {}
	}
	el.focus();
	el.click();
}

function openTimePicker() {
	const el = timeInputRef.value;
	if (!el) return;
	if (typeof el.showPicker === 'function') {
		try { el.showPicker(); return; } catch {}
	}
	el.focus();
	el.click();
}

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
	['议程内容', form.agenda || '—'],
]);

function buildPayload() {
	return {
		title: form.title,
		meetingNature: form.meetingNature,
		unit: form.unit,
		industry: form.industry,
		district: form.district,
		startDate: form.startDate,
		meetingTime: form.meetingTime,
		startHour: form.meetingTime,
		endHour: form.meetingTime,
		leader: form.leader,
		customerCount: form.customerCount ? Number(form.customerCount) : 0,
		internalCount: form.internalCount ? Number(form.internalCount) : 0,
		headCount: Number(form.customerCount || 0) + Number(form.internalCount || 0),
		agenda: form.agenda,
		remark: form.remark,
		services: [],
		visitors: [],
	};
}

function resetForm() {
	Object.assign(form, {
		id: '', title: '', unit: '', industry: '',
		meetingNature: 'external', district: districts.value[0] ?? '',
		startDate: '', meetingTime: '',
		leader: '', customerCount: '', internalCount: '', agenda: '', remark: '',
	});
}

async function openDialog(id?: string) {
	visible.value = true;
	step.value = 1;
	resetForm();
	form.id = id || '';
	if (!id) return;
	try {
		const res = await getObj(id);
		const raw = res?.data;
		if (!raw) { ElMessage.error('未找到申请详情'); return; }
			Object.assign(form, {
				id: String(raw.id || ''),
				title: raw.subject || '',
				meetingNature: raw.meetingNature || 'external',
				unit: raw.visitorCompany || '',
				industry: raw.industry || '',
				district: raw.district || districts.value[0] || '',
				startDate: raw.startTime ? String(raw.startTime).slice(0, 10) : '',
				meetingTime: raw.startTime ? String(raw.startTime).replace('T', ' ').slice(11, 16) : '',
				leader: raw.topLeaderTitle || '',
				customerCount: raw.customerCount ?? raw.visitorCount ?? '',
				internalCount: raw.internalCount ?? '',
				agenda: raw.agenda || '',
				remark: raw.remark || '',
			});
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载申请详情失败');
	}
}

function onPrev() {
	if (step.value === 1) visible.value = false;
	else step.value -= 1;
}
function onNext() {
	if (!form.title || !form.meetingNature || !form.unit || !form.startDate || !form.meetingTime || !form.district) {
		ElMessage.error('请填写所有必填项');
		return;
	}
	step.value += 1;
}
async function onDraft() {
	if (!form.title || !form.unit || !form.startDate) {
		ElMessage.warning('请至少填写主题、来访单位和日期后再保存草稿');
		return;
	}
	saving.value = true;
	try {
		if (form.id) await updateDraft(form.id, buildPayload());
		else await saveDraft(buildPayload());
		ElMessage.success('草稿已保存');
		visible.value = false;
		emit('refresh');
	} catch (e: any) {
		ElMessage.error(e?.msg || '保存草稿失败');
	} finally {
		saving.value = false;
	}
}
async function onSubmit() {
	if (!form.title || !form.unit || !form.startDate || !form.meetingTime) {
		ElMessage.error('请完善必填信息后再提交');
		return;
	}
	saving.value = true;
	try {
		if (form.id) await updateAndSubmit(form.id, buildPayload());
		else await saveCompleted(buildPayload());
		ElMessage.success(form.id ? `申请「${form.title}」已提交，等待审批` : `记录「${form.title}」已保存为已完成`);
		visible.value = false;
		emit('refresh');
	} catch (e: any) {
		ElMessage.error(e?.msg || '提交申请失败');
	} finally {
		saving.value = false;
	}
}

defineExpose({ openDialog });
</script>

<style scoped>
.eh-new__grid { display: grid; gap: 14px; }
.eh-new__grid--col { grid-template-columns: 1fr; }
.eh-new__grid--2col { grid-template-columns: 1fr 1fr; }

.eh-new__date-field {
	position: relative;
	border: 1px solid var(--t-border-dark);
	border-radius: 4px;
	background: var(--t-surface);
	box-sizing: border-box;
	transition: border-color 0.15s ease;
	outline: none;
}
.eh-new__date-field:hover { border-color: var(--t-border-dark); }
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
.eh-new__date-val { font-size: 13px; font-weight: 400; color: var(--t-text1); }
.eh-new__date-empty { font-size: 13px; font-weight: 400; color: var(--t-text3); }
.eh-new__date-meta { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.eh-new__date-icon-row { display: flex; align-items: center; gap: 2px; flex-shrink: 0; opacity: 0.92; }
.eh-new__time-field {
	position: relative;
	border: 1px solid var(--t-border-dark);
	border-radius: 4px;
	background: var(--t-surface);
	box-sizing: border-box;
	transition: border-color 0.15s ease;
	outline: none;
}
.eh-new__time-field:hover { border-color: var(--t-border-dark); }
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
.eh-new__time-val { font-size: 13px; font-weight: 400; color: var(--t-text1); }
.eh-new__time-empty { font-size: 13px; font-weight: 400; color: var(--t-text3); }
.eh-new__time-icon-row { display: flex; align-items: center; gap: 2px; flex-shrink: 0; opacity: 0.92; }
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
	font-size: 16px;
	color: transparent;
	background: transparent;
	box-sizing: border-box;
	-webkit-appearance: none;
	appearance: none;
	z-index: 1;
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
.eh-new__date-input::-webkit-calendar-picker-indicator,
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
.eh-new__date-input:focus,
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

.eh-new__slots { display: flex; gap: 8px; }
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
	transition: border-color 0.15s, background 0.15s, color 0.15s;
}
.eh-new__slot-name { font-size: 13px; font-weight: 600; color: var(--t-text1); }
.eh-new__slot-range { font-size: 10px; color: var(--t-text3); white-space: nowrap; }
.eh-new__slot--on { border-color: var(--t-accent); background: var(--t-accent); }
.eh-new__slot--on .eh-new__slot-name,
.eh-new__slot--on .eh-new__slot-range { color: #fff; }
@keyframes border-pulse {
	0%, 100% { border-color: var(--t-accent); opacity: 1; }
	50% { border-color: var(--t-accent); opacity: 0.5; }
}
.eh-new__slot--checking { border-color: var(--t-accent); background: var(--t-accent); animation: border-pulse 1s ease-in-out infinite; }
.eh-new__slot--checking .eh-new__slot-name,
.eh-new__slot--checking .eh-new__slot-range { color: #fff; }
.eh-new__slot--ok { border-color: var(--t-success); background: var(--t-success); }
.eh-new__slot--ok .eh-new__slot-name,
.eh-new__slot--ok .eh-new__slot-range { color: #fff; }
.eh-new__slot--err { border-color: var(--t-danger); background: var(--t-danger); cursor: pointer; }
.eh-new__slot--err .eh-new__slot-name,
.eh-new__slot--err .eh-new__slot-range { color: #fff; }

.eh-new__modal-mask {
	position: fixed;
	inset: 0;
	background: rgba(22, 39, 75, 0.32);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 9999;
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
	box-shadow: rgba(0,0,0,0.05) 0px 4px 24px;
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
.eh-new__modal-title { font-size: 16px; font-weight: 700; color: var(--t-text1); }
.eh-new__modal-desc { font-size: 12px; color: var(--t-text2); text-align: center; line-height: 1.6; }
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
.eh-new__modal-detail-key { color: var(--t-text3); flex-shrink: 0; width: 52px; }
.eh-new__modal-detail-val { color: var(--t-text1); font-weight: 500; word-break: break-all; }
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

.eh-modal-enter-active, .eh-modal-leave-active { transition: opacity 0.2s; }
.eh-modal-enter-active .eh-new__modal, .eh-modal-leave-active .eh-new__modal { transition: transform 0.25s cubic-bezier(0.34,1.56,0.64,1), opacity 0.2s; }
.eh-modal-enter-from, .eh-modal-leave-to { opacity: 0; }
.eh-modal-enter-from .eh-new__modal, .eh-modal-leave-to .eh-new__modal { transform: scale(0.88); opacity: 0; }

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
.eh-new__summary { border: 1px solid var(--t-border); border-radius: 6px; overflow: hidden; }
.eh-new__summary-row { display: flex; gap: 14px; padding: 9px 14px; background: var(--t-surface); font-size: 12px; }
.eh-new__summary-row--alt { background: var(--t-bg); }
.eh-new__summary-key { color: var(--t-text3); width: 66px; flex-shrink: 0; font-weight: 500; }
.eh-new__summary-val { color: var(--t-text1); font-weight: 500; word-break: break-all; }
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
	box-shadow: 0 4px 12px rgba(0,0,0,0.08);
	z-index: 100;
	box-sizing: border-box;
	max-height: 188px;
	overflow-y: auto;
	overscroll-behavior: contain;
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
.eh-leader-combo__opt {
	padding: 8px 12px;
	font-size: 13px;
	color: var(--t-text1);
	cursor: pointer;
}
.eh-leader-combo__opt:hover {
	background: var(--t-bg);
}
</style>
