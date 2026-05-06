<template>
	<div class="eh-scope eh-aggr">
		<div class="eh-aggr__head">
			<div>
					<h2 class="eh-aggr__title">全部记录</h2>
					<p class="eh-aggr__sub">共 {{ apps.length }} 条记录</p>
				</div>
				<Btn variant="primary" icon="plus" v-auth="'eh_apply_add'" @click="onNew">新增记录</Btn>
		</div>

		<div class="eh-aggr__filters">
			<button
				v-for="f in filters"
				:key="f.key"
				class="eh-aggr__chip"
				:class="{ 'eh-aggr__chip--active': filter === f.key }"
				@click="filter = f.key"
			>
				{{ f.label }}
				<span class="eh-aggr__chip-cnt" :class="{ 'eh-aggr__chip-cnt--active': filter === f.key }">{{ f.count }}</span>
			</button>
		</div>

		<div class="eh-aggr__body">
			<!-- Left list -->
			<div class="eh-aggr__list">
				<Card
					v-for="a in shown"
					:key="a.id"
					clickable
					@click="sel = a"
					:style="{ padding: '14px 16px', borderColor: sel?.id === a.id ? 'var(--t-accent)' : 'var(--t-border)' }"
				>
					<div style="display:flex;align-items:flex-start;gap:12px">
						<div class="eh-aggr__avatar"><Ic n="building" :size="16" color="var(--t-text2)" /></div>
						<div style="flex:1;min-width:0">
							<div class="eh-aggr__item-head">
								<span class="eh-aggr__item-title">{{ recordTitle(a) }}</span>
								<Badge :status="a.status" />
							</div>
							<div class="eh-aggr__item-metas">
								<span class="eh-aggr__item-topic">{{ a.title }}</span>
								<span><Ic n="calendar" :size="11" color="var(--t-text3)" />{{ recordStartLabel(a) }}</span>
								<span><Ic n="users" :size="11" color="var(--t-text3)" />{{ a.headCount }}人</span>
								<span><Ic n="clipboard" :size="11" color="var(--t-text3)" />{{ a.id }}</span>
							</div>
							<div v-if="a.status === 'pending'" class="eh-aggr__chain">
								<template v-for="(n, i) in a.approvalNodes" :key="i">
									<span class="eh-aggr__node" :data-state="n.action">{{ n.role }}</span>
									<Ic v-if="i < a.approvalNodes.length - 1" n="chevronRight" :size="9" color="var(--t-text3)" />
								</template>
							</div>
						</div>
					</div>
				</Card>
				<div v-if="shown.length === 0" class="eh-aggr__empty">
					<Ic n="clipboard" :size="44" color="var(--t-text3)" />
						<p>暂无相关记录</p>
				</div>
			</div>

			<!-- Right detail -->
			<div class="eh-aggr__detail">
				<template v-if="!sel">
					<div class="eh-aggr__empty">
						<Ic n="clipboard" :size="50" color="var(--t-text3)" />
							<p>从左侧选择记录查看详情</p>
					</div>
				</template>
				<Card v-else :style="{ padding: '22px 24px' }">
					<div class="eh-aggr__detail-head">
						<div>
							<h3 class="eh-aggr__detail-title">{{ recordTitle(sel) }}</h3>
							<MonoLabel>{{ sel.title }} · {{ sel.id }}</MonoLabel>
						</div>
						<Badge :status="sel.status" />
					</div>

					<div class="eh-aggr__kv">
						<div v-for="r in kvRows(sel)" :key="r.key" class="eh-aggr__kv-cell">
							<Ic :n="r.icon" :size="12" color="var(--t-text3)" />
							<div>
								<MonoLabel :style="{ display: 'block', marginBottom: '2px' }">{{ r.key }}</MonoLabel>
								<span style="font-size:13px;font-weight:600;color:var(--t-text1)">{{ r.value }}</span>
							</div>
						</div>
					</div>

						<div class="eh-aggr__section">
							<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:4px">
								<MonoLabel>议程内容</MonoLabel>
								<Btn variant="ghost" icon="edit" @click="openAgendaDialog">{{ sel.agenda ? '编辑议程' : '补充议程' }}</Btn>
							</div>
							<p style="margin:0;font-size:13px;color:var(--t-text2);line-height:1.7">{{ sel.agenda || '暂无议程内容' }}</p>
						</div>

					<div class="eh-aggr__section">
						<MonoLabel :style="{ display: 'block', marginBottom: '10px' }">审批进度</MonoLabel>
						<ApprovalTimeline :nodes="sel.approvalNodes" />
					</div>

					<div class="eh-aggr__section">
						<MonoLabel :style="{ display: 'block', marginBottom: '10px' }">操作历史</MonoLabel>
						<div v-if="selHistory.length === 0" style="font-size:12px;color:var(--t-text3);padding:8px 0">暂无历史记录</div>
						<div v-else class="eh-aggr__timeline">
							<div v-for="(evt, idx) in selHistory" :key="idx" class="eh-aggr__tl-item">
								<div class="eh-aggr__tl-left">
									<div class="eh-aggr__tl-dot" :data-type="evt.eventType"></div>
									<div v-if="idx < selHistory.length - 1" class="eh-aggr__tl-line"></div>
								</div>
								<div class="eh-aggr__tl-body">
									<div class="eh-aggr__tl-head">
										<span class="eh-aggr__tl-tag" :data-type="evt.eventType">{{ historyLabel(evt.eventType) }}</span>
										<MonoLabel>{{ evt.eventTime }}</MonoLabel>
									</div>
									<div class="eh-aggr__tl-desc">{{ evt.eventDesc }}</div>
									<div v-if="evt.operator" class="eh-aggr__tl-meta">操作人：{{ evt.operator }}</div>
									<div v-if="evt.remark" class="eh-aggr__tl-remark">{{ evt.remark }}</div>
								</div>
							</div>
						</div>
					</div>

					<div class="eh-aggr__actions" v-if="sel.status === 'pending'">
						<Btn variant="success" icon="check" v-auth="'eh_approval_node_edit'" @click="onAction('approve')">审批通过</Btn>
						<Btn variant="danger" icon="x" v-auth="'eh_approval_node_edit'" @click="onAction('reject')">驳回申请</Btn>
					</div>
				</Card>
			</div>
		</div>

		<!-- 审批动作弹层 -->
		<el-dialog v-model="actionVisible" :title="actionTitle" width="460" :close-on-click-modal="false">
			<div style="display:flex;flex-direction:column;gap:14px">
				<div class="eh-aggr__alert" :data-type="currentAction === 'approve' ? 'success' : 'danger'">
					<Ic :n="currentAction === 'approve' ? 'checkCircle' : 'alertTri'" :size="13" :color="currentAction === 'approve' ? 'var(--t-success)' : 'var(--t-danger)'" />
					<span>{{ currentAction === 'approve' ? '通过后申请将直接完成审批，申请人收到站内通知。' : '驳回后申请人可查看原因并重新提交。' }}</span>
				</div>
				<FancyInput :label="currentAction === 'reject' ? '驳回原因' : '审批意见'" :required="currentAction === 'reject'">
					<textarea v-model="actionComment" :placeholder="currentAction === 'approve' ? '审批意见（选填）' : '请说明原因（必填）'" class="eh-input__field eh-input__field--textarea" rows="4" />
				</FancyInput>
			</div>
			<template #footer>
				<Btn variant="ghost" @click="actionVisible = false" style="margin-right:8px">取消</Btn>
				<Btn
					:variant="currentAction === 'approve' ? 'success' : 'danger'"
					:disabled="currentAction === 'reject' && !actionComment.trim()"
					@click="submitAction"
				>
					{{ currentAction === 'approve' ? '确认通过' : '确认驳回' }}
				</Btn>
			</template>
			</el-dialog>

			<el-dialog v-model="agendaVisible" title="补充议程" width="520" :close-on-click-modal="false">
				<FancyInput label="议程内容" type="textarea" v-model="agendaText" placeholder="补充本次会议议程" />
				<template #footer>
					<Btn variant="ghost" style="margin-right:8px" @click="agendaVisible = false">取消</Btn>
					<Btn variant="primary" :disabled="agendaSaving" @click="submitAgenda">{{ agendaSaving ? '保存中…' : '保存议程' }}</Btn>
				</template>
			</el-dialog>

			<FormDialog ref="formDialogRef" @refresh="loadData" />
	</div>
