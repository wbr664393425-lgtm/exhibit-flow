<template>
  <div class="eh-notif">
    <div class="eh-notif__head">
      <div>
        <h2 class="eh-notif__title">通知中心</h2>
        <p class="eh-notif__sub">未读 {{ unread }} 条</p>
      </div>
      <button v-if="unread > 0" class="eh-notif__mark" @click="onMarkAll">全部标已读</button>
    </div>

    <div class="eh-notif__list">
      <div
        v-for="n in items"
        :key="n.id"
        class="eh-notif__item"
        :class="{ 'eh-notif__item--read': n.read, 'eh-notif__item--linked': !!n.applyId }"
        @click="onItemClick(n)"
      >
        <div class="eh-notif__avatar" :style="{ background: colorMap[n.type].bg, borderColor: colorMap[n.type].c + '22' }">
          <Ic :n="n.icon" :size="14" :color="colorMap[n.type].c" />
        </div>
        <div class="eh-notif__body">
          <div class="eh-notif__line">
            <span class="eh-notif__title-line" :class="{ 'eh-notif__title-line--unread': !n.read }">{{ n.title }}</span>
            <span v-if="!n.read" class="eh-notif__dot" />
          </div>
          <div class="eh-notif__text">{{ n.body }}</div>
          <MonoLabel :style="{ fontSize: '9px' }">{{ n.time }}</MonoLabel>
        </div>
        <Ic v-if="n.applyId" n="chevronRight" :size="12" color="var(--t-text3)" style="flex-shrink:0;align-self:center" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Ic, MonoLabel } from '../../components/eh';
import { fetchNotifications, markAllNotificationsRead, markNotificationRead } from '../../api/eh/notice';
import type { Notification } from '../../mock/applications';

const router = useRouter();
const items = ref<Notification[]>([]);

onMounted(async () => {
  items.value = await fetchNotifications();
});

const unread = computed(() => items.value.filter((n) => !n.read).length);

const colorMap: Record<Notification['type'], { c: string; bg: string }> = {
  approval: { c: '#f59e0b', bg: '#fffbeb' },
  approved: { c: 'var(--t-success)', bg: 'var(--t-success-light)' },
  rejected: { c: 'var(--t-danger)', bg: 'var(--t-danger-light)' },
  reminder: { c: '#1d4ed8', bg: '#eff6ff' },
  system: { c: 'var(--t-text3)', bg: 'var(--t-bg)' },
};

async function onItemClick(n: Notification) {
  if (!n.read) {
    n.read = true;
    await markNotificationRead(n.id);
  }
  if (n.applyId) {
    router.push(`/apply/${n.applyId}`);
  }
}
async function onMarkAll() {
  items.value.forEach((n) => (n.read = true));
  await markAllNotificationsRead();
}
</script>

<style scoped>
.eh-notif {
  padding: 16px;
}
.eh-notif__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}
.eh-notif__title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--t-text1);
}
.eh-notif__sub {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--t-text3);
}
.eh-notif__mark {
  border: 1px solid var(--t-border);
  background: var(--t-surface);
  color: var(--t-text2);
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 11px;
  font-family: inherit;
  font-weight: 600;
}
.eh-notif__list {
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: 8px;
  overflow: hidden;
}
.eh-notif__item {
  padding: 12px 14px;
  border-bottom: 1px solid var(--t-border);
  display: flex;
  gap: 10px;
  cursor: pointer;
  transition: background 0.1s;
  background: var(--t-bg);
}
.eh-notif__item:last-child {
  border-bottom: none;
}
.eh-notif__item--read {
  background: var(--t-surface);
}
.eh-notif__item--linked:active {
  background: var(--t-bg);
}
.eh-notif__avatar {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid transparent;
}
.eh-notif__body {
  flex: 1;
  min-width: 0;
}
.eh-notif__line {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 2px;
}
.eh-notif__title-line {
  font-size: 12px;
  font-weight: 500;
  color: var(--t-text1);
  flex: 1;
  line-height: 1.4;
}
.eh-notif__title-line--unread {
  font-weight: 700;
}
.eh-notif__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--t-accent);
  flex-shrink: 0;
  margin-top: 4px;
}
.eh-notif__text {
  font-size: 11px;
  color: var(--t-text3);
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
