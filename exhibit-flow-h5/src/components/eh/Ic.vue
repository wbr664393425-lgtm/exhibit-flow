<template>
  <span v-if="!path" :style="{ width: `${size}px`, height: `${size}px`, display: 'inline-block', flexShrink: 0 }" />
  <svg
    v-else
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    :stroke="color"
    :stroke-width="sw"
    stroke-linecap="round"
    stroke-linejoin="round"
    style="display: block; flex-shrink: 0"
  >
    <path v-for="(d, i) in paths" :key="i" :d="d" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ICONS } from './icons';

const props = withDefaults(
  defineProps<{ n: string; size?: number; color?: string; sw?: number }>(),
  { size: 16, color: 'currentColor', sw: 1.8 }
);

const path = computed(() => ICONS[props.n] || '');
const paths = computed(() => {
  if (!path.value) return [];
  return path.value
    .split('M')
    .filter(Boolean)
    .map((p) => 'M' + p);
});
</script>
