-- ============================================================
-- V4.0 待办中心升级 — 优先级/截止时间/业务跳转/催办
-- ============================================================

-- 1. sys_todo 表扩展字段
ALTER TABLE sys_todo
  ADD COLUMN priority    TINYINT      DEFAULT 0    COMMENT '优先级: 0普通 1紧急 2特急' AFTER status,
  ADD COLUMN deadline    DATETIME     DEFAULT NULL COMMENT '截止时间' AFTER priority,
  ADD COLUMN link_url    VARCHAR(500) DEFAULT NULL COMMENT '业务跳转路径(前端路由)' AFTER deadline,
  ADD COLUMN biz_title   VARCHAR(200) DEFAULT NULL COMMENT '关联业务单据标题' AFTER link_url,
  ADD COLUMN handled_at  DATETIME     DEFAULT NULL COMMENT '处理时间' AFTER biz_title,
  ADD COLUMN handler_id  INT          DEFAULT NULL COMMENT '处理人ID(委托处理场景)' AFTER handled_at,
  ADD COLUMN remind_count INT         DEFAULT 0    COMMENT '催办次数' AFTER handler_id,
  ADD COLUMN last_remind_at DATETIME  DEFAULT NULL COMMENT '最近催办时间' AFTER remind_count;

-- 2. 索引优化
ALTER TABLE sys_todo
  ADD INDEX idx_todo_user_status (user_id, status),
  ADD INDEX idx_todo_biz (biz_type, biz_id),
  ADD INDEX idx_todo_deadline (deadline);
