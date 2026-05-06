<template>
	<div class="eh-scope eh-photo">
		<div class="eh-photo__head">
			<div>
					<h2 class="eh-photo__title">现场照片</h2>
					<p class="eh-photo__sub">现场照片直接关联申请记录，可按数据表模板导出</p>
				</div>
				<div style="display:flex;gap:8px">
					<Btn variant="ghost" icon="download" v-auth="'eh_visit_photo_view'" @click="exportDataTable">导出数据表</Btn>
					<Btn variant="ghost" icon="download" v-auth="'eh_visit_photo_view'" @click="exportPhotoZip()">导出全部图片</Btn>
					<Btn variant="primary" icon="upload" v-auth="'eh_visit_photo_add'" @click="openUpload">上传照片</Btn>
				</div>
		</div>

		<div class="eh-photo__tools">
			<el-input
				v-model="searchKeyword"
				clearable
				placeholder="搜索申请记录、单位、日期或编号"
				style="width: 320px"
			/>
			<MonoLabel>按参观日期倒序</MonoLabel>
		</div>

		<Card v-for="app in visibleApps" :key="app.id" :style="{ padding: '16px 18px', marginBottom: '8px' }">
			<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
				<div style="display:flex;align-items:center;gap:10px">
					<span style="font-size:14px;font-weight:700;color:var(--t-text1)">{{ app.title }}</span>
					<Badge :status="app.status" />
				</div>
				<div style="display:flex;align-items:center;gap:8px">
					<MonoLabel>{{ app.startTime.split(' ')[0] }} · {{ app.photos.length }} 张</MonoLabel>
					<Btn v-if="app.photos.length" variant="ghost" size="sm" icon="download" v-auth="'eh_visit_photo_view'" @click="exportPhotoZip(app)">导出图片</Btn>
				</div>
			</div>
			<div v-if="app.photos.length" class="eh-photo__grid">
				<div v-for="(photo, index) in app.photos" :key="photo.id" class="eh-photo__thumb-wrap" @click="openPreview(app, index)">
					<img :src="photo.fileUrl" :alt="photo.fileName" class="eh-photo__thumb" />
					<button class="eh-photo__thumb-download" title="下载" @click.stop="downloadPhoto(photo)">
						<Ic n="download" :size="12" color="#fff" />
					</button>
					<div class="eh-photo__thumb-del" @click.stop="deletePhoto(photo, app)">
						<Ic n="x" :size="12" color="#fff" />
					</div>
				</div>
			</div>
			<div v-else class="eh-photo__none"><Ic n="camera" :size="18" color="var(--t-text3)" />暂无照片</div>
		</Card>
		<div v-if="visibleApps.length === 0" class="eh-photo__empty">
			<Ic n="camera" :size="40" color="var(--t-text3)" />
			<p>暂无匹配的现场照片记录</p>
		</div>

		<el-dialog v-model="uploadVisible" title="上传现场照片" width="520" :close-on-click-modal="false">
			<div style="display:flex;flex-direction:column;gap:14px">
				<div>
						<MonoLabel :style="{ display: 'block', marginBottom: '6px' }">关联申请记录</MonoLabel>
						<el-select v-model="selectedApplyId" filterable placeholder="请选择申请记录" style="width:100%">
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
				<Btn variant="primary" icon="upload" :disabled="!selectedApplyId || pendingFiles.length === 0" @click="submitUpload">确认上传</Btn>
			</template>
		</el-dialog>

		<el-dialog
			v-model="previewVisible"
			width="82vw"
			class="eh-scope eh-photo-preview"
			:show-close="false"
			:close-on-click-modal="true"
			modal-class="eh-photo-preview__overlay"
		>
			<div v-if="previewPhoto" class="eh-photo-preview__body">
				<div class="eh-photo-preview__bar">
					<div>
						<div class="eh-photo-preview__title">{{ previewApp?.title || '现场照片' }}</div>
						<MonoLabel>{{ previewIndex + 1 }} / {{ previewApp?.photos.length || 0 }} · {{ previewPhoto.fileName }}</MonoLabel>
					</div>
					<div class="eh-photo-preview__actions">
						<Btn variant="ghost" size="sm" icon="download" @click="downloadPhoto(previewPhoto)">下载</Btn>
						<Btn variant="ghost" size="sm" icon="x" @click="previewVisible = false">关闭</Btn>
					</div>
				</div>
				<button class="eh-photo-preview__nav eh-photo-preview__nav--left" :disabled="!canSwitchPreview" @click="switchPreview(-1)">
					<Ic n="chevronLeft" :size="22" color="#fff" />
				</button>
				<img :src="previewPhoto.fileUrl" :alt="previewPhoto.fileName" class="eh-photo-preview__img" @click="downloadPhoto(previewPhoto)" />
				<button class="eh-photo-preview__nav eh-photo-preview__nav--right" :disabled="!canSwitchPreview" @click="switchPreview(1)">
					<Ic n="chevronRight" :size="22" color="#fff" />
				</button>
			</div>
		</el-dialog>

		<input ref="uploadInputRef" type="file" accept="image/*" multiple style="display:none" @change="onFileChange" />
	</div>
