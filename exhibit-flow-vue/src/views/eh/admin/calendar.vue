<template>
	<div class="eh-scope eh-cal">
		<div class="eh-cal__head">
			<div style="display:flex;align-items:center;gap:10px">
				<h2 class="eh-cal__title">排期日历</h2>
				<div style="display:flex;gap:2px">
					<button class="eh-cal__nav" @click="jump(-7)"><Ic n="chevronLeft" :size="13" /></button>
					<button class="eh-cal__nav" @click="jump(7)"><Ic n="chevronRight" :size="13" /></button>
					<div class="eh-cal__range">
						{{ dates[0].getMonth() + 1 }}月 {{ dates[0].getDate() }}日 — {{ dates[6].getDate() }}日
					</div>
				</div>
			</div>
			<div style="display:flex;gap:12px;align-items:center">
				<span class="eh-cal__legend"><span style="background:var(--t-success)" />已批准</span>
				<span class="eh-cal__legend"><span style="background:var(--t-warning)" />审批中</span>
				<Btn variant="outline" size="sm" icon="download" @click="onExport">导出</Btn>
			</div>
		</div>

		<Card :style="{ overflow: 'hidden' }">
			<!-- 星期头 -->
			<div class="eh-cal__grid eh-cal__thead">
				<div class="eh-cal__gutter" />
				<div
					v-for="(d, i) in dates"
					:key="i"
					class="eh-cal__wd-cell"
					:class="{ 'eh-cal__wd-cell--today': isToday(d) }"
				>
					<MonoLabel :style="{ color: isToday(d) ? 'var(--t-accent)' : 'var(--t-text3)', display: 'block', marginBottom: '3px' }">{{ WD[i] }}</MonoLabel>
					<div class="eh-cal__wd-num" :class="{ 'eh-cal__wd-num--today': isToday(d) }">{{ d.getDate() }}</div>
				</div>
			</div>

			<!-- 时间槽 -->
			<div class="eh-cal__body">
				<div v-for="h in hours" :key="h" class="eh-cal__grid eh-cal__row">
					<div class="eh-cal__hour">{{ h }}:00</div>
					<div
						v-for="(d, i) in dates"
						:key="i"
						class="eh-cal__slot"
						:class="{ 'eh-cal__slot--today': isToday(d) }"
					>
					<div
						v-for="ev in slotEvents(d, h)"
						:key="ev.id"
						:class="['eh-cal__event', eventClass(ev)]"
						:style="eventStyle(ev)"
						@click="ElMessage.info(`${ev.title}：${ev.startTime} — ${ev.end}`)"
					>
							<div class="eh-cal__event-title">{{ ev.title }}</div>
							<div v-if="eventSize(ev) === 'lg' || eventSize(ev) === 'xl'" class="eh-cal__event-meta">
								{{ ev.status === 'approved' ? '已批准' : '审批中' }}
							</div>
							<div class="eh-cal__event-time">{{ ev.startTime }} — {{ ev.end }}</div>
						</div>
					</div>
				</div>
			</div>
		</Card>
	</div>
</template>

<script lang="ts" name="ehcalendar" setup>
import { computed, onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Card, Btn, Ic, MonoLabel } from '/@/components/eh';
import { fetchCalendar } from '/@/api/eh/calendar';

interface CalEvent { id: string; title: string; start: string; startTime: string; end: string; status: string; }

const WD = ['日', '一', '二', '三', '四', '五', '六'];
const cur = ref(new Date('2026-04-23'));
const events = ref<CalEvent[]>([]);

onMounted(async () => {
	const res = await fetchCalendar();
	events.value = res.data || [];
});

const dates = computed(() => {
	const d = cur.value;
	const s = new Date(d);
	s.setDate(d.getDate() - d.getDay());
	return Array.from({ length: 7 }, (_, i) => {
		const x = new Date(s);
		x.setDate(s.getDate() + i);
		return x;
	});
});

const hours = Array.from({ length: 11 }, (_, i) => i + 8);
const HOUR_ROW_HEIGHT = 52;

