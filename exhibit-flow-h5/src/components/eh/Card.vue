<template>
  <div
    class="eh-card"
    :class="{ 'eh-card--clickable': !!clickable }"
    @click="onClick"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{ clickable?: boolean }>();
const emit = defineEmits<{ (e: 'click'): void }>();
function onClick() {
  if (props.clickable) emit('click');
}
</script>

<style scoped>
.eh-card {
  background: var(--t-surface);
  border-radius: 8px;
  border: 1px solid var(--t-border);
  transition: border-color 0.15s;
}
.eh-card--clickable {
  cursor: pointer;
}
.eh-card--clickable:hover {
  border-color: var(--t-border-dark);
}
</style>
