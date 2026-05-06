package com.pig4cloud.pig.eh.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "展厅申请表单")
public class ApplyFormDTO {

	@Schema(description = "会议主题")
	private String title;

	@Schema(description = "会议性质：internal内部/external外部")
	private String meetingNature;

	@Schema(description = "来访单位")
	private String unit;

	@Schema(description = "所属行业")
	private String industry;

	@Schema(description = "所属区县")
	private String district;

	@Schema(description = "申请人姓名")
	private String applicant;

	@Schema(description = "联系电话")
	private String phone;

	@Schema(description = "申请部门")
	private String dept;

	@Schema(description = "参观日期，格式 yyyy-MM-dd")
	private String startDate;

	@Schema(description = "开始小时，格式 HH")
	private String startHour;

	@Schema(description = "会议正式开始时间，格式 HH:mm")
	private String meetingTime;

	@Schema(description = "结束小时，格式 HH")
	private String endHour;

	@Schema(description = "最高陪同领导")
	private String leader;

	@Schema(description = "预计人数")
	private Integer headCount;

	@Schema(description = "客户人数")
	private Integer customerCount;

	@Schema(description = "自有人员人数")
	private Integer internalCount;

	@Schema(description = "简要议程")
	private String agenda;

	@Schema(description = "备注")
	private String remark;

	@Schema(description = "附加服务")
	private List<String> services = new ArrayList<>();

	@Schema(description = "来访客户")
	private List<VisitorDTO> visitors = new ArrayList<>();

	@Data
	@Schema(description = "来访客户")
	public static class VisitorDTO {

		@Schema(description = "客户单位")
		private String unit;

		@Schema(description = "客户姓名")
		private String name;

		@Schema(description = "客户职务")
		private String title;

		@Schema(description = "是否战客")
		private Boolean isStrategic;

		@Schema(description = "战客级别")
		private String strategicLevel;
	}

}