</template>

<script lang="ts" name="ehapply" setup>
import { computed, ref, defineAsyncComponent, onMounted, watch } from 'vue';
import { ElDialog, ElMessage } from 'element-plus';
import { Card, Btn, Badge, Ic, MonoLabel, ApprovalTimeline, FancyInput } from '/@/components/eh';
import { fetchAggregateList, submitApprovalAction, getObj, updateAgenda } from '/@/api/eh/apply';
import type { Application, ApplyHistoryEvent } from '/@/components/eh/mock';

const FormDialog = defineAsyncComponent(() => import('./form.vue'));

const apps = ref<Application[]>([]);
const sel = ref<Application | null>(null);
const filter = ref<'all' | 'pending' | 'approved' | 'completed' | 'rejected'>('all');
const formDialogRef = ref();

const actionVisible = ref(false);
const currentAction = ref<'approve' | 'reject'>('approve');
const actionComment = ref('');
const selHistory = ref<ApplyHistoryEvent[]>([]);
const agendaVisible = ref(false);
const agendaSaving = ref(false);
const agendaText = ref('');

watch(sel, async (app) => {
	selHistory.value = [];
	if (!app) return;
	try {
		const res = await getObj(app.id.replace('EH-', ''));
		const raw = res?.data;
		selHistory.value = (raw?.history || []).map((h: any): ApplyHistoryEvent => ({
			eventType: h.eventType || '',
			eventDesc: h.eventDesc || '',
			operator: h.operator || '',
			remark: h.remark || null,
			eventTime: h.eventTime ? String(h.eventTime).replace('T', ' ').slice(0, 16) : '',
		}));
	} catch { /* 加载失败静默 */ }
});

