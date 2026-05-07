<template>
  <div class="eh-notif">
    <div class="eh-notif__head">
      <div>
        <h2 class="eh-notif__title">通知中心</h2>
        <p class="eh-notif__sub">共 {{ items.length }} 条通知 · 未读 {{ unread }} 条</p>
      </div>
      <button v-if="unread > 0" class="eh-notif__mark" @click="onMarkAll">全部标已读</button>
    </div>

    <div class="eh-notif__summary">
      <div class="eh-notif__summary-icon">
        <img :src="noticeIconMap.reminder" alt="" />
      </div>
      <div>
        <div class="eh-notif__summary-title">展厅申请动态</div>
        <div class="eh-notif__summary-text">审批结果、归还签收和系统提醒会在这里同步</div>
      </div>
    </div>

    <div class="eh-notif__list">
      <div
        v-for="n in items"
        :key="n.id"
        class="eh-notif__item"
        :class="{ 'eh-notif__item--read': n.read, 'eh-notif__item--linked': !!n.applyId }"
        @click="onItemClick(n)"
      >
        <div class="eh-notif__avatar">
          <img :src="noticeIconFor(n)" alt="" />
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
import noticeApproved from '../../assets/notify-icons/notice-approved.png';
import noticeBell from '../../assets/notify-icons/notice-bell.png';
import noticePending from '../../assets/notify-icons/notice-pending.png';
import noticeRejected from '../../assets/notify-icons/notice-rejected.png';
import noticeReturned from '../../assets/notify-icons/notice-returned.png';
import noticeSystem from '../../assets/notify-icons/notice-system.png';

const router = useRouter();
const items = ref<Notification[]>([]);

onMounted(async () => {
  items.value = await fetchNotifications();
});

const unread = computed(() => items.value.filter((n) => !n.read).length);

const noticeIconMap: Record<Notification['type'], string> = {
  approval: noticePending,
  approved: noticeApproved,
  rejected: noticeRejected,
  reminder: noticeBell,
  system: noticeSystem,
};

function noticeIconFor(n: Notification) {
  if (n.title.includes('归还') || n.body.includes('归还签收')) return noticeReturned;
  return noticeIconMap[n.type] || noticeSystem;
}

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
  color: var(--t-text1);
}
.eh-notif__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.eh-notif__title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--t-text1);
}
.eh-notif__sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--t-text3);
}
.eh-notif__mark {
  border: 1px solid var(--t-accent-border);
  background: var(--t-accent-light);
  color: var(--t-accent-strong);
  padding: 6px 11px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 11px;
  font-family: inherit;
  font-weight: 600;
  box-shadow: rgba(47, 103, 216, 0.08) 0 6px 14px;
  white-space: nowrap;
}
.eh-notif__summary {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 13px 14px;
  margin-bottom: 12px;
  border: 1px solid var(--t-border-dark);
  border-radius: 10px;
  background:
    linear-gradient(135deg, rgba(47, 103, 216, 0.1), rgba(47, 103, 216, 0.02)),
    var(--t-surface);
  box-shadow: rgba(47, 103, 216, 0.08) 0 10px 26px;
}
.eh-notif__summary-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--t-accent-light);
  border: 1px solid var(--t-accent-border);
  overflow: hidden;
}
.eh-notif__summary-icon img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}
.eh-notif__summary-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--t-text1);
  line-height: 1.3;
}
.eh-notif__summary-text {
  margin-top: 3px;
  font-size: 11px;
  line-height: 1.5;
  color: var(--t-text2);
}
.eh-notif__list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.eh-notif__item {
  position: relative;
  padding: 13px 13px 13px 14px;
  border: 1px solid var(--t-border);
  border-radius: 10px;
  display: flex;
  gap: 10px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: rgba(47, 103, 216, 0.06) 0 8px 20px;
  overflow: hidden;
}
.eh-notif__item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--t-accent);
}
.eh-notif__item--read {
  background: var(--t-surface);
  box-shadow: none;
}
.eh-notif__item--read::before {
  background: var(--t-border-dark);
}
.eh-notif__item--linked:active {
  background: var(--t-surface-warm);
  border-color: var(--t-accent-border);
}
.eh-notif__avatar {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: rgba(47, 103, 216, 0.08) 0 8px 18px;
}
.eh-notif__avatar img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}
.eh-notif__body {
  flex: 1;
  min-width: 0;
}
.eh-notif__line {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 4px;
}
.eh-notif__title-line {
  font-size: 13px;
  font-weight: 600;
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
  font-size: 12px;
  color: var(--t-text2);
  margin-bottom: 5px;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
