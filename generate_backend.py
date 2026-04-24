#!/usr/bin/env python3
"""Generate backend 4-layer code for eh_* tables (pig-scaffold style)."""
import os

BASE = '/Users/wbr/Documents/project/exhibit-flow/devolop-master/pig-boot/src/main'
JAVA_BASE = f'{BASE}/java/com/pig4cloud/pig/eh'
RES_BASE = f'{BASE}/resources/mapper/eh'

# Table definitions: (table_name, entity_name, cn_name, module_path, perm_prefix, fields)
TABLES = [
    ('eh_apply', 'EhApply', '展厅申请', 'apply', 'eh_apply', [
        ('id', 'Long', '主键ID', True),
        ('subject', 'String', '会议主题', False),
        ('agenda', 'String', '简要议程', False),
        ('startTime', 'LocalDateTime', '使用开始时段', False),
        ('endTime', 'LocalDateTime', '使用结束时段', False),
        ('visitorCompany', 'String', '来访单位', False),
        ('industry', 'String', '所属行业', False),
        ('visitorCount', 'Integer', '人员数量', False),
        ('topLeaderTitle', 'String', '最高陪同领导级别', False),
        ('applicant', 'String', '申请人', False),
        ('applicantDept', 'String', '申请部门', False),
        ('phone', 'String', '联系电话', False),
        ('district', 'String', '所属区县', False),
        ('realName', 'String', '真实姓名', False),
        ('realPhone', 'String', '操作人手机号', False),
        ('extraServices', 'String', '附加服务', False),
        ('status', 'String', '申请状态', False),
        ('actualCount', 'Integer', '实际到场人数', False),
        ('returnSigner', 'String', '归还签字人', False),
        ('returnTime', 'LocalDateTime', '归还时间', False),
        ('remark', 'String', '备注', False),
    ]),
    ('eh_apply_visitor', 'EhApplyVisitor', '申请参观客户', 'apply/visitor', 'eh_apply_visitor', [
        ('id', 'Long', '主键ID', True),
        ('applyId', 'Long', '关联申请ID', False),
        ('visitorCompany', 'String', '来访单位名称', False),
        ('industry', 'String', '所属行业', False),
        ('visitorCount', 'Integer', '人数', False),
        ('isKeyCustomer', 'String', '是否战客', False),
        ('keyCustomerLevel', 'String', '战客级别', False),
        ('customerCode', 'String', '全网客户编码', False),
    ]),
    ('eh_approval_node', 'EhApprovalNode', '审批节点', 'approval', 'eh_approval_node', [
        ('id', 'Long', '主键ID', True),
        ('applyId', 'Long', '关联申请ID', False),
        ('nodeLevel', 'Integer', '审批层级', False),
        ('approver', 'String', '审批人', False),
        ('action', 'String', '动作', False),
        ('opinion', 'String', '审批意见', False),
        ('actionTime', 'LocalDateTime', '操作时间', False),
        ('status', 'String', '节点状态', False),
        ('timeoutFlag', 'String', '是否超时', False),
    ]),
    ('eh_visit_record', 'EhVisitRecord', '参观留存', 'visit', 'eh_visit_record', [
        ('id', 'Long', '主键ID', True),
        ('applyId', 'Long', '关联申请ID', False),
        ('actualVisitTime', 'LocalDateTime', '实际参观时间', False),
        ('actualVisitCount', 'Integer', '实际参观人数', False),
        ('ourLeaderLevel', 'String', '我方领导参会最高级别', False),
        ('opportunityCode', 'String', '商机编码', False),
        ('returnSigner', 'String', '归还签字人', False),
        ('returnTime', 'LocalDateTime', '归还时间', False),
        ('status', 'String', '留存状态', False),
    ]),
    ('eh_visit_photo', 'EhVisitPhoto', '现场照片', 'visit/photo', 'eh_visit_photo', [
        ('id', 'Long', '主键ID', True),
        ('visitId', 'Long', '关联参观留存ID', False),
        ('fileUrl', 'String', '图片URL', False),
        ('fileName', 'String', '原始文件名', False),
        ('fileSize', 'Long', '文件大小', False),
    ]),
    ('eh_opportunity', 'EhOpportunity', '商机跟进', 'opportunity', 'eh_opportunity', [
        ('id', 'Long', '主键ID', True),
        ('visitId', 'Long', '关联参观留存ID', False),
        ('applyId', 'Long', '关联申请ID', False),
        ('opportunityCode', 'String', '商机编码', False),
        ('status', 'String', '商机状态', False),
        ('remark', 'String', '备注', False),
    ]),
    ('eh_change_log', 'EhChangeLog', '改期取消流水', 'changelog', 'eh_change_log', [
        ('id', 'Long', '主键ID', True),
        ('applyId', 'Long', '关联申请ID', False),
        ('changeType', 'String', '变更类型', False),
        ('oldStartTime', 'LocalDateTime', '原开始时段', False),
        ('oldEndTime', 'LocalDateTime', '原结束时段', False),
        ('newStartTime', 'LocalDateTime', '新开始时段', False),
        ('newEndTime', 'LocalDateTime', '新结束时段', False),
        ('reason', 'String', '变更原因', False),
    ]),
]

