<template>
  <div class="eh-login">
    <div class="eh-login__deco eh-login__deco--a" />
    <div class="eh-login__deco eh-login__deco--b" />
    <div class="eh-login__box">
      <div class="eh-login__brand">
        <div class="eh-login__logo"><Ic n="building" :size="22" color="rgba(255,255,255,0.85)" /></div>
        <h1 class="eh-login__title">展厅申请系统</h1>
        <MonoLabel :style="{ color: 'rgba(255,255,255,0.4)' }">Exhibit Flow · Applicant</MonoLabel>
      </div>
      <form class="eh-login__form" @submit.prevent="onSubmit">
        <label class="eh-login__field">
          <span>账号</span>
          <input v-model="form.username" placeholder="请输入账号" autocomplete="username" />
        </label>
        <label class="eh-login__field">
          <span>密码</span>
          <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
        </label>
        <button type="submit" class="eh-login__btn" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
        <p v-if="error" class="eh-login__error">{{ error }}</p>
      </form>
    </div>
    <p class="eh-login__footer">中国电信 · 展厅访问管理平台 · H5</p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import { Ic, MonoLabel } from '../components/eh';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const error = ref('');
const form = reactive({ username: '', password: '' });

async function onSubmit() {
  loading.value = true;
  error.value = '';
  try {
    await userStore.login(form);
    const redirect = (router.currentRoute.value.query.redirect as string) || '/';
    const targetPath = !redirect || redirect.startsWith('/login') ? '/' : redirect;
    await router.replace(targetPath);
  } catch (e: any) {
    error.value = e?.message || '登录失败';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.eh-login {
  min-height: 100vh;
  background: var(--t-text1);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  position: relative;
  overflow: hidden;
}
.eh-login__deco {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.eh-login__deco--a {
  top: -40px;
  left: -30px;
  width: 160px;
  height: 160px;
  background: rgba(255, 86, 0, 0.18);
}
.eh-login__deco--b {
  bottom: -60px;
  right: -60px;
  width: 220px;
  height: 220px;
  background: rgba(255, 86, 0, 0.12);
}
.eh-login__box {
  width: 100%;
  max-width: 340px;
  position: relative;
}
.eh-login__brand {
  text-align: center;
  margin-bottom: 28px;
}
.eh-login__logo {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.15);
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.eh-login__title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.4px;
  margin: 0 0 6px;
}
.eh-login__form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.eh-login__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  font-family: var(--t-font-mono);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.eh-login__field input {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 4px;
  padding: 10px 12px;
  color: #fff;
  font-size: 14px;
  font-family: var(--t-font-sans);
  outline: none;
  transition: border-color 0.15s;
}
.eh-login__field input:focus {
  border-color: var(--t-accent);
}
.eh-login__btn {
  background: var(--t-accent);
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 12px;
  font-size: 14px;
  font-weight: 700;
  margin-top: 6px;
  cursor: pointer;
  transition: background 0.1s, transform 0.1s;
  font-family: inherit;
}
.eh-login__btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.eh-login__btn:not(:disabled):hover {
  background: #e64d00;
}
.eh-login__error {
  margin: 4px 0 0;
  color: #fca5a5;
  font-size: 12px;
  text-align: center;
}
.eh-login__footer {
  margin-top: 32px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.25);
  position: relative;
}
</style>
