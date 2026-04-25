package com.pig4cloud.pig.eh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "申请改期")
public class ApplyRescheduleDTO {

	@Schema(description = "新参观日期，格式 yyyy-MM-dd")
	private String newDate;

	@Schema(description = "新开始小时，格式 HH")
	private String newSH;

	@Schema(description = "新结束小时，格式 HH")
	private String newEH;

	@Schema(description = "改期原因")
	private String reason;
}
