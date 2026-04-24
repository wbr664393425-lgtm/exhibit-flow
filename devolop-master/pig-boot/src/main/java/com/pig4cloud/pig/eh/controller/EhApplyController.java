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

package com.pig4cloud.pig.eh.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.eh.dto.ApprovalActionDTO;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.service.EhApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/apply")
@Tag(description = "eh/apply", name = "展厅申请管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhApplyController {

	private final EhApplyService ehApplyService;
	private final EhAdminAggregateService ehAdminAggregateService;

	@GetMapping("/page")
	@HasPermission("eh_apply_view")
	@Operation(summary = "分页查询")
	public R getPage(@ParameterObject Page page, @ParameterObject EhApply query) {
		return R.ok(ehApplyService.page(page, Wrappers.query(query)));
	}

	@GetMapping("/details/{id}")
	@HasPermission("eh_apply_view")
	@Operation(summary = "查看详情")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(ehApplyService.getById(id));
	}

	@PostMapping
	@SysLog("新增展厅申请")
	@HasPermission("eh_apply_add")
	@Operation(summary = "新增展厅申请")
	public R save(@RequestBody EhApply entity) {
		return R.ok(ehApplyService.save(entity));
	}

	@PutMapping
	@SysLog("修改展厅申请")
	@HasPermission("eh_apply_edit")
	@Operation(summary = "修改展厅申请")
	public R update(@RequestBody EhApply entity) {
		return R.ok(ehApplyService.updateById(entity));
	}

	@DeleteMapping
	@SysLog("删除展厅申请")
	@HasPermission("eh_apply_del")
	@Operation(summary = "删除展厅申请")
	public R removeById(@RequestBody Long[] ids) {
		return R.ok(ehApplyService.removeBatchByIds(CollUtil.toList(ids)));
	}

	@GetMapping("/conflict")
	@Operation(summary = "时段冲突检测（返回冲突申请描述，无冲突返回 null）")
	public R checkConflict(
		@RequestParam String date,
		@RequestParam String startHour,
		@RequestParam String endHour
	) {
		return R.ok(ehApplyService.checkConflict(date, startHour, endHour));
	}

	@GetMapping("/calendar")
	@Operation(summary = "排期日历（返回待审批和已通过的申请）")
	public R calendar() {
		List<EhApply> list = ehApplyService.list(
			Wrappers.<EhApply>lambdaQuery()
				.in(EhApply::getStatus, "1", "2")
				.orderByAsc(EhApply::getStartTime)
		);
		List<CalendarVO> vos = list.stream().map(a -> {
			CalendarVO vo = new CalendarVO();
			vo.setId(String.valueOf(a.getId()));
			vo.setTitle(a.getSubject());
			if (a.getStartTime() != null) {
				vo.setStart(a.getStartTime().toLocalDate().toString());
				vo.setStartTime(String.format("%02d:%02d", a.getStartTime().getHour(), a.getStartTime().getMinute()));
			}
			if (a.getEndTime() != null) {
				vo.setEnd(String.format("%02d:%02d", a.getEndTime().getHour(), a.getEndTime().getMinute()));
			}
			vo.setStatus("2".equals(a.getStatus()) ? "approved" : "pending");
			return vo;
		}).collect(Collectors.toList());
		return R.ok(vos);
	}

	@GetMapping("/page/aggregate")
	@HasPermission("eh_apply_view")
	@Operation(summary = "管理端申请聚合列表")
	public R getAggregateList(@RequestParam(required = false) String status) {
		return R.ok(ehAdminAggregateService.queryApplyAggregateList(status));
	}

	@GetMapping("/approval/todo")
	@HasPermission("eh_approval_node_view")
	@Operation(summary = "管理端待审批聚合列表")
	public R getApprovalTodoList() {
		return R.ok(ehAdminAggregateService.queryApprovalTodoList());
	}

	@PostMapping("/approval/action")
	@HasPermission("eh_approval_node_edit")
	@Operation(summary = "审批动作（通过/驳回/转交）")
	public R approvalAction(@RequestBody ApprovalActionDTO dto) {
		boolean success = ehAdminAggregateService.approveOrRejectOrTransfer(dto);
		if (!success) {
			return R.failed("审批处理失败，请刷新后重试");
		}
		return R.ok(true);
	}

	@Data
	static class CalendarVO {
		private String id;
		private String title;
		private String start;
		private String startTime;
		private String end;
		private String status;
	}

}
