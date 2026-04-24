<template>
  <button
    class="eh-btn"
    :class="[`eh-btn--${variant}`, `eh-btn--${size}`]"
    :disabled="disabled"
    @click="emit('click', $event)"
  >
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

<style scoped>
.eh-btn {
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.1s, transform 0.1s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: inherit;
  white-space: nowrap;
  letter-spacing: 0.01em;
  border: none;
  line-height: 1.2;
}
.eh-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}
.eh-btn:not(:disabled):hover {
  transform: scale(1.04);
}
.eh-btn:not(:disabled):active {
  transform: scale(0.95);
}

.eh-btn--sm { padding: 4px 10px; font-size: 11px; }
.eh-btn--md { padding: 7px 14px; font-size: 13px; }
.eh-btn--lg { padding: 10px 20px; font-size: 14px; }

.eh-btn--primary { background: var(--t-text1); color: #fff; }
.eh-btn--primary:not(:disabled):hover { background: #2d2d2b; }

.eh-btn--orange { background: var(--t-accent); color: #fff; }
.eh-btn--orange:not(:disabled):hover { background: #e64d00; }

.eh-btn--secondary { background: var(--t-bg); color: var(--t-text1); border: 1px solid var(--t-border); }
.eh-btn--secondary:not(:disabled):hover { background: var(--t-surface-warm); }

.eh-btn--ghost { background: transparent; color: var(--t-text2); border: 1px solid var(--t-border); }
.eh-btn--ghost:not(:disabled):hover { background: var(--t-bg); }

.eh-btn--danger { background: var(--t-danger); color: #fff; }
.eh-btn--danger:not(:disabled):hover { background: #991b1b; }

.eh-btn--success { background: var(--t-success); color: #fff; }
.eh-btn--success:not(:disabled):hover { background: #166534; }

.eh-btn--outline { background: transparent; color: var(--t-text1); border: 1px solid var(--t-text1); }
.eh-btn--outline:not(:disabled):hover { background: var(--t-bg); }
</style>
