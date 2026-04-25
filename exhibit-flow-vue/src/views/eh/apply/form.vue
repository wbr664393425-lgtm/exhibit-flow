<template>
	<el-dialog
		v-model="visible"
		:title="form.id ? '编辑申请' : '新建参观申请'"
		width="680"
		:close-on-click-modal="false"
		class="eh-scope eh-form-dlg"
	>
		<div style="display:flex;flex-direction:column;gap:14px">
			<StepBar :steps="STEPS" :cur="step" />

			<!-- 1 基本信息 -->
			<div v-if="step === 1" style="display:flex;flex-direction:column;gap:14px">
				<FancyInput label="会议主题" v-model="form.title" placeholder="本次参观的主题" required />
				<div class="eh-form-dlg__row2">
					<FancyInput label="来访单位" v-model="form.unit" placeholder="单位全称" required />
					<FancySelect label="所属行业" v-model="form.industry" :options="INDUSTRIES" />
				</div>
				<div class="eh-form-dlg__row2">
					<FancyInput label="参观日期" required>
						<input type="date" v-model="form.startDate" class="eh-input__field" />
					</FancyInput>
					<FancyInput label="使用时段" required>
						<div style="display:flex;align-items:center;gap:6px">
							<select v-model="form.startHour" class="eh-input__field" style="flex:1;text-align:center;appearance:none">
								<option v-for="h in startHours" :key="h" :value="h">{{ h }}:00</option>
							</select>
							<span style="color:var(--t-text3);font-size:11px">—</span>
							<select v-model="form.endHour" class="eh-input__field" style="flex:1;text-align:center;appearance:none">
								<option v-for="h in endHours" :key="h" :value="h">{{ h }}:00</option>
							</select>
						</div>
					</FancyInput>
				</div>
				<div class="eh-form-dlg__conflict" :data-state="conflictState">
					<Ic :n="conflictIcon" :size="14" :color="conflictColor" />
					<span style="flex:1;font-size:12px" :style="{ color: conflictColor }">{{ conflictText }}</span>
					<Btn variant="ghost" size="sm" :disabled="checking || !form.startDate" @click="doCheckConflict">
						{{ checking ? '检测中…' : '冲突检测' }}
					</Btn>
				</div>
				<div class="eh-form-dlg__row2">
					<FancySelect label="最高陪同领导" v-model="form.leader" :options="LEADERS" />
					<FancyInput label="预计人数" v-model="form.headCount" placeholder="总人数" type="number" />
				</div>
				<div class="eh-form-dlg__row2">
					<FancySelect label="所属区县" v-model="form.district" :options="DISTRICTS" required />
					<FancySelect label="申请部门" v-model="form.dept" :options="DEPTS" required />
				</div>
				<div class="eh-form-dlg__row2">
					<FancyInput label="申请人姓名" v-model="form.applicant" required />
					<FancyInput label="联系电话" v-model="form.phone" required />
				</div>
				<FancyInput label="简要议程" type="textarea" v-model="form.agenda" placeholder="本次参观主要议程和目的" />
			</div>

			<!-- 2 客户 -->
			<div v-else-if="step === 2" style="display:flex;flex-direction:column;gap:12px">
				<div style="display:flex;align-items:center;justify-content:space-between">
					<span style="font-size:13px;color:var(--t-text2)">每位来访客户单独填写一行</span>
					<Btn variant="outline" size="sm" icon="plus" @click="addVisitor">添加客户</Btn>
				</div>
				<div v-for="(v, i) in form.visitors" :key="i" class="eh-form-dlg__visitor">
					<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px">
						<MonoLabel :style="{ color: 'var(--t-accent)' }">客户 {{ i + 1 }}</MonoLabel>
						<button v-if="i > 0" class="eh-form-dlg__x" @click="removeVisitor(i)"><Ic n="x" :size="13" /></button>
					</div>
					<div class="eh-form-dlg__row2" style="margin-bottom:10px">
						<FancyInput label="来访单位" v-model="v.unit" placeholder="客户单位" required />
						<FancyInput label="姓名" v-model="v.name" placeholder="客户姓名" required />
						<FancyInput label="职务" v-model="v.title" placeholder="职务/职称" />
						<FancyInput label="是否战略客户">
							<div style="display:flex;gap:16px;padding-top:7px">
								<label style="display:flex;align-items:center;gap:5px;cursor:pointer;font-size:13px;color:var(--t-text1)">
									<input type="radio" :checked="v.isStrategic === true" @change="v.isStrategic = true" />是
								</label>
								<label style="display:flex;align-items:center;gap:5px;cursor:pointer;font-size:13px;color:var(--t-text1)">
									<input type="radio" :checked="v.isStrategic === false" @change="v.isStrategic = false" />否
								</label>
							</div>
						</FancyInput>
					</div>
					<FancySelect v-if="v.isStrategic" label="战客级别" v-model="v.strategicLevel" :options="['省直管战客', '市直管战客', '区县直管战客', '其他战客']" />
				</div>
			</div>

			<!-- 3 服务 -->
			<div v-else-if="step === 3" style="display:flex;flex-direction:column;gap:18px">
				<FancyInput label="附加服务（可多选）">
					<div style="display:flex;flex-wrap:wrap;gap:8px;padding-top:6px">
						<label
							v-for="s in SERVICES"
							:key="s"
							class="eh-form-dlg__svc"
							:class="{ 'eh-form-dlg__svc--on': form.services.includes(s) }"
							@click.prevent="toggleService(s)"
						>
							<span class="eh-form-dlg__svc-box" :class="{ 'eh-form-dlg__svc-box--on': form.services.includes(s) }">
								<Ic v-if="form.services.includes(s)" n="check" :size="9" color="#fff" />
							</span>
							{{ s }}
						</label>
					</div>
				</FancyInput>
				<div class="eh-form-dlg__hint">
					<Ic n="info" :size="14" color="var(--t-warning)" />
					<span><b>审批链：</b>{{ chainText }}</span>
				</div>
				<FancyInput label="备注" type="textarea" v-model="form.notes" placeholder="其他特殊需求或说明" />
			</div>

			<!-- 4 确认 -->
			<div v-else-if="step === 4" style="display:flex;flex-direction:column;gap:10px">
				<div class="eh-form-dlg__ok">
					<Ic n="checkCircle" :size="14" color="var(--t-success)" />
					<span>信息确认无误后点击提交，将自动发起审批流程</span>
				</div>
				<div style="border:1px solid var(--t-border);border-radius:6px;overflow:hidden">
					<div
						v-for="(r, i) in summary"
						:key="r[0]"
						:style="{ display: 'flex', gap: '14px', padding: '9px 14px', background: i % 2 === 0 ? 'var(--t-bg)' : 'var(--t-surface)', fontSize: '12px' }"
					>
						<span style="color:var(--t-text3);width:80px;flex-shrink:0;font-weight:500">{{ r[0] }}</span>
						<span style="color:var(--t-text1);font-weight:500">{{ r[1] }}</span>
					</div>
				</div>
			</div>
		</div>

		<template #footer>
			<div style="display:flex;justify-content:space-between;width:100%">
				<Btn variant="ghost" icon="arrowLeft" @click="onPrev">{{ step === 1 ? '取消' : '上一步' }}</Btn>
				<div style="display:flex;gap:8px">
					<Btn v-if="step < 4" variant="ghost" @click="onDraft">存草稿</Btn>
					<Btn v-if="step < 4" variant="primary" icon="chevronRight" @click="onNext">下一步</Btn>
					<Btn v-else variant="orange" icon="send" @click="onSubmit">提交申请</Btn>
				</div>
			</div>
		</template>
	</el-dialog>
