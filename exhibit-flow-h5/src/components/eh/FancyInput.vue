<template>
  <div class="eh-input">
    <MonoLabel v-if="label">
      <span v-if="required" class="eh-input__required">✱</span>
      {{ label }}
    </MonoLabel>
    <slot>
      <input
        v-if="type !== 'textarea'"
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

<style scoped>
.eh-input {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.eh-input__required {
  color: var(--t-accent);
  margin-right: 3px;
}
.eh-input__field,
:slotted(.eh-input__field) {
  border: 1px solid var(--t-border-dark);
  border-radius: 4px;
  padding: 8px 10px;
  font-size: 16px;
  color: var(--t-text1);
  outline: none;
  background: #fffefa;
  width: 100%;
  box-sizing: border-box;
  transition: border-color 0.15s;
  font-family: inherit;
}
.eh-input__field:disabled,
:slotted(.eh-input__field:disabled) {
  background: var(--t-bg);
}
.eh-input__field:focus,
:slotted(.eh-input__field:focus) {
  border-color: var(--t-text1);
  box-shadow: 0 0 0 2px rgba(20, 20, 19, 0.08);
}
.eh-input__field::placeholder,
:slotted(.eh-input__field::placeholder) {
  color: #b0ada6;
}
.eh-input__field--textarea,
:slotted(.eh-input__field--textarea) {
  resize: vertical;
  min-height: 72px;
}
.eh-input__hint {
  font-size: 11px;
  color: var(--t-text3);
}
</style>
