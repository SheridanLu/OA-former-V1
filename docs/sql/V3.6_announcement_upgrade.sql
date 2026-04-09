-- =====================================================
-- V3.6 公告系统升级：审批流程 + 评论 + 图片
-- =====================================================

-- 1. sys_announcement 表新增字段
ALTER TABLE sys_announcement
  ADD COLUMN images TEXT COMMENT '公告图片URL,JSON数组' AFTER scope,
  ADD COLUMN approver_id INT COMMENT '审批人ID' AFTER images,
  ADD COLUMN approve_time DATETIME COMMENT '审批时间' AFTER approver_id,
  ADD COLUMN approve_remark VARCHAR(500) COMMENT '审批意见' AFTER approve_time;

-- 2. 更新 status 字段注释（新增 pending_approval/approved/rejected）
ALTER TABLE sys_announcement
  MODIFY COLUMN status VARCHAR(32) DEFAULT 'draft' COMMENT '状态:draft/pending_approval/approved/rejected/published/offline/expired';

-- 3. 新建公告评论表
CREATE TABLE IF NOT EXISTS sys_announcement_comment (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  announcement_id INT NOT NULL COMMENT '公告ID',
  content     TEXT NOT NULL COMMENT '评论内容',
  user_id     INT NOT NULL COMMENT '评论人ID',
  creator_id  INT COMMENT '创建人ID',
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at  DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted     TINYINT DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_announcement_id (announcement_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告评论表';

-- 4. 新增权限节点（如果尚未存在）
INSERT IGNORE INTO sys_permission (perm_code, perm_name, perm_type, parent_id, sort_order, creator_id, created_at)
VALUES
  ('system:announcement-approve', '公告审批', 'button', NULL, 0, 1, NOW());
