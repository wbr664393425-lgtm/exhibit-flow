import request from '/@/utils/request';
import { MOCK_CALENDAR } from '/@/components/eh/mock';

const USE_MOCK = false;

export function fetchCalendar(params?: { start?: string; end?: string }) {
	if (USE_MOCK) return Promise.resolve({ data: MOCK_CALENDAR });
	return request({ url: '/admin/eh/apply/calendar', method: 'get', params });
}
