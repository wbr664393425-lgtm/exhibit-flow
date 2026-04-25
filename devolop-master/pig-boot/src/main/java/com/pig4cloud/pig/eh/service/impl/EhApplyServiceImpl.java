/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the pig4cloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.pig4cloud.pig.eh.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.eh.dto.ApplyFormDTO;
import com.pig4cloud.pig.eh.dto.ApplyRescheduleDTO;
import com.pig4cloud.pig.eh.entity.EhApprovalNode;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.entity.EhApplyVisitor;
import com.pig4cloud.pig.eh.entity.EhChangeLog;
import com.pig4cloud.pig.eh.entity.EhVisitRecord;
import com.pig4cloud.pig.eh.mapper.EhApprovalNodeMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyVisitorMapper;
import com.pig4cloud.pig.eh.mapper.EhChangeLogMapper;
import com.pig4cloud.pig.eh.mapper.EhVisitRecordMapper;
import com.pig4cloud.pig.eh.service.EhApplyService;
import com.pig4cloud.pig.eh.service.EhNotificationService;
import com.pig4cloud.pig.eh.vo.ApplyDetailVO;
import com.pig4cloud.pig.eh.vo.EhCalendarEventVO;
import com.pig4cloud.pig.eh.vo.EhCalendarExportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EhApplyServiceImpl extends ServiceImpl<EhApplyMapper, EhApply> implements EhApplyService {

	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

	private final EhApprovalNodeMapper ehApprovalNodeMapper;
	private final EhApplyVisitorMapper ehApplyVisitorMapper;
	private final EhVisitRecordMapper ehVisitRecordMapper;
	private final EhChangeLogMapper ehChangeLogMapper;
	private final EhNotificationService ehNotificationService;

	/**
	 * 时段冲突检测。
	 * 冲突条件（区间重叠）：reqStart < existEnd AND reqEnd > existStart
	 * 仅检测审批中（status=1）和已通过（status=2）的申请，草稿和已取消不参与检测。
	 */
	@Override
	public String checkConflict(String date, String startHour, String endHour) {
		LocalDateTime reqStart = parseDateTime(date, startHour);
		LocalDateTime reqEnd = parseDateTime(date, endHour);

		List<EhApply> conflicts = list(
			Wrappers.<EhApply>lambdaQuery()
				.in(EhApply::getStatus, "1", "2", "5")
				.lt(EhApply::getStartTime, reqEnd)
				.gt(EhApply::getEndTime, reqStart)
		);

		if (conflicts.isEmpty()) {
			return null;
		}

		EhApply first = conflicts.get(0);
		String startStr = first.getStartTime() != null ? first.getStartTime().format(TIME_FMT) : "--:--";
		String endStr = first.getEndTime() != null ? first.getEndTime().format(TIME_FMT) : "--:--";
		return String.format("%s（%s - %s）", first.getSubject(), startStr, endStr);
	}

	@Override
	public ApplyDetailVO getApplyDetail(Long id) {
		EhApply apply = getById(id);
		if (apply == null) {
			return null;
		}
		ApplyDetailVO detail = new ApplyDetailVO();
		detail.setId(apply.getId());
		detail.setSubject(apply.getSubject());
		detail.setVisitorCompany(apply.getVisitorCompany());
		detail.setIndustry(apply.getIndustry());
		detail.setDistrict(apply.getDistrict());
		detail.setApplicant(apply.getApplicant());
		detail.setPhone(apply.getPhone());
		detail.setApplicantDept(apply.getApplicantDept());
		detail.setStartTime(apply.getStartTime());
		detail.setEndTime(apply.getEndTime());
		detail.setTopLeaderTitle(apply.getTopLeaderTitle());
		detail.setVisitorCount(apply.getVisitorCount());
		detail.setAgenda(apply.getAgenda());
		detail.setExtraServices(apply.getExtraServices());
		detail.setStatus(apply.getStatus());
		detail.setRemark(apply.getRemark());
		detail.setCreateTime(apply.getCreateTime());

		List<EhApplyVisitor> visitors = ehApplyVisitorMapper.selectList(
			Wrappers.<EhApplyVisitor>lambdaQuery()
				.eq(EhApplyVisitor::getApplyId, id)
				.orderByAsc(EhApplyVisitor::getCreateTime, EhApplyVisitor::getId)
		);
		detail.setVisitors(visitors.stream().map(this::buildVisitorDetail).collect(Collectors.toList()));

		List<EhApprovalNode> nodes = ehApprovalNodeMapper.selectList(
			Wrappers.<EhApprovalNode>lambdaQuery()
				.eq(EhApprovalNode::getApplyId, id)
				.orderByAsc(EhApprovalNode::getNodeLevel, EhApprovalNode::getId)
		);
		detail.setApprovalNodes(nodes.stream().map(this::buildApprovalDetail).collect(Collectors.toList()));

		EhVisitRecord visitRecord = ehVisitRecordMapper.selectOne(
			Wrappers.<EhVisitRecord>lambdaQuery()
				.eq(EhVisitRecord::getApplyId, id)
				.orderByDesc(EhVisitRecord::getCreateTime, EhVisitRecord::getId)
				.last("limit 1")
		);
		if (visitRecord != null) {
			detail.setOpportunityCode(visitRecord.getOpportunityCode());
		}
		return detail;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveDraft(ApplyFormDTO dto) {
		return saveOrUpdateForm(null, dto, "0");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submitApply(ApplyFormDTO dto) {
		return saveOrUpdateForm(null, dto, "1");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateDraft(Long id, ApplyFormDTO dto) {
		return saveOrUpdateForm(id, dto, "0");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateAndSubmit(Long id, ApplyFormDTO dto) {
		return saveOrUpdateForm(id, dto, "1");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean cancelApply(Long id, String reason) {
		EhApply apply = getById(id);
		if (apply == null) {
			return false;
		}
		EhApply patch = new EhApply();
		patch.setId(id);
		patch.setStatus("4");
		boolean updated = updateById(patch);
		if (!updated) {
			return false;
		}
		writeChangeLog(apply, "cancel", reason, null, null);
		notifyApplicant(
			apply,
			"reminder",
			"申请已取消",
			String.format("您的展厅申请《%s》已取消。", defaultString(apply.getSubject(), "未命名申请"))
		);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean rescheduleApply(Long id, ApplyRescheduleDTO dto) {
		EhApply apply = getById(id);
		if (apply == null) {
			return false;
		}
		LocalDateTime newStart = parseDateTime(dto.getNewDate(), dto.getNewSH());
		LocalDateTime newEnd = parseDateTime(dto.getNewDate(), dto.getNewEH());
		EhApply patch = new EhApply();
		patch.setId(id);
		patch.setStartTime(newStart);
		patch.setEndTime(newEnd);
		patch.setStatus("5");
		boolean updated = updateById(patch);
		if (!updated) {
			return false;
		}
		writeChangeLog(apply, "reschedule", dto.getReason(), newStart, newEnd);
		notifyApplicant(
			apply,
			"reminder",
			"申请已改期",
			String.format("您的展厅申请《%s》已提交改期，新的预约时间为 %s。", defaultString(apply.getSubject(), "未命名申请"), newStart)
		);
		return true;
	}

	@Override
	public List<EhCalendarEventVO> listCalendarEvents(String start, String end) {
		return listCalendarApplies(start, end).stream().map(this::buildCalendarEvent).collect(Collectors.toList());
	}

	@Override
	public List<EhCalendarExportVO> listCalendarExportRows(String start, String end) {
		return listCalendarApplies(start, end).stream().map(this::buildCalendarExportRow).collect(Collectors.toList());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean save(EhApply entity) {
		boolean saved = super.save(entity);
		if (!saved || entity == null || entity.getId() == null) {
			return saved;
		}
		if ("1".equals(entity.getStatus())) {
			rebuildApprovalNodes(entity.getId(), entity.getTopLeaderTitle());
		}
		return true;
	}

	private boolean saveOrUpdateForm(Long id, ApplyFormDTO dto, String status) {
		EhApply entity = id == null ? new EhApply() : getById(id);
		if (entity == null) {
			return false;
		}
		fillApply(entity, dto, status);
		boolean ok = id == null ? baseMapper.insert(entity) > 0 : updateById(entity);
		if (!ok) {
			return false;
		}
		replaceVisitors(entity.getId(), dto.getVisitors());
		if ("1".equals(status)) {
			rebuildApprovalNodes(entity.getId(), entity.getTopLeaderTitle());
		} else {
			ehApprovalNodeMapper.delete(
				Wrappers.<EhApprovalNode>lambdaQuery().eq(EhApprovalNode::getApplyId, entity.getId())
			);
		}
		return true;
	}

	private void fillApply(EhApply entity, ApplyFormDTO dto, String status) {
		entity.setSubject(dto.getTitle());
		entity.setAgenda(dto.getAgenda());
		entity.setStartTime(parseDateTime(dto.getStartDate(), dto.getStartHour()));
		entity.setEndTime(parseDateTime(dto.getStartDate(), dto.getEndHour()));
		entity.setVisitorCompany(dto.getUnit());
		entity.setIndustry(dto.getIndustry());
		entity.setVisitorCount(dto.getHeadCount());
		entity.setTopLeaderTitle(dto.getLeader());
		entity.setApplicant(dto.getApplicant());
		entity.setApplicantDept(dto.getDept());
		entity.setPhone(dto.getPhone());
		entity.setDistrict(dto.getDistrict());
		entity.setRealName(dto.getApplicant());
		entity.setRealPhone(dto.getPhone());
		entity.setExtraServices(dto.getServices() == null ? "" : String.join(",", dto.getServices()));
		entity.setStatus(status);
		entity.setRemark(dto.getRemark());
	}

	private void replaceVisitors(Long applyId, List<ApplyFormDTO.VisitorDTO> visitors) {
		ehApplyVisitorMapper.delete(
			Wrappers.<EhApplyVisitor>lambdaQuery().eq(EhApplyVisitor::getApplyId, applyId)
		);
		if (visitors == null || visitors.isEmpty()) {
			return;
		}
		for (ApplyFormDTO.VisitorDTO visitor : visitors) {
			EhApplyVisitor entity = new EhApplyVisitor();
			entity.setApplyId(applyId);
			entity.setVisitorCompany(visitor.getUnit());
			entity.setCustomerCode(visitor.getName());
			entity.setIndustry(visitor.getTitle());
			entity.setVisitorCount(1);
			entity.setIsKeyCustomer(Boolean.TRUE.equals(visitor.getIsStrategic()) ? "1" : "0");
			entity.setKeyCustomerLevel(visitor.getStrategicLevel());
			ehApplyVisitorMapper.insert(entity);
		}
	}

	private void rebuildApprovalNodes(Long applyId, String leader) {
		ehApprovalNodeMapper.delete(
			Wrappers.<EhApprovalNode>lambdaQuery().eq(EhApprovalNode::getApplyId, applyId)
		);
		LocalDateTime now = LocalDateTime.now();
		List<EhApprovalNode> nodes = new ArrayList<>();
		nodes.add(buildNode(applyId, 1, "部门领导", "pending", now));
		nodes.add(buildNode(applyId, 2, "展厅主管", "waiting", now));
		if (shouldAddLeaderNode(leader)) {
			nodes.add(buildNode(applyId, 3, leader, "waiting", now));
		}
		for (EhApprovalNode node : nodes) {
			ehApprovalNodeMapper.insert(node);
		}
	}

	private boolean shouldAddLeaderNode(String leader) {
		if (!StringUtils.hasText(leader)) {
			return false;
		}
		String normalized = leader.trim();
		return normalized.contains("总经理") || normalized.contains("分管副总");
	}

	private EhApprovalNode buildNode(Long applyId, Integer level, String approver, String status, LocalDateTime now) {
		EhApprovalNode node = new EhApprovalNode();
		node.setApplyId(applyId);
		node.setNodeLevel(level);
		node.setApprover(approver);
		node.setAction("");
		node.setStatus(status);
		node.setTimeoutFlag("0");
		node.setCreateTime(now);
		node.setUpdateTime(now);
		return node;
	}

	private ApplyDetailVO.VisitorVO buildVisitorDetail(EhApplyVisitor visitor) {
		ApplyDetailVO.VisitorVO item = new ApplyDetailVO.VisitorVO();
		item.setId(visitor.getId());
		item.setName(defaultString(visitor.getCustomerCode(), visitor.getVisitorCompany()));
		item.setTitle(visitor.getIndustry());
		item.setVisitorCompany(visitor.getVisitorCompany());
		item.setUnit(visitor.getVisitorCompany());
		item.setIsKeyCustomer(visitor.getIsKeyCustomer());
		item.setKeyCustomerLevel(visitor.getKeyCustomerLevel());
		return item;
	}

	private ApplyDetailVO.ApprovalNodeVO buildApprovalDetail(EhApprovalNode node) {
		ApplyDetailVO.ApprovalNodeVO item = new ApplyDetailVO.ApprovalNodeVO();
		item.setId(node.getId());
		item.setNodeLevel(node.getNodeLevel());
		item.setApprover(node.getApprover());
		item.setAction(node.getAction());
		item.setOpinion(node.getOpinion());
		item.setActionTime(node.getActionTime());
		item.setStatus(node.getStatus());
		return item;
	}

	private void writeChangeLog(EhApply apply, String changeType, String reason, LocalDateTime newStart, LocalDateTime newEnd) {
		EhChangeLog log = new EhChangeLog();
		log.setApplyId(apply.getId());
		log.setChangeType(changeType);
		log.setOldStartTime(apply.getStartTime());
		log.setOldEndTime(apply.getEndTime());
		log.setNewStartTime(newStart);
		log.setNewEndTime(newEnd);
		log.setReason(reason);
		ehChangeLogMapper.insert(log);
	}

	private void notifyApplicant(EhApply apply, String type, String title, String content) {
		String recipient = defaultString(apply.getCreateBy(), apply.getApplicant());
		ehNotificationService.createNotification(apply.getId(), recipient, type, title, content, buildApplyRoute(apply.getId()));
	}

	private String buildApplyRoute(Long applyId) {
		return applyId == null ? "/mine" : "/apply/" + applyId;
	}

	private List<EhApply> listCalendarApplies(String start, String end) {
		LocalDateTime startTime = StringUtils.hasText(start) ? LocalDate.parse(start).atStartOfDay() : null;
		LocalDateTime endTime = StringUtils.hasText(end) ? LocalDate.parse(end).plusDays(1).atStartOfDay() : null;
		return list(
			Wrappers.<EhApply>lambdaQuery()
				.in(EhApply::getStatus, "1", "2", "5")
				.ge(startTime != null, EhApply::getStartTime, startTime)
				.lt(endTime != null, EhApply::getStartTime, endTime)
				.orderByAsc(EhApply::getStartTime)
		);
	}

	private EhCalendarEventVO buildCalendarEvent(EhApply apply) {
		EhCalendarEventVO row = new EhCalendarEventVO();
		row.setId(String.valueOf(apply.getId()));
		row.setTitle(apply.getSubject());
		if (apply.getStartTime() != null) {
			row.setStart(apply.getStartTime().toLocalDate().toString());
			row.setStartTime(apply.getStartTime().format(TIME_FMT));
		}
		if (apply.getEndTime() != null) {
			row.setEnd(apply.getEndTime().format(TIME_FMT));
		}
		row.setStatus(convertCalendarStatus(apply.getStatus()));
		return row;
	}

	private EhCalendarExportVO buildCalendarExportRow(EhApply apply) {
		EhCalendarExportVO row = new EhCalendarExportVO();
		row.setId(String.valueOf(apply.getId()));
		row.setTitle(apply.getSubject());
		row.setStart(apply.getStartTime() == null ? "" : apply.getStartTime().toLocalDate().toString());
		row.setStartTime(apply.getStartTime() == null ? "" : apply.getStartTime().format(TIME_FMT));
		row.setEnd(apply.getEndTime() == null ? "" : apply.getEndTime().format(TIME_FMT));
		row.setStatus(convertCalendarStatusText(apply.getStatus()));
		return row;
	}

	private String convertCalendarStatus(String status) {
		if ("2".equals(status)) {
			return "approved";
		}
		if ("5".equals(status)) {
			return "rescheduled";
		}
		return "pending";
	}

	private String convertCalendarStatusText(String status) {
		if ("2".equals(status)) {
			return "已通过";
		}
		if ("5".equals(status)) {
			return "已改期";
		}
		return "待审批";
	}

	private LocalDateTime parseDateTime(String date, String hour) {
		return LocalDate.parse(date).atTime(Integer.parseInt(hour), 0);
	}

	private String defaultString(String primary, String fallback) {
		return StringUtils.hasText(primary) ? primary : fallback;
	}

}
