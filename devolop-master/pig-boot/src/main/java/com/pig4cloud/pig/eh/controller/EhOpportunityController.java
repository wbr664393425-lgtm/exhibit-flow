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
import com.pig4cloud.pig.eh.entity.EhOpportunity;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.service.EhOpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/opportunity")
@Tag(description = "eh/opportunity", name = "商机跟进管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhOpportunityController {

	private final EhOpportunityService ehOpportunityService;
	private final EhAdminAggregateService ehAdminAggregateService;

	@GetMapping("/page")
	@HasPermission("eh_opportunity_view")
	@Operation(summary = "分页查询")
	public R getPage(@ParameterObject Page page, @ParameterObject EhOpportunity query) {
		return R.ok(ehOpportunityService.page(page, Wrappers.query(query)));
	}

	@GetMapping("/details/{id}")
	@HasPermission("eh_opportunity_view")
	@Operation(summary = "查看详情")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(ehOpportunityService.getById(id));
	}

	@PostMapping
	@SysLog("新增商机跟进")
	@HasPermission("eh_opportunity_add")
	@Operation(summary = "新增商机跟进")
	public R save(@RequestBody EhOpportunity entity) {
		return R.ok(ehOpportunityService.save(entity));
	}

	@PutMapping
	@SysLog("修改商机跟进")
	@HasPermission("eh_opportunity_edit")
	@Operation(summary = "修改商机跟进")
	public R update(@RequestBody EhOpportunity entity) {
		return R.ok(ehOpportunityService.updateById(entity));
	}

	@DeleteMapping
	@SysLog("删除商机跟进")
	@HasPermission("eh_opportunity_del")
	@Operation(summary = "删除商机跟进")
	public R removeById(@RequestBody Long[] ids) {
		return R.ok(ehOpportunityService.removeBatchByIds(CollUtil.toList(ids)));
	}

	@GetMapping("/page/aggregate")
	@HasPermission("eh_opportunity_view")
	@Operation(summary = "管理端商机聚合列表")
	public R getAggregateList() {
		return R.ok(ehAdminAggregateService.queryOpportunityAggregateList());
	}

}
