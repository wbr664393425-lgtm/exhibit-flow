package com.pig4cloud.pig.eh.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "申请聚合详情")
public class ApplyDetailVO {

	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	private String subject;

	private String meetingNature;

	private String visitorCompany;

	private String industry;

	private String district;

	private String applicant;

	private String phone;

	private String applicantDept;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private String topLeaderTitle;

	private Integer visitorCount;

	private Integer customerCount;

	private Integer internalCount;

	private String agenda;

	private String extraServices;

	private String status;

	private String remark;

	private LocalDateTime createTime;

	private String opportunityCode;

	private List<VisitorVO> visitors = new ArrayList<>();

	private List<ApprovalNodeVO> approvalNodes = new ArrayList<>();

	private List<HistoryVO> history = new ArrayList<>();

	@Data
	public static class VisitorVO {
		@JsonSerialize(using = ToStringSerializer.class)
		private Long id;
		private String name;
		private String title;
		private String visitorCompany;
		private String unit;
		private String isKeyCustomer;
		private String keyCustomerLevel;
	}

	@Data
	public static class ApprovalNodeVO {
		@JsonSerialize(using = ToStringSerializer.class)
		private Long id;
		private Integer nodeLevel;
		private String approver;
		private String action;
		private String opinion;
		private LocalDateTime actionTime;
		private String status;
	}

	@Data
	public static class HistoryVO {
		private String eventType;
		private String eventDesc;
		private String operator;
		private String remark;
		private LocalDateTime eventTime;
	}
}
