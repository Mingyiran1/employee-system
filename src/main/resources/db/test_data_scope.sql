-- ============================================
-- 数据权限测试数据准备脚本
-- ============================================

-- 1. 准备部门数据（如果不存在）
INSERT IGNORE INTO department (id, name, parent_id, sort_order, create_time, update_time) VALUES
(1, '总裁办', 0, 1, NOW(), NOW()),
(2, '技术部', 1, 2, NOW(), NOW()),
(3, '市场部', 1, 3, NOW(), NOW()),
(4, '技术部-后端组', 2, 1, NOW(), NOW()),
(5, '技术部-前端组', 2, 2, NOW(), NOW());

-- 2. 准备系统用户（不同角色）
-- 密码都是 123456
-- 注意：代码支持 BCrypt 和明文两种格式
-- 这里使用明文方便测试，生产环境请使用 BCrypt
INSERT IGNORE INTO sys_user (id, username, password, real_name, role_id, role_code, status, create_time, update_time) VALUES
(1, 'admin', '123456', '超级管理员', 1, 'admin', 1, NOW(), NOW()),
(2, 'zhangsan', '123456', '张三（技术总监）', 2, 'dept_CEO', 1, NOW(), NOW()),
(3, 'lisi', '123456', '李四（技术经理）', 3, 'dept_manager', 1, NOW(), NOW()),
(4, 'wangwu', '123456', '王五（普通员工）', 4, 'user', 1, NOW(), NOW()),
(5, 'zhaoliu', '123456', '赵六（市场经理）', 3, 'dept_manager', 1, NOW(), NOW()),
(6, 'sunqi', '123456', '孙七（市场专员）', 4, 'user', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE password = '123456';

-- 3. 准备员工数据（关联系统用户）
-- 注意：先清空测试数据避免冲突
DELETE FROM employee WHERE id <= 10;

INSERT INTO employee (id, name, gender, phone, email, dept_id, position, entry_date, status, address, create_by, user_id, create_time, update_time, is_deleted) VALUES
-- 技术部员工
(1, '张三', 1, '13800138001', 'zhangsan@company.com', 2, '技术总监', '2020-01-01', 1, '北京', 1, 2, NOW(), NOW(), 0),
(2, '李四', 1, '13800138002', 'lisi@company.com', 2, '技术经理', '2021-03-15', 1, '北京', 2, 3, NOW(), NOW(), 0),
(3, '王五', 1, '13800138003', 'wangwu@company.com', 4, '后端工程师', '2022-06-01', 1, '北京', 2, 4, NOW(), NOW(), 0),
(4, '员工A', 1, '13800138004', 'staffa@company.com', 4, '后端工程师', '2023-01-01', 1, '北京', 3, NULL, NOW(), NOW(), 0),
(5, '员工B', 0, '13800138005', 'staffb@company.com', 5, '前端工程师', '2023-03-01', 1, '北京', 2, NULL, NOW(), NOW(), 0),

-- 市场部员工
(6, '赵六', 0, '13800138006', 'zhaoliu@company.com', 3, '市场经理', '2021-06-01', 1, '上海', 1, 5, NOW(), NOW(), 0),
(7, '孙七', 0, '13800138007', 'sunqi@company.com', 3, '市场专员', '2022-09-01', 1, '上海', 6, 6, NOW(), NOW(), 0),
(8, '员工C', 0, '13800138008', 'staffc@company.com', 3, '市场专员', '2023-06-01', 1, '上海', 6, NULL, NOW(), NOW(), 0);

-- 4. 验证数据
SELECT '=== 部门结构 ===' AS info;
SELECT id, name, parent_id FROM department WHERE is_deleted = 0;

SELECT '=== 系统用户 ===' AS info;
SELECT id, username, real_name, role_code, role_id FROM sys_user WHERE status = 1;

SELECT '=== 员工数据（关联关系）===' AS info;
SELECT e.id, e.name, e.dept_id, d.name as dept_name, e.user_id, e.create_by
FROM employee e
LEFT JOIN department d ON e.dept_id = d.id
WHERE e.is_deleted = 0;

SELECT '=== 各角色可查看数据预览 ===' AS info;

-- admin (角色1): 查看全部
SELECT 'admin可查看:' AS role, COUNT(*) AS total FROM employee WHERE is_deleted = 0;

-- zhangsan 技术总监 (角色2, 部门2): 本部门及以下（技术部及其子部门：2,4,5）
SELECT '张三(技术总监)可查看:' AS role, COUNT(*) AS total
FROM employee
WHERE is_deleted = 0 AND dept_id IN (2, 4, 5);

-- lisi 技术经理 (角色3, 部门2): 本部门（技术部：2）
SELECT '李四(技术经理)可查看:' AS role, COUNT(*) AS total
FROM employee
WHERE is_deleted = 0 AND dept_id = 2;

-- wangwu 普通员工 (角色4, user_id=4): 仅本人（create_by=2）
SELECT '王五(普通员工)可查看:' AS role, COUNT(*) AS total
FROM employee
WHERE is_deleted = 0 AND create_by = 2;
