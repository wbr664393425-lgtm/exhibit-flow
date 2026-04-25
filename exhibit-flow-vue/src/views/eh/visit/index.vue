<template>
	<div class="eh-scope eh-visit">
		<div class="eh-visit__head">
			<div>
				<h2 class="eh-visit__title">参观留存</h2>
				<p class="eh-visit__sub">录入实际到场情况、商机编码与现场照片</p>
			</div>
		</div>

		<div class="eh-visit__list">
			<Card v-for="app in apps" :key="app.id" :style="{ padding: '16px 18px' }">
				<div style="display:flex;align-items:center;gap:12px">
					<div
						:style="{
							width: '36px',
							height: '36px',
							borderRadius: '6px',
							background: app.status === 'completed' ? 'var(--t-success-light)' : 'var(--t-bg)',
							border: `1px solid ${app.status === 'completed' ? '#2c641533' : 'var(--t-border)'}`,
							display: 'flex',
							alignItems: 'center',
							justifyContent: 'center',
							flexShrink: 0,
						}"
					>
						<Ic :n="app.status === 'completed' ? 'checkCircle' : 'camera'" :size="17" :color="app.status === 'completed' ? 'var(--t-success)' : 'var(--t-text2)'" />
					</div>
					<div style="flex:1;min-width:0">
						<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px;flex-wrap:wrap">
							<span style="font-size:14px;font-weight:700;color:var(--t-text1)">{{ app.title }}</span>
							<Badge :status="app.status" />
							<span v-if="app.visitRecord?.returnSigned" class="eh-visit__ret-sign">
								<Ic n="check" :size="9" color="var(--t-success)" />已归还
							</span>
							<span v-if="app.visitRecord && !app.visitRecord.returnSigned" class="eh-visit__ret-pending">
								<Ic n="clock" :size="9" color="var(--t-warning)" />待归还签收
							</span>
						</div>
						<div class="eh-visit__metas">
							<span><Ic n="calendar" :size="11" />{{ app.startTime }}</span>
							<span><Ic n="building" :size="11" />{{ app.unit }}</span>
							<span><Ic n="users" :size="11" />计划 {{ app.headCount }} 人</span>
							<span v-if="app.opportunityCode"><Ic n="briefcase" :size="11" />{{ app.opportunityCode }}</span>
						</div>
					</div>
					<div style="display:flex;gap:6px;flex-shrink:0">
						<Btn
							v-if="app.visitRecord && !app.visitRecord.returnSigned"
							variant="outline"
							size="sm"
							icon="checkSquare"
							v-auth="'eh_visit_return'"
							@click="openReturn(app)"
						>归还签收</Btn>
						<Btn
							:variant="app.visitRecord ? 'ghost' : 'primary'"
							size="sm"
							:icon="app.visitRecord ? 'edit' : 'plus'"
							v-auth="app.visitRecord ? 'eh_visit_edit' : 'eh_visit_record'"
							@click="openRecord(app)"
						>{{ app.visitRecord ? '编辑' : '录入留存' }}</Btn>
					</div>
				</div>
			</Card>
			<div v-if="apps.length === 0" class="eh-visit__empty">
				<Ic n="camera" :size="44" color="var(--t-text3)" />
				<p>暂无待留存参观</p>
			</div>
		</div>

		<!-- 录入留存弹层 -->
		<el-dialog v-model="recordVisible" title="参观留存录入" width="560" :close-on-click-modal="false" class="eh-scope">
			<div v-if="sel" style="display:flex;flex-direction:column;gap:14px">
				<div style="background:var(--t-bg);border-radius:6px;padding:10px 14px;display:flex;gap:9px">
					<Ic n="building" :size="14" color="var(--t-text3)" />
					<div>
						<div style="font-size:13px;font-weight:700;color:var(--t-text1)">{{ sel.title }}</div>
						<div style="font-size:11px;color:var(--t-text3);margin-top:2px">{{ sel.startTime }} · {{ sel.unit }}</div>
					</div>
				</div>
				<div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
					<FancyInput label="实际到场人数" v-model="recordForm.headCount" :placeholder="`计划${sel.headCount}人`" type="number" required />
					<FancyInput label="商机编码" v-model="recordForm.oppCode" placeholder="填「无」可后续补录" hint="允许事后补录" />
					<FancyInput label="实际开始时间" v-model="recordForm.actualStart" />
					<FancyInput label="实际结束时间" v-model="recordForm.actualEnd" />
				</div>
				<div>
					<MonoLabel :style="{ display: 'block', marginBottom: '8px' }">现场照片</MonoLabel>
					<div class="eh-visit__upload" @click="openRecordFilePicker">
						<Ic n="upload" :size="22" color="var(--t-text2)" />
						<div style="font-size:12px;color:var(--t-text2);font-weight:500;margin-top:6px">点击上传或拍摄照片，支持多张</div>
						<MonoLabel :style="{ marginTop: '4px' }">JPG / PNG · 单张 ≤ 10 MB</MonoLabel>
					</div>
					<div v-if="recordFiles.length" style="margin-top:8px;display:flex;flex-direction:column;gap:4px">
						<MonoLabel v-for="file in recordFiles" :key="file.name + file.size">{{ file.name }} · {{ Math.round(file.size / 1024) }}KB</MonoLabel>
					</div>
				</div>
				<FancyInput label="备注" type="textarea" v-model="recordForm.notes" placeholder="参观情况说明、客户反馈等" />
			</div>
			<template #footer>
				<Btn variant="ghost" @click="recordVisible = false" style="margin-right:8px">取消</Btn>
				<Btn variant="primary" icon="check" @click="submitRecord">确认留存</Btn>
			</template>
		</el-dialog>

		<!-- 归还签收弹层 -->
		<el-dialog v-model="returnVisible" title="归还签收" width="440" :close-on-click-modal="false" class="eh-scope">
			<div v-if="sel" style="display:flex;flex-direction:column;gap:14px">
				<div style="background:var(--t-bg);border-radius:6px;padding:10px 14px;font-size:12px">
					<b style="color:var(--t-text1)">{{ sel.title }}</b>
					<div style="color:var(--t-text3);margin-top:2px">{{ sel.startTime }} — {{ sel.endTime.split(' ')[1] }}</div>
				</div>
				<div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
					<FancyInput label="签收人" v-model="returnForm.person" required />
					<FancyInput label="归还时间" v-model="returnForm.time" placeholder="HH:MM" required />
				</div>
				<div class="eh-visit__alert-ok">
					<Ic n="checkCircle" :size="13" color="var(--t-success)" />
					<span>签收后将记录归还时间，并通知申请人</span>
				</div>
			</div>
			<template #footer>
				<Btn variant="ghost" @click="returnVisible = false" style="margin-right:8px">取消</Btn>
				<Btn variant="success" icon="checkSquare" @click="submitReturn">确认签收</Btn>
			</template>
		</el-dialog>

		<input ref="recordFileInputRef" type="file" accept="image/*" multiple style="display:none" @change="onRecordFileChange" />
	</div>
