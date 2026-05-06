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
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.eh.dto.ApprovalActionDTO;
import com.pig4cloud.pig.eh.dto.ApplyFormDTO;
import com.pig4cloud.pig.eh.dto.ApplyRescheduleDTO;
import com.pig4cloud.pig.eh.entity.EhApply;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.service.EhApplyService;
import com.pig4cloud.pig.eh.vo.EhCalendarExportVO;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

	@GetMapping("/my/page")
	@Operation(summary = "当前用户申请分页查询")
	public R getMyPage(@ParameterObject Page page) {
		String username = SecurityUtils.getUser().getUsername();
		return R.ok(ehApplyService.page(page,
			Wrappers.<EhApply>lambdaQuery()
				.eq(EhApply::getDelFlag, "0")
				.and(wrapper -> wrapper.eq(EhApply::getCreateBy, username).or().eq(EhApply::getApplicant, username))
				.orderByDesc(EhApply::getUpdateTime)
				.orderByDesc(EhApply::getCreateTime)));
	}

	@GetMapping("/details/{id}")
	@HasPermission("eh_apply_view")
	@Operation(summary = "查看聚合详情")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(ehApplyService.getApplyDetail(id));
	}

	@GetMapping("/my/details/{id}")
	@Operation(summary = "当前用户申请聚合详情")
	public R getMyById(@PathVariable("id") Long id) {
		String username = SecurityUtils.getUser().getUsername();
		EhApply apply = ehApplyService.getById(id);
		if (apply == null) {
			return R.ok(null);
		}
		if (!username.equals(apply.getCreateBy()) && !username.equals(apply.getApplicant())) {
			return R.failed("无权查看该申请");
		}
		return R.ok(ehApplyService.getApplyDetail(id));
	}

	@PostMapping
	@SysLog("新增展厅申请")
	@HasPermission("eh_apply_add")
	@Operation(summary = "新增展厅申请")
	public R save(@RequestBody EhApply entity) {
		return R.ok(ehApplyService.save(entity));
	}

	@PostMapping("/draft")
	@SysLog("保存展厅申请草稿")
	@HasPermission("eh_apply_add")
	@Operation(summary = "新建草稿")
	public R saveDraft(@RequestBody ApplyFormDTO dto) {
		return R.ok(ehApplyService.saveDraft(dto));
	}

	@PostMapping("/submit")
	@SysLog("提交展厅申请")
	@HasPermission("eh_apply_add")
	@Operation(summary = "新建并提交")
	public R submit(@RequestBody ApplyFormDTO dto) {
		return R.ok(ehApplyService.submitApply(dto));
	}

	@PutMapping
	@SysLog("修改展厅申请")
	@HasPermission("eh_apply_edit")
	@Operation(summary = "修改展厅申请")
	public R update(@RequestBody EhApply entity) {
		return R.ok(ehApplyService.updateById(entity));
	}

	@PutMapping("/{id}/draft")
	@SysLog("更新展厅申请草稿")
	@HasPermission("eh_apply_edit")
	@Operation(summary = "更新草稿")
	public R updateDraft(@PathVariable Long id, @RequestBody ApplyFormDTO dto) {
		return R.ok(ehApplyService.updateDraft(id, dto));
	}

	@PutMapping("/{id}/submit")
	@SysLog("更新并提交展厅申请")
	@HasPermission("eh_apply_edit")
	@Operation(summary = "更新后提交")
	public R updateAndSubmit(@PathVariable Long id, @RequestBody ApplyFormDTO dto) {
		return R.ok(ehApplyService.updateAndSubmit(id, dto));
	}

	@PutMapping("/{id}/agenda")
	@SysLog("补充展厅记录议程")
	@HasPermission("eh_apply_edit")
	@Operation(summary = "补充议程")
	public R updateAgenda(@PathVariable Long id, @RequestBody AgendaRequest request) {
		return R.ok(ehApplyService.updateAgenda(id, request == null ? null : request.getAgenda()));
	}

	@PutMapping("/{id}/cancel")
	@SysLog("取消展厅申请")
	@HasPermission("eh_apply_edit")
	@Operation(summary = "取消申请")
	public R cancel(@PathVariable Long id, @RequestBody(required = false) CancelRequest request) {
		return R.ok(ehApplyService.cancelApply(id, request == null ? null : request.getReason()));
	}

	@PutMapping("/{id}/reschedule")
	@SysLog("改期展厅申请")
	@HasPermission("eh_apply_edit")
	@Operation(summary = "改期申请")
	public R reschedule(@PathVariable Long id, @RequestBody ApplyRescheduleDTO dto) {
		return R.ok(ehApplyService.rescheduleApply(id, dto));
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
	public R checkConflict(@RequestParam String date, @RequestParam String startHour, @RequestParam String endHour) {
		return R.ok(ehApplyService.checkConflict(date, startHour, endHour));
	}

	@GetMapping("/calendar")
	@Operation(summary = "排期日历")
	public R calendar(@RequestParam(required = false) String start, @RequestParam(required = false) String end) {
		return R.ok(ehApplyService.listCalendarEvents(start, end));
	}

	@ResponseExcel(name = "排期日历")
	@GetMapping("/calendar/export")
	@HasPermission("eh_apply_export")
	@Operation(summary = "导出排期日历")
	public List<EhCalendarExportVO> exportCalendar(@RequestParam(required = false) String start,
		@RequestParam(required = false) String end) {
		return ehApplyService.listCalendarExportRows(start, end);
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
	static class CancelRequest {
		private String reason;
	}

	@Data
	static class AgendaRequest {
		private String agenda;
	}

}
