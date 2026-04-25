<template>
	<div class="eh-scope eh-photo">
		<div class="eh-photo__head">
			<div>
				<h2 class="eh-photo__title">现场照片</h2>
				<p class="eh-photo__sub">每条参观留存可关联多张现场照片</p>
			</div>
			<Btn variant="primary" icon="upload" v-auth="'eh_photo_upload'" @click="openUpload">上传照片</Btn>
		</div>

		<Card v-for="app in apps" :key="app.id" :style="{ padding: '16px 18px', marginBottom: '8px' }">
			<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
				<div style="display:flex;align-items:center;gap:10px">
					<span style="font-size:14px;font-weight:700;color:var(--t-text1)">{{ app.title }}</span>
					<Badge :status="app.status" />
				</div>
				<MonoLabel>{{ app.startTime.split(' ')[0] }} · {{ app.photos.length }} 张</MonoLabel>
			</div>
			<div v-if="app.photos.length" class="eh-photo__grid">
				<img v-for="photo in app.photos" :key="photo.id" :src="photo.fileUrl" :alt="photo.fileName" class="eh-photo__thumb" />
			</div>
			<div v-else class="eh-photo__none"><Ic n="camera" :size="18" color="var(--t-text3)" />暂无照片</div>
		</Card>

		<el-dialog v-model="uploadVisible" title="上传现场照片" width="520" :close-on-click-modal="false">
			<div style="display:flex;flex-direction:column;gap:14px">
				<div>
					<MonoLabel :style="{ display: 'block', marginBottom: '6px' }">关联留存</MonoLabel>
					<el-select v-model="selectedVisitId" placeholder="请选择留存记录" style="width:100%">
						<el-option v-for="item in uploadTargets" :key="item.value" :label="item.label" :value="item.value" />
					</el-select>
				</div>
				<div class="eh-photo__picker" @click="openUploadPicker">
					<Ic n="upload" :size="20" color="var(--t-text2)" />
					<div style="font-size:12px;color:var(--t-text2);font-weight:600;margin-top:6px">点击选择照片文件</div>
					<MonoLabel :style="{ marginTop: '4px' }">支持多张图片批量上传</MonoLabel>
				</div>
				<div v-if="pendingFiles.length" style="display:flex;flex-direction:column;gap:4px">
					<MonoLabel v-for="file in pendingFiles" :key="file.name + file.size">{{ file.name }} · {{ Math.round(file.size / 1024) }}KB</MonoLabel>
				</div>
			</div>
			<template #footer>
				<Btn variant="ghost" style="margin-right:8px" @click="uploadVisible = false">取消</Btn>
				<Btn variant="primary" icon="upload" :disabled="!selectedVisitId || pendingFiles.length === 0" @click="submitUpload">确认上传</Btn>
			</template>
		</el-dialog>

		<input ref="uploadInputRef" type="file" accept="image/*" multiple style="display:none" @change="onFileChange" />
	</div>
</template>

<script lang="ts" name="ehvisitphoto" setup>
import { computed, onMounted, ref } from 'vue';
import { ElDialog, ElMessage, ElOption, ElSelect } from 'element-plus';
import { Card, Btn, Badge, Ic, MonoLabel } from '/@/components/eh';
import { fetchAggregateList, saveBatchPhotos } from '/@/api/eh/visit-photo';
import request from '/@/utils/request';

interface PhotoItem {
	id: number;
	fileUrl: string;
	fileName: string;
	fileSize: number;
}

interface PhotoAppItem {
	id: string;
	title: string;
	startTime: string;
	status: 'approved' | 'rescheduled';
	visitId: number | null;
	photos: PhotoItem[];
}

const apps = ref<PhotoAppItem[]>([]);
const uploadVisible = ref(false);
const selectedVisitId = ref<number | null>(null);
const pendingFiles = ref<File[]>([]);
const uploadInputRef = ref<HTMLInputElement>();

onMounted(() => {
	loadData();
});

const uploadTargets = computed(() =>
	apps.value
		.filter((item) => !!item.visitId)
		.map((item) => ({
			label: `${item.title} · ${item.startTime.split(' ')[0]}`,
			value: item.visitId as number,
		}))
);

function fmtTime(v?: string) {
	if (!v) return '';
	return v.replace('T', ' ').slice(0, 16);
}

function mapStatus(status?: string): 'approved' | 'rescheduled' {
	return status === '5' ? 'rescheduled' : 'approved';
}

async function loadData() {
	try {
		const res = await fetchAggregateList();
		apps.value = (res.data || []).map((item: any) => ({
			id: `EH-${item.applyId}`,
			title: item.title || '-',
			startTime: fmtTime(item.startTime) || '-- --',
			status: mapStatus(item.status),
			visitId: item.visitId || null,
			photos: item.photos || [],
		}));
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载现场照片失败');
	}
}

function openUpload() {
	selectedVisitId.value = uploadTargets.value[0]?.value || null;
	pendingFiles.value = [];
	uploadVisible.value = true;
}

function onFileChange(event: Event) {
	const input = event.target as HTMLInputElement;
	pendingFiles.value = Array.from(input.files || []);
}

function openUploadPicker() {
	uploadInputRef.value?.click();
}

async function submitUpload() {
	if (!selectedVisitId.value || pendingFiles.value.length === 0) {
		return;
	}
	try {
		const photos = [];
		for (const file of pendingFiles.value) {
			photos.push(await uploadFile(file));
		}
		await saveBatchPhotos({
			visitId: selectedVisitId.value,
			photos,
		});
		uploadVisible.value = false;
		ElMessage.success('照片上传成功');
		await loadData();
	} catch (e: any) {
		ElMessage.error(e?.msg || '上传照片失败');
	}
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
	border: 1px solid var(--t-border);
	object-fit: cover;
	background: var(--t-surface-warm);
}
.eh-photo__none {
	display: flex;
	align-items: center;
	gap: 6px;
	font-size: 12px;
	color: var(--t-text3);
	padding: 12px 0;
}
.eh-photo__picker {
	border: 1px dashed var(--t-border-dark);
	border-radius: 6px;
	padding: 20px;
	background: var(--t-surface);
	text-align: center;
	cursor: pointer;
}
</style>
