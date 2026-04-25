import request from '../request';
import { MOCK_NOTIFS, type Notification } from '../../mock/applications';

const USE_MOCK = false;

function fmtTime(v?: string) {
  if (!v) return '';
  return v.replace('T', ' ').slice(0, 16);
}

function mapType(type?: string): Notification['type'] {
  const allow: Notification['type'][] = ['approval', 'approved', 'rejected', 'reminder', 'system'];
  return allow.includes(type as Notification['type']) ? (type as Notification['type']) : 'system';
}

function mapIcon(type: Notification['type']) {
  if (type === 'approval') return 'clock';
  if (type === 'approved') return 'checkCircle';
  if (type === 'rejected') return 'xCircle';
  if (type === 'reminder') return 'bell';
  return 'info';
}

export async function fetchNotifications(): Promise<Notification[]> {
  if (USE_MOCK) return Promise.resolve([...MOCK_NOTIFS]);
  const res: any = await request.get('/eh/notification/page', { params: { current: 1, size: 50 } });
  const records: any[] = res?.records ?? [];
  return records.map((item) => {
    const type = mapType(item.type);
    return {
      id: Number(item.id),
      type,
      icon: mapIcon(type),
      title: item.title || '系统通知',
      body: item.content || '',
      time: fmtTime(item.createTime),
      read: item.readFlag === '1',
    };
  });
}

export function markNotificationRead(id: number) {
  if (USE_MOCK) {
    const n = MOCK_NOTIFS.find((x) => x.id === id);
    if (n) n.read = true;
    return Promise.resolve();
  }
  return request.put(`/eh/notification/${id}/read`);
}

export function markAllNotificationsRead() {
  if (USE_MOCK) {
    MOCK_NOTIFS.forEach((n) => (n.read = true));
    return Promise.resolve();
  }
  return request.put('/eh/notification/read-all');
}
