<template>
	<div class="eh-scope eh-log">
		<div class="eh-log__head">
			<div>
				<h2 class="eh-log__title">改期取消日志</h2>
				<p class="eh-log__sub">记录申请改期与取消的完整流水</p>
			</div>
		</div>

		<Card v-for="(item, i) in logs" :key="i" :style="{ padding: '14px 18px', marginBottom: '8px' }">
			<div style="display:flex;align-items:flex-start;gap:12px">
				<div
					:style="{
						width: '32px',
						height: '32px',
						borderRadius: '6px',
						display: 'flex',
						alignItems: 'center',
						justifyContent: 'center',
						flexShrink: 0,
						background: item.type === 'cancel' ? 'var(--t-danger-light)' : 'var(--t-accent-light)',
						border: `1px solid ${item.type === 'cancel' ? '#c41c1c33' : 'var(--t-accent-border)'}`,
					}"
				>
					<Ic :n="item.type === 'cancel' ? 'xCircle' : 'rotate'" :size="14" :color="item.type === 'cancel' ? 'var(--t-danger)' : 'var(--t-accent)'" />
				</div>
				<div style="flex:1">
					<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
						<span style="font-size:13px;font-weight:700;color:var(--t-text1)">{{ item.title }}</span>
						<span class="eh-log__tag" :data-type="item.type">{{ item.type === 'cancel' ? '取消' : '改期' }}</span>
					</div>
					<div style="font-size:12px;color:var(--t-text2);margin-bottom:2px">{{ item.reason }}</div>
					<div style="font-size:11px;color:var(--t-text3)">{{ item.operator }} · {{ item.time }}</div>
				</div>
			</div>
		</Card>
	</div>
</template>

<script lang="ts" name="ehchangelog" setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchAggregateList } from '/@/api/eh/changelog';
import { Card, Ic } from '/@/components/eh';

const logs = ref<any[]>([]);

function fmtTime(v?: string) {
	if (!v) return '';
	return v.replace('T', ' ').slice(0, 16);
}

onMounted(async () => {
	try {
		const res = await fetchAggregateList();
		logs.value = (res.data || []).map((item: any) => ({
			type: item.type || 'reschedule',
			title: `EH-${item.applyId} ${item.title || ''}`.trim(),
			reason: item.reason || '-',
			operator: item.operator || '-',
			time: fmtTime(item.time),
		}));
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载改期取消日志失败');
	}
});
</script>

<style scoped>
.eh-log {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-log__head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 18px;
}
.eh-log__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-log__sub {
	margin: 3px 0 0;
	font-size: 12px;
	color: var(--t-text3);
}
.eh-log__tag {
	font-size: 10px;
	font-weight: 600;
	padding: 2px 7px;
	border-radius: 3px;
}
.eh-log__tag[data-type='cancel'] {
	background: var(--t-danger-light);
	color: var(--t-danger);
	border: 1px solid #c41c1c33;
}
.eh-log__tag[data-type='reschedule'] {
	background: var(--t-accent-light);
	color: var(--t-accent);
	border: 1px solid var(--t-accent-border);
}
</style>
