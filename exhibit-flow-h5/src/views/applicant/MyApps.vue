<template>
  <div class="eh-mine">
    <div class="eh-mine__head">
      <div>
        <h2 class="eh-mine__title">我的申请</h2>
        <p class="eh-mine__sub">共 {{ apps.length }} 条记录</p>
      </div>
      <Btn variant="primary" size="sm" icon="plus" @click="router.push('/apply/new')">新建</Btn>
    </div>

    <div class="eh-mine__filters">
      <button
        v-for="f in filters"
        :key="f.key"
        class="eh-mine__chip"
        :class="{ 'eh-mine__chip--active': filter === f.key }"
        @click="filter = f.key"
      >
        {{ f.label }}
        <span class="eh-mine__chip-cnt" :class="{ 'eh-mine__chip-cnt--active': filter === f.key }">{{ f.count }}</span>
      </button>
    </div>

    <div v-if="shown.length === 0" class="eh-mine__empty">
      <Ic n="clipboard" :size="44" color="var(--t-text3)" />
      <p>暂无相关申请</p>
      <Btn variant="outline" icon="plus" @click="router.push('/apply/new')">立即新建</Btn>
    </div>

    <div v-else class="eh-mine__list">
      <div v-for="a in shown" :key="a.id" class="eh-mine__card" @click="router.push(`/apply/${a.id}`)">
        <div class="eh-mine__card-head">
          <span class="eh-mine__card-title">{{ a.title }}</span>
          <Badge :status="a.status" />
        </div>
        <div class="eh-mine__card-meta">
          <span class="eh-mine__meta-line">
            <Ic n="building" :size="11" color="var(--t-text3)" />{{ a.unit }}
          </span>
          <span class="eh-mine__meta-line">
            <Ic n="calendar" :size="11" color="var(--t-text3)" />{{ a.startTime }} — {{ a.endTime.split(' ')[1] }}
          </span>
          <span class="eh-mine__meta-line">
            <Ic n="users" :size="11" color="var(--t-text3)" />{{ a.headCount }}人
          </span>
        </div>
        <div v-if="a.status === 'pending'" class="eh-mine__chain">
          <template v-for="(n, i) in a.approvalNodes" :key="i">
            <span class="eh-mine__node" :data-state="n.action">{{ n.role }}</span>
            <Ic v-if="i < a.approvalNodes.length - 1" n="chevronRight" :size="9" color="var(--t-text3)" />
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Badge, Btn, Ic } from '../../components/eh';
import { fetchMyApplications } from '../../api/eh/apply';
import type { Application } from '../../mock/applications';

const router = useRouter();
const apps = ref<Application[]>([]);
const filter = ref<'all' | 'pending' | 'approved' | 'completed' | 'rejected'>('all');

onMounted(async () => {
  apps.value = await fetchMyApplications();
});

const filters = computed(() => [
  { key: 'all' as const, label: '全部', count: apps.value.length },
  { key: 'pending' as const, label: '审批中', count: apps.value.filter((a) => a.status === 'pending').length },
  { key: 'approved' as const, label: '已批准', count: apps.value.filter((a) => a.status === 'approved').length },
  { key: 'completed' as const, label: '已完成', count: apps.value.filter((a) => a.status === 'completed').length },
  { key: 'rejected' as const, label: '已驳回', count: apps.value.filter((a) => a.status === 'rejected').length },
]);

const shown = computed(() =>
  filter.value === 'all' ? apps.value : apps.value.filter((a) => a.status === filter.value)
);
</script>

<style scoped>
.eh-mine {
  padding: 16px;
}
.eh-mine__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}
.eh-mine__title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--t-text1);
}
.eh-mine__sub {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--t-text3);
}
.eh-mine__filters {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.eh-mine__chip {
  border: 1px solid var(--t-border);
  background: var(--t-surface);
  color: var(--t-text2);
  padding: 5px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
  font-family: inherit;
}
.eh-mine__chip--active {
  border-color: var(--t-text1);
  background: var(--t-text1);
  color: #fff;
}
.eh-mine__chip-cnt {
  background: #f0ede8;
  color: var(--t-text3);
  border-radius: 3px;
  padding: 0 5px;
  font-size: 11px;
}
.eh-mine__chip-cnt--active {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.eh-mine__empty {
  text-align: center;
  padding: 64px 0;
  color: var(--t-text3);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.eh-mine__empty p {
  font-size: 13px;
}

.eh-mine__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.eh-mine__card {
  background: var(--t-surface);
  border: 1px solid var(--t-border);
  border-radius: 8px;
  padding: 14px 14px;
  cursor: pointer;
  transition: border-color 0.15s;
}
.eh-mine__card:active {
  border-color: var(--t-border-dark);
}
.eh-mine__card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  gap: 8px;
}
.eh-mine__card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--t-text1);
  flex: 1;
}
.eh-mine__card-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.eh-mine__meta-line {
  font-size: 11px;
  color: var(--t-text3);
  display: flex;
  align-items: center;
  gap: 4px;
}
.eh-mine__chain {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.eh-mine__node {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: 600;
  background: var(--t-bg);
  color: var(--t-text3);
  border: 1px solid var(--t-border);
}
.eh-mine__node[data-state='approved'] {
  background: var(--t-success-light);
  color: var(--t-success);
  border-color: #2c641533;
}
.eh-mine__node[data-state='pending'] {
  background: var(--t-warning-light);
  color: var(--t-warning);
  border-color: #92400e33;
}
</style>
