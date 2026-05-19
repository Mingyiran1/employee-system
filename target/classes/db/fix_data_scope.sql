-- ============================================
-- 数据权限功能修复脚本
-- 修复部门结构和员工关联关系
-- ============================================

-- 1. 清理并重新创建部门数据
DELETE FROM department WHERE id <= 5;

INSERT INTO department (id, name, parent_id, sort_order, create_time, update_time, is_deleted) VALUES
(1, '总裁办', 0, 1, NOW(), NOW(), 0),
(2, '技术部', 1, 2, NOW(), NOW(), 0),
(3, '市场部', 1, 3, NOW(), NOW(), 0),
(4, '后端组', 2, 1, NOW(), NOW(), 0),
(5, '前端组', 2, 2, NOW(), NOW(), 0);

-- 2. 清理并重新创建系统用户
DELETE FROM sys_user WHERE id <= 6;

INSERT INTO sys_user (id, username, password, real_name, role_id, role_code, status, create_time, update_time) VALUES
(1, 'admin', '123456', '超级管理员', 1, 'admin', 1, NOW(), NOW()),
(2, 'zhangsan', '123456', '张三（技术总监）', 2, 'dept_CEO', 1, NOW(), NOW()),
(3, 'lisi', '123456', '李四（技术经理）', 3, 'dept_manager', 1, NOW(), NOW()),
(4, 'wangwu', '123456', '王五（普通员工）', 4, 'user', 1, NOW(), NOW()),
(5, 'zhaoliu', '123456', '赵六（市场经理）', 3, 'dept_manager', 1, NOW(), NOW()),
(6, 'sunqi', '123456', '孙七（市场专员）', 4, 'user', 1, NOW(), NOW());

-- 3. 清理并重新创建员工数据（正确关联user_id和create_by）
DELETE FROM employee WHERE id <= 10;

-- 注意：create_by 是 sys_user.id，表示谁创建的这条记录
-- admin (id=1) 创建：张三、赵六
-- 张三 (id=2) 创建：李四、王五、员工A、员工B
-- 赵六 (id=5) 创建：孙七、员工C

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

-- 4. 验证数据
SELECT '=== 部门结构 ===' AS info;
SELECT id, name, parent_id FROM department WHERE is_deleted = 0 ORDER BY id;

SELECT '=== 系统用户 ===' AS info;
SELECT id, username, real_name, role_code, role_id FROM sys_user WHERE status = 1 ORDER BY id;

SELECT '=== 员工数据（带关联关系）===' AS info;
SELECT
    e.id,
    e.name,
    e.dept_id,
    d.name AS dept_name,
    e.user_id,
    e.create_by,
    u.username AS create_by_name
FROM employee e
LEFT JOIN department d ON e.dept_id = d.id
LEFT JOIN sys_user u ON e.create_by = u.id
WHERE e.is_deleted = 0
ORDER BY e.id;

-- 5. 各角色应看到的数据量预览
SELECT '=== 各角色数据权限预览 ===' AS info;

-- admin (角色1): 全部
SELECT 'admin(全部)' AS role, COUNT(*) AS count FROM employee WHERE is_deleted = 0;

-- zhangsan 技术总监(角色2, 部门2): 本部门及以下(2,4,5)
SELECT 'zhangsan-技术总监(部门2及以下)' AS role, COUNT(*) AS count
FROM employee WHERE is_deleted = 0 AND dept_id IN (2, 4, 5);

-- lisi 技术经理(角色3, 部门2): 本部门(2)
SELECT 'lisi-技术经理(部门2)' AS role, COUNT(*) AS count
FROM employee WHERE is_deleted = 0 AND dept_id = 2;

-- wangwu 普通员工(角色4, user_id=4): create_by=2的数据（王五是张三创建的，张三sys_user.id=2）
SELECT 'wangwu-普通员工(create_by=2)' AS role, COUNT(*) AS count
FROM employee WHERE is_deleted = 0 AND create_by = 2;

-- zhaoliu 市场经理(角色3, 部门3): 本部门(3)
SELECT 'zhaoliu-市场经理(部门3)' AS role, COUNT(*) AS count
FROM employee WHERE is_deleted = 0 AND dept_id = 3;

-- sunqi 市场专员(角色4, user_id=6): create_by=5的数据（孙七是赵六创建的，赵六sys_user.id=5）
SELECT 'sunqi-市场专员(create_by=5)' AS role, COUNT(*) AS count
FROM employee WHERE is_deleted = 0 AND create_by = 5;
