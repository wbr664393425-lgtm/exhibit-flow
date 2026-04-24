import { defineStore } from 'pinia';
import { ref } from 'vue';
import { setToken, removeToken } from '../utils/auth';
import request from '../api/request';
import { encryptPassword } from '../utils/encrypt';

export const useUserStore = defineStore('user', () => {
  const username = ref('');
  const token = ref('');

  const login = async (formData: { username: string; password: string }) => {
    const res: any = await request.post('/oauth2/token', null, {
      params: {
        grant_type: 'password',
        username: formData.username,
        password: encryptPassword(formData.password),
        scope: 'server',
      },
      headers: { Authorization: 'Basic cGlnOnBpZw==' },
    });
    token.value = res.access_token;
    username.value = formData.username;
    setToken(res.access_token);
    return res;
  };

  const logout = () => {
    token.value = '';
    username.value = '';
    removeToken();
  };

  return { username, token, login, logout };
});
