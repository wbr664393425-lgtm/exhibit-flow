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

package com.pig4cloud.pig.eh.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("eh_apply")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "展厅申请")
public class EhApply extends Model<EhApply> {

	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	@Schema(description = "会议主题")
	private String subject;

	@Schema(description = "简要议程")
	private String agenda;

	@Schema(description = "使用开始时段")
	private LocalDateTime startTime;

	@Schema(description = "使用结束时段")
	private LocalDateTime endTime;

	@Schema(description = "来访单位")
	private String visitorCompany;

	@Schema(description = "所属行业")
	private String industry;

	@Schema(description = "人员数量")
	private Integer visitorCount;

	@Schema(description = "最高陪同领导级别")
	private String topLeaderTitle;

	@Schema(description = "申请人")
	private String applicant;

	@Schema(description = "申请部门")
	private String applicantDept;

	@Schema(description = "联系电话")
	private String phone;

	@Schema(description = "所属区县")
	private String district;

	@Schema(description = "真实姓名")
	private String realName;

	@Schema(description = "操作人手机号")
	private String realPhone;

	@Schema(description = "附加服务")
	private String extraServices;

	@Schema(description = "申请状态")
	private String status;

	@Schema(description = "实际到场人数")
	private Integer actualCount;

	@Schema(description = "归还签字人")
	private String returnSigner;

	@Schema(description = "归还时间")
	private LocalDateTime returnTime;

	@Schema(description = "备注")
	private String remark;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改人")
	private String updateBy;

	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改时间")
	private LocalDateTime updateTime;

	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "删除标记,1:已删除,0:正常")
	private String delFlag;

}
