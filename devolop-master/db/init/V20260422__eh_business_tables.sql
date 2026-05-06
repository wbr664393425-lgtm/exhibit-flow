-- ============================================================
-- 展厅访问申请与留存系统 · 业务表结构
-- ============================================================

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `eh_apply` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `subject` VARCHAR(255) DEFAULT NULL COMMENT '会议主题',
    `meeting_nature` VARCHAR(16) NOT NULL DEFAULT 'external' COMMENT '会议性质：internal内部/external外部',
    `agenda` VARCHAR(2000) DEFAULT NULL COMMENT '简要议程',
    `start_time` DATETIME DEFAULT NULL COMMENT '使用开始时段',
    `end_time` DATETIME DEFAULT NULL COMMENT '使用结束时段',
    `visitor_company` VARCHAR(255) DEFAULT NULL COMMENT '来访单位',
    `industry` VARCHAR(64) DEFAULT NULL COMMENT '所属行业',
    `visitor_count` INT DEFAULT NULL COMMENT '人员数量',
    `customer_count` INT DEFAULT NULL COMMENT '客户人数',
    `internal_count` INT DEFAULT NULL COMMENT '自有人员人数',
    `top_leader_title` VARCHAR(64) DEFAULT NULL COMMENT '最高陪同领导级别',
    `applicant` VARCHAR(64) DEFAULT NULL COMMENT '申请人',
    `applicant_dept` VARCHAR(128) DEFAULT NULL COMMENT '申请部门',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `district` VARCHAR(64) DEFAULT NULL COMMENT '所属区县',
    `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    `real_phone` VARCHAR(32) DEFAULT NULL COMMENT '操作人手机号',
    `extra_services` VARCHAR(512) DEFAULT NULL COMMENT '附加服务',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '申请状态：0草稿/1待审批/2已通过/3已驳回/4已取消/5已改期',
    `actual_count` INT DEFAULT NULL COMMENT '实际到场人数',
    `return_signer` VARCHAR(64) DEFAULT NULL COMMENT '归还签字人',
    `return_time` DATETIME DEFAULT NULL COMMENT '归还时间',
    `remark` VARCHAR(2000) DEFAULT NULL COMMENT '备注',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记,0正常,1删除',
    PRIMARY KEY (`id`),
    KEY `idx_eh_apply_status` (`status`),
    KEY `idx_eh_apply_start_time` (`start_time`),
    KEY `idx_eh_apply_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='展厅申请';

CREATE TABLE IF NOT EXISTS `eh_apply_visitor` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `apply_id` BIGINT NOT NULL COMMENT '关联申请ID',
    `visitor_company` VARCHAR(255) DEFAULT NULL COMMENT '来访单位名称',
    `industry` VARCHAR(64) DEFAULT NULL COMMENT '所属行业/职务',
    `visitor_count` INT DEFAULT NULL COMMENT '人数',
    `is_key_customer` CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否战客,0否,1是',
    `key_customer_level` VARCHAR(64) DEFAULT NULL COMMENT '战客级别',
    `customer_code` VARCHAR(128) DEFAULT NULL COMMENT '全网客户编码/客户姓名',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记,0正常,1删除',
    PRIMARY KEY (`id`),
    KEY `idx_eh_apply_visitor_apply_id` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请参观客户';

CREATE TABLE IF NOT EXISTS `eh_approval_node` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `apply_id` BIGINT NOT NULL COMMENT '关联申请ID',
    `node_level` INT NOT NULL COMMENT '审批层级',
    `approver` VARCHAR(64) DEFAULT NULL COMMENT '审批人',
    `action` VARCHAR(32) DEFAULT NULL COMMENT '动作',
    `opinion` VARCHAR(1000) DEFAULT NULL COMMENT '审批意见',
    `action_time` DATETIME DEFAULT NULL COMMENT '操作时间',
    `status` VARCHAR(32) NOT NULL DEFAULT 'waiting' COMMENT '节点状态：waiting/pending/approved/rejected/transferred',
    `timeout_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否超时,0否,1是',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记,0正常,1删除',
    PRIMARY KEY (`id`),
    KEY `idx_eh_approval_node_apply_id` (`apply_id`),
    KEY `idx_eh_approval_node_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点';

CREATE TABLE IF NOT EXISTS `eh_visit_record` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `apply_id` BIGINT NOT NULL COMMENT '关联申请ID',
    `actual_visit_time` DATETIME DEFAULT NULL COMMENT '实际参观时间',
    `actual_visit_count` INT DEFAULT NULL COMMENT '实际参观人数',
    `our_leader_level` VARCHAR(64) DEFAULT NULL COMMENT '我方领导参会最高级别',
    `opportunity_code` VARCHAR(128) DEFAULT NULL COMMENT '商机编码',
    `return_signer` VARCHAR(64) DEFAULT NULL COMMENT '归还签字人',
    `return_time` DATETIME DEFAULT NULL COMMENT '归还时间',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '留存状态：0待录入/1已录入/2已归还',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记,0正常,1删除',
    PRIMARY KEY (`id`),
    KEY `idx_eh_visit_record_apply_id` (`apply_id`),
    KEY `idx_eh_visit_record_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参观留存';

CREATE TABLE IF NOT EXISTS `eh_visit_photo` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `visit_id` BIGINT DEFAULT NULL COMMENT '关联参观留存ID',
    `apply_id` BIGINT DEFAULT NULL COMMENT '关联申请ID',
    `file_url` VARCHAR(1000) NOT NULL COMMENT '图片URL',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记,0正常,1删除',
    PRIMARY KEY (`id`),
    KEY `idx_eh_visit_photo_visit_id` (`visit_id`),
    KEY `idx_eh_visit_photo_apply_id` (`apply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='现场照片';

CREATE TABLE IF NOT EXISTS `eh_opportunity` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `visit_id` BIGINT DEFAULT NULL COMMENT '关联参观留存ID',
    `apply_id` BIGINT DEFAULT NULL COMMENT '关联申请ID',
    `opportunity_code` VARCHAR(128) NOT NULL COMMENT '商机编码',
    `status` VARCHAR(32) NOT NULL DEFAULT 'clue' COMMENT '商机状态：clue线索/opportunity商机/signed签约',
    `remark` VARCHAR(2000) DEFAULT NULL COMMENT '备注',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记,0正常,1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_eh_opportunity_code` (`opportunity_code`),
    KEY `idx_eh_opportunity_apply_id` (`apply_id`),
    KEY `idx_eh_opportunity_visit_id` (`visit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机跟进';

CREATE TABLE IF NOT EXISTS `eh_change_log` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `apply_id` BIGINT NOT NULL COMMENT '关联申请ID',
    `change_type` VARCHAR(32) NOT NULL COMMENT '变更类型：cancel/reschedule/supplement',
    `old_start_time` DATETIME DEFAULT NULL COMMENT '原开始时段',
    `old_end_time` DATETIME DEFAULT NULL COMMENT '原结束时段',
    `new_start_time` DATETIME DEFAULT NULL COMMENT '新开始时段',
    `new_end_time` DATETIME DEFAULT NULL COMMENT '新结束时段',
    `reason` VARCHAR(1000) DEFAULT NULL COMMENT '变更原因',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '修改人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标记,0正常,1删除',
    PRIMARY KEY (`id`),
    KEY `idx_eh_change_log_apply_id` (`apply_id`),
    KEY `idx_eh_change_log_type` (`change_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='改期取消流水';
