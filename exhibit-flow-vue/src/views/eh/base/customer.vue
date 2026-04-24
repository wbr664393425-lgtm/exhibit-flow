<template>
	<div class="eh-scope eh-sub">
		<div class="eh-sub__head">
			<h2 class="eh-sub__title">客户档案</h2>
			<p class="eh-sub__sub">战略客户画像与历史参观记录（功能建设中）</p>
		</div>
		<Card :style="{ padding: '40px', textAlign: 'center' }">
			<Ic n="users" :size="44" color="var(--t-text3)" />
			<p style="margin-top:14px;color:var(--t-text3);font-size:13px">客户档案模块即将上线</p>
		</Card>
	</div>
</template>

<script setup lang="ts" name="ehbasecustomer">
import { Card, Ic } from '/@/components/eh';
</script>

<style scoped>
.eh-sub { padding: 20px 24px; background: var(--t-bg); min-height: calc(100vh - 100px); }
.eh-sub__head { margin-bottom: 18px; }
.eh-sub__title { margin: 0; font-size: 19px; font-weight: 700; color: var(--t-text1); }
.eh-sub__sub { margin: 3px 0 0; font-size: 12px; color: var(--t-text3); }
</style>
