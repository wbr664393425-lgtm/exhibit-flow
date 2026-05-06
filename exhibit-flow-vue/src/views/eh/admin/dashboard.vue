<template>
	<div class="eh-scope eh-dash">
		<div class="eh-dash__head">
			<div>
				<h2 class="eh-dash__title">统计看板</h2>
				<p class="eh-dash__sub">2026年度 · 数据截至 4月23日</p>
			</div>
			<div style="display:flex;gap:6px">
				<Btn variant="ghost" size="sm" icon="fileText" @click="exportVisible = true">导出预览</Btn>
				<Btn variant="outline" size="sm" icon="download" v-auth="'eh_dashboard_export_excel'" @click="onExportExcel">导出Excel</Btn>
			</div>
		</div>

		<!-- KPI -->
		<div class="eh-dash__kpi">
			<Card v-for="(k, i) in kpis" :key="i" :style="{ padding: '18px' }">
				<div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:12px">
					<div class="eh-dash__icon"><Ic :n="k.icon" :size="16" color="var(--t-text2)" /></div>
					<span v-if="k.change" class="eh-dash__change" :class="{ 'eh-dash__change--up': k.up }">↑ {{ k.change }}</span>
				</div>
				<div style="display:flex;align-items:flex-end;gap:3px;margin-bottom:3px">
					<span style="font-size:28px;font-weight:800;color:var(--t-text1);line-height:1">{{ k.value }}</span>
					<span style="font-size:12px;color:var(--t-text3);margin-bottom:3px">{{ k.unit }}</span>
				</div>
				<MonoLabel>{{ k.label }}</MonoLabel>
				<div v-if="k.target" style="margin-top:10px">
					<div style="display:flex;justify-content:space-between;font-size:10px;color:var(--t-text3);margin-bottom:3px">
						<span>目标 ≥50%</span>
						<span style="color:var(--t-success);font-weight:700">已达标</span>
					</div>
					<div class="eh-dash__bar">
						<div class="eh-dash__bar-fill" :style="{ width: k.target + '%' }" />
					</div>
				</div>
			</Card>
		</div>

		<!-- 月度 + 行业 -->
		<div class="eh-dash__row" style="grid-template-columns:2fr 1fr">
			<Card :style="{ padding: '20px 22px' }">
				<div class="eh-dash__card-head">
					<div style="font-size:14px;font-weight:700;color:var(--t-text1)">月度参观场次</div>
					<div style="display:flex;gap:10px">
						<span class="eh-dash__lg"><span style="background:var(--t-accent)" />今年</span>
						<span class="eh-dash__lg"><span style="background:var(--t-border);height:2px" />去年</span>
					</div>
				</div>
				<SVGLineChart :data="monthly" :h="156" />
			</Card>
			<Card :style="{ padding: '20px 22px' }">
				<div style="font-size:14px;font-weight:700;color:var(--t-text1);margin-bottom:16px">行业分布</div>
				<div style="display:flex;flex-direction:column;gap:8px">
					<div v-for="(d, i) in industry" :key="i" class="eh-dash__bar-row">
						<span :style="{ width: '8px', height: '8px', borderRadius: '2px', background: d.c }" />
						<span style="font-size:12px;color:var(--t-text2);flex:1">{{ d.name }}</span>
						<div class="eh-dash__mini-bar">
							<div :style="{ height: '100%', width: (d.count / 5) * 100 + '%', background: d.c, borderRadius: '2px' }" />
						</div>
						<span style="font-size:11px;color:var(--t-text3);width:14px;text-align:right">{{ d.count }}</span>
					</div>
				</div>
			</Card>
		</div>

		<!-- 地市 + 战客 + 漏斗 -->
		<div class="eh-dash__row" style="grid-template-columns:1fr 1fr 1fr">
			<Card :style="{ padding: '20px 22px' }">
				<div style="font-size:14px;font-weight:700;color:var(--t-text1);margin-bottom:14px">地市分布</div>
				<div style="display:flex;flex-direction:column;gap:8px">
					<div v-for="(d, i) in city" :key="i" class="eh-dash__bar-row">
						<span style="font-size:11px;color:var(--t-text2);width:44px">{{ d.city }}</span>
						<div style="flex:1;height:5px;background:var(--t-bg);border-radius:2px;overflow:hidden;border:1px solid var(--t-border)">
							<div :style="{ height: '100%', width: (d.count / maxCity) * 100 + '%', background: i < 3 ? 'var(--t-accent)' : 'var(--t-text3)', borderRadius: '2px' }" />
						</div>
						<span style="font-size:11px;font-weight:700;color:var(--t-text1);width:16px;text-align:right">{{ d.count }}</span>
					</div>
				</div>
			</Card>
			<Card :style="{ padding: '20px 22px' }">
				<div style="font-size:14px;font-weight:700;color:var(--t-text1);margin-bottom:14px">战客级别</div>
				<div style="display:flex;flex-direction:column;gap:10px">
					<div v-for="(d, i) in strategic" :key="i">
						<div style="display:flex;justify-content:space-between;font-size:11px;margin-bottom:4px">
							<span style="color:var(--t-text2)">{{ d.level }}</span>
							<span style="font-weight:700;color:var(--t-text1)">{{ d.count }} <span style="color:var(--t-text3);font-weight:400">({{ Math.round((d.count / totalS) * 100) }}%)</span></span>
						</div>
						<div style="height:5px;background:var(--t-bg);border-radius:2px;overflow:hidden;border:1px solid var(--t-border)">
							<div :style="{ height: '100%', width: (d.count / totalS) * 100 + '%', background: d.c, borderRadius: '2px' }" />
						</div>
					</div>
				</div>
			</Card>
			<Card :style="{ padding: '20px 22px' }">
				<div style="font-size:14px;font-weight:700;color:var(--t-text1);margin-bottom:14px">商机转化漏斗</div>
				<div style="display:flex;flex-direction:column;gap:7px">
					<div v-for="(d, i) in funnel" :key="i" style="display:flex;align-items:center;gap:8px">
						<div
							:style="{
								height: '34px',
								background: funnelColors[i],
								borderRadius: '4px',
								display: 'flex',
								alignItems: 'center',
								paddingLeft: '10px',
								width: (d.count / funnel[0].count) * 100 + '%',
								minWidth: '72px',
							}"
						>
							<span style="color:#f8fbff;font-size:11px;font-weight:600;white-space:nowrap">{{ d.stage }}</span>
						</div>
						<span style="font-size:13px;font-weight:700;color:var(--t-text1)">{{ d.count }}</span>
					</div>
					<div style="padding-top:10px;border-top:1px solid var(--t-border);display:flex;justify-content:space-between;font-size:11px">
						<span style="color:var(--t-text3)">参观→签约转化率</span>
						<span style="font-weight:700;color:var(--t-success)">{{ conversionRate }}%</span>
					</div>
				</div>
			</Card>
		</div>

		<!-- 导出预览 -->
		<el-dialog v-model="exportVisible" title="导出预览" width="720" class="eh-scope">
			<div style="display:flex;gap:6px;margin-bottom:16px">
				<button
					v-for="t in exportTabs"
					:key="t.key"
					:style="{
						border: 'none',
						background: exportTab === t.key ? 'var(--t-accent)' : 'transparent',
						color: exportTab === t.key ? '#f8fbff' : 'var(--t-text2)',
						padding: '5px 12px',
						borderRadius: '6px',
						cursor: 'pointer',
						fontSize: '12px',
						fontWeight: 600,
						display: 'flex',
						alignItems: 'center',
						gap: '6px',
					}"
					@click="exportTab = t.key"
				>
					<Ic :n="t.key === 'excel' ? 'table' : 'fileText'" :size="13" />{{ t.label }}
				</button>
			</div>
			<div v-if="exportTab === 'excel'" style="overflow:auto;border:1px solid var(--t-border);border-radius:6px;max-height:400px">
				<table style="width:100%;border-collapse:collapse;font-size:11px;min-width:900px">
					<thead>
						<tr style="background:var(--t-accent)">
							<th v-for="c in EXCEL_COLS" :key="c" style="padding:7px 10px;color:#f8fbff;font-weight:600;white-space:nowrap;text-align:left;border-right:1px solid rgba(248,251,255,0.1);font-family:var(--t-font-mono);letter-spacing:0.03em">{{ c }}</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="(row, i) in EXCEL_ROWS" :key="i" :style="{ background: i % 2 === 0 ? 'var(--t-bg)' : 'var(--t-surface)' }">
							<td v-for="(cell, j) in row" :key="j" style="padding:6px 10px;border-right:1px solid var(--t-border);border-bottom:1px solid var(--t-border);white-space:nowrap" :style="{ color: cell === '待录入' ? 'var(--t-text3)' : 'var(--t-text1)' }">{{ cell }}</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div v-else style="padding:16px;background:var(--t-bg);border-radius:6px;max-height:480px;overflow:auto;text-align:center;color:var(--t-text3);font-size:13px">
				Word 预览（poi-tl 按原模板回填生成存档版申请单）
			</div>
			<template #footer>
				<Btn
					variant="outline"
					size="sm"
					icon="download"
					:disabled="exportTab === 'docx'"
					@click="exportTab === 'excel' ? onExportExcel() : ElMessage.info('Word 导出本轮未开放')"
				>
					{{ exportTab === 'excel' ? '下载 Excel' : 'Word 本轮未开放' }}
				</Btn>
			</template>
		</el-dialog>
	</div>
