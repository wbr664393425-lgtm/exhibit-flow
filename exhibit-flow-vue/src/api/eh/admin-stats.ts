import request from '/@/utils/request';

const USE_MOCK = false;

export function fetchKpis() {
	if (USE_MOCK)
		return Promise.resolve({
			data: [
				{ label: '本月参观场次', value: '18', unit: '场', up: true, change: '+3', icon: 'building' },
				{ label: '战客覆盖率', value: '62', unit: '%', up: true, change: '+12%', icon: 'star', target: 62 },
				{ label: '待审批申请', value: '4', unit: '条', icon: 'clock' },
				{ label: '商机触达数', value: '23', unit: '条', up: true, change: '+5', icon: 'briefcase' },
			],
		});
	return request({ url: '/admin/eh/stats/kpi', method: 'get' });
}

export function fetchMonthly() {
	if (USE_MOCK)
		return Promise.resolve({
			data: [
				{ label: '1月', v1: 8, v2: 6 },
				{ label: '2月', v1: 5, v2: 9 },
				{ label: '3月', v1: 12, v2: 8 },
				{ label: '4月', v1: 18, v2: 11 },
				{ label: '5月', v1: 0, v2: 14 },
				{ label: '6月', v1: 0, v2: 10 },
			],
		});
	return request({ url: '/admin/eh/stats/monthly', method: 'get' });
}

export function fetchCityDist() {
	if (USE_MOCK)
		return Promise.resolve({
			data: [
				{ city: '天河区', count: 8 },
				{ city: '越秀区', count: 5 },
				{ city: '海珠区', count: 4 },
				{ city: '黄埔区', count: 3 },
				{ city: '番禺区', count: 3 },
				{ city: '白云区', count: 2 },
				{ city: '荔湾区', count: 2 },
				{ city: '花都区', count: 1 },
			],
		});
	return request({ url: '/admin/eh/stats/city', method: 'get' });
}

export function fetchIndustryDist() {
	if (USE_MOCK)
		return Promise.resolve({
			data: [
				{ name: '信息技术', count: 5, c: '#ff5600' },
				{ name: '政府机构', count: 4, c: '#1d4ed8' },
				{ name: '金融保险', count: 3, c: '#92400ecc' },
				{ name: '建筑工程', count: 2, c: '#7e22ce' },
				{ name: '制造业', count: 2, c: '#c41c1c' },
				{ name: '其他', count: 2, c: '#9c9fa5' },
			],
		});
	return request({ url: '/admin/eh/stats/industry', method: 'get' });
}

export function fetchStrategic() {
	if (USE_MOCK)
		return Promise.resolve({
			data: [
				{ level: '省直管战客', count: 6, c: '#c41c1c' },
				{ level: '市直管战客', count: 10, c: '#92400e' },
				{ level: '区县直管战客', count: 8, c: '#1d4ed8' },
				{ level: '非战略客户', count: 12, c: '#9c9fa5' },
			],
		});
	return request({ url: '/admin/eh/stats/strategic', method: 'get' });
}

export function fetchFunnel() {
	if (USE_MOCK)
		return Promise.resolve({
			data: [
				{ stage: '参观接待', count: 36 },
				{ stage: '商机触达', count: 23 },
				{ stage: '商机跟进', count: 14 },
				{ stage: '签约转化', count: 7 },
			],
		});
	return request({ url: '/admin/eh/stats/funnel', method: 'get' });
}
