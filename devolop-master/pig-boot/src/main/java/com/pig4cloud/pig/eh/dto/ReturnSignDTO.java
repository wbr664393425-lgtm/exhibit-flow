package com.pig4cloud.pig.eh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "归还签收请求")
public class ReturnSignDTO {

	@Schema(description = "留存ID")
	private Long visitRecordId;

	@Schema(description = "签收人")
	private String returnSigner;

	@Schema(description = "签收时间")
	private LocalDateTime returnTime;
}
