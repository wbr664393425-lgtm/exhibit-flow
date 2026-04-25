package com.pig4cloud.pig.eh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "审批动作请求")
public class ApprovalActionDTO {

	@Schema(description = "申请ID")
	private Long applyId;

	@Schema(description = "审批节点ID")
	private Long nodeId;

	@Schema(description = "动作: approve/reject/forward")
	private String action;

	@Schema(description = "审批意见")
	private String comment;

	@Schema(description = "转交目标审批人用户名")
	private String targetApprover;
}
