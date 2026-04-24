package com.pig4cloud.pig.eh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理端改期取消日志聚合视图")
public class AdminChangeLogVO {

	private Long id;
	private String type;
	private Long applyId;
	private String title;
	private LocalDateTime oldStartTime;
	private LocalDateTime newStartTime;
	private String reason;
	private String operator;
	private LocalDateTime time;
}
