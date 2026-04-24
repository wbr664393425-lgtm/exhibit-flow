<template>
	<span class="eh-badge" :style="{ color: conf.color, background: conf.bg, border: `1px solid ${conf.dot}33` }">
		<span class="eh-badge__dot" :style="{ background: conf.dot }" />
		{{ conf.label }}
	</span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { STATUS_MAP, type ApplicationStatus } from './tokens';

const props = defineProps<{ status: ApplicationStatus | string }>();
const conf = computed(() => STATUS_MAP[(props.status as ApplicationStatus)] || STATUS_MAP.pending);
</script>