</template>

<script lang="ts" name="ehdashboard" setup>
import { computed, onMounted, ref } from 'vue';
import { ElDialog, ElMessage } from 'element-plus';
import { Card, Btn, Ic, MonoLabel, SVGLineChart } from '/@/components/eh';
import { fetchKpis, fetchMonthly, fetchCityDist, fetchIndustryDist, fetchStrategic, fetchFunnel } from '/@/api/eh/admin-stats';
import { downBlobFile } from '/@/utils/other';

const kpis = ref<any[]>([]);
const monthly = ref<any[]>([]);
const city = ref<any[]>([]);
const industry = ref<any[]>([]);
const strategic = ref<any[]>([]);
const funnel = ref<any[]>([]);
const funnelColors = ['#2f67d8', '#5d8ceb', '#bfd4fb', '#8293ab'];

const exportVisible = ref(false);
const exportTab = ref<'excel' | 'docx'>('excel');
const exportTabs: { key: 'excel' | 'docx'; label: string }[] = [
	{ key: 'excel', label: '省公司 Excel 报表' },
	{ key: 'docx', label: '审批单 Word 文档' },
];

const EXCEL_COLS = ['序号', '参观日期', '参观单位', '客户姓名', '职务', '是否战客', '战客级别', '商机编码', '参观人数', '最高陪同领导', '申请部门', '申请人', '联系电话'];
const EXCEL_ROWS = [
	['1', '2026-04-18', '广州市政府采购中心', '黄主任', '采购主任', '是', '省直管战客', 'OPP-GZ-2026-0312', '11', '总经理', '公共事业部', '陈小红', '139****2233'],
	['2', '2026-04-25', '华为技术有限公司', '王总', '区域总监', '是', '市直管战客', '', '8', '分管副总', '政企客户部', '李建国', '138****8801'],
	['3', '2026-04-30', '腾讯科技（深圳）', '刘经理', '商务经理', '否', '', '', '4', '无', '政企客户部', '李建国', '138****8801'],
];

