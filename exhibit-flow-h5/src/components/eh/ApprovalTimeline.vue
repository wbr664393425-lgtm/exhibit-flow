<template>
  <div class="eh-timeline">
    <div v-for="(n, i) in nodes" :key="i" class="eh-timeline__row">
      <div class="eh-timeline__gutter">
        <div
          class="eh-timeline__dot"
          :style="{
            background: n.action === 'waiting' ? 'var(--t-bg)' : colors[n.action] + '18',
            borderColor: colors[n.action] || 'var(--t-border)',
          }"
        >
          <Ic
            :n="iconMap[n.action] || 'clock'"
            :size="13"
            :color="n.action === 'waiting' ? 'var(--t-text3)' : colors[n.action]"
          />
        </div>
        <div
          v-if="i < nodes.length - 1"
          class="eh-timeline__line"
        />
      </div>
      <div class="eh-timeline__body" :style="{ paddingBottom: i === nodes.length - 1 ? 0 : '18px' }">
        <div class="eh-timeline__head">
          <span class="eh-timeline__role">{{ n.role }}</span>
          <span class="eh-timeline__name">{{ n.name }}</span>
          <span v-if="n.time" class="eh-timeline__time">{{ n.time }}</span>
        </div>
        <p v-if="n.comment" class="eh-timeline__comment">{{ n.comment }}</p>
        <p v-if="n.action === 'waiting'" class="eh-timeline__waiting">等待上一节点完成后触发</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Ic from './Ic.vue';

export interface ApprovalNode {
  role: string;
  name: string;
  action: 'approved' | 'rejected' | 'pending' | 'waiting' | string;
  time?: string | null;
  comment?: string;
}
defineProps<{ nodes: ApprovalNode[] }>();

const iconMap: Record<string, string> = {
  approved: 'checkCircle',
  rejected: 'xCircle',
  pending: 'clock',
  waiting: 'clock',
};
const colors: Record<string, string> = {
  approved: '#2c6415',
  rejected: '#c41c1c',
  pending: '#f59e0b',
  waiting: '#c5d8f5',
};
</script>

<style scoped>
.eh-timeline {
  display: flex;
  flex-direction: column;
}
.eh-timeline__row {
  display: flex;
  gap: 12px;
}
.eh-timeline__gutter {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.eh-timeline__dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1.5px solid var(--t-border);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.eh-timeline__line {
  width: 1px;
  flex: 1;
  background: var(--t-border);
  margin: 3px 0;
  min-height: 18px;
}
.eh-timeline__body {
  flex: 1;
  padding-top: 3px;
}
.eh-timeline__head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.eh-timeline__role {
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text1);
}
.eh-timeline__name {
  font-size: 12px;
  color: var(--t-text2);
}
.eh-timeline__time {
  font-size: 11px;
  color: var(--t-text3);
  margin-left: auto;
}
.eh-timeline__comment {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--t-text2);
  background: var(--t-bg);
  padding: 5px 9px;
  border-radius: 4px;
  border: 1px solid var(--t-border);
}
.eh-timeline__waiting {
  margin: 3px 0 0;
  font-size: 11px;
  color: var(--t-text3);
}
</style>
