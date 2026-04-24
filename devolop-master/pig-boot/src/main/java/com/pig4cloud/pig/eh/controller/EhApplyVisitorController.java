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
import com.pig4cloud.pig.eh.entity.EhApplyVisitor;
import com.pig4cloud.pig.eh.service.EhApplyVisitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/apply/visitor")
@Tag(description = "eh/apply/visitor", name = "申请参观客户管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhApplyVisitorController {

	private final EhApplyVisitorService ehApplyVisitorService;

	@GetMapping("/page")
	@HasPermission("eh_apply_visitor_view")
	@Operation(summary = "分页查询")
	public R getPage(@ParameterObject Page page, @ParameterObject EhApplyVisitor query) {
		return R.ok(ehApplyVisitorService.page(page, Wrappers.query(query)));
	}

	@GetMapping("/details/{id}")
	@HasPermission("eh_apply_visitor_view")
	@Operation(summary = "查看详情")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(ehApplyVisitorService.getById(id));
	}

	@PostMapping
	@SysLog("新增申请参观客户")
	@HasPermission("eh_apply_visitor_add")
	@Operation(summary = "新增申请参观客户")
	public R save(@RequestBody EhApplyVisitor entity) {
		return R.ok(ehApplyVisitorService.save(entity));
	}

	@PutMapping
	@SysLog("修改申请参观客户")
	@HasPermission("eh_apply_visitor_edit")
	@Operation(summary = "修改申请参观客户")
	public R update(@RequestBody EhApplyVisitor entity) {
		return R.ok(ehApplyVisitorService.updateById(entity));
	}

	@DeleteMapping
	@SysLog("删除申请参观客户")
	@HasPermission("eh_apply_visitor_del")
	@Operation(summary = "删除申请参观客户")
	public R removeById(@RequestBody Long[] ids) {
		return R.ok(ehApplyVisitorService.removeBatchByIds(CollUtil.toList(ids)));
	}

}
