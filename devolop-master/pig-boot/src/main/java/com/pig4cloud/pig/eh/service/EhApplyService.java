/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the pig4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.pig4cloud.pig.eh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.eh.entity.EhApply;

public interface EhApplyService extends IService<EhApply> {

	/**
	 * 检测指定日期时段是否与已有申请（审批中/已通过）存在时间冲突。
	 *
	 * @param date      日期，格式 yyyy-MM-dd
	 * @param startHour 开始小时，如 "09"
	 * @param endHour   结束小时，如 "12"
	 * @return 冲突申请的描述字符串（如"华为技术参观（09:00 - 11:30）"），无冲突返回 null
	 */
	String checkConflict(String date, String startHour, String endHour);

}
