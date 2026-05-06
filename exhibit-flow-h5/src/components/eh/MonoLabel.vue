<template>
  <span class="eh-mono-label" :style="style">
    <slot />
  </span>
</template>

<script setup lang="ts">
import type { CSSProperties } from 'vue';
defineProps<{ style?: CSSProperties }>();
</script>

<style scoped>
.eh-mono-label {
  font-family: var(--t-font-mono);
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.9px;
  color: var(--t-text2);
}
</style>
