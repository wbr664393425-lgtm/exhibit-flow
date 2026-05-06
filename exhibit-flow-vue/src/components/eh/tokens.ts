export const T = {
	bg: '#f4f8ff',
	surface: '#ffffff',
	surfaceWarm: '#edf4ff',
	border: '#dde8fa',
	borderDark: '#c5d8f5',
	text1: '#1b2a41',
	text2: '#52627a',
	text3: '#8293ab',
	accent: '#2f67d8',
	accentLight: '#eaf2ff',
	accentBorder: '#bfd4fb',
	success: '#2c6415',
	successLight: '#f0f9eb',
	warning: '#92400e',
	warningLight: '#fffbeb',
	danger: '#c41c1c',
	dangerLight: '#fef2f2',
	sidebar: '#f2f7ff',
	sidebarSub: '#e8f0fe',
} as const;

export type ApplicationStatus =
	| 'draft'
	| 'pending'
	| 'approved'
	| 'rejected'
	| 'cancelled'
	| 'completed'
	| 'rescheduled';

export const STATUS_MAP: Record<ApplicationStatus, { label: string; color: string; bg: string; dot: string }> = {
	draft: { label: '草稿', color: '#9c9fa5', bg: '#f5f4f0', dot: '#9c9fa5' },
	pending: { label: '审批中', color: '#92400e', bg: '#fffbeb', dot: '#f79009' },
	approved: { label: '已批准', color: '#2c6415', bg: '#f0f9eb', dot: '#2c6415' },
	rejected: { label: '已驳回', color: '#c41c1c', bg: '#fef2f2', dot: '#c41c1c' },
	cancelled: { label: '已取消', color: '#9c9fa5', bg: '#f5f4f0', dot: '#9c9fa5' },
	completed: { label: '已完成', color: '#1d4ed8', bg: '#eff6ff', dot: '#1d4ed8' },
	rescheduled: { label: '已改期', color: '#6b21a8', bg: '#faf5ff', dot: '#7e22ce' },
};
