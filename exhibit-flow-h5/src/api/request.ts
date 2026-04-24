import axios from 'axios';
import { getToken } from '../utils/auth';
import { showToast } from 'vant';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/admin',
  timeout: 30000,
});

service.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

service.interceptors.response.use(
  (res) => {
    const resData = res.data;
    // OAuth2 token 响应（含 access_token），直接返回
    if (resData.access_token !== undefined) return resData;
    // 业务响应：code=1 为错误
    if (resData.code === 1) {
      showToast(resData.msg || '请求失败');
      return Promise.reject(new Error(resData.msg));
    }
    return resData.data !== undefined ? resData.data : resData;
  },
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login';
    }
    showToast(error.message || '网络异常');
    return Promise.reject(error);
  }
);

export default service;
