<template>
	<div style="display:flex;align-items:center;margin-bottom:24px">
		<template v-for="(s, i) in steps" :key="i">
			<div style="display:flex;align-items:center;gap:8px">
				<div
					:style="{
						width: '26px',
						height: '26px',
						borderRadius: '50%',
						display: 'flex',
						alignItems: 'center',
						justifyContent: 'center',
						fontSize: '11px',
						fontWeight: 700,
						flexShrink: 0,
						border: `1.5px solid ${i + 1 < cur ? 'var(--t-success)' : i + 1 === cur ? 'var(--t-text1)' : 'var(--t-border)'}`,
						background: i + 1 < cur ? 'var(--t-success)' : i + 1 === cur ? 'var(--t-text1)' : 'var(--t-surface)',
						color: i + 1 <= cur ? '#fff' : 'var(--t-text3)',
						transition: 'all 0.2s',
					}"
				>
					<Ic v-if="i + 1 < cur" n="check" :size="11" color="#fff" />
					<template v-else>{{ i + 1 }}</template>
				</div>
				<span :style="{ fontSize: '12px', fontWeight: i + 1 === cur ? 700 : 400, color: i + 1 === cur ? 'var(--t-text1)' : 'var(--t-text3)', whiteSpace: 'nowrap' }">{{ s }}</span>
			</div>
			<div v-if="i < steps.length - 1" :style="{ flex: 1, height: '1px', background: i + 1 < cur ? 'var(--t-success)' : 'var(--t-border)', margin: '0 10px' }" />
		</template>
	</div>
</template>

<script setup lang="ts">
import Ic from './Ic.vue';
defineProps<{ steps: string[]; cur: number }>();
</script>
