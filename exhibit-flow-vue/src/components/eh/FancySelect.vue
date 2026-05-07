<template>
	<div class="eh-select">
		<MonoLabel v-if="label">
			<span v-if="required" class="eh-select__required">✱</span>
			{{ label }}
		</MonoLabel>
		<div
			class="eh-select__trigger"
			:class="{ 'eh-select__trigger--disabled': disabled, 'eh-select__trigger--open': open }"
			@click="toggle"
		>
			<span :class="currentLabel ? 'eh-select__val' : 'eh-select__ph'">
				{{ currentLabel || '请选择' }}
			</span>
			<span class="eh-select__chevron" :class="{ 'eh-select__chevron--up': open }">
				<Ic n="chevronDown" :size="13" />
			</span>
		</div>
		<Transition name="eh-sel">
			<div v-if="open" class="eh-select__drop">
				<div
					v-for="o in normalized"
					:key="String(o.value)"
					class="eh-select__opt"
					:class="{ 'eh-select__opt--on': String(o.value) === String(modelValue) }"
					@click="pick(o.value)"
				>
					<span class="eh-select__opt-label">{{ o.label }}</span>
					<Ic v-if="String(o.value) === String(modelValue)" n="check" :size="13" />
				</div>
			</div>
		</Transition>
		<div v-if="open" class="eh-select__mask" @click="open = false" />
	</div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
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
const open = ref(false);
const normalized = computed(() => props.options.map((o) => (typeof o === 'string' ? { value: o, label: o } : o)));
const currentLabel = computed(() => {
	const found = normalized.value.find((o) => String(o.value) === String(props.modelValue));
	return found?.label ?? '';
});

function toggle() {
	if (!props.disabled) open.value = !open.value;
}

function pick(val: string | number) {
	emit('update:modelValue', String(val));
	open.value = false;
}
</script>

<style scoped>
.eh-select {
	display: flex;
	flex-direction: column;
	gap: 4px;
	position: relative;
}
.eh-select__required {
	color: var(--t-accent);
	margin-right: 3px;
}
.eh-select__trigger {
	display: flex;
	align-items: center;
	justify-content: space-between;
	min-height: 36px;
	border: 1px solid var(--t-border-dark);
	border-radius: 4px;
	padding: 8px 10px;
	background: var(--t-surface);
	box-sizing: border-box;
	cursor: pointer;
	transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
}
.eh-select__trigger--open {
	border-color: var(--t-accent);
	box-shadow: 0 0 0 2px rgba(47, 103, 216, 0.12);
}
.eh-select__trigger--disabled {
	background: var(--t-bg);
	cursor: not-allowed;
	opacity: 0.65;
}
.eh-select__val,
.eh-select__ph {
	flex: 1;
	min-width: 0;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	font-size: 13px;
}
.eh-select__val {
	color: var(--t-text1);
}
.eh-select__ph {
	color: var(--t-text3);
}
.eh-select__chevron {
	display: flex;
	flex-shrink: 0;
	color: var(--t-text3);
	transition: transform 0.18s ease;
}
.eh-select__chevron--up {
	transform: rotate(180deg);
}
.eh-select__drop {
	position: absolute;
	top: calc(100% + 4px);
	left: 0;
	right: 0;
	max-height: 220px;
	overflow-y: auto;
	overscroll-behavior: contain;
	background: var(--t-surface);
	border: 1px solid var(--t-border);
	border-radius: 6px;
	box-shadow: 0 8px 24px rgba(47, 103, 216, 0.14);
	z-index: 120;
}
.eh-select__opt {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 8px;
	padding: 10px 12px;
	font-size: 13px;
	color: var(--t-text1);
	cursor: pointer;
	border-bottom: 1px solid var(--t-surface-warm);
}
.eh-select__opt:last-child {
	border-bottom: none;
}
.eh-select__opt:hover {
	background: var(--t-bg);
}
.eh-select__opt--on {
	color: var(--t-accent);
	font-weight: 600;
	background: var(--t-accent-light);
}
.eh-select__opt-label {
	min-width: 0;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.eh-select__mask {
	position: fixed;
	inset: 0;
	z-index: 119;
}
.eh-sel-enter-active,
.eh-sel-leave-active {
	transition: opacity 0.15s, transform 0.15s;
}
.eh-sel-enter-from,
.eh-sel-leave-to {
	opacity: 0;
	transform: translateY(-6px);
}
</style>
