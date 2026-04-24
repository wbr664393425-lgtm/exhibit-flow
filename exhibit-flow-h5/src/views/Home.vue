<template>
  <div class="home-page">
    <van-nav-bar title="展厅申请系统" />
    <div class="home-content">
      <van-grid :column-num="3" :border="false">
        <van-grid-item icon="orders-o" text="我的申请" />
        <van-grid-item icon="clock-o" text="待审批" />
        <van-grid-item icon="records-o" text="参观留存" />
        <van-grid-item icon="chart-trending-o" text="商机跟进" />
        <van-grid-item icon="photo-o" text="现场照片" />
        <van-grid-item icon="setting-o" text="基础数据" />
      </van-grid>
      <van-notice-bar left-icon="volume-o" text="欢迎使用展厅访问申请与留存系统" />
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.home-content {
  padding: 16px;
}
</style>
