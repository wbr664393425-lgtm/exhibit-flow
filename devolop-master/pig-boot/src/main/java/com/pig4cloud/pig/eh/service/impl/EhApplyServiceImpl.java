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
import com.pig4cloud.pig.eh.entity.EhApprovalNode;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.mapper.EhApprovalNodeMapper;
import com.pig4cloud.pig.eh.mapper.EhApplyMapper;
import com.pig4cloud.pig.eh.service.EhApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EhApplyServiceImpl extends ServiceImpl<EhApplyMapper, EhApply> implements EhApplyService {

	private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
	private final EhApprovalNodeMapper ehApprovalNodeMapper;

	/**
	 * 时段冲突检测。
	 * 冲突条件（区间重叠）：reqStart < existEnd AND reqEnd > existStart
	 * 仅检测审批中（status=1）和已通过（status=2）的申请，草稿和已取消不参与检测。
	 */
	@Override
	public String checkConflict(String date, String startHour, String endHour) {
		LocalDateTime reqStart = LocalDate.parse(date).atTime(Integer.parseInt(startHour), 0);
		LocalDateTime reqEnd   = LocalDate.parse(date).atTime(Integer.parseInt(endHour), 0);

		List<EhApply> conflicts = list(
			Wrappers.<EhApply>lambdaQuery()
				.in(EhApply::getStatus, "1", "2")
				.lt(EhApply::getStartTime, reqEnd)
				.gt(EhApply::getEndTime, reqStart)
		);

		if (conflicts.isEmpty()) {
			return null;
		}

		EhApply first = conflicts.get(0);
		String startStr = first.getStartTime() != null ? first.getStartTime().format(TIME_FMT) : "--:--";
		String endStr   = first.getEndTime()   != null ? first.getEndTime().format(TIME_FMT)   : "--:--";
		return String.format("%s（%s - %s）", first.getSubject(), startStr, endStr);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean save(EhApply entity) {
		boolean saved = super.save(entity);
		if (!saved || entity == null || entity.getId() == null) {
			return saved;
		}

		// Ensure pending applications always have approval nodes.
		if (!"1".equals(entity.getStatus())) {
			return true;
		}

		Long existed = ehApprovalNodeMapper.selectCount(
			Wrappers.<EhApprovalNode>lambdaQuery()
				.eq(EhApprovalNode::getApplyId, entity.getId())
		);
		if (existed != null && existed > 0) {
			return true;
		}

		LocalDateTime now = LocalDateTime.now();
		List<EhApprovalNode> nodes = new ArrayList<>();
		nodes.add(buildNode(entity.getId(), 1, "部门领导", "pending", now));
		nodes.add(buildNode(entity.getId(), 2, "展厅主管", "waiting", now));
		nodes.add(buildNode(entity.getId(), 3, "分管领导", "waiting", now));

		for (EhApprovalNode node : nodes) {
			ehApprovalNodeMapper.insert(node);
		}
		return true;
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

}
