<template>
	<div class="eh-scope eh-sub">
		<div class="eh-sub__head">
			<h2 class="eh-sub__title">字典维护</h2>
			<p class="eh-sub__sub">行业、区县、服务项等基础字典维护</p>
		</div>
		<Card :style="{ padding: '40px', textAlign: 'center' }">
			<Ic n="layers" :size="44" color="var(--t-text3)" />
			<p style="margin-top:14px;color:var(--t-text3);font-size:13px">字典维护请使用 系统管理 &gt; 字典管理</p>
		</Card>
	</div>
</template>

<script setup lang="ts" name="ehbasedict">
import { Card, Ic } from '/@/components/eh';
</script>

<style scoped>
.eh-sub { padding: 20px 24px; background: var(--t-bg); min-height: calc(100vh - 100px); }
.eh-sub__head { margin-bottom: 18px; }
.eh-sub__title { margin: 0; font-size: 19px; font-weight: 700; color: var(--t-text1); }
.eh-sub__sub { margin: 3px 0 0; font-size: 12px; color: var(--t-text3); }
</style>