</template>

<script setup lang="ts" name="ehapplyDialog">
import { computed, reactive, ref } from 'vue';
import { ElDialog, ElMessage } from 'element-plus';
import { FancyInput, FancySelect, Btn, MonoLabel, Ic, StepBar } from '/@/components/eh';
import { SERVICES, INDUSTRIES, DISTRICTS, LEADERS, DEPTS } from '/@/components/eh/mock';
import { checkConflict, getObj, saveDraft, submitApply, updateAndSubmit, updateDraft } from '/@/api/eh/apply';

const emit = defineEmits<{ (e: 'refresh'): void }>();

const STEPS = ['基本信息', '来访客户', '服务需求', '确认提交'];
const visible = ref(false);
const step = ref(1);
const checking = ref(false);
const saving = ref(false);

const form = reactive({
	id: '',
	title: '',
	unit: '',
	industry: INDUSTRIES[0],
	district: DISTRICTS[0],
	applicant: '李建国',
	phone: '13812345678',
	dept: DEPTS[0],
	startDate: '2026-05-06',
	startHour: '09',
	endHour: '11',
	leader: LEADERS[0],
	headCount: '',
	agenda: '',
	notes: '',
	services: [] as string[],
	visitors: [{ unit: '', name: '', title: '', isStrategic: false, strategicLevel: '' }],
	conflictChecked: false,
	conflictResult: null as string | null,
});

