<template>
	<div class="eh-scope eh-cal">
		<div class="eh-cal__head">
			<div style="display:flex;align-items:center;gap:10px">
				<h2 class="eh-cal__title">排期日历</h2>
				<div style="display:flex;gap:2px">
					<button class="eh-cal__nav" @click="jump(-7)"><Ic n="chevronLeft" :size="13" /></button>
					<button class="eh-cal__nav" @click="jump(7)"><Ic n="chevronRight" :size="13" /></button>
					<div class="eh-cal__range">
						{{ dates[0].getMonth() + 1 }}月 {{ dates[0].getDate() }}日 — {{ dates[6].getDate() }}日
					</div>
				</div>
			</div>
			<div style="display:flex;gap:12px;align-items:center">
				<span class="eh-cal__legend"><span style="background:var(--t-success)" />已批准</span>
				<span class="eh-cal__legend"><span style="background:var(--t-warning)" />审批中</span>
				<Btn variant="outline" size="sm" icon="download" @click="onExport">导出</Btn>
			</div>
		</div>

		<Card :style="{ overflow: 'hidden', padding: 0 }">
			<div class="eh-cal__slide-wrap">
				<Transition :name="slideName">
					<div :key="weekKey" class="eh-cal__pane">
						<!-- 星期头 -->
						<div class="eh-cal__grid eh-cal__thead">
							<div class="eh-cal__gutter" />
							<div
								v-for="(d, i) in dates"
								:key="i"
								class="eh-cal__wd-cell"
								:class="{ 'eh-cal__wd-cell--today': isToday(d) }"
							>
								<MonoLabel :style="{ color: isToday(d) ? 'var(--t-accent)' : 'var(--t-text3)', display: 'block', marginBottom: '3px' }">{{ WD[i] }}</MonoLabel>
								<div class="eh-cal__wd-num" :class="{ 'eh-cal__wd-num--today': isToday(d) }">{{ d.getDate() }}</div>
							</div>
						</div>

						<!-- 时间槽 -->
						<div class="eh-cal__body">
							<div v-for="h in hours" :key="h" class="eh-cal__grid eh-cal__row">
								<div class="eh-cal__hour">{{ h }}:00</div>
								<div
									v-for="(d, i) in dates"
									:key="i"
									class="eh-cal__slot"
									:class="{ 'eh-cal__slot--today': isToday(d) }"
								>
									<div
										v-for="ev in slotEvents(d, h)"
										:key="ev.id"
										:class="['eh-cal__event', eventClass(ev)]"
										:style="eventStyle(ev)"
										@click="onEventClick(ev)"
									>
										<div class="eh-cal__event-title">{{ ev.title }}</div>
										<div class="eh-cal__event-sub">{{ eventSubTitle(ev) }}</div>
										<div v-if="eventSize(ev) === 'lg' || eventSize(ev) === 'xl'" class="eh-cal__event-meta">
											{{ ev.status === 'approved' ? '已批准' : '审批中' }}
										</div>
										<div class="eh-cal__event-time">{{ eventTimeText(ev) }}</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</Transition>
			</div>
		</Card>

	<!-- 申请详情弹窗 -->
	<el-dialog
		v-model="dlgVisible"
		width="660"
		:close-on-click-modal="true"
		:show-close="true"
		class="eh-scope eh-cal-dlg"
		destroy-on-close
	>
		<template #header>
			<div style="display:flex;align-items:center;justify-content:space-between;padding-right:8px">
				<div>
					<h3 style="margin:0 0 3px;font-size:17px;font-weight:700;color:var(--t-text1)">
						{{ dlgData?.subject || dlgData?.title || '申请详情' }}
					</h3>
					<MonoLabel v-if="dlgData?.id">EH-{{ dlgData.id }}</MonoLabel>
				</div>
				<Badge v-if="dlgData" :status="mapStatus(dlgData.status)" />
			</div>
		</template>

		<!-- 加载中 -->
		<div v-if="dlgLoading" style="display:flex;align-items:center;justify-content:center;padding:48px 0;gap:10px;color:var(--t-text3)">
			<Ic n="rotate" :size="16" color="var(--t-text3)" />
			<span style="font-size:13px">加载中…</span>
		</div>

		<template v-else-if="dlgData">
			<!-- KV 信息格 -->
			<div class="eh-cal-dlg__kv">
				<div v-for="r in dlgKvRows(dlgData)" :key="r.key" class="eh-cal-dlg__kv-cell">
					<Ic :n="r.icon" :size="12" color="var(--t-text3)" />
					<div>
						<MonoLabel :style="{ display:'block', marginBottom:'2px' }">{{ r.key }}</MonoLabel>
						<span style="font-size:13px;font-weight:600;color:var(--t-text1)">{{ r.value }}</span>
					</div>
				</div>
			</div>

			<!-- 简要议程 -->
			<div v-if="dlgData.agenda" class="eh-cal-dlg__section">
				<MonoLabel :style="{ display:'block', marginBottom:'4px' }">简要议程</MonoLabel>
				<p style="margin:0;font-size:13px;color:var(--t-text2);line-height:1.7">{{ dlgData.agenda }}</p>
			</div>

			<!-- 审批进度 -->
			<div class="eh-cal-dlg__section">
				<MonoLabel :style="{ display:'block', marginBottom:'10px' }">审批进度</MonoLabel>
				<ApprovalTimeline :nodes="dlgNodes(dlgData)" />
			</div>

			<div v-if="showOps(dlgData)" class="eh-cal-dlg__ops">
				<div>
					<MonoLabel :style="{ display:'block', marginBottom:'3px' }">下一步操作</MonoLabel>
					<span class="eh-cal-dlg__ops-hint">{{ opsHint(dlgData) }}</span>
				</div>
				<div class="eh-cal-dlg__ops-actions">
					<template v-if="mapStatus(dlgData.status) === 'pending' || mapStatus(dlgData.status) === 'rescheduled'">
						<Btn variant="success" size="sm" icon="check" v-auth="'eh_approval_node_edit'" @click="openApproval('approve')">审批通过</Btn>
						<Btn variant="danger" size="sm" icon="x" v-auth="'eh_approval_node_edit'" @click="openApproval('reject')">驳回</Btn>
					</template>
					<template v-else>
						<Btn variant="primary" size="sm" icon="camera" v-auth="'eh_visit_record_edit'" @click="openVisitRecord">录入/上传留存</Btn>
					</template>
				</div>
			</div>
		</template>
	</el-dialog>

	<el-dialog v-model="approvalVisible" :title="approvalTitle" width="460" :close-on-click-modal="false" class="eh-scope">
		<div style="display:flex;flex-direction:column;gap:14px">
			<div class="eh-cal-dlg__alert" :data-type="approvalAction === 'approve' ? 'success' : 'danger'">
				<Ic :n="approvalAction === 'approve' ? 'checkCircle' : 'alertTri'" :size="13" :color="approvalAction === 'approve' ? 'var(--t-success)' : 'var(--t-danger)'" />
				<span>{{ approvalAction === 'approve' ? '通过后申请将直接完成审批，申请人收到站内通知。' : '驳回后申请人可查看原因并重新提交。' }}</span>
			</div>
			<FancyInput :label="approvalAction === 'reject' ? '驳回原因' : '审批意见'" :required="approvalAction === 'reject'">
				<textarea v-model="approvalComment" :placeholder="approvalAction === 'approve' ? '审批意见（选填）' : '请说明原因（必填）'" class="eh-input__field eh-input__field--textarea" rows="4" />
			</FancyInput>
		</div>
		<template #footer>
			<Btn variant="ghost" @click="approvalVisible = false" style="margin-right:8px">取消</Btn>
			<Btn
				:variant="approvalAction === 'approve' ? 'success' : 'danger'"
				:disabled="approvalAction === 'reject' && !approvalComment.trim()"
				@click="submitApproval"
			>
				{{ approvalAction === 'approve' ? '确认通过' : '确认驳回' }}
			</Btn>
		</template>
	</el-dialog>

	<el-dialog v-model="recordVisible" title="参观留存录入" width="560" :close-on-click-modal="false" class="eh-scope">
		<div v-if="dlgData" style="display:flex;flex-direction:column;gap:14px">
			<div class="eh-cal-dlg__record-head">
				<Ic n="building" :size="14" color="var(--t-text3)" />
				<div>
					<div style="font-size:13px;font-weight:700;color:var(--t-text1)">{{ dlgData.subject || dlgData.title }}</div>
					<div style="font-size:11px;color:var(--t-text3);margin-top:2px">{{ fmtDt(dlgData.startTime) }} · {{ dlgData.visitorCompany || dlgData.unit }}</div>
				</div>
			</div>
			<div style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
				<FancyInput label="实际到场人数" v-model="recordForm.headCount" :placeholder="`计划${dlgData.visitorCount || dlgData.headCount || 0}人`" type="number" required />
				<FancyInput label="商机编码" v-model="recordForm.oppCode" placeholder="填「无」可后续补录" hint="录入后自动生成商机线索" />
				<FancyInput label="实际开始时间" type="datetime" v-model="recordForm.actualStart" />
				<FancyInput label="实际结束时间" type="datetime" v-model="recordForm.actualEnd" />
			</div>
			<div>
				<MonoLabel :style="{ display: 'block', marginBottom: '8px' }">现场照片</MonoLabel>
				<div class="eh-cal-dlg__upload" @click="openRecordFilePicker">
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
			<Btn variant="primary" icon="check" @click="submitVisitRecord">确认留存</Btn>
		</template>
	</el-dialog>

	<input ref="recordFileInputRef" type="file" accept="image/*" multiple style="display:none" @change="onRecordFileChange" />
	</div>
