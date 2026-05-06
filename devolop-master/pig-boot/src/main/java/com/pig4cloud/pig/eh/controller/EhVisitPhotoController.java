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
import com.pig4cloud.pig.eh.dto.VisitPhotoBatchDTO;
import com.pig4cloud.pig.eh.entity.EhVisitPhoto;
import com.pig4cloud.pig.eh.service.EhAdminAggregateService;
import com.pig4cloud.pig.eh.service.EhVisitPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/visit/photo")
@Tag(description = "eh/visit/photo", name = "现场照片管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class EhVisitPhotoController {

	private final EhVisitPhotoService ehVisitPhotoService;
	private final EhAdminAggregateService ehAdminAggregateService;

	@GetMapping("/page")
	@HasPermission("eh_visit_photo_view")
	@Operation(summary = "分页查询")
	public R getPage(@ParameterObject Page page, @ParameterObject EhVisitPhoto query) {
		return R.ok(ehVisitPhotoService.page(page, Wrappers.query(query)));
	}

	@GetMapping("/details/{id}")
	@HasPermission("eh_visit_photo_view")
	@Operation(summary = "查看详情")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(ehVisitPhotoService.getById(id));
	}

	@PostMapping
	@SysLog("新增现场照片")
	@HasPermission("eh_visit_photo_add")
	@Operation(summary = "新增现场照片")
	public R save(@RequestBody EhVisitPhoto entity) {
		return R.ok(ehVisitPhotoService.save(entity));
	}

	@PostMapping("/batch")
	@SysLog("批量保存现场照片")
	@HasPermission("eh_visit_photo_add")
	@Operation(summary = "批量保存现场照片")
	public R batchSave(@RequestBody VisitPhotoBatchDTO dto) {
		return R.ok(ehAdminAggregateService.saveVisitPhotos(dto));
	}

	@PutMapping
	@SysLog("修改现场照片")
	@HasPermission("eh_visit_photo_edit")
	@Operation(summary = "修改现场照片")
	public R update(@RequestBody EhVisitPhoto entity) {
		return R.ok(ehVisitPhotoService.updateById(entity));
	}

	@DeleteMapping
	@SysLog("删除现场照片")
	@HasPermission("eh_visit_photo_del")
	@Operation(summary = "删除现场照片")
	public R removeById(@RequestBody Long[] ids) {
		return R.ok(ehVisitPhotoService.removeBatchByIds(CollUtil.toList(ids)));
	}

	@GetMapping("/page/aggregate")
	@HasPermission("eh_visit_photo_view")
	@Operation(summary = "管理端照片聚合列表")
	public R getAggregateList() {
		return R.ok(ehAdminAggregateService.queryVisitPhotoAggregateList());
	}

	@GetMapping("/export-data-table")
	@HasPermission("eh_visit_photo_view")
	@Operation(summary = "按展厅参观数据表模板导出")
	public void exportDataTable(HttpServletResponse response) {
		ehAdminAggregateService.exportVisitPhotoDataTable(response);
	}

	@GetMapping("/export-zip")
	@HasPermission("eh_visit_photo_view")
	@Operation(summary = "导出现场照片压缩包")
	public void exportZip(@RequestParam(value = "applyId", required = false) Long applyId, HttpServletResponse response) {
		ehAdminAggregateService.exportVisitPhotosZip(applyId, response);
	}

}
