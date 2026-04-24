<template>
	<div style="display:flex;flex-direction:column">
		<div v-for="(n, i) in nodes" :key="i" style="display:flex;gap:12px">
			<div style="display:flex;flex-direction:column;align-items:center">
				<div
					:style="{
						width: '28px',
						height: '28px',
						borderRadius: '50%',
						background: n.action === 'waiting' ? 'var(--t-bg)' : colors[n.action] + '18',
						border: `1.5px solid ${colors[n.action] || 'var(--t-border)'}`,
						display: 'flex',
						alignItems: 'center',
						justifyContent: 'center',
						flexShrink: 0,
					}"
				>
					<Ic
						:n="iconMap[n.action] || 'clock'"
						:size="13"
						:color="n.action === 'waiting' ? 'var(--t-text3)' : colors[n.action]"
					/>
				</div>
				<div v-if="i < nodes.length - 1" style="width:1px;flex:1;background:var(--t-border);margin:3px 0;min-height:18px" />
			</div>
			<div :style="{ flex: 1, paddingTop: '3px', paddingBottom: i === nodes.length - 1 ? 0 : '18px' }">
				<div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
					<span style="font-size:13px;font-weight:600;color:var(--t-text1)">{{ n.role }}</span>
					<span style="font-size:12px;color:var(--t-text2)">{{ n.name }}</span>
					<span v-if="n.time" style="font-size:11px;color:var(--t-text3);margin-left:auto">{{ n.time }}</span>
				</div>
				<p
					v-if="n.comment"
					style="margin:4px 0 0;font-size:12px;color:var(--t-text2);background:var(--t-bg);padding:5px 9px;border-radius:4px;border:1px solid var(--t-border)"
				>{{ n.comment }}</p>
				<p v-if="n.action === 'waiting'" style="margin:3px 0 0;font-size:11px;color:var(--t-text3)">等待上一节点完成后触发</p>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import Ic from './Ic.vue';

export interface ApprovalNode {
	role: string;
	name: string;
	action: 'approved' | 'rejected' | 'pending' | 'waiting' | string;
	time?: string | null;
	comment?: string;
}
defineProps<{ nodes: ApprovalNode[] }>();

const iconMap: Record<string, string> = {
	approved: 'checkCircle',
	rejected: 'xCircle',
	pending: 'clock',
	waiting: 'clock',
};
const colors: Record<string, string> = {
	approved: '#2c6415',
	rejected: '#c41c1c',
	pending: '#f59e0b',
	waiting: '#dedbd6',
};
</script>