</template>

<script lang="ts" name="ehcalendar" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElDialog, ElMessage } from 'element-plus';
import { Card, Btn, Ic, MonoLabel, Badge, ApprovalTimeline, FancyInput } from '/@/components/eh';
import { fetchCalendar } from '/@/api/eh/calendar';
import { getObj, submitApprovalAction } from '/@/api/eh/apply';
import { fetchAggregateList as fetchVisitAggregateList, upsertRecord } from '/@/api/eh/visit';
import { saveBatchPhotos } from '/@/api/eh/visit-photo';
import { downBlobFile } from '/@/utils/other';
import request from '/@/utils/request';

interface CalEvent {
	id: string;
	title: string;
	unit?: string;
	district?: string;
	start: string;
	startTime: string;
	end: string;
	status: string;
}

const WD = ['日', '一', '二', '三', '四', '五', '六'];

const dlgVisible = ref(false);
const dlgLoading = ref(false);
const dlgData = ref<any>(null);
const approvalVisible = ref(false);
const approvalAction = ref<'approve' | 'reject'>('approve');
const approvalComment = ref('');
const recordVisible = ref(false);
const recordFileInputRef = ref<HTMLInputElement>();
const recordFiles = ref<File[]>([]);
const recordForm = reactive({ headCount: '' as string | number, oppCode: '', actualStart: '', actualEnd: '', notes: '' });

