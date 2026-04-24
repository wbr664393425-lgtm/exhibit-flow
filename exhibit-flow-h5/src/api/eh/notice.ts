import request from '../request';
import { MOCK_NOTIFS, type Notification } from '../../mock/applications';

const USE_MOCK = false;

export function fetchNotifications(): Promise<Notification[]> {
  if (USE_MOCK) return Promise.resolve([...MOCK_NOTIFS]);
  return request.get('/eh/notification/page') as unknown as Promise<Notification[]>;
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
