-- ========================================================
-- 添加离职时间字段到保险员工表
-- 修改日期: 2026-05-24
-- ========================================================

-- 为 insurance_employee 表添加离职时间字段
ALTER TABLE insurance_employee
ADD COLUMN IF NOT EXISTS leave_date DATE DEFAULT NULL COMMENT '离职日期' AFTER hire_date;
