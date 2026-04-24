<template>
	<div class="eh-scope eh-sub">
		<div class="eh-sub__head">
			<h2 class="eh-sub__title">战客覆盖率</h2>
			<p class="eh-sub__sub">已并入统计看板，点此直达完整视图</p>
		</div>
		<Card :style="{ padding: '20px 22px' }">
			<div style="font-size:14px;font-weight:700;color:var(--t-text1);margin-bottom:14px">战客级别</div>
			<div style="display:flex;flex-direction:column;gap:10px">
				<div v-for="(d, i) in strategic" :key="i">
					<div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px">
						<span style="color:var(--t-text2)">{{ d.level }}</span>
						<span style="font-weight:700;color:var(--t-text1)">{{ d.count }} ({{ Math.round((d.count / total) * 100) }}%)</span>
					</div>
					<div style="height:6px;background:var(--t-bg);border-radius:2px;overflow:hidden;border:1px solid var(--t-border)">
						<div :style="{ height: '100%', width: (d.count / total) * 100 + '%', background: d.c, borderRadius: '2px' }" />
					</div>
				</div>
			</div>
			<Btn variant="outline" size="sm" icon="barChart" style="margin-top:20px" @click="router.push('/eh/admin/dashboard')">查看统计看板</Btn>
		</Card>
	</div>
</template>

<script lang="ts" name="ehreportcoverage" setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Card, Btn, Ic } from '/@/components/eh';
import { fetchStrategic } from '/@/api/eh/admin-stats';

const router = useRouter();
const strategic = ref<any[]>([]);
onMounted(async () => {
	strategic.value = (await fetchStrategic()).data;
});
const total = computed(() => strategic.value.reduce((s: number, d: any) => s + d.count, 0) || 1);
</script>

<style scoped>
.eh-sub { padding: 20px 24px; background: var(--t-bg); min-height: calc(100vh - 100px); }
.eh-sub__head { margin-bottom: 18px; }
.eh-sub__title { margin: 0; font-size: 19px; font-weight: 700; color: var(--t-text1); }
.eh-sub__sub { margin: 3px 0 0; font-size: 12px; color: var(--t-text3); }
</style>
