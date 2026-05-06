CREATE TABLE IF NOT EXISTS eh_apply_history (
    id          BIGINT       NOT NULL               COMMENT '主键ID',
    apply_id    BIGINT       NOT NULL               COMMENT '关联申请ID',
    event_type  VARCHAR(32)  NOT NULL               COMMENT '事件类型：submit/resubmit/approve/reject/reschedule/cancel',
    event_desc  VARCHAR(256) NOT NULL               COMMENT '事件描述',
    operator    VARCHAR(64)  DEFAULT NULL           COMMENT '操作人',
    remark      VARCHAR(512) DEFAULT NULL           COMMENT '备注（驳回原因、改期原因等）',
    event_time  DATETIME     NOT NULL               COMMENT '事件时间',
    create_by   VARCHAR(64)  DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    del_flag    CHAR(1)      DEFAULT '0'            COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_apply_history_apply_id (apply_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '申请历史事件流水';
