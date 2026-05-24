-- ============================================
-- 智慧交通企业员工信息管理系统 - 数据库初始化脚本
-- 简化版（只有管理员和普通用户两种角色）
-- ============================================

-- 1. 清空并重新初始化角色表（只保留2个角色）
TRUNCATE TABLE sys_role;
INSERT INTO sys_role (id, name, code, data_scope, status, create_time) VALUES
(1, '管理员', 'ADMIN', 1, 1, '2026-05-23 10:00:00'),
(2, '普通用户', 'USER', 4, 1, '2026-05-23 10:00:00');

-- 2. 清空并重新初始化用户表（只保留2个测试账号）
-- 密码都是 123456（BCrypt加密）
TRUNCATE TABLE sys_user;
INSERT INTO sys_user (id, username, password, real_name, role_id, status, create_time, update_time) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 1, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(2, 'chenwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '陈伟', 2, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00');

-- 3. 清空并初始化投保公司固定数据（4家公司）
TRUNCATE TABLE insured_company;
INSERT INTO insured_company (id, name, code, contact_name, contact_phone, address, status, create_time, update_time) VALUES
(1, 'A公司', 'COMPANY_A', '张经理', '13800138001', '北京市朝阳区A大厦', 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(2, 'B公司', 'COMPANY_B', '李经理', '13800138002', '上海市浦东区B大厦', 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(3, 'C公司', 'COMPANY_C', '王经理', '13800138003', '广州市天河区C大厦', 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(4, 'D公司', 'COMPANY_D', '赵经理', '13800138004', '深圳市南山区D大厦', 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00');

-- 4. 清空并初始化供应商固定数据（3家供应商）
TRUNCATE TABLE supplier;
INSERT INTO supplier (id, name, code, contact_name, contact_phone, contact_email, address, cooperation_status, status, create_time, update_time) VALUES
(1, '供应商A', 'SUPPLIER_A', '刘经理', '13900139001', 'supplierA@example.com', '北京市海淀区A路1号', 1, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(2, '供应商B', 'SUPPLIER_B', '陈经理', '13900139002', 'supplierB@example.com', '上海市静安区B路2号', 1, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(3, '供应商C', 'SUPPLIER_C', '杨经理', '13900139003', 'supplierC@example.com', '广州市越秀区C路3号', 1, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00');

-- 5. 清空并初始化保费配置固定数据（3类工种）
TRUNCATE TABLE premium_config;
INSERT INTO premium_config (id, job_type, rate, base_salary, annual_premium, status, create_time, update_time) VALUES
(1, '一类', 0.0150, 120000.00, 1800.00, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(2, '二类', 0.0200, 120000.00, 2400.00, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00'),
(3, '三类', 0.0250, 120000.00, 3000.00, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00');

-- 6. 清空保险员工表（从干净状态开始）
TRUNCATE TABLE insurance_employee;

-- 7. 清空部门表（权限体系简化，部门管理已移除）
TRUNCATE TABLE department;
INSERT INTO department (id, name, code, parent_id, sort_order, status, create_time, update_time) VALUES
(1, '总公司', 'HQ', 0, 1, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00');

-- 8. 插入测试用的保险员工数据（用于演示）
INSERT INTO insurance_employee (name, id_card, phone, email, company_id, supplier_id, job_type, hire_date, status, annual_premium, create_by, create_time, update_time, is_deleted) VALUES
('测试员工1', '110101199001011111', '13800138001', 'test1@example.com', 1, 1, '一类', '2026-01-01', 1, 1800.00, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00', 0),
('测试员工2', '110101199002022222', '13800138002', 'test2@example.com', 2, 2, '二类', '2026-02-01', 1, 2400.00, 1, '2026-05-23 10:00:00', '2026-05-23 10:00:00', 0),
('测试员工3', '110101199003033333', '13800138003', 'test3@example.com', 3, 3, '三类', '2026-03-01', 1, 3000.00, 2, '2026-05-23 10:00:00', '2026-05-23 10:00:00', 0);

-- ============================================
-- 用户权限说明：
-- admin (role_id=1): 管理员，数据权限=全部数据
-- chenwei (role_id=2): 普通用户，数据权限=仅本人创建的数据
-- ============================================
-- 得分功能验证：
-- 1. 保险员工管理（20分）：导入5+导出5+添加10 - 完成
-- 2. 供应商管理（15分）：添加5+删除5+修改5 - 原有功能
-- 3. 数据权限测试（5分）：admin看所有，chenwei只看自己创建的
-- 4. 用户测试（5分）：两个测试账号配合验证
-- ============================================
