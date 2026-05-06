import { defineStore } from 'pinia';
import { ref } from 'vue';
import { setToken, removeToken } from '../utils/auth';
import request from '../api/request';
import { encryptPassword } from '../utils/encrypt';

export const useUserStore = defineStore('user', () => {
  const username = ref('');
  const token = ref('');
  const realName = ref('');
  const deptName = ref('');

  const fetchProfile = async () => {
    const res: any = await request.get('/user/info');
    const root = res?.data ?? res ?? {};
    const user = root?.user ?? root;
    username.value = user?.username || username.value;
    realName.value = user?.name || user?.nickname || user?.username || username.value || '';
    deptName.value = user?.dept?.name || user?.deptName || '';
    return user;
  };

  const login = async (formData: { username: string; password: string }) => {
    try {
      const res: any = await request.post('/oauth2/token', null, {
        params: {
          grant_type: 'password',
          username: formData.username,
          password: encryptPassword(formData.password),
          scope: 'server',
        },
        auth: {
          username: 'pig',
          password: 'pig',
        },
        headers: {
          Authorization: 'Basic cGlnOnBpZw==',
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });
      if (!res?.access_token) {
        throw new Error('账号或密码错误');
      }
      token.value = res.access_token;
      username.value = formData.username;
      setToken(res.access_token);
      await fetchProfile().catch(() => undefined);
      return res;
    } catch (e: any) {
      const msg =
        e?.response?.data?.error_description ||
        e?.response?.data?.msg ||
        e?.message ||
        '登录失败';
      if (String(msg).includes('Bad credentials') || String(msg).includes('invalid_grant')) {
        throw new Error('账号或密码错误');
      }
      throw new Error(msg);
    }
  };

  const logout = () => {
    token.value = '';
    username.value = '';
    realName.value = '';
    deptName.value = '';
    removeToken();
  };

  return { username, token, realName, deptName, login, logout, fetchProfile };
});
