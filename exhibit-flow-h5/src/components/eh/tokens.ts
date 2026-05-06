export const T = {
  bg: '#f5f4ed',
  surface: '#faf9f5',
  surfaceWarm: '#f0eee6',
  border: '#f0eee6',
  borderDark: '#e8e6dc',
  text1: '#141413',
  text2: '#5e5d59',
  text3: '#87867f',
  accent: '#c96442',
  accentLight: '#fdf0ea',
  accentBorder: '#f0c0a0',
  success: '#2c6415',
  successLight: '#f0f9eb',
  warning: '#92400e',
  warningLight: '#fffbeb',
  danger: '#b53333',
  dangerLight: '#fef2f2',
  sidebar: '#141413',
  sidebarSub: '#1e1c1a',
} as const;

export type ApplicationStatus =
  | 'draft'
  | 'pending'
  | 'approved'
  | 'rejected'
  | 'cancelled'
  | 'completed'
  | 'rescheduled';

export const STATUS_MAP: Record<
  ApplicationStatus,
  { label: string; color: string; bg: string; dot: string }
> = {
  draft: { label: '草稿', color: '#87867f', bg: '#f0eee6', dot: '#87867f' },
  pending: { label: '审批中', color: '#92400e', bg: '#fffbeb', dot: '#f79009' },
  approved: { label: '已批准', color: '#2c6415', bg: '#f0f9eb', dot: '#2c6415' },
  rejected: { label: '已驳回', color: '#b53333', bg: '#fef2f2', dot: '#b53333' },
  cancelled: { label: '已取消', color: '#87867f', bg: '#f0eee6', dot: '#87867f' },
  completed: { label: '已完成', color: '#1d4ed8', bg: '#eff6ff', dot: '#1d4ed8' },
  rescheduled: { label: '已改期', color: '#6b21a8', bg: '#faf5ff', dot: '#7e22ce' },
};
