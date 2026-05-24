-- ============================================
-- 修复员工数据和用户密码
-- 执行方式: 在DataGrip中选择 employee_system 数据库后执行
-- ============================================

-- 1. 更新所有测试账号的密码为正确的BCrypt哈希 (密码都是 123456)
UPDATE sys_user SET password = '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK'
WHERE username IN ('admin', 'zhangsan', 'lisi', 'wangwu', 'zhaoliu', 'sunqi', 'manager', 'user01');

-- 2. 清理并重新创建部门数据
DELETE FROM department WHERE id <= 5;

INSERT INTO department (id, name, parent_id, sort_order, create_time, update_time, is_deleted) VALUES
(1, '总裁办', 0, 1, NOW(), NOW(), 0),
(2, '技术部', 1, 2, NOW(), NOW(), 0),
(3, '市场部', 1, 3, NOW(), NOW(), 0),
(4, '后端组', 2, 1, NOW(), NOW(), 0),
(5, '前端组', 2, 2, NOW(), NOW(), 0);

-- 3. 清理并重新创建测试系统用户
DELETE FROM sys_user WHERE id BETWEEN 2 AND 6;

INSERT INTO sys_user (id, username, password, real_name, role_id, role_code, status, create_time, update_time) VALUES
(2, 'zhangsan', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '张三（技术总监）', 2, 'dept_CEO', 1, NOW(), NOW()),
(3, 'lisi', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '李四（技术经理）', 3, 'dept_manager', 1, NOW(), NOW()),
(4, 'wangwu', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '王五（普通员工）', 4, 'user', 1, NOW(), NOW()),
(5, 'zhaoliu', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '赵六（市场经理）', 3, 'dept_manager', 1, NOW(), NOW()),
(6, 'sunqi', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '孙七（市场专员）', 4, 'user', 1, NOW(), NOW());

-- 4. 清理并重新创建员工数据
DELETE FROM employee WHERE id <= 10;

INSERT INTO employee (id, name, gender, phone, email, dept_id, position, entry_date, status, address, create_by, user_id, create_time, update_time, is_deleted) VALUES
-- 技术部员工（部门2：技术部）
(1, '张三', 1, '13800138001', 'zhangsan@company.com', 2, '技术总监', '2020-01-01', 1, '北京', 1, 2, NOW(), NOW(), 0),
(2, '李四', 1, '13800138002', 'lisi@company.com', 2, '技术经理', '2021-03-15', 1, '北京', 2, 3, NOW(), NOW(), 0),

-- 后端组员工（部门4：后端组，parent_id=2）
(3, '王五', 1, '13800138003', 'wangwu@company.com', 4, '后端工程师', '2022-06-01', 1, '北京', 2, 4, NOW(), NOW(), 0),
(4, '员工A', 1, '13800138004', 'staffa@company.com', 4, '后端工程师', '2023-01-01', 1, '北京', 2, NULL, NOW(), NOW(), 0),

-- 前端组员工（部门5：前端组，parent_id=2）
(5, '员工B', 0, '13800138005', 'staffb@company.com', 5, '前端工程师', '2023-03-01', 1, '北京', 2, NULL, NOW(), NOW(), 0),

-- 市场部员工（部门3：市场部）
(6, '赵六', 0, '13800138006', 'zhaoliu@company.com', 3, '市场经理', '2021-06-01', 1, '上海', 1, 5, NOW(), NOW(), 0),
(7, '孙七', 0, '13800138007', 'sunqi@company.com', 3, '市场专员', '2022-09-01', 1, '上海', 5, 6, NOW(), NOW(), 0),
(8, '员工C', 0, '13800138008', 'staffc@company.com', 3, '市场专员', '2023-06-01', 1, '上海', 5, NULL, NOW(), NOW(), 0);

-- 5. 验证数据
SELECT '=== 部门结构 ===' AS info;
SELECT id, name, parent_id FROM department WHERE is_deleted = 0 ORDER BY id;

SELECT '=== 系统用户 ===' AS info;
SELECT id, username, real_name, role_code, role_id FROM sys_user WHERE status = 1 ORDER BY id;

SELECT '=== 员工数据 ===' AS info;
SELECT e.id, e.name, d.name AS dept_name, e.position, e.phone
FROM employee e
LEFT JOIN department d ON e.dept_id = d.id
WHERE e.is_deleted = 0
ORDER BY e.id;