onMounted(async () => {
	const [kpiRes, monthRes, cityRes, indRes, stratRes, funRes] = await Promise.all([
		fetchKpis(),
		fetchMonthly(),
		fetchCityDist(),
		fetchIndustryDist(),
		fetchStrategic(),
		fetchFunnel(),
	]);
	kpis.value = kpiRes.data;
	monthly.value = monthRes.data;
	city.value = cityRes.data;
	industry.value = indRes.data;
	strategic.value = stratRes.data;
	funnel.value = funRes.data;
});

const maxCity = computed(() => Math.max(...city.value.map((d: any) => d.count), 1));
const totalS = computed(() => strategic.value.reduce((s: number, d: any) => s + d.count, 0) || 1);
const conversionRate = computed(() => {
	if (funnel.value.length < 4) return 0;
	return ((funnel.value[3].count / funnel.value[0].count) * 100).toFixed(1);
});

function onExportExcel() {
	downBlobFile('/admin/eh/stats/export', {}, '省公司报表.xlsx');
}
</script>

<style scoped>
.eh-dash {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
	display: flex;
	flex-direction: column;
	gap: 18px;
}
.eh-dash__head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 12px;
	flex-wrap: wrap;
}
.eh-dash__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-dash__sub {
	margin: 3px 0 0;
	font-size: 12px;
	color: var(--t-text3);
}
.eh-dash__kpi {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 12px;
}
.eh-dash__icon {
	width: 34px;
	height: 34px;
	border-radius: 6px;
	background: var(--t-bg);
	border: 1px solid var(--t-border);
	display: flex;
	align-items: center;
	justify-content: center;
}
.eh-dash__change {
	font-size: 10px;
	font-weight: 700;
	background: var(--t-danger-light);
	color: var(--t-danger);
	padding: 2px 6px;
	border-radius: 3px;
}
.eh-dash__change--up {
	background: var(--t-success-light);
	color: var(--t-success);
}
.eh-dash__bar {
	height: 4px;
	background: var(--t-bg);
	border-radius: 2px;
	border: 1px solid var(--t-border);
	overflow: hidden;
}
.eh-dash__bar-fill {
	height: 100%;
	background: var(--t-accent);
	border-radius: 2px;
	transition: width 0.5s;
}
.eh-dash__row {
	display: grid;
	gap: 16px;
}
.eh-dash__card-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 16px;
}
.eh-dash__lg {
	display: inline-flex;
	align-items: center;
	gap: 4px;
	font-size: 11px;
	color: var(--t-text2);
}
.eh-dash__lg span {
	width: 16px;
	height: 2px;
	display: inline-block;
	border-radius: 1px;
}
.eh-dash__bar-row {
	display: flex;
	align-items: center;
	gap: 7px;
}
.eh-dash__mini-bar {
	width: 64px;
	height: 4px;
	background: var(--t-bg);
	border-radius: 2px;
	overflow: hidden;
	border: 1px solid var(--t-border);
}

@media (max-width: 1200px) {
	.eh-dash__kpi { grid-template-columns: repeat(2, 1fr); }
	.eh-dash__row { grid-template-columns: 1fr !important; }
}
</style>
