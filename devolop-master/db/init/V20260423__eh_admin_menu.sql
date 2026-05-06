-- ============================================================
-- 展厅访问申请与留存系统 · 菜单 + 按钮权限 + 角色授权
--
-- 作用：
-- 1) 新增「排期日历」菜单（挂到 11000 展厅申请管理下）
-- 2) 新增「统计看板」菜单（挂到 14000 统计报表下）
-- 3) 新增 15 条业务按钮权限（对齐前端 v-auth 指令串）
-- 4) 新增「展厅管理员」角色 role_id=5 EXHIBIT_ADMIN
-- 5) 超级管理员(1) 绑定所有新增条目；展厅管理员(5) 绑定全部展厅菜单+按钮
--
-- 已有菜单结构（exhibit_flow 实际数据，MAX menu_id=30014）：
--   11000 展厅申请管理  11001 申请列表  11002 审批节点  11003 改期取消日志
--   12000 参观留存      12001 留存记录  12002 现场照片
--   13000 商机跟进      13001 商机列表
--   14000 统计报表      14001 战客覆盖率 14002 分布统计 14003 商机转化
--   15000 基础数据      15001 客户档案  15002 字典维护
-- 新 menu_id 区段：30100–30211
-- 幂等性：全部用 INSERT IGNORE + 固定 ID，可重复执行
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 新增功能菜单（menu_type=0）
-- ------------------------------------------------------------
INSERT IGNORE INTO `sys_menu`
    (`menu_id`, `name`, `en_name`, `permission`, `path`, `parent_id`, `icon`, `visible`, `sort_order`, `keep_alive`, `embedded`, `menu_type`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES
    -- 排期日历：挂到「展厅申请管理」(11000) 下
    (30100, '排期日历', 'calendar',  NULL, '/eh/apply/calendar',   11000, 'ele-Calendar', '1', 4, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
    -- 统计看板：挂到「统计报表」(14000) 下
    (30200, '统计看板', 'dashboard', NULL, '/eh/report/dashboard', 14000, 'ele-DataLine', '1', 0, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0');

-- ------------------------------------------------------------
-- 2. 新增按钮权限（menu_type=1）
-- ------------------------------------------------------------
INSERT IGNORE INTO `sys_menu`
    (`menu_id`, `name`, `en_name`, `permission`, `path`, `parent_id`, `icon`, `visible`, `sort_order`, `keep_alive`, `embedded`, `menu_type`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`)
VALUES
    -- 申请列表（parent=11001）
    (30110, '新增记录',   NULL, 'eh_apply_add',     NULL, 11001, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30111, '编辑申请',   NULL, 'eh_apply_edit',    NULL, 11001, NULL, '1', 6, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30112, '删除申请',   NULL, 'eh_apply_del',     NULL, 11001, NULL, '1', 7, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30113, '导出申请',   NULL, 'eh_apply_export',  NULL, 11001, NULL, '1', 8, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

    -- 审批节点（parent=11002）
    (30120, '查看审批',   NULL, 'eh_approval_node_view', NULL, 11002, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30121, '处理审批',   NULL, 'eh_approval_node_edit', NULL, 11002, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30122, '删除审批',   NULL, 'eh_approval_node_del',  NULL, 11002, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30123, '新增审批',   NULL, 'eh_approval_node_add',  NULL, 11002, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

    -- 留存记录（parent=12001）
    (30130, '查看留存',   NULL, 'eh_visit_record_view', NULL, 12001, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30131, '录入留存',   NULL, 'eh_visit_record_add',  NULL, 12001, NULL, '1', 6, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30132, '编辑留存',   NULL, 'eh_visit_record_edit', NULL, 12001, NULL, '1', 7, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30133, '删除留存',   NULL, 'eh_visit_record_del',    NULL, 12001, NULL, '1', 8, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30134, '导出留存',   NULL, 'eh_visit_record_export', NULL, 12001, NULL, '1', 9, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

    -- 现场照片（parent=12002）
    (30140, '查看照片',   NULL, 'eh_visit_photo_view', NULL, 12002, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30141, '上传照片',   NULL, 'eh_visit_photo_add',  NULL, 12002, NULL, '1', 6, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30142, '编辑照片',   NULL, 'eh_visit_photo_edit', NULL, 12002, NULL, '1', 7, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30143, '删除照片',   NULL, 'eh_visit_photo_del',  NULL, 12002, NULL, '1', 8, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),

    -- 统计看板（parent=30200）
    (30210, '导出Excel',  NULL, 'eh_dashboard_export_excel', NULL, 30200, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
    (30211, '导出Word',   NULL, 'eh_dashboard_export_word',  NULL, 30200, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0');

-- ------------------------------------------------------------
-- 3. 新增角色：展厅管理员
-- ------------------------------------------------------------
INSERT IGNORE INTO `sys_role`
    (`role_id`, `role_code`, `role_name`, `role_desc`, `create_by`, `update_by`, `create_time`, `update_time`, `del_flag`)
VALUES
    (5, 'EXHIBIT_ADMIN', '展厅管理员', '展厅访问申请与留存系统管理员：排期/审批/留存/统计', 'admin', 'admin', NOW(), NOW(), '0');

-- ------------------------------------------------------------
-- 4. 角色菜单授权
-- 超级管理员(1)：追加所有新增菜单+按钮
-- 展厅管理员(5)：全部展厅目录+菜单+按钮（含既有 + 新增）
-- ------------------------------------------------------------
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
    -- ==== 超级管理员：新增部分 ====
    (1, 30100), (1, 30200),
    (1, 30110), (1, 30111), (1, 30112), (1, 30113),
    (1, 30120), (1, 30121), (1, 30122), (1, 30123),
    (1, 30130), (1, 30131), (1, 30132), (1, 30133), (1, 30134),
    (1, 30140), (1, 30141), (1, 30142), (1, 30143),
    (1, 30210), (1, 30211),

    -- ==== 展厅管理员：既有目录 ====
    (5, 11000), (5, 11001), (5, 11002), (5, 11003),
    (5, 12000), (5, 12001), (5, 12002),
    (5, 13000), (5, 13001),
    (5, 14000), (5, 14001), (5, 14002), (5, 14003),
    (5, 15000), (5, 15001), (5, 15002),
    -- ==== 展厅管理员：既有按钮 ====
    (5, 11100), (5, 11101), (5, 11102), (5, 11103), (5, 11104), (5, 11105),
    (5, 11200), (5, 11201), (5, 11202), (5, 11203),
    (5, 11300), (5, 11301), (5, 11302), (5, 11303),
    (5, 12100), (5, 12101), (5, 12102), (5, 12103), (5, 12104),
    (5, 12200), (5, 12201), (5, 12202), (5, 12203),
    (5, 13100), (5, 13101), (5, 13102), (5, 13103), (5, 13104),
    -- ==== 展厅管理员：新增菜单 ====
    (5, 30100), (5, 30200),
    -- ==== 展厅管理员：新增按钮 ====
    (5, 30110), (5, 30111), (5, 30112), (5, 30113),
    (5, 30120), (5, 30121), (5, 30122), (5, 30123),
    (5, 30130), (5, 30131), (5, 30132), (5, 30133), (5, 30134),
    (5, 30140), (5, 30141), (5, 30142), (5, 30143),
    (5, 30210), (5, 30211);

UPDATE `sys_menu`
SET `name` = '新增记录', `update_by` = 'admin', `update_time` = NOW()
WHERE `permission` = 'eh_apply_add';

UPDATE `sys_menu`
SET `visible` = '0', `update_by` = 'admin', `update_time` = NOW()
WHERE `menu_id` = 12001 OR `path` = '/eh/visit/index';
