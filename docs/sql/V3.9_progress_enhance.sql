-- =====================================================
-- V3.9 进度管理增强 — 审计日志 + 预警 + 延期闭环 + 关联 + 统计
-- =====================================================

-- 1. 进度操作审计日志
CREATE TABLE IF NOT EXISTS biz_progress_audit_log (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  project_id    INT COMMENT '项目ID',
  target_type   VARCHAR(30) NOT NULL COMMENT '操作对象类型: task/milestone/change',
  target_id     INT NOT NULL COMMENT '操作对象ID',
  target_name   VARCHAR(200) COMMENT '对象名称快照',
  action        VARCHAR(50) NOT NULL COMMENT '操作: create/update/delete/status_change/progress_update/approve/reject/lock/complete',
  field_name    VARCHAR(80) COMMENT '变更字段名',
  old_value     TEXT COMMENT '变更前值',
  new_value     TEXT COMMENT '变更后值',
  remark        VARCHAR(500) COMMENT '操作备注',
  operator_id   INT NOT NULL COMMENT '操作人ID',
  operated_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_target (target_type, target_id),
  KEY idx_project (project_id),
  KEY idx_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进度操作审计日志';

-- 2. 预警配置表
CREATE TABLE IF NOT EXISTS biz_warning_config (
  id                INT AUTO_INCREMENT PRIMARY KEY,
  project_id        INT COMMENT '项目ID(NULL=全局)',
  warning_type      VARCHAR(30) NOT NULL COMMENT '预警类型: due_soon/overdue/milestone_risk',
  threshold_days    INT DEFAULT 3 COMMENT '预警阈值(天)',
  enabled           TINYINT DEFAULT 1 COMMENT '是否启用',
  notify_roles      VARCHAR(200) COMMENT '通知角色(逗号分隔)',
  created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_project (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进度预警配置';

-- 3. 延期原因记录表
CREATE TABLE IF NOT EXISTS biz_delay_reason (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  task_id         INT NOT NULL COMMENT '延期任务ID',
  delay_days      INT COMMENT '延期天数',
  reason_type     VARCHAR(30) COMMENT '原因分类: material/labor/design/weather/other',
  reason_detail   TEXT NOT NULL COMMENT '延期原因详情',
  rectify_task_id INT COMMENT '关联整改任务ID(闭环)',
  creator_id      INT NOT NULL COMMENT '录入人',
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='延期原因记录';

-- 4. biz_gantt_task 新增关联字段 + 关键路径 + 自定义字段
ALTER TABLE biz_gantt_task
  ADD COLUMN linked_contract_id INT COMMENT '关联合同ID' AFTER approve_remark,
  ADD COLUMN linked_doc_ids VARCHAR(500) COMMENT '关联文档ID(逗号分隔)' AFTER linked_contract_id,
  ADD COLUMN linked_material_ids VARCHAR(500) COMMENT '关联物料ID(逗号分隔)' AFTER linked_doc_ids,
  ADD COLUMN is_critical TINYINT DEFAULT 0 COMMENT '是否关键路径节点' AFTER linked_material_ids,
  ADD COLUMN risk_level VARCHAR(20) COMMENT '风险等级: low/medium/high' AFTER is_critical,
  ADD COLUMN custom_fields JSON COMMENT '自定义扩展字段' AFTER risk_level,
  ADD COLUMN milestone_type VARCHAR(30) COMMENT '里程碑类型(自定义分类)' AFTER custom_fields;

-- 5. 插入默认预警配置
INSERT INTO biz_warning_config (project_id, warning_type, threshold_days, enabled, notify_roles) VALUES
  (NULL, 'due_soon', 3, 1, 'progress:edit'),
  (NULL, 'overdue', 0, 1, 'progress:edit'),
  (NULL, 'milestone_risk', 5, 1, 'progress:edit');
