<template>
	<div class="eh-scope eh-sub">
		<div class="eh-sub__head">
			<h2 class="eh-sub__title">分布统计</h2>
			<p class="eh-sub__sub">行业分布 + 地市分布</p>
		</div>
		<div style="display:grid;grid-template-columns:1fr 1fr;gap:16px">
			<Card :style="{ padding: '20px 22px' }">
				<div style="font-size:14px;font-weight:700;color:var(--t-text1);margin-bottom:16px">行业分布</div>
				<div style="display:flex;flex-direction:column;gap:8px">
					<div v-for="(d, i) in industry" :key="i" style="display:flex;align-items:center;gap:7px">
						<span :style="{ width: '8px', height: '8px', borderRadius: '2px', background: d.c }" />
						<span style="font-size:12px;color:var(--t-text2);flex:1">{{ d.name }}</span>
						<span style="font-size:12px;font-weight:700;color:var(--t-text1)">{{ d.count }}</span>
					</div>
				</div>
			</Card>
			<Card :style="{ padding: '20px 22px' }">
				<div style="font-size:14px;font-weight:700;color:var(--t-text1);margin-bottom:16px">地市分布</div>
				<div style="display:flex;flex-direction:column;gap:8px">
					<div v-for="(d, i) in city" :key="i" style="display:flex;align-items:center;gap:7px">
						<span style="font-size:12px;color:var(--t-text2);width:60px">{{ d.city }}</span>
						<div style="flex:1;height:5px;background:var(--t-bg);border-radius:2px;overflow:hidden;border:1px solid var(--t-border)">
							<div :style="{ height: '100%', width: (d.count / maxCity) * 100 + '%', background: i < 3 ? 'var(--t-accent)' : 'var(--t-text3)', borderRadius: '2px' }" />
						</div>
						<span style="font-size:12px;font-weight:700;color:var(--t-text1);width:16px;text-align:right">{{ d.count }}</span>
					</div>
				</div>
			</Card>
		</div>
	</div>
</template>

<script lang="ts" name="ehreportdistribution" setup>
import { computed, onMounted, ref } from 'vue';
import { Card } from '/@/components/eh';
import { fetchCityDist, fetchIndustryDist } from '/@/api/eh/admin-stats';

const city = ref<any[]>([]);
const industry = ref<any[]>([]);
onMounted(async () => {
	city.value = (await fetchCityDist()).data;
	industry.value = (await fetchIndustryDist()).data;
});
const maxCity = computed(() => Math.max(...city.value.map((d: any) => d.count), 1));
</script>

<style scoped>
.eh-sub { padding: 20px 24px; background: var(--t-bg); min-height: calc(100vh - 100px); }
.eh-sub__head { margin-bottom: 18px; }
.eh-sub__title { margin: 0; font-size: 19px; font-weight: 700; color: var(--t-text1); }
.eh-sub__sub { margin: 3px 0 0; font-size: 12px; color: var(--t-text3); }
</style>
