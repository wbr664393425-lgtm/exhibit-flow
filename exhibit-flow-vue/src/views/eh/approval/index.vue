<template>
	<div class="eh-scope eh-appr">
		<div class="eh-appr__body">
			<!-- 左侧 -->
			<div class="eh-appr__left">
				<div class="eh-appr__head">
					<h2 class="eh-appr__title">待我审批</h2>
					<span v-if="pending.length > 0" class="eh-appr__badge">{{ pending.length }} 条待办</span>
				</div>
				<div class="eh-appr__list">
					<div v-if="pending.length === 0" class="eh-appr__empty">
						<Ic n="checkCircle" :size="44" color="var(--t-text3)" />
						<p>暂无待审批申请</p>
					</div>
					<div
						v-for="a in pending"
						:key="a.id"
						class="eh-appr__card"
						:class="{ 'eh-appr__card--active': sel?.id === a.id }"
						@click="sel = a"
					>
						<div class="eh-appr__card-title">{{ a.title }}</div>
						<div v-for="m in metas(a)" :key="m.text" class="eh-appr__meta">
							<Ic :n="m.icon" :size="10" />{{ m.text }}
						</div>
						<div class="eh-appr__foot">
							<span>{{ a.created }}</span>
							<span class="eh-appr__foot-tag">待审批</span>
						</div>
					</div>
				</div>
			</div>

			<!-- 右侧详情 -->
			<div class="eh-appr__right">
				<div v-if="!sel" class="eh-appr__empty-right">
					<Ic n="clipboard" :size="50" color="var(--t-text3)" />
					<p>从左侧选择申请查看详情</p>
				</div>
				<Card v-else :style="{ padding: '22px 24px' }">
					<div class="eh-appr__detail-head">
						<div>
							<h3 class="eh-appr__detail-title">{{ sel.title }}</h3>
							<MonoLabel>{{ sel.id }}</MonoLabel>
						</div>
						<Badge :status="sel.status" />
					</div>

					<div class="eh-appr__kv">
						<div v-for="r in kvRows(sel)" :key="r.key" class="eh-appr__cell">
							<Ic :n="r.icon" :size="12" color="var(--t-text3)" />
							<div>
								<MonoLabel :style="{ display: 'block', marginBottom: '2px' }">{{ r.key }}</MonoLabel>
								<span style="font-size:13px;font-weight:600;color:var(--t-text1)">{{ r.value }}</span>
							</div>
						</div>
					</div>

					<div v-if="sel.agenda" class="eh-appr__section">
						<MonoLabel :style="{ display: 'block', marginBottom: '4px' }">简要议程</MonoLabel>
						<p style="margin:0;font-size:13px;color:var(--t-text2);line-height:1.7">{{ sel.agenda }}</p>
					</div>

					<div class="eh-appr__section">
						<MonoLabel :style="{ display: 'block', marginBottom: '10px' }">审批进度</MonoLabel>
						<ApprovalTimeline :nodes="sel.approvalNodes" />
					</div>

					<div class="eh-appr__actions">
						<Btn variant="success" icon="check" v-auth="'eh_approval_node_edit'" @click="onAction('approve')">审批通过</Btn>
						<Btn variant="danger" icon="x" v-auth="'eh_approval_node_edit'" @click="onAction('reject')">驳回申请</Btn>
					</div>
				</Card>
			</div>
		</div>

		<el-dialog v-model="actionVisible" :title="actionTitle" width="460" :close-on-click-modal="false">
			<div style="display:flex;flex-direction:column;gap:14px">
				<div class="eh-appr__alert" :data-type="currentAction === 'approve' ? 'success' : 'danger'">
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
	</div>
</template>

<script lang="ts" name="ehapproval" setup>
import { computed, onMounted, ref } from 'vue';
import { ElDialog, ElMessage } from 'element-plus';
import { Card, Btn, Badge, Ic, MonoLabel, ApprovalTimeline, FancyInput } from '/@/components/eh';
import { fetchTodoList, submitAction as submitApprovalAction } from '/@/api/eh/approval';
import type { Application } from '/@/components/eh/mock';

const apps = ref<Application[]>([]);
const sel = ref<Application | null>(null);

const actionVisible = ref(false);
const currentAction = ref<'approve' | 'reject'>('approve');
const actionComment = ref('');

onMounted(() => {
	loadData();
});

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
	};
}

async function loadData() {
	try {
		const res = await fetchTodoList();
		apps.value = (res.data || []).map(toApp).filter((a: Application) => a.status === 'pending' || a.status === 'rescheduled');
		sel.value = apps.value[0] || null;
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载待审批列表失败');
	}
}

const pending = computed(() => apps.value);
const actionTitle = computed(() => (currentAction.value === 'approve' ? '确认审批通过' : '驳回申请'));

function metas(a: Application) {
	return [
		{ icon: 'building', text: a.unit },
		{ icon: 'calendar', text: recordStartLabel(a) },
		{ icon: 'users', text: `客户${a.customerCount ?? a.headCount}人 · 自有${a.internalCount ?? 0}人` },
	];
}

function kvRows(a: Application) {
	return [
		{ key: '来访单位', value: a.unit, icon: 'building' },
		{ key: '会议性质', value: a.meetingNature === 'internal' ? '内部' : '外部', icon: 'clipboard' },
		{ key: '参观日期', value: a.startTime.split(' ')[0], icon: 'calendar' },
		{ key: '会议正式开始时间', value: startTimeOnly(a), icon: 'clock' },
		{ key: '客户人数', value: `${a.customerCount ?? a.headCount} 人`, icon: 'users' },
		{ key: '自有人员人数', value: `${a.internalCount ?? 0} 人`, icon: 'users' },
		{ key: '我公司陪同领导', value: a.leader, icon: 'user' },
	];
}

