import request from '/@/utils/request';

export const fetchList = (params?: Object) => {
	return request({
		url: '/admin/eh/apply/page',
		method: 'get',
		params,
	});
};

export const getObj = (id: string) => {
	return request({
		url: '/admin/eh/apply/details/' + id,
		method: 'get',
	});
};

export const addObj = (obj: Object) => {
	return request({
		url: '/admin/eh/apply',
		method: 'post',
		data: obj,
	});
};

export const putObj = (obj: Object) => {
	return request({
		url: '/admin/eh/apply',
		method: 'put',
		data: obj,
	});
};

export const delObj = (ids: object) => {
	return request({
		url: '/admin/eh/apply',
		method: 'delete',
		data: ids,
	});
};

export const fetchAggregateList = (params?: Object) => {
	return request({
		url: '/admin/eh/apply/page/aggregate',
		method: 'get',
		params,
	});
};

export const fetchApprovalTodoList = () => {
	return request({
		url: '/admin/eh/apply/approval/todo',
		method: 'get',
	});
};

export const submitApprovalAction = (data: Object) => {
	return request({
		url: '/admin/eh/apply/approval/action',
		method: 'post',
		data,
	});
};
