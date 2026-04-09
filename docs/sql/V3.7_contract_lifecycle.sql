-- =====================================================
-- V3.7 合同模板导入审批 + 合同生成全闭环
-- =====================================================

-- 1. biz_contract 表新增字段
ALTER TABLE biz_contract
  ADD COLUMN content LONGTEXT COMMENT '合同正文HTML(模板渲染+编辑后)' AFTER remark,
  ADD COLUMN generated_file_path VARCHAR(500) COMMENT '生成的带水印合同文件路径' AFTER content,
  ADD COLUMN approver_id INT COMMENT '审批人ID' AFTER generated_file_path,
  ADD COLUMN approve_time DATETIME COMMENT '审批时间' AFTER approver_id,
  ADD COLUMN approve_remark VARCHAR(500) COMMENT '审批意见' AFTER approve_time;

-- 2. 更新 biz_contract status 字段注释
ALTER TABLE biz_contract
  MODIFY COLUMN status VARCHAR(32) DEFAULT 'draft' COMMENT '状态:draft/pending/approved/rejected/terminated';

-- 3. sys_contract_tpl_version 支持 Excel 文件类型标注（可选）
ALTER TABLE sys_contract_tpl_version
  ADD COLUMN file_type VARCHAR(20) DEFAULT 'docx' COMMENT '文件类型:docx/xlsx' AFTER file_name;