function ds(d: Date) {
	return d.toISOString().split('T')[0];
}
function isToday(d: Date) {
	return ds(d) === '2026-04-23';
}
function jump(delta: number) {
	const n = new Date(cur.value);
	n.setDate(cur.value.getDate() + delta);
	cur.value = n;
}
function slotEvents(d: Date, h: number) {
	return events.value.filter((e) => e.start === ds(d) && parseInt(e.startTime.split(':')[0], 10) === h);
}
function toMinutes(time: string) {
	const [hh = '0', mm = '0'] = time.split(':');
	return parseInt(hh, 10) * 60 + parseInt(mm, 10);
}
function eventDurationMinutes(ev: CalEvent) {
	const startMinute = toMinutes(ev.startTime);
	const endMinute = toMinutes(ev.end);
	return Math.max(15, endMinute - startMinute);
}
function eventSize(ev: CalEvent) {
	const duration = eventDurationMinutes(ev);
	if (duration <= 60) return 'sm';
	if (duration <= 120) return 'md';
	if (duration <= 180) return 'lg';
	return 'xl';
}
function eventClass(ev: CalEvent) {
	return `eh-cal__event--${eventSize(ev)}`;
}
function eventStyle(ev: CalEvent) {
	const startMinute = toMinutes(ev.startTime);
	const duration = eventDurationMinutes(ev);
	const minuteInHour = startMinute % 60;
	const pxPerMinute = HOUR_ROW_HEIGHT / 60;
	const approved = ev.status === 'approved';
	return {
		background: approved
			? 'linear-gradient(180deg, #2f8d2f 0%, #236f23 100%)'
			: 'linear-gradient(180deg, #d18a1a 0%, #b87710 100%)',
		border: approved ? '1px solid rgba(28, 83, 28, 0.5)' : '1px solid rgba(132, 86, 15, 0.5)',
		top: `${minuteInHour * pxPerMinute + 2}px`,
		height: `${Math.max(18, duration * pxPerMinute - 4)}px`
	};
}
function onExport() {
	ElMessage.info('正在生成导出文件…');
}
</script>

