-- =====================================================
-- V3.8 项目进度管理模块升级 — WBS多级任务 + 依赖约束 + 状态流转 + 进度回滚
-- =====================================================

-- 1. biz_gantt_task 扩展字段
ALTER TABLE biz_gantt_task
  ADD COLUMN assignee_id INT COMMENT '负责人ID' AFTER parent_id,
  ADD COLUMN weight DECIMAL(5,2) DEFAULT 1.00 COMMENT '进度权重(用于加权汇总)' AFTER progress_pct,
  ADD COLUMN planned_duration INT COMMENT '计划工期(天)' AFTER weight,
  ADD COLUMN actual_duration INT COMMENT '实际工期(天)' AFTER planned_duration,
  ADD COLUMN delay_days INT DEFAULT 0 COMMENT '延期天数' AFTER actual_duration,
  ADD COLUMN wbs_code VARCHAR(50) COMMENT 'WBS编码(如1.2.3)' AFTER delay_days,
  ADD COLUMN level INT DEFAULT 0 COMMENT '层级(0=顶层)' AFTER wbs_code,
  ADD COLUMN approver_id INT COMMENT '审批人ID' AFTER level,
  ADD COLUMN approve_time DATETIME COMMENT '审批时间' AFTER approver_id,
  ADD COLUMN approve_remark VARCHAR(500) COMMENT '审批意见' AFTER approve_time;

-- 2. 更新 status 字段注释（任务:not_started/in_progress/pending_review/completed/rejected; 里程碑:draft/pending/approved/locked/completed）
ALTER TABLE biz_gantt_task
  MODIFY COLUMN status VARCHAR(32) DEFAULT 'not_started'
    COMMENT '任务状态:not_started/in_progress/pending_review/completed/rejected; 里程碑:draft/pending/approved/locked/completed';

-- 3. 任务多依赖关系表
CREATE TABLE IF NOT EXISTS biz_task_dependency (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  task_id     INT NOT NULL COMMENT '当前任务ID(后继)',
  dep_task_id INT NOT NULL COMMENT '前置任务ID(前驱)',
  dep_type    VARCHAR(10) DEFAULT 'FS' COMMENT '依赖类型:FS/SS/FF/SF',
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_task_dep (task_id, dep_task_id),
  KEY idx_dep_task (dep_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务依赖关系表';

-- 4. 迁移旧单依赖数据到新表
INSERT IGNORE INTO biz_task_dependency (task_id, dep_task_id, dep_type)
SELECT id, dependency_task_id, COALESCE(dependency_type, 'FS')
FROM biz_gantt_task
WHERE dependency_task_id IS NOT NULL AND dependency_task_id > 0;
