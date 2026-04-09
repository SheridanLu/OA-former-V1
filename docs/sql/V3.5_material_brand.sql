-- ====================================================================
-- V3.5 材料模块：新增品牌字段 + 确保V3.4字段存在
-- 日期: 2026-04-08
-- 说明: 幂等脚本，可重复执行
-- ====================================================================

-- 1. 确保 base_price_with_tax 列存在（若仍为 base_price 则重命名）
-- 注意: 如果 V3.4 迁移未执行，需先执行 docs/sql/V3.4_material_standardization.sql

-- 2. 新增品牌字段
ALTER TABLE biz_material_base
  ADD COLUMN IF NOT EXISTS brand VARCHAR(100) DEFAULT NULL COMMENT '品牌' AFTER spec_model;

-- 3. 验证
SELECT '=== V3.5 材料品牌字段迁移完成 ===' AS info;
DESC biz_material_base;
