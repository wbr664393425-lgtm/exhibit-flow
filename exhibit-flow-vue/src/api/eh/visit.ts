import request from '/@/utils/request';

export const fetchList = (params?: Object) => {
	return request({
		url: '/admin/eh/visit/page',
		method: 'get',
		params,
	});
};

export const getObj = (id: string) => {
	return request({
		url: '/admin/eh/visit/details/' + id,
		method: 'get',
	});
};

export const addObj = (obj: Object) => {
	return request({
		url: '/admin/eh/visit',
		method: 'post',
		data: obj,
	});
};

export const putObj = (obj: Object) => {
	return request({
		url: '/admin/eh/visit',
		method: 'put',
		data: obj,
	});
};

export const delObj = (ids: object) => {
	return request({
		url: '/admin/eh/visit',
		method: 'delete',
		data: ids,
	});
};

export const fetchAggregateList = () => {
	return request({
		url: '/admin/eh/visit/page/aggregate',
		method: 'get',
	});
};

export const upsertRecord = (data: Object) => {
	return request({
		url: '/admin/eh/visit/upsert',
		method: 'post',
		data,
	});
};

export const submitReturnSign = (data: Object) => {
	return request({
		url: '/admin/eh/visit/return-sign',
		method: 'post',
		data,
	});
};