async function onEventClick(ev: CalEvent) {
	dlgVisible.value = true;
	dlgLoading.value = true;
	dlgData.value = null;
	try {
		const res = await getObj(ev.id);
		dlgData.value = res?.data ?? res ?? null;
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载详情失败');
		dlgVisible.value = false;
	} finally {
		dlgLoading.value = false;
	}
}

function fmtDt(v?: string) {
	if (!v) return '—';
	return String(v).replace('T', ' ').slice(0, 16);
}

function timePart(v?: string) {
	const time = fmtDt(v).split(' ')[1];
	return time && time !== '—' ? time : '—';
}

function dlgKvRows(d: any) {
	return [
		{ key: '来访单位', value: d.visitorCompany || d.unit || '—', icon: 'building' },
		{ key: '所属行业', value: d.industry || '—', icon: 'list' },
		{ key: '所属区县', value: d.district || '—', icon: 'mapPin' },
		{ key: '会议性质', value: d.meetingNature === 'internal' ? '内部' : '外部', icon: 'clipboard' },
		{ key: '参观日期', value: fmtDt(d.startTime).split(' ')[0], icon: 'calendar' },
		{ key: '会议正式开始时间', value: timePart(d.startTime), icon: 'clock' },
		{ key: '客户人数', value: `${d.customerCount ?? d.visitorCount ?? d.headCount ?? '—'} 人`, icon: 'users' },
		{ key: '自有人员人数', value: `${d.internalCount ?? 0} 人`, icon: 'users' },
		{ key: '我公司陪同领导', value: d.topLeaderTitle || d.leader || '—', icon: 'user' },
	];
}

