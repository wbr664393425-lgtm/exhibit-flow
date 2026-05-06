package com.pig4cloud.pig.eh.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("eh_apply_history")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "申请历史事件")
public class EhApplyHistory extends Model<EhApplyHistory> {

	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	@Schema(description = "关联申请ID")
	private Long applyId;

	@Schema(description = "事件类型：submit/resubmit/approve/reject/reschedule/cancel")
	private String eventType;

	@Schema(description = "事件描述")
	private String eventDesc;

	@Schema(description = "操作人")
	private String operator;

	@Schema(description = "备注（驳回原因、改期原因等）")
	private String remark;

	@Schema(description = "事件时间")
	private LocalDateTime eventTime;

	@TableField(fill = FieldFill.INSERT)
	private String createBy;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	@TableField(fill = FieldFill.UPDATE)
	private String updateBy;

	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;

	@TableLogic
	@TableField(fill = FieldFill.INSERT)
	private String delFlag;

}