onMounted(loadData);

function mapStatus(status?: string) {
	const map: Record<string, Application['status']> = {
		'0': 'draft',
		'1': 'pending',
		'2': 'approved',
		'3': 'rejected',
		'4': 'cancelled',
		'5': 'rescheduled',
	};
	return map[status || ''] || 'pending';
}

function fmtTime(v?: string) {
	if (!v) return '';
	return v.replace('T', ' ').slice(0, 16);
}

function toApp(raw: any): Application {
	return {
			id: `EH-${raw.id}`,
			title: raw.title || '-',
			meetingNature: raw.meetingNature || 'external',
			unit: raw.unit || '-',
		industry: raw.industry || '-',
		district: raw.district || '-',
		applicant: raw.applicant || '-',
		phone: raw.phone || '-',
		dept: raw.dept || '-',
		startTime: fmtTime(raw.startTime) || '-- --',
		endTime: fmtTime(raw.endTime) || '-- --',
		leader: raw.leader || '-',
		visitors: (raw.visitors || []).map((v: any) => ({
			name: v.name || '-',
			title: v.title || '-',
			isStrategic: !!v.strategic,
			strategicLevel: v.strategicLevel || '',
		})),
		services: [],
		status: mapStatus(raw.status),
		approvalNodes: (raw.approvalNodes || []).map((n: any) => ({
			...( {
				role: n.role || '审批节点',
				name: n.name || '',
				action: n.action || 'waiting',
				time: n.time ? fmtTime(n.time) : null,
				comment: n.comment || '',
			} as any),
			id: n.id,
			})),
			headCount: raw.headCount || 0,
			customerCount: raw.customerCount ?? raw.headCount ?? 0,
			internalCount: raw.internalCount ?? 0,
			agenda: raw.agenda || '',
		created: fmtTime(raw.created),
		opportunityCode: raw.opportunityCode || '',
		visitRecord: raw.actualHeadCount
			? {
					actualStart: fmtTime(raw.actualVisitTime),
					actualEnd: fmtTime(raw.actualVisitTime),
					actualHeadCount: raw.actualHeadCount || 0,
					photos: raw.photoCount || 0,
					returnSigned: !!raw.returnTime,
					returnPerson: raw.returnSigner || '',
					returnTime: fmtTime(raw.returnTime),
			  }
			: undefined,
	};
}