const startHours = Array.from({ length: 12 }, (_, i) => String(i + 8).padStart(2, '0'));
const endHours = Array.from({ length: 12 }, (_, i) => String(i + 9).padStart(2, '0'));

const conflictState = computed(() => (!form.conflictChecked ? 'idle' : form.conflictResult ? 'error' : 'ok'));
const conflictIcon = computed(() => (!form.conflictChecked ? 'search' : form.conflictResult ? 'alertTri' : 'checkCircle'));
const conflictColor = computed(() => (!form.conflictChecked ? 'var(--t-text3)' : form.conflictResult ? 'var(--t-danger)' : 'var(--t-success)'));
const conflictText = computed(() =>
	!form.conflictChecked ? '建议填写日期后检测时段冲突' : form.conflictResult ? `冲突：${form.conflictResult}` : '时段可用，无冲突'
);

function doCheckConflict() {
	if (!form.startDate || !form.startHour || !form.endHour) {
		ElMessage.warning('请先选择参观日期和时段');
		return;
	}
	checking.value = true;
	checkConflict({
		date: form.startDate,
		startHour: form.startHour,
		endHour: form.endHour,
	})
		.then((res: any) => {
			form.conflictResult = res?.data ?? null;
		})
		.catch((e: any) => {
			ElMessage.error(e?.msg || '冲突检测失败');
			form.conflictResult = null;
		})
		.finally(() => {
			form.conflictChecked = true;
			checking.value = false;
		});
}

function buildPayload() {
	return {
		title: form.title,
		unit: form.unit,
		industry: form.industry,
		district: form.district,
		applicant: form.applicant,
		phone: form.phone,
		dept: form.dept,
		startDate: form.startDate,
		startHour: form.startHour,
		endHour: form.endHour,
		leader: form.leader,
		headCount: form.headCount ? Number(form.headCount) : 0,
		agenda: form.agenda,
		remark: form.notes,
		services: [...form.services],
		visitors: form.visitors.map((item) => ({
			unit: item.unit,
			name: item.name,
			title: item.title,
			isStrategic: !!item.isStrategic,
			strategicLevel: item.strategicLevel,
		})),
	};
}

function resetForm() {
	Object.assign(form, {
		id: '',
		title: '',
		unit: '',
		industry: INDUSTRIES[0],
		district: DISTRICTS[0],
		applicant: '李建国',
		phone: '13812345678',
		dept: DEPTS[0],
		startDate: '2026-05-06',
		startHour: '09',
		endHour: '11',
		leader: LEADERS[0],
		headCount: '',
		agenda: '',
		notes: '',
		services: [],
		visitors: [{ unit: '', name: '', title: '', isStrategic: false, strategicLevel: '' }],
		conflictChecked: false,
		conflictResult: null,
	});
}

const chainText = computed(() => {
	if (form.leader === '总经理') return '部门领导 → 展厅主管 → 总经理（三级）';
	if (form.leader === '分管副总') return '部门领导 → 展厅主管 → 分管副总（三级）';
	return '部门领导 → 展厅主管（二级）';
});

function addVisitor() {
	form.visitors.push({ unit: '', name: '', title: '', isStrategic: false, strategicLevel: '' });
}
function removeVisitor(i: number) {
	form.visitors.splice(i, 1);
}
function toggleService(s: string) {
	const i = form.services.indexOf(s);
	if (i === -1) form.services.push(s);
	else form.services.splice(i, 1);
}

const summary = computed<[string, string][]>(() => [
	['会议主题', form.title],
	['来访单位', form.unit],
	['所属行业', form.industry],
	['参观日期', form.startDate],
	['使用时段', `${form.startHour}:00 — ${form.endHour}:00`],
	['最高领导', form.leader],
	['预计人数', form.headCount || '—'],
	['申请部门', form.dept],
	['申请人', `${form.applicant}（${form.district}）`],
	['联系电话', form.phone],
	['附加服务', form.services.join('、') || '无'],
	['来访客户', form.visitors.map((v) => `${v.name}/${v.unit}`).join('，')],
]);

