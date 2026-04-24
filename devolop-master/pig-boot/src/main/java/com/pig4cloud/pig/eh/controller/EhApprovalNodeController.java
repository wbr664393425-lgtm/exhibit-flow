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
import com.pig4cloud.pig.eh.entity.EhApprovalNode;
import com.pig4cloud.pig.eh.service.EhApprovalNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/approval")
@Tag(description = "eh/approval", name = "审批节点管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhApprovalNodeController {

	private final EhApprovalNodeService ehApprovalNodeService;

	@GetMapping("/page")
	@HasPermission("eh_approval_node_view")
	@Operation(summary = "分页查询")
	public R getPage(@ParameterObject Page page, @ParameterObject EhApprovalNode query) {
		return R.ok(ehApprovalNodeService.page(page, Wrappers.query(query)));
	}

	@GetMapping("/details/{id}")
	@HasPermission("eh_approval_node_view")
	@Operation(summary = "查看详情")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(ehApprovalNodeService.getById(id));
	}

	@PostMapping
	@SysLog("新增审批节点")
	@HasPermission("eh_approval_node_add")
	@Operation(summary = "新增审批节点")
	public R save(@RequestBody EhApprovalNode entity) {
		return R.ok(ehApprovalNodeService.save(entity));
	}

	@PutMapping
	@SysLog("修改审批节点")
	@HasPermission("eh_approval_node_edit")
	@Operation(summary = "修改审批节点")
	public R update(@RequestBody EhApprovalNode entity) {
		return R.ok(ehApprovalNodeService.updateById(entity));
	}

	@DeleteMapping
	@SysLog("删除审批节点")
	@HasPermission("eh_approval_node_del")
	@Operation(summary = "删除审批节点")
	public R removeById(@RequestBody Long[] ids) {
		return R.ok(ehApprovalNodeService.removeBatchByIds(CollUtil.toList(ids)));
	}

}