COPYRIGHT = """/*
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
"""

def to_snake(name):
    """camelCase to snake_case"""
    import re
    return re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()

def gen_entity(tbl_name, entity, cn, fields):
    business_fields = ""
    for fname, ftype, fcomment, is_pk in fields:
        if is_pk:
            business_fields += f"\n\t@TableId(type = IdType.ASSIGN_ID)\n\t@Schema(description = \"{fcomment}\")\n\tprivate {ftype} {fname};\n"
        else:
            business_fields += f"\n\t@Schema(description = \"{fcomment}\")\n\tprivate {ftype} {fname};\n"

    return f"""{COPYRIGHT}
package com.pig4cloud.pig.eh.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("{tbl_name}")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "{cn}")
public class {entity} extends Model<{entity}> {{

\tprivate static final long serialVersionUID = 1L;
{business_fields}
\t@TableField(fill = FieldFill.INSERT)
\t@Schema(description = "创建人")
\tprivate String createBy;

\t@TableField(fill = FieldFill.INSERT)
\t@Schema(description = "创建时间")
\tprivate LocalDateTime createTime;

\t@TableField(fill = FieldFill.UPDATE)
\t@Schema(description = "修改人")
\tprivate String updateBy;

\t@TableField(fill = FieldFill.UPDATE)
\t@Schema(description = "修改时间")
\tprivate LocalDateTime updateTime;

\t@TableLogic
\t@TableField(fill = FieldFill.INSERT)
\t@Schema(description = "删除标记,1:已删除,0:正常")
\tprivate String delFlag;

}}
"""


def gen_mapper(entity, tbl_name):
    return f"""{COPYRIGHT}
package com.pig4cloud.pig.eh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.eh.entity.{entity};
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {entity}Mapper extends BaseMapper<{entity}> {{

}}
"""


def gen_mapper_xml(entity, tbl_name, mapper_pkg):
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.pig4cloud.pig.eh.mapper.{entity}Mapper">
</mapper>
"""


def gen_service(entity, cn):
    return f"""{COPYRIGHT}
package com.pig4cloud.pig.eh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.eh.entity.{entity};

public interface {entity}Service extends IService<{entity}> {{

}}
"""


def gen_service_impl(entity, cn):
    return f"""{COPYRIGHT}
package com.pig4cloud.pig.eh.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.eh.entity.{entity};
import com.pig4cloud.pig.eh.mapper.{entity}Mapper;
import com.pig4cloud.pig.eh.service.{entity}Service;
import org.springframework.stereotype.Service;

@Service
public class {entity}ServiceImpl extends ServiceImpl<{entity}Mapper, {entity}> implements {entity}Service {{

}}
"""


def gen_controller(entity, cn, module_path, perm_prefix, tbl_name):
    return f"""{COPYRIGHT}
