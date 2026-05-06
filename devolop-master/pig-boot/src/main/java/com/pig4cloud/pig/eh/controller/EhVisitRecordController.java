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
import com.pig4cloud.pig.eh.dto.ReturnSignDTO;
import com.pig4cloud.pig.eh.dto.VisitRecordUpsertDTO;
import com.pig4cloud.pig.eh.entity.EhVisitRecord;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.service.EhVisitRecordService;
import com.pig4cloud.pig.eh.vo.EhVisitRecordExportVO;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/visit")
@Tag(description = "eh/visit", name = "参观留存管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhVisitRecordController {

	private final EhVisitRecordService ehVisitRecordService;
	private final EhAdminAggregateService ehAdminAggregateService;

	@GetMapping("/page")
	@HasPermission("eh_visit_record_view")
	@Operation(summary = "分页查询")
	public R getPage(@ParameterObject Page page, @ParameterObject EhVisitRecord query) {
		return R.ok(ehVisitRecordService.page(page, Wrappers.query(query)));
	}

	@GetMapping("/details/{id}")
	@HasPermission("eh_visit_record_view")
	@Operation(summary = "查看详情")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(ehVisitRecordService.getById(id));
	}

	@PostMapping
	@SysLog("新增参观留存")
	@HasPermission("eh_visit_record_add")
	@Operation(summary = "新增参观留存")
	public R save(@RequestBody EhVisitRecord entity) {
		return R.ok(ehVisitRecordService.save(entity));
	}

	@PutMapping
	@SysLog("修改参观留存")
	@HasPermission("eh_visit_record_edit")
	@Operation(summary = "修改参观留存")
	public R update(@RequestBody EhVisitRecord entity) {
		return R.ok(ehVisitRecordService.updateById(entity));
	}

	@DeleteMapping
	@SysLog("删除参观留存")
	@HasPermission("eh_visit_record_del")
	@Operation(summary = "删除参观留存")
	public R removeById(@RequestBody Long[] ids) {
		return R.ok(ehVisitRecordService.removeBatchByIds(CollUtil.toList(ids)));
	}

	@GetMapping("/page/aggregate")
	@HasPermission("eh_visit_record_view")
	@Operation(summary = "管理端参观留存聚合列表")
	public R getAggregateList() {
		return R.ok(ehAdminAggregateService.queryVisitAggregateList());
	}

	@PostMapping("/upsert")
	@HasPermission("eh_visit_record_edit")
	@Operation(summary = "录入或编辑参观留存")
	public R upsert(@RequestBody VisitRecordUpsertDTO dto) {
		return R.ok(ehAdminAggregateService.upsertVisitRecord(dto));
	}

	@PostMapping("/return-sign")
	@HasPermission("eh_visit_record_edit")
	@Operation(summary = "归还签收")
	public R returnSign(@RequestBody ReturnSignDTO dto) {
		return R.ok(ehAdminAggregateService.signVisitReturn(dto));
	}

	@ResponseExcel(name = "参观留存记录")
	@GetMapping("/export")
	@HasPermission("eh_visit_record_export")
	@Operation(summary = "导出参观留存记录")
	public List<EhVisitRecordExportVO> export() {
		return ehAdminAggregateService.exportVisitRecordList();
	}

}
