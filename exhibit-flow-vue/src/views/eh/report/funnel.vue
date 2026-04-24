<template>
	<div class="eh-scope eh-sub">
		<div class="eh-sub__head">
			<h2 class="eh-sub__title">商机转化</h2>
			<p class="eh-sub__sub">从参观接待到签约转化的完整漏斗</p>
		</div>
		<Card :style="{ padding: '22px 24px', maxWidth: '560px' }">
			<div style="display:flex;flex-direction:column;gap:8px">
				<div v-for="(d, i) in funnel" :key="i" style="display:flex;align-items:center;gap:10px">
					<div
						:style="{
							height: '38px',
							background: colors[i],
							borderRadius: '4px',
							display: 'flex',
							alignItems: 'center',
							paddingLeft: '12px',
							width: funnel.length ? (d.count / funnel[0].count) * 100 + '%' : '0%',
							minWidth: '100px',
						}"
					>
						<span style="color:#fff;font-size:12px;font-weight:600">{{ d.stage }}</span>
					</div>
					<span style="font-size:14px;font-weight:700;color:var(--t-text1)">{{ d.count }}</span>
				</div>
				<div style="padding-top:12px;border-top:1px solid var(--t-border);display:flex;justify-content:space-between;font-size:12px">
					<span style="color:var(--t-text3)">参观→签约转化率</span>
					<span style="font-weight:700;color:var(--t-success)">{{ rate }}%</span>
				</div>
			</div>
		</Card>
	</div>
</template>

<script lang="ts" name="ehreportfunnel" setup>
import { computed, onMounted, ref } from 'vue';
import { Card } from '/@/components/eh';
import { fetchFunnel } from '/@/api/eh/admin-stats';

const funnel = ref<any[]>([]);
const colors = ['#111111', '#313130', '#626260', '#9c9fa5'];
onMounted(async () => {
	funnel.value = (await fetchFunnel()).data;
});
const rate = computed(() => (funnel.value.length < 4 ? 0 : ((funnel.value[3].count / funnel.value[0].count) * 100).toFixed(1)));
</script>

<style scoped>
.eh-sub { padding: 20px 24px; background: var(--t-bg); min-height: calc(100vh - 100px); }
.eh-sub__head { margin-bottom: 18px; }
.eh-sub__title { margin: 0; font-size: 19px; font-weight: 700; color: var(--t-text1); }
.eh-sub__sub { margin: 3px 0 0; font-size: 12px; color: var(--t-text3); }
</style>
