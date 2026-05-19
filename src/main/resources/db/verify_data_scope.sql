-- ============================================
-- 数据权限验证脚本 - 检查数据是否正确设置
-- ============================================

-- 1. 检查 sys_user 表数据
SELECT '=== 系统用户表 (sys_user) ===' AS info;
SELECT id, username, real_name, role_id, role_code, status
FROM sys_user
WHERE status = 1;

-- 2. 检查 employee 表结构
SELECT '=== Employee 表结构 ===' AS info;
DESCRIBE employee;

-- 3. 检查 employee 表的关键字段
SELECT '=== Employee 数据（关键字段）===' AS info;
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

-- 4. 检查用户与员工的关联
SELECT '=== 用户与员工关联检查 ===' AS info;
SELECT
    su.id AS user_id,
    su.username,
    su.real_name,
    su.role_code,
    e.id AS employee_id,
    e.name AS employee_name,
    e.dept_id,
    CASE
        WHEN e.user_id IS NOT NULL THEN '已关联'
        ELSE '未关联'
    END AS link_status
FROM sys_user su
LEFT JOIN employee e ON e.user_id = su.id AND e.is_deleted = 0
WHERE su.status = 1;

-- 5. 数据权限预期结果预览
SELECT '=== 各角色应看到的数据量 ===' AS info;

-- admin: 全部
SELECT 'admin (全部)' AS role, COUNT(*) AS count
FROM employee WHERE is_deleted = 0;

-- 张三(技术总监, role_id=2, user_id=2, dept_id=2): 本部门及以下(2,4,5)
SELECT '张三-技术总监(本部门及以下:2,4,5)' AS role, COUNT(*) AS count
FROM employee
WHERE is_deleted = 0 AND dept_id IN (2, 4, 5);

-- 李四(技术经理, role_id=3, user_id=3, dept_id=2): 本部门(2)
SELECT '李四-技术经理(本部门:2)' AS role, COUNT(*) AS count
FROM employee
WHERE is_deleted = 0 AND dept_id = 2;

-- 王五(普通员工, role_id=4, user_id=4): 仅本人(create_by=2)
SELECT '王五-普通员工(create_by=2)' AS role, COUNT(*) AS count
FROM employee
WHERE is_deleted = 0 AND create_by = 2;

-- 赵六(市场经理, role_id=3, user_id=5, dept_id=3): 本部门(3)
SELECT '赵六-市场经理(本部门:3)' AS role, COUNT(*) AS count
FROM employee
WHERE is_deleted = 0 AND dept_id = 3;

-- 孙七(市场专员, role_id=4, user_id=6): 仅本人(create_by=6)
SELECT '孙七-市场专员(create_by=6)' AS role, COUNT(*) AS count
FROM employee
WHERE is_deleted = 0 AND create_by = 6;
