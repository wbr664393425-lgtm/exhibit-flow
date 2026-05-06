<template>
  <div class="eh-step">
    <template v-for="(s, i) in steps" :key="i">
      <div class="eh-step__item">
        <div
          class="eh-step__circle"
          :class="{
            'eh-step__circle--done': i + 1 < cur,
            'eh-step__circle--active': i + 1 === cur,
          }"
        >
          <Ic v-if="i + 1 < cur" n="check" :size="11" color="#fff" />
          <template v-else>{{ i + 1 }}</template>
        </div>
        <span
          class="eh-step__label"
          :class="{ 'eh-step__label--active': i + 1 === cur }"
        >{{ s }}</span>
      </div>
      <div
        v-if="i < steps.length - 1"
        class="eh-step__line"
        :class="{ 'eh-step__line--done': i + 1 < cur }"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import Ic from './Ic.vue';
defineProps<{ steps: string[]; cur: number }>();
</script>

<style scoped>
.eh-step {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}
.eh-step__item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.eh-step__circle {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
  border: 1.5px solid var(--t-border);
  background: var(--t-surface);
  color: var(--t-text3);
  transition: all 0.2s;
}
.eh-step__circle--done {
  background: var(--t-success);
  border-color: var(--t-success);
  color: #fff;
}
.eh-step__circle--active {
  background: var(--t-accent);
  border-color: var(--t-accent);
  color: #f8fbff;
}
.eh-step__label {
  font-size: 12px;
  color: var(--t-text3);
  white-space: nowrap;
}
.eh-step__label--active {
  color: var(--t-accent-strong);
  font-weight: 700;
}
.eh-step__line {
  flex: 1;
  height: 1px;
  background: var(--t-border);
  margin: 0 10px;
  transition: background 0.2s;
}
.eh-step__line--done {
  background: var(--t-success);
}
</style>