function mapStatus(s?: string) {
	const m: Record<string, string> = { '0': 'draft', '1': 'pending', '2': 'approved', '3': 'rejected', '4': 'cancelled', '5': 'rescheduled' };
	return (m[s || ''] || s || 'pending') as any;
}

function mapNodeAction(n: any) {
	if (n?.action === 'agree' || n?.status === 'approved') return 'approved';
	if (n?.action === 'reject' || n?.status === 'rejected') return 'rejected';
	if (n?.status === 'pending') return 'pending';
	return 'waiting';
}

function dlgNodes(d: any) {
	return (d?.approvalNodes ?? []).map((n: any) => ({
		role: n.role || '审批节点',
		name: n.name || n.approver || '',
		action: mapNodeAction(n),
		time: n.time || n.actionTime ? fmtDt(n.time || n.actionTime) : null,
		comment: n.comment || n.opinion || '',
		id: n.id,
		status: n.status,
	}));
}

const approvalTitle = computed(() => (approvalAction.value === 'approve' ? '确认审批通过' : '驳回申请'));

function showOps(d: any) {
	const status = mapStatus(d?.status);
	return status === 'pending' || status === 'approved' || status === 'rescheduled';
}

function opsHint(d: any) {
	const status = mapStatus(d?.status);
	if (status === 'pending') return '该申请还在审批中，可直接处理当前审批节点。';
	return '该申请已进入可接待状态，可录入到场情况并上传现场照片。';
}

function openApproval(action: 'approve' | 'reject') {
	approvalAction.value = action;
	approvalComment.value = '';
	approvalVisible.value = true;
}

async function submitApproval() {
	if (!dlgData.value) return;
	try {
		const pendingNode = (dlgData.value.approvalNodes || []).find((n: any) => n.status === 'pending' || mapNodeAction(n) === 'pending');
		await submitApprovalAction({
			applyId: String(dlgData.value.id),
			nodeId: pendingNode?.id,
			action: approvalAction.value,
			comment: approvalComment.value,
		});
		approvalVisible.value = false;
		ElMessage.success(approvalAction.value === 'approve' ? '审批已通过' : approvalAction.value === 'reject' ? '申请已驳回' : '审批已转交');
		await refreshDialogData();
		await loadCalendar();
	} catch (e: any) {
		ElMessage.error(e?.msg || '审批失败');
	}
}

async function refreshDialogData() {
	if (!dlgData.value?.id) return;
	const res = await getObj(String(dlgData.value.id));
	dlgData.value = res?.data ?? res ?? null;
}

function toDateTimeString(value?: string) {
	if (!value) return '';
	const normalized = value.replace('T', ' ').trim();
	if (!normalized) return '';
	return normalized.length === 16 ? `${normalized}:00` : normalized;
}

async function findVisitRecord(applyId: string | number) {
	const res = await fetchVisitAggregateList();
	const rows = res?.data || [];
	return rows.find((item: any) => String(item.applyId) === String(applyId));
}