</template>

<script lang="ts" name="ehvisit" setup>
import { onMounted, reactive, ref } from 'vue';
import { ElDialog, ElMessage } from 'element-plus';
import { Card, Btn, Badge, Ic, MonoLabel, FancyInput } from '/@/components/eh';
import { fetchAggregateList, submitReturnSign, upsertRecord } from '/@/api/eh/visit';
import { saveBatchPhotos } from '/@/api/eh/visit-photo';
import request from '/@/utils/request';
import type { Application } from '/@/components/eh/mock';

const apps = ref<Application[]>([]);
const sel = ref<Application | null>(null);
const recordVisible = ref(false);
const returnVisible = ref(false);
const recordFileInputRef = ref<HTMLInputElement>();
const recordFiles = ref<File[]>([]);

const recordForm = reactive({ headCount: '' as string | number, oppCode: '', actualStart: '', actualEnd: '', notes: '' });
const returnForm = reactive({ person: '', time: '' });

onMounted(() => {
	loadData();
});

function mapStatus(status?: string): Application['status'] {
	const map: Record<string, Application['status']> = {
		'2': 'approved',
		'5': 'rescheduled',
	};
	return map[status || ''] || 'approved';
}

function fmtTime(v?: string) {
	if (!v) return '';
	return v.replace('T', ' ').slice(0, 16);
}

function toDateTimeString(value?: string) {
	if (!value) return '';
	const normalized = value.replace('T', ' ').trim();
	if (!normalized) return '';
	return normalized.length === 16 ? `${normalized}:00` : normalized;
}

function toApp(raw: any): Application {
	const hasVisit = !!raw.visitId;
	return {
		id: `EH-${raw.applyId}`,
		title: raw.title || '-',
		unit: raw.unit || '-',
		industry: '-',
		district: '-',
		applicant: '-',
		phone: '-',
		dept: '-',
		startTime: fmtTime(raw.startTime) || '-- --',
		endTime: fmtTime(raw.endTime) || '-- --',
		leader: '-',
		visitors: [],
		services: [],
		status: hasVisit ? 'completed' : mapStatus(raw.status),
		approvalNodes: [],
		headCount: raw.headCount || 0,
		agenda: '',
		created: '',
		opportunityCode: raw.opportunityCode || '',
		visitRecord: hasVisit
			? {
					actualStart: fmtTime(raw.actualVisitTime) || fmtTime(raw.startTime),
					actualEnd: fmtTime(raw.actualVisitTime) || fmtTime(raw.endTime),
					actualHeadCount: raw.actualVisitCount || 0,
					photos: 0,
					returnSigned: !!raw.returnTime,
					returnPerson: raw.returnSigner || '',
					returnTime: fmtTime(raw.returnTime),
			  }
			: undefined,
		...( { visitRecordId: raw.visitId } as any),
	};
}

