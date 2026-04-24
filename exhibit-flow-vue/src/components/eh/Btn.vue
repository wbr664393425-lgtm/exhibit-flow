<template>
	<button class="eh-btn" :class="[`eh-btn--${variant}`, `eh-btn--${size}`]" :disabled="disabled" @click="emit('click', $event)">
		<Ic v-if="icon" :n="icon" :size="iconSize" />
		<slot />
	</button>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import Ic from './Ic.vue';

const props = withDefaults(
	defineProps<{
		variant?: 'primary' | 'orange' | 'secondary' | 'ghost' | 'danger' | 'success' | 'outline';
		size?: 'sm' | 'md' | 'lg';
		disabled?: boolean;
		icon?: string;
	}>(),
	{ variant: 'primary', size: 'md' }
);
const emit = defineEmits<{ (e: 'click', ev: MouseEvent): void }>();
const iconSize = computed(() => (props.size === 'sm' ? 12 : props.size === 'lg' ? 15 : 14));
</script>
