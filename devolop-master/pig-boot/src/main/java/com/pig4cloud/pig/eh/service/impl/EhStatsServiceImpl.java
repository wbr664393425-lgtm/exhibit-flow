package com.pig4cloud.pig.eh.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.entity.EhApplyVisitor;
import com.pig4cloud.pig.eh.entity.EhOpportunity;
import com.pig4cloud.pig.eh.entity.EhVisitRecord;
import com.pig4cloud.pig.eh.mapper.EhApplyMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyVisitorMapper;
import com.pig4cloud.pig.eh.mapper.EhOpportunityMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitRecordMapper;
import com.pig4cloud.pig.eh.service.EhStatsService;
import com.pig4cloud.pig.eh.vo.EhStatsCityVO;
import com.pig4cloud.pig.eh.vo.EhStatsExportVO;
import com.pig4cloud.pig.eh.vo.EhStatsFunnelVO;
import com.pig4cloud.pig.eh.vo.EhStatsIndustryVO;
import com.pig4cloud.pig.eh.vo.EhStatsKpiVO;
import com.pig4cloud.pig.eh.vo.EhStatsMonthlyVO;
import com.pig4cloud.pig.eh.vo.EhStatsStrategicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EhStatsServiceImpl implements EhStatsService {

	private static final Map<String, String> INDUSTRY_COLOR_MAP = Map.of(
		"信息技术", "#ff5600",
		"政府机构", "#1d4ed8",
		"金融保险", "#92400ecc",
		"建筑工程", "#7e22ce",
		"能源电力", "#c41c1c",
		"其他", "#9c9fa5"
	);

	private static final Map<String, String> STRATEGIC_COLOR_MAP = Map.of(
		"省直管战客", "#c41c1c",
		"市直管战客", "#92400e",
		"区县直管战客", "#1d4ed8",
		"非战略客户", "#9c9fa5"
	);

	private final EhApplyMapper ehApplyMapper;
	private final EhApplyVisitorMapper ehApplyVisitorMapper;
	private final EhOpportunityMapper ehOpportunityMapper;
	private final EhVisitRecordMapper ehVisitRecordMapper;

	@Override
	public List<EhStatsKpiVO> getKpis() {
		List<EhApply> applies = listApplies();
		List<EhApplyVisitor> visitors = listVisitors();
		int currentMonth = LocalDate.now().getMonthValue();
		int currentYear = LocalDate.now().getYear();
		long monthCount = applies.stream()
			.filter(item -> item.getStartTime() != null)
			.filter(item -> Objects.equals(item.getStartTime().getYear(), currentYear))
			.filter(item -> Objects.equals(item.getStartTime().getMonthValue(), currentMonth))
			.filter(item -> !"0".equals(item.getStatus()) && !"4".equals(item.getStatus()))
			.count();
		long strategicCount = visitors.stream().filter(item -> "1".equals(item.getIsKeyCustomer())).count();
		int strategicRate = visitors.isEmpty() ? 0 : (int) Math.round(strategicCount * 100.0 / visitors.size());
		long pendingCount = applies.stream().filter(item -> "1".equals(item.getStatus())).count();
		long oppCount = listOpportunities().size();

		List<EhStatsKpiVO> list = new ArrayList<>();
		list.add(buildKpi("本月参观场次", String.valueOf(monthCount), "场", true, monthCount > 0 ? "+1" : "0", "building", null));
		list.add(buildKpi("战客覆盖率", String.valueOf(strategicRate), "%", true, "+" + strategicRate + "%", "star", strategicRate));
		list.add(buildKpi("待审批申请", String.valueOf(pendingCount), "条", null, null, "clock", null));
		list.add(buildKpi("商机触达数", String.valueOf(oppCount), "条", true, oppCount > 0 ? "+" + oppCount : "0", "briefcase", null));
		return list;
	}

	@Override
	public List<EhStatsMonthlyVO> getMonthly() {
		List<EhApply> applies = listApplies();
		int currentYear = LocalDate.now().getYear();
		int previousYear = currentYear - 1;
		List<EhStatsMonthlyVO> rows = new ArrayList<>();
		for (int month = 1; month <= 12; month++) {
			EhStatsMonthlyVO row = new EhStatsMonthlyVO();
			row.setLabel(month + "月");
			row.setV1(countMonthly(applies, currentYear, month));
			row.setV2(countMonthly(applies, previousYear, month));
			rows.add(row);
		}
		return rows;
	}

	@Override
	public List<EhStatsCityVO> getCityDistribution() {
		return listApplies().stream()
			.filter(item -> item.getDistrict() != null && !item.getDistrict().isBlank())
			.collect(Collectors.groupingBy(EhApply::getDistrict, Collectors.summingInt(item -> 1)))
			.entrySet()
			.stream()
			.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
			.map(entry -> {
				EhStatsCityVO row = new EhStatsCityVO();
				row.setCity(entry.getKey());
				row.setCount(entry.getValue());
				return row;
			})
			.collect(Collectors.toList());
	}

	@Override
	public List<EhStatsIndustryVO> getIndustryDistribution() {
		Map<String, Integer> countMap = listApplies().stream()
			.collect(Collectors.groupingBy(item -> normalizeIndustry(item.getIndustry()), Collectors.summingInt(item -> 1)));
		return countMap.entrySet()
			.stream()
			.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
			.map(entry -> {
				EhStatsIndustryVO row = new EhStatsIndustryVO();
				row.setName(entry.getKey());
				row.setCount(entry.getValue());
				row.setC(INDUSTRY_COLOR_MAP.getOrDefault(entry.getKey(), INDUSTRY_COLOR_MAP.get("其他")));
				return row;
			})
			.collect(Collectors.toList());
	}

	@Override
	public List<EhStatsStrategicVO> getStrategicDistribution() {
		Map<String, Integer> countMap = new LinkedHashMap<>();
		countMap.put("省直管战客", 0);
		countMap.put("市直管战客", 0);
		countMap.put("区县直管战客", 0);
		countMap.put("非战略客户", 0);
		for (EhApplyVisitor visitor : listVisitors()) {
			if ("1".equals(visitor.getIsKeyCustomer())) {
				String level = visitor.getKeyCustomerLevel();
				countMap.compute(level == null || level.isBlank() ? "市直管战客" : level, (key, val) -> val == null ? 1 : val + 1);
			} else {
				countMap.compute("非战略客户", (key, val) -> val == null ? 1 : val + 1);
			}
		}
		List<EhStatsStrategicVO> rows = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
			EhStatsStrategicVO row = new EhStatsStrategicVO();
			row.setLevel(entry.getKey());
			row.setCount(entry.getValue());
			row.setC(STRATEGIC_COLOR_MAP.getOrDefault(entry.getKey(), "#9c9fa5"));
			rows.add(row);
		}
		return rows;
	}

	@Override
	public List<EhStatsFunnelVO> getFunnel() {
		List<EhApply> applies = listApplies();
		List<EhOpportunity> opportunities = listOpportunities();
		List<EhVisitRecord> visitRecords = listVisitRecords();
		Set<Long> touchedApplyIds = opportunities.stream()
			.map(EhOpportunity::getApplyId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		touchedApplyIds.addAll(visitRecords.stream()
			.filter(item -> item.getOpportunityCode() != null && !item.getOpportunityCode().isBlank())
			.map(EhVisitRecord::getApplyId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet()));
		int receptionCount = (int) applies.stream().filter(item -> !"0".equals(item.getStatus()) && !"4".equals(item.getStatus())).count();
		int touchCount = touchedApplyIds.size();
		int followCount = (int) opportunities.stream().filter(item -> "opportunity".equals(item.getStatus()) || "signed".equals(item.getStatus())).count();
		int signedCount = (int) opportunities.stream().filter(item -> "signed".equals(item.getStatus())).count();

		List<EhStatsFunnelVO> rows = new ArrayList<>();
		rows.add(buildFunnel("参观接待", receptionCount));
		rows.add(buildFunnel("商机触达", touchCount));
		rows.add(buildFunnel("商机跟进", followCount));
		rows.add(buildFunnel("签约转化", signedCount));
		return rows;
	}

	@Override
	public List<EhStatsExportVO> exportStatsRows() {
		List<EhApply> applies = listApplies().stream()
			.sorted(Comparator.comparing(EhApply::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
			.collect(Collectors.toList());
		Map<Long, List<EhApplyVisitor>> visitorMap = listVisitors().stream().collect(Collectors.groupingBy(EhApplyVisitor::getApplyId));
		Map<Long, EhVisitRecord> visitRecordMap = listVisitRecords().stream()
			.collect(Collectors.toMap(EhVisitRecord::getApplyId, item -> item, (left, right) -> right));
		List<EhStatsExportVO> rows = new ArrayList<>();
		int serialNo = 1;
		for (EhApply apply : applies) {
			List<EhApplyVisitor> visitors = visitorMap.getOrDefault(apply.getId(), List.of());
			if (visitors.isEmpty()) {
				rows.add(buildExportRow(serialNo++, apply, null, visitRecordMap.get(apply.getId())));
				continue;
			}
			for (EhApplyVisitor visitor : visitors) {
				rows.add(buildExportRow(serialNo++, apply, visitor, visitRecordMap.get(apply.getId())));
			}
		}
		return rows;
	}

	private EhStatsKpiVO buildKpi(String label, String value, String unit, Boolean up, String change, String icon, Integer target) {
		EhStatsKpiVO row = new EhStatsKpiVO();
		row.setLabel(label);
		row.setValue(value);
		row.setUnit(unit);
		row.setUp(up);
		row.setChange(change);
		row.setIcon(icon);
		row.setTarget(target);
		return row;
	}

	private int countMonthly(List<EhApply> applies, int year, int month) {
		return (int) applies.stream()
			.filter(item -> item.getStartTime() != null)
			.filter(item -> item.getStartTime().getYear() == year)
			.filter(item -> item.getStartTime().getMonthValue() == month)
			.filter(item -> !"0".equals(item.getStatus()) && !"4".equals(item.getStatus()))
			.count();
	}

	private String normalizeIndustry(String industry) {
		if (industry == null || industry.isBlank()) {
			return "其他";
		}
		if (INDUSTRY_COLOR_MAP.containsKey(industry)) {
			return industry;
		}
		return "其他";
	}

	private EhStatsFunnelVO buildFunnel(String stage, int count) {
		EhStatsFunnelVO row = new EhStatsFunnelVO();
		row.setStage(stage);
		row.setCount(count);
		return row;
	}

	private EhStatsExportVO buildExportRow(Integer serialNo, EhApply apply, EhApplyVisitor visitor, EhVisitRecord visitRecord) {
		EhStatsExportVO row = new EhStatsExportVO();
		row.setSerialNo(serialNo);
		row.setVisitDate(apply.getStartTime() == null ? "" : apply.getStartTime().toLocalDate().toString());
		row.setVisitCompany(apply.getVisitorCompany());
		row.setCustomerName(visitor == null ? "" : defaultString(visitor.getCustomerCode(), visitor.getVisitorCompany()));
		row.setTitle(visitor == null ? "" : defaultString(visitor.getIndustry(), ""));
		row.setStrategicFlag(visitor != null && "1".equals(visitor.getIsKeyCustomer()) ? "是" : "否");
		row.setStrategicLevel(visitor == null ? "" : defaultString(visitor.getKeyCustomerLevel(), ""));
		row.setOpportunityCode(visitRecord == null ? "" : defaultString(visitRecord.getOpportunityCode(), ""));
		row.setHeadCount(apply.getVisitorCount());
		row.setLeader(defaultString(apply.getTopLeaderTitle(), "无"));
		row.setDept(apply.getApplicantDept());
		row.setApplicant(apply.getApplicant());
		row.setPhone(apply.getPhone());
		return row;
	}

	private String defaultString(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private List<EhApply> listApplies() {
		return ehApplyMapper.selectList(
			Wrappers.<EhApply>lambdaQuery()
				.eq(EhApply::getDelFlag, "0")
		);
	}

	private List<EhApplyVisitor> listVisitors() {
		return ehApplyVisitorMapper.selectList(
			Wrappers.<EhApplyVisitor>lambdaQuery()
				.eq(EhApplyVisitor::getDelFlag, "0")
		);
	}

	private List<EhOpportunity> listOpportunities() {
		return ehOpportunityMapper.selectList(
			Wrappers.<EhOpportunity>lambdaQuery()
				.eq(EhOpportunity::getDelFlag, "0")
		);
	}

	private List<EhVisitRecord> listVisitRecords() {
		return ehVisitRecordMapper.selectList(
			Wrappers.<EhVisitRecord>lambdaQuery()
				.eq(EhVisitRecord::getDelFlag, "0")
		);
	}
}
