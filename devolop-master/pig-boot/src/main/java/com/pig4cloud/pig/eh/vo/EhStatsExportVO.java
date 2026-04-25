package com.pig4cloud.pig.eh.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EhStatsExportVO {

	@ExcelProperty("序号")
	private Integer serialNo;

	@ExcelProperty("参观日期")
	private String visitDate;

	@ExcelProperty("参观单位")
	private String visitCompany;

	@ExcelProperty("客户姓名")
	private String customerName;

	@ExcelProperty("职务")
	private String title;

	@ExcelProperty("是否战客")
	private String strategicFlag;

	@ExcelProperty("战客级别")
	private String strategicLevel;

	@ExcelProperty("商机编码")
	private String opportunityCode;

	@ExcelProperty("参观人数")
	private Integer headCount;

	@ExcelProperty("最高陪同领导")
	private String leader;

	@ExcelProperty("申请部门")
	private String dept;

	@ExcelProperty("申请人")
	private String applicant;

	@ExcelProperty("联系电话")
	private String phone;
}
