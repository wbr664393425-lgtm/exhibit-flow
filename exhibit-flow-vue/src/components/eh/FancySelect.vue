<template>
	<div class="eh-select" style="display:flex;flex-direction:column;gap:4px">
		<MonoLabel v-if="label">
			<span v-if="required" style="color:var(--t-accent);margin-right:3px">✱</span>
			{{ label }}
		</MonoLabel>
		<div style="position:relative">
			<select :value="modelValue" :disabled="disabled" class="eh-select__field" @change="onChange">
				<option v-for="o in normalized" :key="String(o.value)" :value="o.value">{{ o.label }}</option>
			</select>
			<span style="position:absolute;right:8px;top:50%;transform:translateY(-50%);pointer-events:none;color:var(--t-text3);display:flex">
				<Ic n="chevronDown" :size="13" />
			</span>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MonoLabel from './MonoLabel.vue';
import Ic from './Ic.vue';

type RawOption = string | { value: string | number; label: string };
const props = defineProps<{
	label?: string;
	modelValue?: string | number;
	options: RawOption[];
	required?: boolean;
	disabled?: boolean;
}>();
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>();
const normalized = computed(() => props.options.map((o) => (typeof o === 'string' ? { value: o, label: o } : o)));
function onChange(e: Event) {
	emit('update:modelValue', (e.target as HTMLSelectElement).value);
}
</script>
