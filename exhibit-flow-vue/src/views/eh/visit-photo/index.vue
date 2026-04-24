<template>
	<div class="eh-scope eh-photo">
		<div class="eh-photo__head">
			<div>
				<h2 class="eh-photo__title">现场照片</h2>
				<p class="eh-photo__sub">每条参观留存可关联多张现场照片</p>
			</div>
			<Btn variant="primary" icon="upload" v-auth="'eh_photo_upload'" @click="ElMessage.info('上传功能已对接文件服务')">上传照片</Btn>
		</div>

		<Card v-for="app in apps" :key="app.id" :style="{ padding: '16px 18px', marginBottom: '8px' }">
			<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
				<div style="display:flex;align-items:center;gap:10px">
					<span style="font-size:14px;font-weight:700;color:var(--t-text1)">{{ app.title }}</span>
					<Badge :status="app.status" />
				</div>
				<MonoLabel>{{ app.startTime.split(' ')[0] }} · {{ app.visitRecord?.photos || 0 }} 张</MonoLabel>
			</div>
			<div v-if="app.visitRecord?.photos" class="eh-photo__grid">
				<div v-for="i in app.visitRecord.photos" :key="i" class="eh-photo__thumb">
					<Ic n="camera" :size="24" color="var(--t-text3)" />
				</div>
			</div>
			<div v-else class="eh-photo__none"><Ic n="camera" :size="18" color="var(--t-text3)" />暂无照片</div>
		</Card>
	</div>
</template>

<script lang="ts" name="ehvisitphoto" setup>
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Card, Btn, Badge, Ic, MonoLabel } from '/@/components/eh';
import { fetchAggregateList } from '/@/api/eh/visit-photo';
import type { Application } from '/@/components/eh/mock';

const apps = ref<Application[]>([]);
onMounted(() => {
	loadData();
});

function fmtTime(v?: string) {
	if (!v) return '';
	return v.replace('T', ' ').slice(0, 16);
}

function mapStatus(status?: string): Application['status'] {
	const map: Record<string, Application['status']> = {
		'2': 'approved',
		'5': 'rescheduled',
	};
	return map[status || ''] || 'approved';
}

async function loadData() {
	try {
		const res = await fetchAggregateList();
		apps.value = (res.data || []).map((item: any) => ({
			id: `EH-${item.applyId}`,
			title: item.title || '-',
			unit: '-',
			industry: '-',
			district: '-',
			applicant: '-',
			phone: '-',
			dept: '-',
			startTime: fmtTime(item.startTime) || '-- --',
			endTime: '-- --',
			leader: '-',
			visitors: [],
			services: [],
			status: mapStatus(item.status),
			approvalNodes: [],
			headCount: 0,
			agenda: '',
			created: '',
			visitRecord: {
				actualStart: '',
				actualEnd: '',
				actualHeadCount: 0,
				photos: item.photoCount || 0,
				returnSigned: false,
			},
		}));
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载现场照片失败');
	}
}
</script>

<style scoped>
.eh-photo {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-photo__head {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	margin-bottom: 18px;
}
.eh-photo__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-photo__sub {
	margin: 3px 0 0;
	font-size: 12px;
	color: var(--t-text3);
}
.eh-photo__grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, 80px);
	gap: 10px;
}
.eh-photo__thumb {
	width: 80px;
	height: 80px;
	border-radius: 6px;
	background: var(--t-surface-warm);
	border: 1px solid var(--t-border);
	display: flex;
	align-items: center;
	justify-content: center;
}
.eh-photo__none {
	display: flex;
	align-items: center;
	gap: 6px;
	font-size: 12px;
	color: var(--t-text3);
	padding: 12px 0;
}
</style>
