package com.pig4cloud.pig.eh.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.HeadFontStyle;
import cn.idev.excel.annotation.write.style.HeadStyle;
import cn.idev.excel.enums.BooleanEnum;
import cn.idev.excel.enums.poi.FillPatternTypeEnum;
import lombok.Data;

@Data
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 18)
@HeadFontStyle(bold = BooleanEnum.TRUE, color = 9, fontHeightInPoints = 11)
public class EhCalendarExportVO {

	@ColumnWidth(24)
	@ExcelProperty("申请编号")
	private String id;

	@ColumnWidth(32)
	@ExcelProperty("会议主题")
	private String title;

	@ColumnWidth(26)
	@ExcelProperty("来访单位")
	private String unit;

	@ColumnWidth(14)
	@ExcelProperty("参观日期")
	private String start;

	@ColumnWidth(12)
	@ExcelProperty("开始时间")
	private String startTime;

	@ColumnWidth(12)
	@ExcelProperty("结束时间")
	private String end;

	@ColumnWidth(14)
	@ExcelProperty("申请人")
	private String applicant;

	@ColumnWidth(18)
	@ExcelProperty("申请部门")
	private String dept;

	@ColumnWidth(12)
	@ExcelProperty("状态")
	private String status;

}