async function loadData() {
	try {
		const res = await fetchAggregateList();
		apps.value = (res.data || []).map(toApp);
		const prevId = sel.value?.id;
		sel.value = prevId
			? (apps.value.find((a) => a.id === prevId) ?? apps.value[0] ?? null)
			: (apps.value[0] ?? null);
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载申请数据失败');
	}
}

const filters = computed(() => [
	{ key: 'all' as const, label: '全部', count: apps.value.length },
	{ key: 'pending' as const, label: '审批中', count: apps.value.filter((a) => a.status === 'pending').length },
	{ key: 'approved' as const, label: '已批准', count: apps.value.filter((a) => a.status === 'approved').length },
	{ key: 'completed' as const, label: '已完成', count: apps.value.filter((a) => a.status === 'completed').length },
	{ key: 'rejected' as const, label: '已驳回', count: apps.value.filter((a) => a.status === 'rejected').length },
]);

const shown = computed(() => (filter.value === 'all' ? apps.value : apps.value.filter((a) => a.status === filter.value)));

const actionTitle = computed(() => (currentAction.value === 'approve' ? '确认审批通过' : currentAction.value === 'reject' ? '驳回申请' : '转交审批'));

function recordTitle(a: Application) {
	const district = a.district && a.district !== '-' ? a.district : '未填区县';
	const unit = a.unit && a.unit !== '-' ? a.unit : '未填来访单位';
	return `${district} · ${unit}`;
}

function recordStartLabel(a: Application) {
	const [date = '—', time = '—'] = a.startTime.split(' ');
	return `${date} · ${time}开始`;
}

function kvRows(a: Application) {
	return [
		{ key: '来访单位', value: a.unit, icon: 'building' },
			{ key: '所属行业', value: a.industry, icon: 'list' },
			{ key: '所属区县', value: a.district, icon: 'mapPin' },
			{ key: '会议性质', value: a.meetingNature === 'internal' ? '内部' : '外部', icon: 'clipboard' },
			{ key: '参观日期', value: a.startTime.split(' ')[0], icon: 'calendar' },
			{ key: '会议正式开始时间', value: a.startTime.split(' ')[1] || '—', icon: 'clock' },
			{ key: '客户人数', value: `${a.customerCount ?? a.headCount} 人`, icon: 'users' },
			{ key: '自有人员人数', value: `${a.internalCount ?? 0} 人`, icon: 'users' },
			{ key: '我公司陪同领导', value: a.leader, icon: 'user' },
		];
	}

function historyLabel(eventType: string): string {
	const map: Record<string, string> = {
		submit: '提交申请', resubmit: '重新提交', approve: '审批通过',
		reject: '审批驳回', reschedule: '申请改期', cancel: '取消申请',
	};
	return map[eventType] || eventType;
}

function onNew() {
	formDialogRef.value?.openDialog();
}

function openAgendaDialog() {
	if (!sel.value) return;
	agendaText.value = sel.value.agenda || '';
	agendaVisible.value = true;
}

async function submitAgenda() {
	if (!sel.value) return;
	agendaSaving.value = true;
	try {
		await updateAgenda(sel.value.id.replace('EH-', ''), { agenda: agendaText.value });
		ElMessage.success('议程已保存');
		agendaVisible.value = false;
		await loadData();
	} catch (e: any) {
		ElMessage.error(e?.msg || '保存议程失败');
	} finally {
		agendaSaving.value = false;
	}
}

function onAction(type: 'approve' | 'reject') {
	currentAction.value = type;
	actionComment.value = '';
	actionVisible.value = true;
}

async function submitAction() {
	if (!sel.value) return;
	try {
		const t = currentAction.value;
		const node = sel.value.approvalNodes.find((n: any) => n.action === 'pending' || n.status === 'pending');
		await submitApprovalAction({
			applyId: sel.value.id.replace('EH-', ''),
			nodeId: node ? (node as any).id : undefined,
			action: t,
			comment: actionComment.value,
		});
		actionVisible.value = false;
		ElMessage.success(t === 'approve' ? '审批通过，申请人将收到通知' : '已驳回，申请人将收到通知');
		await loadData();
	} catch (e: any) {
		ElMessage.error(e?.msg || '审批操作失败');
	}
}
</script>