async function loadData() {
	try {
		const res = await fetchAggregateList();
		apps.value = (res.data || []).map(toApp);
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载参观留存失败');
	}
}

function openRecord(app: Application) {
	sel.value = app;
	recordForm.headCount = app.visitRecord?.actualHeadCount ?? '';
	recordForm.oppCode = app.opportunityCode ?? '';
	recordForm.actualStart = app.visitRecord?.actualStart ?? app.startTime;
	recordForm.actualEnd = app.visitRecord?.actualEnd ?? app.endTime;
	recordForm.notes = '';
	recordFiles.value = [];
	recordVisible.value = true;
}
function openReturn(app: Application) {
	sel.value = app;
	returnForm.person = app.visitRecord?.returnPerson || '';
	returnForm.time = app.visitRecord?.returnTime?.slice(11, 16) || '';
	returnVisible.value = true;
}
async function submitRecord() {
	if (!sel.value) return;
	try {
		await upsertRecord({
			applyId: sel.value.id.replace('EH-', ''),
			actualVisitCount: Number(recordForm.headCount || 0),
			opportunityCode: recordForm.oppCode,
			actualVisitTime: toDateTimeString(recordForm.actualStart),
			remark: recordForm.notes,
		});
		await loadData();
		const current = (apps.value as any[]).find((item) => item.id === sel.value?.id);
		const visitRecordId = current?.visitRecordId;
		if (visitRecordId && recordFiles.value.length > 0) {
			const photos = [];
			for (const file of recordFiles.value) {
				photos.push(await uploadFile(file));
			}
			await saveBatchPhotos({
				visitId: visitRecordId,
				photos,
			});
		}
		recordVisible.value = false;
		ElMessage.success('参观留存已提交');
		await loadData();
	} catch (e: any) {
		ElMessage.error(e?.msg || '提交失败');
	}
}
async function submitReturn() {
	if (!sel.value?.visitRecord) return;
	try {
		const hit = (apps.value as any[]).find((a) => a.id === sel.value?.id);
		const visitRecordId = hit?.visitRecordId;
		await submitReturnSign({
			visitRecordId,
			returnSigner: returnForm.person,
			returnTime: toDateTimeString(`${sel.value.startTime.split(' ')[0]} ${returnForm.time}`),
		});
		returnVisible.value = false;
		ElMessage.success('归还签收已记录');
		await loadData();
	} catch (e: any) {
		ElMessage.error(e?.msg || '签收失败');
	}
}

function onRecordFileChange(event: Event) {
	const input = event.target as HTMLInputElement;
	recordFiles.value = Array.from(input.files || []);
}

function openRecordFilePicker() {
	recordFileInputRef.value?.click();
}

async function uploadFile(file: File) {
	const formData = new FormData();
	formData.append('file', file);
	const res = await request({
		url: '/admin/sys-file/upload',
		method: 'post',
		data: formData,
		headers: {
			'Content-Type': 'multipart/form-data',
		},
	});
	return {
		fileUrl: res?.data?.url,
		fileName: file.name,
		fileSize: file.size,
	};
}
</script>

<style scoped>
.eh-visit {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-visit__head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 18px;
}
.eh-visit__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-visit__sub {
	margin: 3px 0 0;
	font-size: 12px;
	color: var(--t-text3);
}
.eh-visit__list {
	display: flex;
	flex-direction: column;
	gap: 8px;
}
.eh-visit__metas {
	display: flex;
	gap: 12px;
	font-size: 11px;
	color: var(--t-text3);
	flex-wrap: wrap;
}
.eh-visit__metas span {
	display: inline-flex;
	align-items: center;
	gap: 4px;
}
.eh-visit__ret-sign {
	display: inline-flex;
	align-items: center;
	gap: 3px;
	font-size: 10px;
	background: var(--t-success-light);
	color: var(--t-success);
	padding: 2px 7px;
	border-radius: 3px;
	font-weight: 600;
}
.eh-visit__ret-pending {
	display: inline-flex;
	align-items: center;
	gap: 3px;
	font-size: 10px;
	background: var(--t-warning-light);
	color: var(--t-warning);
	padding: 2px 7px;
	border-radius: 3px;
	font-weight: 600;
}
.eh-visit__empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 10px;
	padding: 52px 20px;
	color: var(--t-text3);
	font-size: 13px;
}
.eh-visit__upload {
	border: 2px dashed var(--t-border);
	border-radius: 6px;
	padding: 24px;
	text-align: center;
	cursor: pointer;
	background: var(--t-bg);
	transition: border-color 0.15s;
	display: flex;
	flex-direction: column;
	align-items: center;
}
.eh-visit__upload:hover {
	border-color: var(--t-text1);
}
.eh-visit__alert-ok {
	background: var(--t-success-light);
	border: 1px solid #2c641533;
	border-radius: 4px;
	padding: 10px 12px;
	font-size: 12px;
	color: var(--t-success);
	display: flex;
	gap: 7px;
	align-items: flex-start;
}
</style>