package com.pig4cloud.pig.eh.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.eh.entity.{entity};
import com.pig4cloud.pig.eh.service.{entity}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/eh/{module_path}")
@Tag(description = "eh/{module_path}", name = "{cn}管理模块")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class {entity}Controller {{

\tprivate final {entity}Service {entity[0].lower() + entity[1:]}Service;

\t@GetMapping("/page")
\t@HasPermission("{perm_prefix}_view")
\t@Operation(summary = "分页查询")
\tpublic R getPage(@ParameterObject Page page, @ParameterObject {entity} query) {{
\t\treturn R.ok({entity[0].lower() + entity[1:]}Service.page(page, Wrappers.query(query)));
\t}}

\t@GetMapping("/details/{{id}}")
\t@HasPermission("{perm_prefix}_view")
\t@Operation(summary = "查看详情")
\tpublic R getById(@PathVariable("id") Long id) {{
\t\treturn R.ok({entity[0].lower() + entity[1:]}Service.getById(id));
\t}}

\t@PostMapping
\t@SysLog("新增{cn}")
\t@HasPermission("{perm_prefix}_add")
\t@Operation(summary = "新增{cn}")
\tpublic R save(@RequestBody {entity} entity) {{
\t\treturn R.ok({entity[0].lower() + entity[1:]}Service.save(entity));
\t}}

\t@PutMapping
\t@SysLog("修改{cn}")
\t@HasPermission("{perm_prefix}_edit")
\t@Operation(summary = "修改{cn}")
\tpublic R update(@RequestBody {entity} entity) {{
\t\treturn R.ok({entity[0].lower() + entity[1:]}Service.updateById(entity));
\t}}

\t@DeleteMapping
\t@SysLog("删除{cn}")
\t@HasPermission("{perm_prefix}_del")
\t@Operation(summary = "删除{cn}")
\tpublic R removeById(@RequestBody Long[] ids) {{
\t\treturn R.ok({entity[0].lower() + entity[1:]}Service.removeBatchByIds(CollUtil.toList(ids)));
\t}}

}}
"""


# Generate all files
for tbl_name, entity, cn, module_path, perm_prefix, fields in TABLES:
    # Entity
    path = f'{JAVA_BASE}/entity'
    os.makedirs(path, exist_ok=True)
    with open(f'{path}/{entity}.java', 'w', encoding='utf-8') as f:
        f.write(gen_entity(tbl_name, entity, cn, fields))
    print(f"  Created {path}/{entity}.java")

    # Mapper
    path = f'{JAVA_BASE}/mapper'
    os.makedirs(path, exist_ok=True)
    with open(f'{path}/{entity}Mapper.java', 'w', encoding='utf-8') as f:
        f.write(gen_mapper(entity, tbl_name))
    print(f"  Created {path}/{entity}Mapper.java")

    # Mapper XML
    path = RES_BASE
    os.makedirs(path, exist_ok=True)
    with open(f'{path}/{entity}Mapper.xml', 'w', encoding='utf-8') as f:
        f.write(gen_mapper_xml(entity, tbl_name, f'com.pig4cloud.pig.eh.mapper.{entity}Mapper'))
    print(f"  Created {path}/{entity}Mapper.xml")

    # Service
    path = f'{JAVA_BASE}/service'
    os.makedirs(path, exist_ok=True)
    with open(f'{path}/{entity}Service.java', 'w', encoding='utf-8') as f:
        f.write(gen_service(entity, cn))
    print(f"  Created {path}/{entity}Service.java")

    # Service Impl
    path = f'{JAVA_BASE}/service/impl'
    os.makedirs(path, exist_ok=True)
    with open(f'{path}/{entity}ServiceImpl.java', 'w', encoding='utf-8') as f:
        f.write(gen_service_impl(entity, cn))
    print(f"  Created {path}/{entity}ServiceImpl.java")

    # Controller
    path = f'{JAVA_BASE}/controller'
    os.makedirs(path, exist_ok=True)
    with open(f'{path}/{entity}Controller.java', 'w', encoding='utf-8') as f:
        f.write(gen_controller(entity, cn, module_path, perm_prefix, tbl_name))
    print(f"  Created {path}/{entity}Controller.java")

print("\nDone! All 7 entities x 6 files = 42 files generated.")
