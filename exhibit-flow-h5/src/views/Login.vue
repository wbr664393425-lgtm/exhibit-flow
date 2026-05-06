<template>
  <div class="eh-login">
    <div class="eh-login__deco eh-login__deco--a" />
    <div class="eh-login__deco eh-login__deco--b" />
    <div class="eh-login__box">
      <div class="eh-login__brand">
        <div class="eh-login__logo"><Ic n="building" :size="22" color="#c96442" /></div>
        <h1 class="eh-login__title">展厅申请系统</h1>
        <MonoLabel :style="{ color: 'var(--t-text3)' }">Exhibit Flow · Applicant</MonoLabel>
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
        <p class="eh-login__error" :class="{ 'eh-login__error--show': !!error }">{{ error || ' ' }}</p>
      </form>
    </div>
    <p class="eh-login__footer">展厅访问管理平台 · H5</p>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../store/user';
import { Ic, MonoLabel } from '../components/eh';
import { showToast } from 'vant';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const error = ref('');
const form = reactive({ username: '', password: '' });

async function onSubmit() {
  if (!form.username || !form.password) {
    error.value = '请输入账号和密码';
    showToast(error.value);
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    await userStore.login(form);
    const to = (router.currentRoute.value.query.to as string) || '';
    const targetPath = !to || to.startsWith('login') ? '/' : `/${to}`;
    await router.replace(targetPath);
  } catch (e: any) {
    error.value = e?.message || '登录失败';
    showToast(error.value);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.eh-login {
  min-height: 100vh;
  background: var(--t-bg);
  color: var(--t-text1);
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
  background: rgba(201, 100, 66, 0.10);
}
.eh-login__deco--b {
  bottom: -60px;
  right: -60px;
  width: 220px;
  height: 220px;
  background: rgba(201, 100, 66, 0.07);
}
.eh-login__box {
  width: 100%;
  max-width: 340px;
  position: relative;
  background: var(--t-surface);
  border: 1px solid var(--t-border-dark);
  border-radius: 16px;
  padding: 32px 28px;
  box-shadow: rgba(0, 0, 0, 0.05) 0px 4px 24px;
}
.eh-login__brand {
  text-align: center;
  margin-bottom: 28px;
}
.eh-login__logo {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  background: var(--t-accent-light);
  border: 1px solid var(--t-accent-border);
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.eh-login__title {
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.4px;
  margin: 0 0 6px;
  color: var(--t-text1);
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
  color: var(--t-text2);
  font-family: var(--t-font-sans);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 500;
}
.eh-login__field input {
  background: var(--t-bg);
  border: 1px solid var(--t-border-dark);
  border-radius: 8px;
  padding: 10px 12px;
  color: var(--t-text1);
  font-size: 16px;
  font-family: var(--t-font-sans);
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.eh-login__field input:focus {
  border-color: #3898ec;
  box-shadow: 0 0 0 3px rgba(56, 152, 236, 0.12);
}
.eh-login__btn {
  background: var(--t-accent);
  color: #faf9f5;
  border: none;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  font-weight: 600;
  margin-top: 6px;
  cursor: pointer;
  transition: background 0.1s;
  font-family: inherit;
}
.eh-login__btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.eh-login__btn:not(:disabled):hover {
  background: #b85538;
}
.eh-login__error {
  margin: 4px 0 0;
  color: var(--t-danger);
  font-size: 12px;
  text-align: center;
  min-height: 18px;
  opacity: 0;
  transition: opacity 0.15s ease;
}
.eh-login__error--show {
  opacity: 1;
}
.eh-login__footer {
  margin-top: 24px;
  font-size: 11px;
  color: var(--t-text3);
  position: relative;
  text-align: center;
}
</style>
