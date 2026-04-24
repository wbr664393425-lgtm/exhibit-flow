<template>
  <div class="eh-home">
    <!-- Hero -->
    <div class="eh-home__hero">
      <div class="eh-home__deco eh-home__deco--a" />
      <div class="eh-home__deco eh-home__deco--b" />
      <MonoLabel :style="{ color: 'rgba(255,255,255,0.5)', display: 'block', marginBottom: '6px' }">
        欢迎回来
      </MonoLabel>
      <div class="eh-home__name">李建国</div>
      <div class="eh-home__meta">天河区 · 政企客户部</div>
      <div class="eh-home__stats">
        <div v-for="s in stats" :key="s.label" class="eh-home__stat">
          <div class="eh-home__stat-value">{{ s.value }}</div>
          <div class="eh-home__stat-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <!-- Quick actions -->
    <div class="eh-home__body">
      <div class="eh-home__actions">
        <button class="eh-home__action eh-home__action--accent" @click="router.push('/apply/new')">
          <Ic n="plus" :size="14" color="#fff" />新建申请
        </button>
        <button class="eh-home__action eh-home__action--ghost" @click="router.push('/mine')">
          <Ic n="list" :size="14" />我的申请
        </button>
      </div>
      <MonoLabel :style="{ display: 'block', marginBottom: '10px' }">最近申请</MonoLabel>
      <div class="eh-home__list">
        <div
          v-for="a in apps.slice(0, 3)"
          :key="a.id"
          class="eh-home__item"
          @click="router.push(`/apply/${a.id}`)"
        >
          <div class="eh-home__item-head">
            <span class="eh-home__item-title">{{ a.title }}</span>
            <Badge :status="a.status" />
          </div>
          <span class="eh-home__item-meta">{{ a.startTime.split(' ')[0] }} · {{ a.unit }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { MonoLabel, Badge, Ic } from '../../components/eh';
import { fetchMyApplications } from '../../api/eh/apply';
import type { Application } from '../../mock/applications';

const router = useRouter();
const apps = ref<Application[]>([]);

onMounted(async () => {
  apps.value = await fetchMyApplications();
});

const stats = computed(() => {
  const pending = apps.value.filter((a) => a.status === 'pending').length;
  const todayDate = '2026-04-23';
  const today = apps.value.filter((a) => a.startTime.startsWith(todayDate)).length;
  const approved = apps.value.filter((a) => a.status === 'approved').length;
  return [
    { value: apps.value.length || 0, label: '全部申请' },
    { value: pending, label: '审批中' },
    { value: today || approved, label: '今日/已批准' },
  ];
});
</script>

<style scoped>
.eh-home {
  min-height: 100%;
  background: var(--t-bg);
}
.eh-home__hero {
  background: var(--t-text1);
  padding: 24px 16px 28px;
  position: relative;
  overflow: hidden;
  color: #fff;
}
.eh-home__deco {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.eh-home__deco--a {
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  background: rgba(255, 86, 0, 0.15);
}
.eh-home__deco--b {
  bottom: -30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background: rgba(255, 86, 0, 0.1);
}
.eh-home__name {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}
.eh-home__meta {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}
.eh-home__stats {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}
.eh-home__stat {
  flex: 1;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.eh-home__stat-value {
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
}
.eh-home__stat-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 4px;
}

.eh-home__body {
  padding: 16px;
}
.eh-home__actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.eh-home__action {
  flex: 1;
  border: none;
  border-radius: 6px;
  padding: 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-family: inherit;
}
.eh-home__action--accent {
  background: var(--t-accent);
  color: #fff;
}
.eh-home__action--ghost {
  background: var(--t-bg);
  color: var(--t-text1);
  border: 1px solid var(--t-border);
}

.eh-home__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.eh-home__item {
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: 6px;
  padding: 12px 14px;
}
.eh-home__item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 5px;
}
.eh-home__item-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--t-text1);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 8px;
}
.eh-home__item-meta {
  font-size: 11px;
  color: var(--t-text3);
}
</style>
