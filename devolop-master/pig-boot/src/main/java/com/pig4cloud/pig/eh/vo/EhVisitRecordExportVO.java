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
public class EhVisitRecordExportVO {

	@ColumnWidth(8)
	@ExcelProperty("序号")
	private Integer rowNo;

	@ColumnWidth(20)
	@ExcelProperty("参观时间")
	private String visitTime;

	@ColumnWidth(30)
	@ExcelProperty("参观客户单位")
	private String unit;

	@ColumnWidth(26)
	@ExcelProperty("我方领导参会最高级别")
	private String ourLeaderLevel;

	@ColumnWidth(16)
	@ExcelProperty("客户所属行业")
	private String industry;

	@ColumnWidth(14)
	@ExcelProperty("计划参观人数")
	private Integer headCount;

	@ColumnWidth(14)
	@ExcelProperty("实际到场人数")
	private Integer actualVisitCount;

	@ColumnWidth(22)
	@ExcelProperty("触发商机编码")
	private String opportunityCode;

	@ColumnWidth(14)
	@ExcelProperty("现场照片数量")
	private Integer photoCount;

	@ColumnWidth(16)
	@ExcelProperty("归还签字人")
	private String returnSigner;

	@ColumnWidth(20)
	@ExcelProperty("归还时间")
	private String returnTime;

	@ColumnWidth(12)
	@ExcelProperty("留存状态")
	private String visitStatus;

}