<style scoped>
.eh-aggr {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-aggr__head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 16px;
}
.eh-aggr__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-aggr__sub {
	margin: 3px 0 0;
	font-size: 12px;
	color: var(--t-text3);
}
.eh-aggr__filters {
	display: flex;
	gap: 6px;
	margin-bottom: 16px;
	flex-wrap: wrap;
}
.eh-aggr__chip {
	border: 1px solid var(--t-border);
	background: var(--t-surface);
	color: var(--t-text2);
	padding: 5px 12px;
	border-radius: 4px;
	cursor: pointer;
	font-size: 12px;
	font-weight: 600;
	display: flex;
	align-items: center;
	gap: 5px;
	font-family: inherit;
}
.eh-aggr__chip--active {
	border-color: var(--t-accent-border);
	background: var(--t-accent-light);
	color: var(--t-accent-strong);
	box-shadow: 0 1px 3px rgba(154, 95, 53, 0.12);
}
.eh-aggr__chip-cnt {
	background: #f0ede8;
	color: var(--t-text3);
	border-radius: 3px;
	padding: 0 5px;
	font-size: 11px;
}
.eh-aggr__chip-cnt--active {
	background: #fffaf2;
	color: var(--t-accent);
}

.eh-aggr__body {
	display: grid;
	grid-template-columns: 360px 1fr;
	gap: 18px;
	align-items: flex-start;
}
.eh-aggr__list {
	display: flex;
	flex-direction: column;
	gap: 8px;
	max-height: calc(100vh - 220px);
	overflow: auto;
}
.eh-aggr__detail {
	min-width: 0;
}
.eh-aggr__avatar {
	width: 36px;
	height: 36px;
	border-radius: 6px;
	background: var(--t-bg);
	border: 1px solid var(--t-border);
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	margin-top: 2px;
}
.eh-aggr__item-head {
	display: flex;
	align-items: center;
	gap: 8px;
	flex-wrap: wrap;
	margin-bottom: 5px;
}
.eh-aggr__item-title {
	font-size: 14px;
	font-weight: 700;
	color: var(--t-text1);
	min-width: 0;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.eh-aggr__item-metas {
	display: flex;
	gap: 12px;
	flex-wrap: wrap;
}
.eh-aggr__item-metas span {
	display: inline-flex;
	align-items: center;
	gap: 4px;
	font-size: 11px;
	color: var(--t-text3);
}
.eh-aggr__item-topic {
	padding: 1px 6px;
	border-radius: 4px;
	background: var(--t-bg);
	border: 1px solid var(--t-border);
	color: var(--t-text2) !important;
	font-weight: 600;
}
.eh-aggr__chain {
	display: flex;
	gap: 3px;
	align-items: center;
	margin-top: 7px;
	flex-wrap: wrap;
}
.eh-aggr__node {
	font-size: 10px;
	padding: 2px 6px;
	border-radius: 3px;
	background: var(--t-bg);
	color: var(--t-text3);
	font-weight: 600;
	border: 1px solid var(--t-border);
}
.eh-aggr__node[data-state='approved'] {
	background: var(--t-success-light);
	color: var(--t-success);
	border-color: #2c641533;
}
.eh-aggr__node[data-state='pending'] {
	background: var(--t-warning-light);
	color: var(--t-warning);
	border-color: #92400e33;
}

.eh-aggr__empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 64px 20px;
	color: var(--t-text3);
	gap: 10px;
	font-size: 13px;
}