</template>

<script lang="ts" name="ehvisitphoto" setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
	import { ElDialog, ElInput, ElMessage, ElOption, ElSelect } from 'element-plus';
	import { Card, Btn, Badge, Ic, MonoLabel } from '/@/components/eh';
	import { fetchAggregateList, saveBatchPhotos, delObj } from '/@/api/eh/visit-photo';
	import request from '/@/utils/request';
	import { downBlobFile, handleBlobFile } from '/@/utils/other';

interface PhotoItem {
	id: number;
	fileUrl: string;
	fileName: string;
	fileSize: number;
}

interface PhotoAppItem {
	id: string;
	applyId: string;
	title: string;
	unit: string;
	startTime: string;
	status: 'draft' | 'pending' | 'approved' | 'rejected' | 'cancelled' | 'rescheduled';
	photos: PhotoItem[];
}

const apps = ref<PhotoAppItem[]>([]);
const uploadVisible = ref(false);
const selectedApplyId = ref<string | null>(null);
const pendingFiles = ref<File[]>([]);
const uploadInputRef = ref<HTMLInputElement>();
const searchKeyword = ref('');
const previewVisible = ref(false);
const previewApp = ref<PhotoAppItem | null>(null);
const previewIndex = ref(0);

onMounted(() => {
	loadData();
	document.addEventListener('keydown', onPreviewKeydown);
});

onBeforeUnmount(() => {
	document.removeEventListener('keydown', onPreviewKeydown);
});

const uploadTargets = computed(() =>
	sortedApps.value
		.map((item) => ({
			label: `${item.title} · ${item.unit} · ${item.startTime.split(' ')[0]}`,
			value: item.applyId,
		}))
);

const sortedApps = computed(() =>
	[...apps.value].sort((a, b) => {
		const timeDiff = dateValue(b.startTime) - dateValue(a.startTime);
		return timeDiff || b.applyId.localeCompare(a.applyId);
	})
);

const visibleApps = computed(() => {
	const keyword = searchKeyword.value.trim().toLowerCase();
	if (!keyword) return sortedApps.value;
	return sortedApps.value.filter((item) =>
		[item.title, item.unit, item.startTime, item.id, String(item.applyId)]
			.some((text) => text.toLowerCase().includes(keyword))
	);
});

const previewPhoto = computed(() => previewApp.value?.photos[previewIndex.value] || null);
const canSwitchPreview = computed(() => (previewApp.value?.photos.length || 0) > 1);

function fmtTime(v?: string) {
	if (!v) return '';
	return v.replace('T', ' ').slice(0, 16);
}

function dateValue(v?: string) {
	const time = v ? new Date(v.replace(' ', 'T')).getTime() : 0;
	return Number.isNaN(time) ? 0 : time;
}

