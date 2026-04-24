<template>
  <div class="eh-select" ref="rootRef">
    <MonoLabel v-if="label">
      <span v-if="required" class="eh-select__required">✱</span>
      {{ label }}
    </MonoLabel>

    <!-- 触发器 -->
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

    <!-- 选项列表 -->
    <Transition name="eh-sel">
      <div v-if="open" class="eh-select__drop">
        <div
          v-for="o in normalized"
          :key="o.value"
          class="eh-select__opt"
          :class="{ 'eh-select__opt--on': String(o.value) === String(modelValue) }"
          @click="pick(o.value)"
        >
          <span class="eh-select__opt-label">{{ o.label }}</span>
          <svg
            v-if="String(o.value) === String(modelValue)"
            class="eh-select__check"
            width="14" height="14" viewBox="0 0 24 24"
            fill="none" stroke="currentColor" stroke-width="2.5"
            stroke-linecap="round" stroke-linejoin="round"
          >
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
      </div>
    </Transition>

    <!-- 遮罩：点击外部关闭 -->
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
const rootRef = ref<HTMLElement>();

const normalized = computed(() =>
  props.options.map((o) => (typeof o === 'string' ? { value: o, label: o } : o))
);

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

/* 触发器 */
.eh-select__trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid var(--t-border);
  border-radius: 4px;
  padding: 8px 10px;
  font-size: 13px;
  background: var(--t-surface);
  cursor: pointer;
  box-sizing: border-box;
  transition: border-color 0.15s;
  min-height: 35px;
  line-height: 1.2;
}
.eh-select__trigger--open {
  border-color: var(--t-text1);
}
.eh-select__trigger--disabled {
  background: var(--t-bg);
  cursor: not-allowed;
  opacity: 0.6;
}
.eh-select__val {
  color: var(--t-text1);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.eh-select__ph {
  color: var(--t-text3);
  flex: 1;
}
.eh-select__chevron {
  color: var(--t-text3);
  display: flex;
  flex-shrink: 0;
  transition: transform 0.2s;
}
.eh-select__chevron--up {
  transform: rotate(180deg);
}

/* 下拉选项列表 */
.eh-select__drop {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: 6px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 100;
  max-height: 220px;
  overflow-y: auto;
  overscroll-behavior: contain;
}
.eh-select__opt {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 14px;
  font-size: 13px;
  color: var(--t-text1);
  cursor: pointer;
  border-bottom: 1px solid var(--t-surface-warm, #f5f2ed);
  transition: background 0.1s;
}
.eh-select__opt:last-child {
  border-bottom: none;
}
.eh-select__opt:active {
  background: var(--t-bg);
}
.eh-select__opt--on {
  color: var(--t-accent);
  font-weight: 600;
}
.eh-select__opt-label {
  flex: 1;
  min-width: 0;
}
.eh-select__check {
  color: var(--t-accent);
  flex-shrink: 0;
  margin-left: 8px;
}

/* 遮罩 */
.eh-select__mask {
  position: fixed;
  inset: 0;
  z-index: 99;
}

/* 展开/收起动画 */
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
