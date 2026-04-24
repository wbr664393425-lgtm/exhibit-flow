import request from '/@/utils/request';

export const fetchList = (params?: Object) => {
	return request({
		url: '/admin/eh/approval/page',
		method: 'get',
		params,
	});
};

export const getObj = (id: string) => {
	return request({
		url: '/admin/eh/approval/details/' + id,
		method: 'get',
	});
};

export const addObj = (obj: Object) => {
	return request({
		url: '/admin/eh/approval',
		method: 'post',
		data: obj,
	});
};

export const putObj = (obj: Object) => {
	return request({
		url: '/admin/eh/approval',
		method: 'put',
		data: obj,
	});
};

export const delObj = (ids: object) => {
	return request({
		url: '/admin/eh/approval',
		method: 'delete',
		data: ids,
	});
};

export const fetchTodoList = () => {
	return request({
		url: '/admin/eh/apply/approval/todo',
		method: 'get',
	});
};

export const submitAction = (data: Object) => {
	return request({
		url: '/admin/eh/apply/approval/action',
		method: 'post',
		data,
	});
};
