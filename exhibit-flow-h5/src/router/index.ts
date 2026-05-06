import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { getToken } from '../utils/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('../layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/applicant/Home.vue'),
        meta: { title: '首页', tab: 'home' },
      },
      {
        path: 'mine',
        name: 'MyApps',
        component: () => import('../views/applicant/MyApps.vue'),
        meta: { title: '我的申请', tab: 'mine' },
      },
      {
        path: 'apply/new',
        name: 'NewApp',
        component: () => import('../views/applicant/NewApp.vue'),
        meta: { title: '新建申请', tab: 'apply' },
      },
      {
        path: 'apply/edit/:id',
        name: 'EditApp',
        component: () => import('../views/applicant/NewApp.vue'),
        meta: { title: '编辑申请', tab: 'apply' },
      },
      {
        path: 'apply/:id',
        name: 'AppDetail',
        component: () => import('../views/applicant/AppDetail.vue'),
        meta: { title: '申请详情', tab: 'mine', hideTabbar: true },
      },
      {
        path: 'notif',
        name: 'Notifications',
        component: () => import('../views/applicant/Notifications.vue'),
        meta: { title: '通知', tab: 'notif' },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !getToken()) {
    next({ path: '/login', query: { to: to.fullPath.replace(/^\//, '') } });
  } else {
    next();
  }
});

export default router;
