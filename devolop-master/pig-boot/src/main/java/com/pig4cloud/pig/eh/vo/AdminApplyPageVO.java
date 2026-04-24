package com.pig4cloud.pig.eh.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "管理端申请聚合视图")
public class AdminApplyPageVO {

	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	private String title;
	private String unit;
	private String industry;
	private String district;
	private String applicant;
	private String phone;
	private String dept;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String leader;
	private Integer headCount;
	private String agenda;
	private String status;
	private LocalDateTime created;
	private String opportunityCode;
	private Integer actualHeadCount;
	private LocalDateTime actualVisitTime;
	private String returnSigner;
	private LocalDateTime returnTime;
	private String visitStatus;
	private Integer photoCount;
	private List<VisitorVO> visitors = new ArrayList<>();
	private List<ApprovalNodeVO> approvalNodes = new ArrayList<>();

	@Data
	public static class VisitorVO {
		@JsonSerialize(using = ToStringSerializer.class)
		private Long id;
		private String name;
		private String title;
		private Boolean strategic;
		private String strategicLevel;
	}

	@Data
	public static class ApprovalNodeVO {
		@JsonSerialize(using = ToStringSerializer.class)
		private Long id;
		private Integer level;
		private String role;
		private String name;
		private String action;
		private String comment;
		private LocalDateTime time;
		private String status;
	}
}