.eh-aggr__detail-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 18px;
}
.eh-aggr__detail-title {
	margin: 0 0 3px;
	font-size: 18px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-aggr__kv {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 10px;
	margin-bottom: 18px;
}
.eh-aggr__kv-cell {
	background: var(--t-bg);
	border-radius: 6px;
	padding: 9px 12px;
	display: flex;
	gap: 8px;
	align-items: flex-start;
}
.eh-aggr__section {
	margin-bottom: 16px;
	background: var(--t-bg);
	border-radius: 6px;
	padding: 10px 14px;
}
.eh-aggr__visitor {
	display: flex;
	align-items: center;
	gap: 9px;
	padding: 8px 12px;
	background: var(--t-surface);
	border: 1px solid var(--t-border);
	border-radius: 6px;
}
.eh-aggr__avatar-round {
	width: 30px;
	height: 30px;
	border-radius: 50%;
	background: var(--t-accent);
	color: #fffaf2;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 13px;
	font-weight: 700;
	flex-shrink: 0;
}
.eh-aggr__strategic {
	display: inline-flex;
	align-items: center;
	gap: 3px;
	background: var(--t-warning-light);
	color: var(--t-warning);
	font-size: 10px;
	font-weight: 600;
	padding: 2px 7px;
	border-radius: 3px;
	border: 1px solid #92400e33;
}
.eh-aggr__svc-tag {
	font-size: 11px;
	font-weight: 600;
	padding: 3px 8px;
	border-radius: 3px;
	background: var(--t-accent-light);
	color: var(--t-accent-strong);
	border: 1px solid var(--t-accent-border);
}
.eh-aggr__actions {
	display: flex;
	gap: 8px;
	padding-top: 14px;
	border-top: 1px solid var(--t-border);
}
.eh-aggr__alert {
	border-radius: 4px;
	padding: 10px 12px;
	font-size: 12px;
	display: flex;
	gap: 7px;
	align-items: flex-start;
}
.eh-aggr__alert[data-type='success'] { background: var(--t-success-light); border: 1px solid #2c641533; color: var(--t-success); }
.eh-aggr__alert[data-type='danger'] { background: var(--t-danger-light); border: 1px solid #c41c1c33; color: var(--t-danger); }
.eh-aggr__alert[data-type='info'] { background: var(--t-bg); border: 1px solid var(--t-border); color: var(--t-text2); }
.eh-aggr__timeline { display: flex; flex-direction: column; }
.eh-aggr__tl-item { display: flex; gap: 10px; min-height: 52px; }
.eh-aggr__tl-left { display: flex; flex-direction: column; align-items: center; flex-shrink: 0; width: 12px; padding-top: 3px; }
.eh-aggr__tl-dot {
	width: 10px; height: 10px; border-radius: 50%; background: var(--t-text3);
	flex-shrink: 0; border: 2px solid var(--t-surface); box-shadow: 0 0 0 2px var(--t-text3);
}
.eh-aggr__tl-dot[data-type='submit'],
.eh-aggr__tl-dot[data-type='resubmit'] { background: var(--t-accent); box-shadow: 0 0 0 2px var(--t-accent); }
.eh-aggr__tl-dot[data-type='approve'] { background: var(--t-success); box-shadow: 0 0 0 2px var(--t-success); }
.eh-aggr__tl-dot[data-type='reject'] { background: var(--t-danger); box-shadow: 0 0 0 2px var(--t-danger); }
.eh-aggr__tl-dot[data-type='reschedule'] { background: var(--t-warning); box-shadow: 0 0 0 2px var(--t-warning); }
.eh-aggr__tl-line { flex: 1; width: 1px; background: var(--t-border); margin: 3px 0; }
.eh-aggr__tl-body { flex: 1; padding-bottom: 14px; }
.eh-aggr__tl-head { display: flex; align-items: center; gap: 8px; margin-bottom: 3px; }
.eh-aggr__tl-tag {
	font-size: 11px; font-weight: 700; padding: 2px 7px; border-radius: 3px;
	background: var(--t-bg); color: var(--t-text2); border: 1px solid var(--t-border);
}
.eh-aggr__tl-tag[data-type='submit'],
.eh-aggr__tl-tag[data-type='resubmit'] { background: var(--t-accent-light); color: var(--t-accent); border-color: var(--t-accent-border); }
.eh-aggr__tl-tag[data-type='approve'] { background: var(--t-success-light); color: var(--t-success); border-color: #2c641533; }
.eh-aggr__tl-tag[data-type='reject'] { background: var(--t-danger-light); color: var(--t-danger); border-color: #c41c1c33; }
.eh-aggr__tl-tag[data-type='reschedule'] { background: #fffbeb; color: #b45309; border-color: #92400e33; }
.eh-aggr__tl-desc { font-size: 12px; color: var(--t-text1); font-weight: 500; }
.eh-aggr__tl-meta { font-size: 11px; color: var(--t-text3); margin-top: 2px; }
.eh-aggr__tl-remark { font-size: 11px; color: var(--t-text2); margin-top: 3px; font-style: italic; }
</style>
