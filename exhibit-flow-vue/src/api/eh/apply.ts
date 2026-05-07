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

export const checkConflict = (params: { date: string; startHour: string; endHour: string }) => {
	return request({
		url: '/admin/eh/apply/conflict',
		method: 'get',
		params,
	});
};

export const saveDraft = (data: Object) => {
	return request({
		url: '/admin/eh/apply/draft',
		method: 'post',
		data,
	});
};

export const submitApply = (data: Object) => {
	return request({
		url: '/admin/eh/apply/submit',
		method: 'post',
		data,
	});
};

export const saveCompleted = (data: Object) => {
	return request({
		url: '/admin/eh/apply/complete',
		method: 'post',
		data,
	});
};

export const updateDraft = (id: string, data: Object) => {
	return request({
		url: `/admin/eh/apply/${id}/draft`,
		method: 'put',
		data,
	});
};

export const updateAndSubmit = (id: string, data: Object) => {
	return request({
		url: `/admin/eh/apply/${id}/submit`,
		method: 'put',
		data,
	});
};

export const cancelApply = (id: string, data: { reason?: string }) => {
	return request({
		url: `/admin/eh/apply/${id}/cancel`,
		method: 'put',
		data,
	});
};

export const rescheduleApply = (id: string, data: Object) => {
	return request({
		url: `/admin/eh/apply/${id}/reschedule`,
		method: 'put',
		data,
	});
};

export const updateAgenda = (id: string, data: { agenda: string }) => {
	return request({
		url: `/admin/eh/apply/${id}/agenda`,
		method: 'put',
		data,
	});
};
