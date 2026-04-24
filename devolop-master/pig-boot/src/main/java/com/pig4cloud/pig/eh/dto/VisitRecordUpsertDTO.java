package com.pig4cloud.pig.eh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "留存录入/编辑")
public class VisitRecordUpsertDTO {

	@Schema(description = "留存ID，新增可不传")
	private Long id;

	@Schema(description = "申请ID")
	private Long applyId;

	@Schema(description = "实际参观时间")
	private LocalDateTime actualVisitTime;

	@Schema(description = "实际参观人数")
	private Integer actualVisitCount;

	@Schema(description = "商机编码")
	private String opportunityCode;

	@Schema(description = "我方领导参会最高级别")
	private String ourLeaderLevel;

	@Schema(description = "备注")
	private String remark;
}
