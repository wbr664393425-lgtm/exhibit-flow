-- 展厅预约表单与现场照片导出调整
-- 兼容 MySQL 5.7：通过 information_schema 判断后再执行 DDL。

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_eh_column_if_missing;
DELIMITER $$
CREATE PROCEDURE add_eh_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition VARCHAR(1000),
    IN p_after_column VARCHAR(64)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN `', p_column_name, '` ', p_column_definition);
        IF p_after_column IS NOT NULL AND p_after_column <> '' THEN
            SET @ddl = CONCAT(@ddl, ' AFTER `', p_after_column, '`');
        END IF;
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_eh_column_if_missing('eh_apply', 'meeting_nature',
    'VARCHAR(16) NOT NULL DEFAULT ''external'' COMMENT ''会议性质：internal内部/external外部''', 'subject');
CALL add_eh_column_if_missing('eh_apply', 'customer_count',
    'INT DEFAULT NULL COMMENT ''客户人数''', 'visitor_count');
CALL add_eh_column_if_missing('eh_apply', 'internal_count',
    'INT DEFAULT NULL COMMENT ''自有人员人数''', 'customer_count');
CALL add_eh_column_if_missing('eh_visit_photo', 'apply_id',
    'BIGINT DEFAULT NULL COMMENT ''关联申请ID''', 'visit_id');

DROP PROCEDURE IF EXISTS add_eh_column_if_missing;

UPDATE `eh_apply`
SET `customer_count` = COALESCE(`customer_count`, `visitor_count`, 0),
    `internal_count` = COALESCE(`internal_count`, 0),
    `meeting_nature` = COALESCE(NULLIF(`meeting_nature`, ''), 'external')
WHERE `del_flag` = '0';

SET @visit_nullable = (
    SELECT IS_NULLABLE
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'eh_visit_photo'
      AND COLUMN_NAME = 'visit_id'
);
SET @ddl = IF(@visit_nullable = 'NO',
    'ALTER TABLE `eh_visit_photo` MODIFY COLUMN `visit_id` BIGINT DEFAULT NULL COMMENT ''关联参观留存ID''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'eh_visit_photo'
      AND INDEX_NAME = 'idx_eh_visit_photo_apply_id'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE `eh_visit_photo` ADD INDEX `idx_eh_visit_photo_apply_id` (`apply_id`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `eh_visit_photo` p
LEFT JOIN `eh_visit_record` v ON v.id = p.visit_id
SET p.apply_id = v.apply_id
WHERE p.apply_id IS NULL
  AND p.visit_id IS NOT NULL
  AND v.apply_id IS NOT NULL;

UPDATE `sys_menu`
SET `name` = '新增记录', `update_by` = 'admin', `update_time` = NOW()
WHERE `permission` = 'eh_apply_add';

UPDATE `sys_menu`
SET `visible` = '0', `update_by` = 'admin', `update_time` = NOW()
WHERE `menu_id` = 12001 OR `path` = '/eh/visit/index';