async function openVisitRecord() {
	if (!dlgData.value) return;
	try {
		const visit = await findVisitRecord(dlgData.value.id);
		recordForm.headCount = visit?.actualVisitCount ?? dlgData.value.visitorCount ?? dlgData.value.headCount ?? '';
		recordForm.oppCode = visit?.opportunityCode ?? dlgData.value.opportunityCode ?? '';
		recordForm.actualStart = toDateTimeString(fmtDt(visit?.actualVisitTime || dlgData.value.startTime));
		recordForm.actualEnd = toDateTimeString(fmtDt(visit?.actualVisitTime || dlgData.value.endTime));
		recordForm.notes = '';
		recordFiles.value = [];
		recordVisible.value = true;
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载留存信息失败');
	}
}

async function submitVisitRecord() {
	if (!dlgData.value) return;
	try {
		const applyId = String(dlgData.value.id);
		const existing = await findVisitRecord(applyId);
		await upsertRecord({
			id: existing?.visitId,
			applyId,
			actualVisitCount: Number(recordForm.headCount || 0),
			opportunityCode: recordForm.oppCode,
			actualVisitTime: toDateTimeString(recordForm.actualStart),
			remark: recordForm.notes,
		});
		const latest = await findVisitRecord(applyId);
		const visitId = latest?.visitId || existing?.visitId;
		if (visitId && recordFiles.value.length > 0) {
			const photos = [];
			for (const file of recordFiles.value) {
				photos.push(await uploadFile(file));
			}
			await saveBatchPhotos({ visitId, photos });
		}
		recordVisible.value = false;
		ElMessage.success('参观留存已提交');
		await refreshDialogData();
		await loadCalendar();
	} catch (e: any) {
		ElMessage.error(e?.msg || '提交留存失败');
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
const cur = ref(new Date());
const events = ref<CalEvent[]>([]);
const slideDir = ref<'left' | 'right'>('left');

const dates = computed(() => {
	const d = cur.value;
	const s = new Date(d);
	s.setDate(d.getDate() - d.getDay());
	return Array.from({ length: 7 }, (_, i) => {
		const x = new Date(s);
		x.setDate(s.getDate() + i);
		return x;
	});
});
const weekKey = computed(() => ds(dates.value[0]));
const slideName = computed(() => (slideDir.value === 'left' ? 'eh-slide-left' : 'eh-slide-right'));

const hours = Array.from({ length: 11 }, (_, i) => i + 8);
const HOUR_ROW_HEIGHT = 52;

onMounted(() => {
	loadCalendar();
});

function ds(d: Date) {
	const y = d.getFullYear();
	const m = String(d.getMonth() + 1).padStart(2, '0');
	const day = String(d.getDate()).padStart(2, '0');
	return `${y}-${m}-${day}`;
}
function isToday(d: Date) {
	return ds(d) === ds(new Date());
}
function jump(delta: number) {
	slideDir.value = delta > 0 ? 'left' : 'right';
	const n = new Date(cur.value);
	n.setDate(cur.value.getDate() + delta);
	cur.value = n;
	loadCalendar();
}
function slotEvents(d: Date, h: number) {
	return events.value.filter((e) => e.start === ds(d) && parseInt(e.startTime.split(':')[0], 10) === h);
}
function toMinutes(time: string) {
	const [hh = '0', mm = '0'] = time.split(':');
	return parseInt(hh, 10) * 60 + parseInt(mm, 10);
}
function eventDurationMinutes(ev: CalEvent) {
	const startMinute = toMinutes(ev.startTime);
	const endMinute = toMinutes(ev.end);
	const duration = endMinute - startMinute;
	return duration <= 0 ? 60 : Math.max(30, duration);
}

function eventSubTitle(ev: CalEvent) {
	const parts = [ev.district, ev.unit].filter((item) => item && item !== '-');
	return parts.length ? parts.join(' · ') : '';
}
function eventTimeText(ev: CalEvent) {
	return ev.end && ev.end !== ev.startTime ? `${ev.startTime} — ${ev.end}` : ev.startTime;
}
function eventSize(ev: CalEvent) {
	const duration = eventDurationMinutes(ev);
	if (duration <= 60) return 'sm';
	if (duration <= 120) return 'md';
	if (duration <= 180) return 'lg';
	return 'xl';
}
function eventClass(ev: CalEvent) {
	return `eh-cal__event--${eventSize(ev)}`;
}
function eventStyle(ev: CalEvent) {
	const startMinute = toMinutes(ev.startTime);
	const duration = eventDurationMinutes(ev);
	const minuteInHour = startMinute % 60;
	const pxPerMinute = HOUR_ROW_HEIGHT / 60;
	const approved = ev.status === 'approved';
	return {
		background: approved
			? 'linear-gradient(180deg, #2f8d2f 0%, #236f23 100%)'
			: 'linear-gradient(180deg, #d18a1a 0%, #b87710 100%)',
		border: approved ? '1px solid rgba(28, 83, 28, 0.5)' : '1px solid rgba(132, 86, 15, 0.5)',
		top: `${minuteInHour * pxPerMinute + 2}px`,
		height: `${Math.max(44, duration * pxPerMinute - 4)}px`
	};
}
function onExport() {
	downBlobFile('/admin/eh/apply/calendar/export', currentRange(), '排期日历.xlsx');
}

async function loadCalendar() {
	try {
		const res = await fetchCalendar(currentRange());
		events.value = res.data || [];
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载日历失败');
	}
}

function currentRange() {
	return {
		start: ds(dates.value[0]),
		end: ds(dates.value[6]),
	};
}
</script>

<style scoped>
.eh-cal {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-cal__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 14px;
	flex-wrap: wrap;
	gap: 12px;
}
.eh-cal__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-cal__nav {
	border: 1px solid var(--t-border);
	background: var(--t-surface);
	border-radius: 6px;
	width: 28px;
	height: 28px;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	color: var(--t-text2);
	transition: all 0.2s ease;
}
.eh-cal__nav:hover {
	background: var(--t-accent-light);
	color: var(--t-accent);
	border-color: var(--t-accent-border);
}
.eh-cal__range {
	margin: 0 4px;
	padding: 5px 12px;
	background: #fff;
	border: 1px solid var(--t-border);
	border-radius: 6px;
	font-size: 12px;
	font-weight: 600;
	color: var(--t-text1);
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.eh-cal__legend {
	display: inline-flex;
	align-items: center;
	gap: 6px;
	font-size: 12px;
	color: var(--t-text2);
}
.eh-cal__legend span {
	width: 9px;
	height: 9px;
	border-radius: 999px;
	display: inline-block;
	box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}
.eh-cal__body {
	max-height: 540px;
	overflow-x: hidden;
	overflow-y: scroll;
	scrollbar-gutter: stable;
	background: #fff;
}
.eh-cal__body::-webkit-scrollbar {
	width: 8px;
}
.eh-cal__body::-webkit-scrollbar-thumb {
	background: var(--t-border-dark);
	border-radius: 999px;
}
.eh-cal__body::-webkit-scrollbar-track {
	background: var(--t-bg);
}

.eh-cal__grid {
	display: grid;
	grid-template-columns: 56px repeat(7, minmax(0, 1fr));
}
.eh-cal__thead {
	background: var(--t-surface-warm);
	border-bottom: 2px solid var(--t-border);
	box-sizing: border-box;
	padding-right: 8px;
}
.eh-cal__gutter {
	border-right: 1px solid var(--t-border);
}
.eh-cal__wd-cell {
	padding: 10px 6px;
	text-align: center;
	border-left: 1px solid var(--t-border);
}
.eh-cal__wd-cell--today {
	background: linear-gradient(180deg, #deeeff 0%, #f0f8ff 100%);
}
.eh-cal__wd-num {
	width: 30px;
	height: 30px;
	border-radius: 50%;
	margin: 0 auto;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 14px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-cal__wd-num--today {
	background: linear-gradient(180deg, var(--t-accent) 0%, var(--t-accent-strong) 100%);
	color: #fff;
}
.eh-cal__row {
	height: 52px;
	border-bottom: 1px solid var(--t-border);
}
.eh-cal__hour {
	padding: 6px 8px;
	font-size: 11px;
	color: var(--t-text3);
	text-align: right;
	background: var(--t-surface-warm);
	border-right: 1px solid var(--t-border);
	padding-top: 9px;
	font-family: var(--t-font-mono);
	letter-spacing: 0.2px;
}
.eh-cal__slot {
	border-left: 1px solid var(--t-border);
	background:
		linear-gradient(to bottom, transparent calc(50% - 0.5px), rgba(180, 210, 250, 0.35) calc(50% - 0.5px), rgba(180, 210, 250, 0.35) calc(50% + 0.5px), transparent calc(50% + 0.5px));
	padding: 0;
	position: relative;
	overflow: visible;
}
.eh-cal__slot--today {
	background:
		linear-gradient(to bottom, transparent calc(50% - 0.5px), rgba(150, 195, 255, 0.5) calc(50% - 0.5px), rgba(150, 195, 255, 0.5) calc(50% + 0.5px), transparent calc(50% + 0.5px)),
		linear-gradient(180deg, #edf6ff 0%, #f0f8ff 100%);
}
.eh-cal__event {
	color: #fff;
	border-radius: 8px;
	padding: 7px 9px;
	font-size: 11px;
	font-weight: 600;
	cursor: pointer;
	position: absolute;
	left: 3px;
	right: 3px;
	z-index: 2;
	box-shadow: 0 4px 10px rgba(0, 0, 0, 0.14);
	overflow: hidden;
	transition: transform 0.15s ease, box-shadow 0.15s ease;
	display: flex;
	flex-direction: column;
	gap: 3px;
}
.eh-cal__event:hover {
	transform: translateY(-1px);
	box-shadow: 0 8px 16px rgba(0, 0, 0, 0.18);
}
.eh-cal__event--sm {
	padding: 6px 9px;
	justify-content: center;
	border-radius: 6px;
}
.eh-cal__event--sm .eh-cal__event-time {
	display: none;
}
.eh-cal__event--sm .eh-cal__event-title {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	line-height: 1.2;
}
.eh-cal__event--sm .eh-cal__event-sub {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}
.eh-cal__event--md {
	justify-content: flex-start;
}
.eh-cal__event--md .eh-cal__event-title {
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}
.eh-cal__event--lg,
.eh-cal__event--xl {
	justify-content: space-between;
}
.eh-cal__event--lg .eh-cal__event-title,
.eh-cal__event--xl .eh-cal__event-title {
	white-space: normal;
	overflow: visible;
	display: block;
}
.eh-cal__event--lg .eh-cal__event-time,
.eh-cal__event--xl .eh-cal__event-time {
	align-self: flex-start;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 999px;
	padding: 2px 8px;
}
.eh-cal__event--xl {
	padding-top: 9px;
	padding-bottom: 9px;
}
.eh-cal__event--xl::after {
	content: '';
	position: absolute;
	inset: 0;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.14) 0%, rgba(255, 255, 255, 0) 55%);
	pointer-events: none;
}
.eh-cal__event-title {
	white-space: normal;
	overflow-wrap: anywhere;
	word-break: break-word;
	line-height: 1.35;
	letter-spacing: 0.1px;
	text-shadow: 0 1px 1px rgba(0, 0, 0, 0.16);
}
.eh-cal__event-sub {
	font-size: 10px;
	font-weight: 500;
	line-height: 1.25;
	opacity: 0.88;
	text-shadow: 0 1px 1px rgba(0, 0, 0, 0.14);
	overflow: hidden;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}
.eh-cal__event-meta {
	font-size: 10px;
	font-weight: 500;
	opacity: 0.95;
	padding: 1px 7px;
	width: fit-content;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.18);
}
.eh-cal__event-time {
	font-size: 10px;
	opacity: 0.95;
	margin-top: 3px;
	font-family: var(--t-font-mono);
	letter-spacing: 0.2px;
}

/* 滑动容器：用 grid 叠层 + overflow 裁切，两个面板同时运动 */
.eh-cal__slide-wrap {
	display: grid;
	overflow: hidden;
}
.eh-cal__pane {
	grid-area: 1 / 1;
	min-width: 0;
	will-change: transform;
}

/* 向左滑（下一周）：旧面板向左飞出，新面板从右进入 */
.eh-slide-left-enter-active,
.eh-slide-left-leave-active {
	transition: transform 0.36s cubic-bezier(0.4, 0, 0.2, 1);
}
.eh-slide-left-enter-from  { transform: translateX(100%); }
.eh-slide-left-enter-to    { transform: translateX(0); }
.eh-slide-left-leave-from  { transform: translateX(0); }
.eh-slide-left-leave-to    { transform: translateX(-100%); }

/* 向右滑（上一周）：旧面板向右飞出，新面板从左进入 */
.eh-slide-right-enter-active,
.eh-slide-right-leave-active {
	transition: transform 0.36s cubic-bezier(0.4, 0, 0.2, 1);
}
.eh-slide-right-enter-from { transform: translateX(-100%); }
.eh-slide-right-enter-to   { transform: translateX(0); }
.eh-slide-right-leave-from { transform: translateX(0); }
.eh-slide-right-leave-to   { transform: translateX(100%); }

/* 详情弹窗 */
:deep(.eh-cal-dlg .el-dialog__header) {
	padding: 18px 22px 14px;
	border-bottom: 1px solid var(--t-border);
	margin-right: 0;
}
:deep(.eh-cal-dlg .el-dialog__body) {
	padding: 18px 22px 22px;
}
:deep(.eh-cal-dlg .el-dialog__headerbtn) {
	top: 16px;
	right: 16px;
}
.eh-cal-dlg__kv {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 8px;
	margin-bottom: 14px;
}
.eh-cal-dlg__kv-cell {
	background: var(--t-bg);
	border-radius: 6px;
	padding: 9px 12px;
	display: flex;
	gap: 8px;
	align-items: flex-start;
}
.eh-cal-dlg__section {
	background: var(--t-bg);
	border-radius: 6px;
	padding: 10px 14px;
	margin-bottom: 10px;
}
.eh-cal-dlg__ops {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12px;
	background: linear-gradient(180deg, #fff 0%, var(--t-surface-warm) 100%);
	border: 1px solid var(--t-border);
	border-radius: 8px;
	padding: 12px 14px;
	margin-top: 12px;
}
.eh-cal-dlg__ops-hint {
	color: var(--t-text3);
	font-size: 12px;
	line-height: 1.5;
}
.eh-cal-dlg__ops-actions {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	gap: 8px;
	flex-wrap: wrap;
}
.eh-cal-dlg__alert {
	display: flex;
	align-items: flex-start;
	gap: 8px;
	padding: 10px 12px;
	border-radius: 8px;
	font-size: 12px;
	line-height: 1.6;
}
.eh-cal-dlg__alert[data-type='success'] {
	background: var(--t-success-light);
	color: var(--t-success);
}
.eh-cal-dlg__alert[data-type='danger'] {
	background: var(--t-danger-light);
	color: var(--t-danger);
}
.eh-cal-dlg__alert[data-type='info'] {
	background: var(--t-accent-light);
	color: var(--t-accent);
}
.eh-cal-dlg__record-head {
	background: var(--t-bg);
	border-radius: 8px;
	padding: 10px 14px;
	display: flex;
	gap: 9px;
	border: 1px solid var(--t-border);
}
.eh-cal-dlg__upload {
	border: 1px dashed var(--t-border-dark);
	border-radius: 8px;
	padding: 18px 16px;
	text-align: center;
	background: #fff;
	cursor: pointer;
	transition: all 0.15s ease;
}
.eh-cal-dlg__upload:hover {
	border-color: var(--t-accent);
	background: var(--t-accent-light);
}
@media (max-width: 760px) {
	.eh-cal-dlg__kv {
		grid-template-columns: 1fr;
	}
	.eh-cal-dlg__ops {
		align-items: flex-start;
		flex-direction: column;
	}
	.eh-cal-dlg__ops-actions {
		justify-content: flex-start;
	}
}
</style>
