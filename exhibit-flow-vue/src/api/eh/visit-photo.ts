import request from '/@/utils/request';

export const fetchList = (params?: Object) => {
	return request({
		url: '/admin/eh/visit/photo/page',
		method: 'get',
		params,
	});
};

export const getObj = (id: string) => {
	return request({
		url: '/admin/eh/visit/photo/details/' + id,
		method: 'get',
	});
};

export const addObj = (obj: Object) => {
	return request({
		url: '/admin/eh/visit/photo',
		method: 'post',
		data: obj,
	});
};

export const putObj = (obj: Object) => {
	return request({
		url: '/admin/eh/visit/photo',
		method: 'put',
		data: obj,
	});
};

export const delObj = (ids: object) => {
	return request({
		url: '/admin/eh/visit/photo',
		method: 'delete',
		data: ids,
	});
};

export const fetchAggregateList = () => {
	return request({
		url: '/admin/eh/visit/photo/page/aggregate',
		method: 'get',
	});
};

export const saveBatchPhotos = (data: Object) => {
	return request({
		url: '/admin/eh/visit/photo/batch',
		method: 'post',
		data,
	});
};
