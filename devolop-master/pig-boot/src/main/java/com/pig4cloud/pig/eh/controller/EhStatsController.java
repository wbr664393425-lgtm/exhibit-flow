package com.pig4cloud.pig.eh.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.eh.service.EhStatsService;
import com.pig4cloud.pig.eh.vo.EhStatsExportVO;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/stats")
@Tag(description = "eh/stats", name = "展厅统计")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhStatsController {

	private final EhStatsService ehStatsService;

	@GetMapping("/kpi")
	@Operation(summary = "看板KPI")
	public R kpi() {
		return R.ok(ehStatsService.getKpis());
	}

	@GetMapping("/monthly")
	@Operation(summary = "月度趋势")
	public R monthly() {
		return R.ok(ehStatsService.getMonthly());
	}

	@GetMapping("/city")
	@Operation(summary = "区县分布")
	public R city() {
		return R.ok(ehStatsService.getCityDistribution());
	}

	@GetMapping("/industry")
	@Operation(summary = "行业分布")
	public R industry() {
		return R.ok(ehStatsService.getIndustryDistribution());
	}

	@GetMapping("/strategic")
	@Operation(summary = "战客级别分布")
	public R strategic() {
		return R.ok(ehStatsService.getStrategicDistribution());
	}

	@GetMapping("/funnel")
	@Operation(summary = "商机漏斗")
	public R funnel() {
		return R.ok(ehStatsService.getFunnel());
	}

	@ResponseExcel
	@GetMapping("/export")
	@HasPermission("eh_dashboard_export_excel")
	@Operation(summary = "导出省公司报表")
	public List<EhStatsExportVO> export() {
		return ehStatsService.exportStatsRows();
	}
}
