package com.pig4cloud.pig.eh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端参观留存聚合视图")
public class AdminVisitAggregateVO {

	private Long applyId;
	private Long visitId;
	private String title;
	private String unit;
	private String status;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer headCount;
	private Integer actualVisitCount;
	private LocalDateTime actualVisitTime;
	private String opportunityCode;
	private String returnSigner;
	private LocalDateTime returnTime;
	private String visitStatus;
}
