package com.pig4cloud.pig.eh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端商机聚合视图")
public class AdminOpportunityVO {

	private Long id;
	private Long applyId;
	private Long visitId;
	private String code;
	private String customer;
	private String stage;
	private String status;
	private String amount;
	private LocalDateTime lastFollow;
	private String remark;
}
