<template>
  <div class="eh-layout">
    <div class="eh-layout__content">
      <router-view v-slot="{ Component }">
        <component :is="Component" />
      </router-view>
    </div>
    <div v-if="!route.meta.hideTabbar" class="eh-tabbar">
      <button
        v-for="t in tabs"
        :key="t.id"
        class="eh-tabbar__item"
        :class="{ 'eh-tabbar__item--active': t.id === current }"
        @click="go(t)"
      >
        <Ic :n="t.icon" :size="18" :color="t.id === current ? 'var(--t-accent)' : 'var(--t-text3)'" />
        <span>{{ t.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Ic from '../components/eh/Ic.vue';

const route = useRoute();
const router = useRouter();

const tabs = [
  { id: 'home', label: '首页', icon: 'home', path: '/' },
  { id: 'apply', label: '申请', icon: 'filePlus', path: '/apply/new' },
  { id: 'mine', label: '我的', icon: 'list', path: '/mine' },
  { id: 'notif', label: '通知', icon: 'bell', path: '/notif' },
] as const;

const current = computed(() => (route.meta.tab as string) || 'home');

function go(t: { path: string }) {
  router.push(t.path);
}
</script>

<style scoped>
.eh-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--t-bg);
}
.eh-layout__content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: v-bind("route.meta.hideTabbar ? '0' : '56px'");
}
.eh-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 56px;
  background: var(--t-surface);
  border-top: 1px solid var(--t-border);
  display: flex;
  align-items: stretch;
  z-index: 50;
}

@media (min-width: 768px) {
  .eh-tabbar {
    left: 50%;
    transform: translateX(-50%);
    width: 480px;
    border-radius: 0 0 16px 16px;
    border-left: 1px solid var(--t-border);
    border-right: 1px solid var(--t-border);
  }
}
.eh-tabbar__item {
  flex: 1;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  color: var(--t-text3);
  font-size: 10px;
  font-weight: 400;
  padding: 4px 0 6px;
  font-family: inherit;
}
.eh-tabbar__item--active {
  color: var(--t-accent);
  font-weight: 700;
}
</style>
