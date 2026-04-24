<template>
	<div class="eh-scope eh-opp">
		<div class="eh-opp__head">
			<div>
				<h2 class="eh-opp__title">商机跟进</h2>
				<p class="eh-opp__sub">参观转化出来的商机与后续签约情况</p>
			</div>
		</div>

		<Card v-for="o in items" :key="o.code" :style="{ padding: '16px 18px', marginBottom: '8px' }">
			<div style="display:flex;align-items:center;gap:12px">
				<div class="eh-opp__icon"><Ic n="briefcase" :size="16" color="var(--t-text2)" /></div>
				<div style="flex:1">
					<div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
						<span style="font-size:14px;font-weight:700;color:var(--t-text1)">{{ o.customer }}</span>
						<span class="eh-opp__stage" :data-stage="o.stage">{{ o.stageLabel }}</span>
					</div>
					<div style="font-size:12px;color:var(--t-text3)">
						<MonoLabel>{{ o.code }}</MonoLabel> · 预估金额 {{ o.amount }}
					</div>
				</div>
				<div style="text-align:right">
					<div style="font-size:11px;color:var(--t-text3)">最近跟进</div>
					<div style="font-size:12px;color:var(--t-text1);font-weight:600">{{ o.lastFollow }}</div>
				</div>
			</div>
		</Card>
	</div>
</template>

<script lang="ts" name="ehopportunity" setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchAggregateList } from '/@/api/eh/opportunity';
import { Card, Ic, MonoLabel } from '/@/components/eh';

const items = ref<any[]>([]);

const stageLabelMap: Record<string, string> = {
	clue: '商机触达',
	opportunity: '商机跟进',
	signed: '签约转化',
};
const stageMap: Record<string, string> = {
	clue: 'touching',
	opportunity: 'following',
	signed: 'contracting',
};

function fmtTime(v?: string) {
	if (!v) return '';
	return v.replace('T', ' ').slice(0, 10);
}

onMounted(async () => {
	try {
		const res = await fetchAggregateList();
		items.value = (res.data || []).map((o: any) => ({
			code: o.code,
			customer: o.customer || '-',
			stage: stageMap[o.status] || 'touching',
			stageLabel: stageLabelMap[o.status] || '商机触达',
			amount: o.amount || '—',
			lastFollow: fmtTime(o.lastFollow),
		}));
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载商机数据失败');
	}
});
</script>

<style scoped>
.eh-opp {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-opp__head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 18px;
}
.eh-opp__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-opp__sub {
	margin: 3px 0 0;
	font-size: 12px;
	color: var(--t-text3);
}
.eh-opp__icon {
	width: 36px;
	height: 36px;
	border-radius: 6px;
	background: var(--t-bg);
	border: 1px solid var(--t-border);
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}
.eh-opp__stage {
	font-size: 10px;
	font-weight: 600;
	padding: 2px 7px;
	border-radius: 3px;
}
.eh-opp__stage[data-stage='contracting'] { background: var(--t-success-light); color: var(--t-success); border: 1px solid #2c641533; }
.eh-opp__stage[data-stage='following'] { background: var(--t-accent-light); color: var(--t-accent); border: 1px solid var(--t-accent-border); }
.eh-opp__stage[data-stage='touching'] { background: var(--t-bg); color: var(--t-text2); border: 1px solid var(--t-border); }
</style>
