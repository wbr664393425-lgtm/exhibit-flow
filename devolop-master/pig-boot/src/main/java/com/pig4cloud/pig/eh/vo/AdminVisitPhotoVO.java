package com.pig4cloud.pig.eh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端现场照片聚合视图")
public class AdminVisitPhotoVO {

	private Long applyId;
	private Long visitId;
	private String title;
	private String status;
	private LocalDateTime startTime;
	private Integer photoCount;
}