async function openDialog(id?: string) {
	visible.value = true;
	step.value = 1;
	resetForm();
	form.id = id || '';
	if (!id) {
		return;
	}
	try {
		const res = await getObj(id);
		const raw = res?.data;
		if (!raw) {
			ElMessage.error('未找到申请详情');
			return;
		}
		Object.assign(form, {
			id: String(raw.id || ''),
			title: raw.subject || '',
			unit: raw.visitorCompany || '',
			industry: raw.industry || INDUSTRIES[0],
			district: raw.district || DISTRICTS[0],
			applicant: raw.applicant || '',
			phone: raw.phone || '',
			dept: raw.applicantDept || DEPTS[0],
			startDate: raw.startTime ? String(raw.startTime).slice(0, 10) : '',
			startHour: raw.startTime ? String(raw.startTime).slice(11, 13) : '09',
			endHour: raw.endTime ? String(raw.endTime).slice(11, 13) : '11',
			leader: raw.topLeaderTitle || LEADERS[0],
			headCount: raw.visitorCount || '',
			agenda: raw.agenda || '',
			notes: raw.remark || '',
			services: raw.extraServices ? String(raw.extraServices).split(',').filter(Boolean) : [],
			visitors:
				raw.visitors?.length > 0
					? raw.visitors.map((item: any) => ({
							unit: item.unit || item.visitorCompany || '',
							name: item.name || '',
							title: item.title || '',
							isStrategic: item.isKeyCustomer === '1',
							strategicLevel: item.keyCustomerLevel || '',
					  }))
					: [{ unit: '', name: '', title: '', isStrategic: false, strategicLevel: '' }],
			conflictChecked: false,
			conflictResult: null,
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
	if (step.value === 1 && (!form.title || !form.unit)) {
		ElMessage.error('请填写必填项');
		return;
	}
	if (step.value === 2 && !form.visitors.every((v) => v.name && v.unit)) {
		ElMessage.error('请完善客户信息');
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
		const payload = buildPayload();
		if (form.id) {
			await updateDraft(form.id, payload);
		} else {
			await saveDraft(payload);
		}
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
	if (!form.title || !form.unit || !form.startDate || !form.applicant || !form.phone) {
		ElMessage.error('请完善必填信息后再提交');
		return;
	}
	if (!form.visitors.every((v) => v.name && v.unit)) {
		ElMessage.error('请完善来访客户信息');
		return;
	}
	saving.value = true;
	try {
		const payload = buildPayload();
		if (form.id) {
			await updateAndSubmit(form.id, payload);
		} else {
			await submitApply(payload);
		}
		ElMessage.success(`申请「${form.title}」已提交，等待审批`);
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
.eh-form-dlg__row2 {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 14px;
}
.eh-form-dlg__conflict {
	display: flex;
	align-items: center;
	gap: 10px;
	padding: 10px 12px;
	border-radius: 4px;
	background: var(--t-bg);
	border: 1px solid var(--t-border);
}
.eh-form-dlg__conflict[data-state='error'] {
	background: var(--t-danger-light);
	border-color: #c41c1c44;
}
.eh-form-dlg__conflict[data-state='ok'] {
	background: var(--t-success-light);
	border-color: #2c641544;
}
.eh-form-dlg__visitor {
	background: var(--t-bg);
	border-radius: 6px;
	padding: 14px;
	border: 1px solid var(--t-border);
}
.eh-form-dlg__x {
	border: none;
	background: none;
	cursor: pointer;
	color: var(--t-text3);
	display: flex;
	padding: 4px;
}
.eh-form-dlg__svc {
	display: flex;
	align-items: center;
	gap: 7px;
	padding: 7px 14px;
	border-radius: 4px;
	cursor: pointer;
	border: 1px solid var(--t-border);
	background: var(--t-surface);
	color: var(--t-text2);
	font-size: 13px;
	transition: all 0.12s;
}
.eh-form-dlg__svc--on {
	border-color: var(--t-text1);
	background: var(--t-text1);
	color: #fff;
	font-weight: 600;
}
.eh-form-dlg__svc-box {
	width: 14px;
	height: 14px;
	border-radius: 3px;
	border: 1.5px solid var(--t-border);
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}
.eh-form-dlg__svc-box--on {
	background: var(--t-accent);
	border-color: #fff;
}
.eh-form-dlg__hint {
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
.eh-form-dlg__ok {
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
</style>