function mapStatus(status?: string): PhotoAppItem['status'] {
	const map: Record<string, PhotoAppItem['status']> = {
		'0': 'draft',
		'1': 'pending',
		'2': 'approved',
		'3': 'rejected',
		'4': 'cancelled',
		'5': 'rescheduled',
	};
	return map[status || ''] || 'pending';
}

async function loadData() {
	try {
		const res = await fetchAggregateList();
		apps.value = (res.data || []).map((item: any) => ({
			id: `EH-${String(item.applyId)}`,
			applyId: String(item.applyId),
				title: item.title || '-',
				unit: item.unit || '-',
				startTime: fmtTime(item.startTime) || '-- --',
				status: mapStatus(item.status),
				photos: item.photos || [],
			}));
	} catch (e: any) {
		ElMessage.error(e?.msg || '加载现场照片失败');
	}
}

function openUpload() {
	selectedApplyId.value = uploadTargets.value[0]?.value || null;
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
	if (!selectedApplyId.value || pendingFiles.value.length === 0) {
		return;
	}
	try {
		const photos = [];
		for (const file of pendingFiles.value) {
			photos.push(await uploadFile(file));
		}
		await saveBatchPhotos({
			applyId: selectedApplyId.value,
			photos,
		}).then((res: any) => {
			if (res?.data !== true) {
				throw new Error('照片关联申请记录失败');
			}
		});
		uploadVisible.value = false;
		ElMessage.success('照片上传成功');
		await loadData();
	} catch (e: any) {
		ElMessage.error(e?.msg || '上传照片失败');
	}
}

function exportDataTable() {
	downBlobFile('/admin/eh/visit/photo/export-data-table', {}, '展厅参观数据表.xlsx');
}

function exportPhotoZip(app?: PhotoAppItem) {
	const query = app ? { applyId: app.applyId } : {};
	const fileName = app ? `现场照片-${app.title}-${app.applyId}.zip` : '现场照片.zip';
	downBlobFile('/admin/eh/visit/photo/export-zip', query, sanitizeFileName(fileName));
}

function openPreview(app: PhotoAppItem, index: number) {
	previewApp.value = app;
	previewIndex.value = index;
	previewVisible.value = true;
}

function switchPreview(offset: number) {
	const photos = previewApp.value?.photos || [];
	if (photos.length <= 1) return;
	previewIndex.value = (previewIndex.value + offset + photos.length) % photos.length;
}

function onPreviewKeydown(event: KeyboardEvent) {
	if (!previewVisible.value) return;
	if (event.key === 'ArrowLeft') {
		event.preventDefault();
		switchPreview(-1);
	}
	if (event.key === 'ArrowRight') {
		event.preventDefault();
		switchPreview(1);
	}
	if (event.key === 'Escape') {
		previewVisible.value = false;
	}
}

async function downloadPhoto(photo: PhotoItem) {
	try {
		const response = await fetch(photo.fileUrl, { credentials: 'include' });
		if (!response.ok) {
			throw new Error('下载失败');
		}
		handleBlobFile(await response.blob(), sanitizeFileName(photo.fileName || `现场照片-${photo.id}.jpg`));
	} catch (e: any) {
		ElMessage.error(e?.message || '下载照片失败');
	}
}