function recordStartLabel(a: Application) {
	const [date = '—', time = '—'] = a.startTime.split(' ');
	return `${date} · ${time}开始`;
}

function startTimeOnly(a: Application) {
	return a.startTime.split(' ')[1] || '—';
}

function onAction(t: 'approve' | 'reject') {
	currentAction.value = t;
	actionComment.value = '';
	actionVisible.value = true;
}

async function submitAction() {
	if (!sel.value) return;
	try {
		const node = sel.value.approvalNodes.find((n: any) => n.action === 'pending' || n.status === 'pending');
		await submitApprovalAction({
			applyId: sel.value.id.replace('EH-', ''),
			nodeId: node ? (node as any).id : undefined,
			action: currentAction.value,
			comment: actionComment.value,
		});
		const name = sel.value.title;
		actionVisible.value = false;
		ElMessage.success(currentAction.value === 'approve' ? `「${name}」已通过审批` : `「${name}」已驳回`);
		await loadData();
	} catch (e: any) {
		ElMessage.error(e?.msg || '审批失败');
	}
}
</script>

<style scoped>
.eh-appr {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-appr__body {
	display: grid;
	grid-template-columns: 300px 1fr;
	gap: 18px;
	align-items: flex-start;
}
.eh-appr__left {
	display: flex;
	flex-direction: column;
}
.eh-appr__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 12px;
}
.eh-appr__title {
	margin: 0;
	font-size: 17px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-appr__badge {
	background: var(--t-danger-light);
	color: var(--t-danger);
	font-size: 11px;
	font-weight: 700;
	padding: 2px 8px;
	border-radius: 4px;
	border: 1px solid #c41c1c33;
}
.eh-appr__list {
	display: flex;
	flex-direction: column;
	gap: 6px;
	max-height: calc(100vh - 200px);
	overflow: auto;
}
.eh-appr__card {
	padding: 12px 14px;
	border-radius: 6px;
	cursor: pointer;
	border: 1px solid var(--t-border);
	background: var(--t-surface);
	transition: all 0.12s;
}
.eh-appr__card:hover {
	border-color: var(--t-border-dark);
}
.eh-appr__card--active {
	border-color: var(--t-accent);
	background: var(--t-accent-light);
}
.eh-appr__card-title {
	font-size: 13px;
	font-weight: 600;
	color: var(--t-text1);
	margin-bottom: 5px;
}
.eh-appr__meta {
	display: flex;
	align-items: center;
	gap: 5px;
	font-size: 11px;
	color: var(--t-text3);
	margin-bottom: 2px;
}
.eh-appr__foot {
	margin-top: 8px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 10px;
	color: var(--t-text3);
}
.eh-appr__foot-tag {
	background: var(--t-warning-light);
	color: var(--t-warning);
	padding: 2px 7px;
	border-radius: 6px;
	font-weight: 600;
}

.eh-appr__right {
	min-width: 0;
}
.eh-appr__empty,
.eh-appr__empty-right {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 52px 20px;
	color: var(--t-text3);
	gap: 10px;
	font-size: 13px;
}

.eh-appr__detail-head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 18px;
}
.eh-appr__detail-title {
	margin: 0 0 3px;
	font-size: 18px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-appr__kv {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 10px;
	margin-bottom: 18px;
}
.eh-appr__cell {
	background: var(--t-bg);
	border-radius: 6px;
	padding: 9px 12px;
	display: flex;
	gap: 8px;
	align-items: flex-start;
}
.eh-appr__section {
	margin-bottom: 16px;
	background: var(--t-bg);
	border-radius: 6px;
	padding: 10px 14px;
}
.eh-appr__visitor {
	display: flex;
	align-items: center;
	gap: 9px;
	padding: 8px 12px;
	background: var(--t-surface);
	border: 1px solid var(--t-border);
	border-radius: 6px;
}
.eh-appr__avatar {
	width: 30px;
	height: 30px;
	border-radius: 50%;
	background: var(--t-accent);
	color: #f8fbff;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 13px;
	font-weight: 700;
	flex-shrink: 0;
}
.eh-appr__strategic {
	display: inline-flex;
	align-items: center;
	gap: 3px;
	background: var(--t-warning-light);
	color: var(--t-warning);
	font-size: 10px;
	font-weight: 600;
	padding: 2px 7px;
	border-radius: 6px;
	border: 1px solid var(--t-accent-border);
}
.eh-appr__actions {
	display: flex;
	gap: 8px;
	padding-top: 14px;
	border-top: 1px solid var(--t-border);
}
.eh-appr__alert {
	border-radius: 4px;
	padding: 10px 12px;
	font-size: 12px;
	display: flex;
	gap: 7px;
	align-items: flex-start;
}
.eh-appr__alert[data-type='success'] { background: var(--t-success-light); border: 1px solid #2c641533; color: var(--t-success); }
.eh-appr__alert[data-type='danger'] { background: var(--t-danger-light); border: 1px solid #c41c1c33; color: var(--t-danger); }
.eh-appr__alert[data-type='info'] { background: var(--t-bg); border: 1px solid var(--t-border); color: var(--t-text2); }
</style>
