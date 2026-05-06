<template>
	<div class="eh-input" style="display:flex;flex-direction:column;gap:4px">
		<MonoLabel v-if="label">
			<span v-if="required" style="color:var(--t-accent);margin-right:3px">✱</span>
			{{ label }}
		</MonoLabel>
		<slot>
			<el-date-picker
				v-if="type === 'datetime'"
				:model-value="modelValue as string"
				type="datetime"
				value-format="YYYY-MM-DD HH:mm:ss"
				format="YYYY-MM-DD HH:mm"
				:placeholder="placeholder || '请选择日期时间'"
				:disabled="disabled"
				style="width:100%"
				@update:model-value="(v: string) => emit('update:modelValue', v || '')"
			/>
			<el-time-picker
				v-else-if="type === 'time'"
				:model-value="modelValue as string"
				value-format="HH:mm"
				format="HH:mm"
				:placeholder="placeholder || '请选择时间'"
				:disabled="disabled"
				style="width:100%"
				@update:model-value="(v: string) => emit('update:modelValue', v || '')"
			/>
			<input
				v-else-if="type !== 'textarea'"
				:type="type"
				:value="modelValue"
				:placeholder="placeholder"
				:disabled="disabled"
				class="eh-input__field"
				@input="onInput"
			/>
			<textarea
				v-else
				:value="modelValue"
				:placeholder="placeholder"
				:disabled="disabled"
				:rows="rows ?? 3"
				class="eh-input__field eh-input__field--textarea"
				@input="onInput"
			/>
		</slot>
		<span v-if="hint" class="eh-input__hint">{{ hint }}</span>
	</div>
</template>

<script setup lang="ts">
import { ElDatePicker, ElTimePicker } from 'element-plus';
import MonoLabel from './MonoLabel.vue';
withDefaults(
	defineProps<{
		label?: string;
		modelValue?: string | number;
		placeholder?: string;
		type?: string;
		required?: boolean;
		disabled?: boolean;
		hint?: string;
		rows?: number;
	}>(),
	{ type: 'text' }
);
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>();
function onInput(e: Event) {
	const target = e.target as HTMLInputElement | HTMLTextAreaElement;
	emit('update:modelValue', target.value);
}
</script>
