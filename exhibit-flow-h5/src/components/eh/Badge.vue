<template>
  <span
    class="eh-badge"
    :style="{
      color: conf.color,
      background: conf.bg,
      border: `1px solid ${conf.dot}33`,
    }"
  >
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

<style scoped>
.eh-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.02em;
  white-space: nowrap;
  line-height: 1;
}
.eh-badge__dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}
</style>
