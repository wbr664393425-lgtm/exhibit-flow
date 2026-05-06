import axios from 'axios';
import { getToken, removeToken } from '../utils/auth';
import { showToast } from 'vant';

/** Pig 资源服务：令牌过期返回 424，非 401（见 ResourceAuthExceptionEntryPoint） */
let authRedirecting = false;

function redirectToLogin() {
  if (authRedirecting) return;
  authRedirecting = true;
  showToast('登录状态已过期，请重新登录');
  removeToken();
  const path = window.location.pathname + window.location.search;
  const to = path !== '/login' && !path.startsWith('/login?') ? path.replace(/^\//, '') : '';
  const qs = to ? `?to=${encodeURIComponent(to)}` : '';
  setTimeout(() => {
    window.location.replace(`/login${qs}`);
  }, 600);
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/admin',
  timeout: 30000,
});

service.interceptors.request.use(
  (config) => {
    const token = getToken();
    const requestUrl = config.url || '';
    const isTokenRequest = requestUrl.includes('/oauth2/token');
    const hasAuthorizationHeader =
      !!config.headers?.Authorization ||
      !!config.headers?.authorization ||
      (typeof (config.headers as any)?.get === 'function' &&
        !!(config.headers as any).get('Authorization'));
    // 登录换 token 请求使用 Basic，不注入 Bearer
    if (token && !isTokenRequest && !hasAuthorizationHeader) {
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
    const requestUrl = error?.config?.url || '';
    const isTokenRequest = requestUrl.includes('/oauth2/token');
    const msg =
      error?.response?.data?.error_description ||
      error?.response?.data?.msg ||
      error?.response?.data?.message ||
      error?.message ||
      '网络异常';

    // 登录接口错误交给登录页统一提示，避免被全局重定向打断
    if (isTokenRequest) {
      return Promise.reject(new Error(msg));
    }

    const status = error.response?.status;
    if (status === 401 || status === 424) {
      redirectToLogin();
      return Promise.reject(error);
    }
    showToast(msg);
    return Promise.reject(error);
  }
);

export default service;