<style scoped>
.eh-cal {
	padding: 20px 24px;
	background: var(--t-bg);
	min-height: calc(100vh - 100px);
}
.eh-cal__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 14px;
	flex-wrap: wrap;
	gap: 12px;
}
.eh-cal__title {
	margin: 0;
	font-size: 19px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-cal__nav {
	border: 1px solid var(--t-border);
	background: var(--t-surface);
	border-radius: 6px;
	width: 28px;
	height: 28px;
	cursor: pointer;
	display: flex;
	align-items: center;
	justify-content: center;
	color: var(--t-text2);
	transition: all 0.2s ease;
}
.eh-cal__nav:hover {
	background: #fff;
	color: var(--t-text1);
	border-color: #ddd3c8;
}
.eh-cal__range {
	margin: 0 4px;
	padding: 5px 12px;
	background: #fff;
	border: 1px solid var(--t-border);
	border-radius: 6px;
	font-size: 12px;
	font-weight: 600;
	color: var(--t-text1);
	box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.eh-cal__legend {
	display: inline-flex;
	align-items: center;
	gap: 6px;
	font-size: 12px;
	color: var(--t-text2);
}
.eh-cal__legend span {
	width: 9px;
	height: 9px;
	border-radius: 999px;
	display: inline-block;
	box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}
.eh-cal__body {
	max-height: 540px;
	overflow: auto;
	background: #fff;
}
.eh-cal__body::-webkit-scrollbar {
	width: 8px;
}
.eh-cal__body::-webkit-scrollbar-thumb {
	background: #ddd5cc;
	border-radius: 999px;
}
.eh-cal__body::-webkit-scrollbar-track {
	background: #f5f2ed;
}

.eh-cal__grid {
	display: grid;
	grid-template-columns: 56px repeat(7, 1fr);
}
.eh-cal__thead {
	background: #faf8f5;
	border-bottom: 2px solid var(--t-border);
}
.eh-cal__gutter {
	border-right: 1px solid var(--t-border);
}
.eh-cal__wd-cell {
	padding: 10px 6px;
	text-align: center;
	border-left: 1px solid var(--t-border);
}
.eh-cal__wd-cell--today {
	background: linear-gradient(180deg, #fff7ea 0%, #fffdf7 100%);
}
.eh-cal__wd-num {
	width: 30px;
	height: 30px;
	border-radius: 50%;
	margin: 0 auto;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 14px;
	font-weight: 700;
	color: var(--t-text1);
}
.eh-cal__wd-num--today {
	background: linear-gradient(180deg, #2f3133 0%, #1d1f21 100%);
	color: #fff;
}
.eh-cal__row {
	height: 52px;
	border-bottom: 1px solid #efe9e1;
}
.eh-cal__hour {
	padding: 6px 8px;
	font-size: 11px;
	color: var(--t-text3);
	text-align: right;
	background: #faf8f5;
	border-right: 1px solid var(--t-border);
	padding-top: 9px;
	font-family: var(--t-font-mono);
	letter-spacing: 0.2px;
}
.eh-cal__slot {
	border-left: 1px solid #f0ede8;
	background:
		linear-gradient(to bottom, transparent calc(50% - 0.5px), rgba(237, 231, 223, 0.45) calc(50% - 0.5px), rgba(237, 231, 223, 0.45) calc(50% + 0.5px), transparent calc(50% + 0.5px));
	padding: 0;
	position: relative;
	overflow: visible;
}
.eh-cal__slot--today {
	background:
		linear-gradient(to bottom, transparent calc(50% - 0.5px), rgba(240, 226, 197, 0.55) calc(50% - 0.5px), rgba(240, 226, 197, 0.55) calc(50% + 0.5px), transparent calc(50% + 0.5px)),
		linear-gradient(180deg, #fffef8 0%, #fffdf6 100%);
}
.eh-cal__event {
	color: #fff;
	border-radius: 8px;
	padding: 7px 9px;
	font-size: 11px;
	font-weight: 600;
	cursor: pointer;
	position: absolute;
	left: 3px;
	right: 3px;
	z-index: 2;
	box-shadow: 0 4px 10px rgba(0, 0, 0, 0.14);
	overflow: hidden;
	transition: transform 0.15s ease, box-shadow 0.15s ease;
	display: flex;
	flex-direction: column;
	gap: 3px;
}
.eh-cal__event:hover {
	transform: translateY(-1px);
	box-shadow: 0 8px 16px rgba(0, 0, 0, 0.18);
}
.eh-cal__event--sm {
	padding: 5px 8px;
	justify-content: center;
}
.eh-cal__event--sm .eh-cal__event-time {
	display: none;
}
.eh-cal__event--sm .eh-cal__event-title {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}
.eh-cal__event--md {
	justify-content: flex-start;
}
.eh-cal__event--md .eh-cal__event-title {
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}
.eh-cal__event--lg,
.eh-cal__event--xl {
	justify-content: space-between;
}
.eh-cal__event--lg .eh-cal__event-title,
.eh-cal__event--xl .eh-cal__event-title {
	white-space: normal;
	overflow: visible;
	display: block;
}
.eh-cal__event--lg .eh-cal__event-time,
.eh-cal__event--xl .eh-cal__event-time {
	align-self: flex-start;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 999px;
	padding: 2px 8px;
}
.eh-cal__event--xl {
	padding-top: 9px;
	padding-bottom: 9px;
}
.eh-cal__event--xl::after {
	content: '';
	position: absolute;
	inset: 0;
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.14) 0%, rgba(255, 255, 255, 0) 55%);
	pointer-events: none;
}
.eh-cal__event-title {
	white-space: normal;
	overflow-wrap: anywhere;
	word-break: break-word;
	line-height: 1.35;
	letter-spacing: 0.1px;
	text-shadow: 0 1px 1px rgba(0, 0, 0, 0.16);
}
.eh-cal__event-meta {
	font-size: 10px;
	font-weight: 500;
	opacity: 0.95;
	padding: 1px 7px;
	width: fit-content;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.18);
}
.eh-cal__event-time {
	font-size: 10px;
	opacity: 0.95;
	margin-top: 3px;
	font-family: var(--t-font-mono);
	letter-spacing: 0.2px;
}
</style>
