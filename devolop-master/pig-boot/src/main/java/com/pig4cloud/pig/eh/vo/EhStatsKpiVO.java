package com.pig4cloud.pig.eh.vo;

import lombok.Data;

@Data
public class EhStatsKpiVO {

	private String label;

	private String value;

	private String unit;

	private Boolean up;

	private String change;

	private String icon;

	private Integer target;
}
