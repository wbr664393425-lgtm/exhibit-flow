<template>
	<!--
		审批动作弹层已在 approval/index.vue 内嵌实现（审批通过/驳回/转交三合一）。
		此文件保留为占位，防止菜单路由对 form.vue 的历史引用报错。
	-->
	<div />
</template>

<script setup lang="ts" name="ehapprovalDialog">
const openDialog = () => {};
defineExpose({ openDialog });
</script>
