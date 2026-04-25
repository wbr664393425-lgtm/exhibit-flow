package com.pig4cloud.pig.eh.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EhCalendarExportVO {

	@ExcelProperty("申请编号")
	private String id;

	@ExcelProperty("会议主题")
	private String title;

	@ExcelProperty("参观日期")
	private String start;

	@ExcelProperty("开始时间")
	private String startTime;

	@ExcelProperty("结束时间")
	private String end;

	@ExcelProperty("状态")
	private String status;
}