function sanitizeFileName(fileName: string) {
	return fileName.replace(/[\\/:*?"<>|\r\n]+/g, '_').trim() || '现场照片';
}

async function deletePhoto(photo: PhotoItem, app: PhotoAppItem) {
	try {
		await delObj([photo.id]);
		app.photos = app.photos.filter(p => p.id !== photo.id);
		ElMessage.success('照片已删除');
	} catch (e: any) {
		ElMessage.error(e?.msg || '删除失败');
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
.eh-photo__tools {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12px;
	margin-bottom: 12px;
}
.eh-photo__grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, 80px);
	gap: 10px;
}
.eh-photo__thumb-wrap {
	position: relative;
	width: 80px;
	height: 80px;
	border-radius: 6px;
	overflow: hidden;
	border: 1px solid var(--t-border);
	cursor: zoom-in;
}
.eh-photo__thumb {
	width: 100%;
	height: 100%;
	object-fit: cover;
	background: var(--t-surface-warm);
}
.eh-photo__thumb-del,
.eh-photo__thumb-download {
	position: absolute;
	top: 3px;
	width: 18px;
	height: 18px;
	border-radius: 50%;
	background: rgba(0, 0, 0, 0.55);
	border: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	opacity: 0;
	transition: opacity 0.15s;
}
.eh-photo__thumb-del {
	right: 3px;
}
.eh-photo__thumb-download {
	right: 24px;
}
.eh-photo__thumb-wrap:hover .eh-photo__thumb-del {
	opacity: 1;
}
.eh-photo__thumb-wrap:hover .eh-photo__thumb-download {
	opacity: 1;
}
.eh-photo__none {
	display: flex;
	align-items: center;
	gap: 6px;
	font-size: 12px;
	color: var(--t-text3);
	padding: 12px 0;
}
.eh-photo__empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 10px;
	padding: 72px 20px;
	color: var(--t-text3);
	font-size: 13px;
}
.eh-photo__picker {
	border: 1px dashed var(--t-border-dark);
	border-radius: 6px;
	padding: 20px;
	background: var(--t-surface);
	text-align: center;
	cursor: pointer;
}
:global(.eh-photo-preview__overlay) {
	background: rgba(61, 51, 40, 0.32);
	backdrop-filter: blur(2px);
}
:deep(.eh-photo-preview.el-dialog) {
	background: #fffaf2;
	border: 1px solid #eadfce;
	border-radius: 10px;
	overflow: hidden;
	box-shadow: 0 22px 60px rgba(64, 52, 38, 0.26);
	padding: 10px;
}
:deep(.eh-photo-preview .el-dialog__header) {
	display: none;
}
:deep(.eh-photo-preview .el-dialog__body) {
	padding: 0;
}
.eh-photo-preview__body {
	position: relative;
	min-height: 70vh;
	background: linear-gradient(180deg, #fbf6ed 0%, #f5eadb 100%);
	border: 1px solid #eadfce;
	border-radius: 8px;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
}
.eh-photo-preview__bar {
	position: absolute;
	top: 12px;
	left: 12px;
	right: 12px;
	z-index: 3;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 12px;
	padding: 10px 12px;
	background: rgba(255, 250, 242, 0.9);
	border: 1px solid rgba(218, 204, 184, 0.9);
	border-radius: 8px;
	box-shadow: 0 8px 24px rgba(91, 73, 50, 0.12);
	backdrop-filter: blur(8px);
}
.eh-photo-preview__title {
	color: #2b2722;
	font-size: 14px;
	font-weight: 700;
	margin-bottom: 3px;
}
.eh-photo-preview__bar :deep(.eh-mono) {
	color: #8b7d6d;
}
.eh-photo-preview__actions {
	display: flex;
	align-items: center;
	gap: 8px;
}
.eh-photo-preview__actions :deep(.eh-btn--ghost) {
	background: #fffdf8;
	border-color: #decfba;
	color: #4a4035;
}
.eh-photo-preview__img {
	max-width: 100%;
	max-height: 82vh;
	object-fit: contain;
	cursor: pointer;
	border-radius: 6px;
	box-shadow: 0 12px 34px rgba(76, 60, 38, 0.18);
}
.eh-photo-preview__nav {
	position: absolute;
	top: 50%;
	z-index: 2;
	width: 42px;
	height: 64px;
	border: 0;
	border-radius: 6px;
	background: rgba(180, 109, 64, 0.88);
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	transform: translateY(-50%);
	box-shadow: 0 10px 24px rgba(139, 82, 42, 0.22);
	transition: background 0.15s ease, transform 0.15s ease;
}
.eh-photo-preview__nav:hover {
	background: rgba(150, 82, 42, 0.95);
	transform: translateY(-50%) scale(1.02);
}
.eh-photo-preview__nav:disabled {
	opacity: 0.25;
	cursor: default;
}
.eh-photo-preview__nav--left {
	left: 14px;
}
.eh-photo-preview__nav--right {
	right: 14px;
}
</style>
