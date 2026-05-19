-- ============================================
-- 修复部门数据，确保数据权限测试正确
-- ============================================

-- 1. 查看当前部门数据
SELECT '=== 当前部门数据 ===' AS info;
SELECT id, name, parent_id, sort_order FROM department WHERE is_deleted = 0 ORDER BY id;

-- 2. 清空并重新插入正确的部门数据
-- 先删除子部门，再删除父部门（避免外键冲突）
DELETE FROM department WHERE id IN (4, 5);
DELETE FROM department WHERE id IN (1, 2, 3);

-- 3. 插入正确的部门结构
INSERT INTO department (id, name, parent_id, sort_order, create_time, update_time, is_deleted) VALUES
(1, '总裁办', 0, 1, NOW(), NOW(), 0),
(2, '技术部', 1, 2, NOW(), NOW(), 0),
(3, '市场部', 1, 3, NOW(), NOW(), 0),
(4, '技术部-后端组', 2, 1, NOW(), NOW(), 0),
(5, '技术部-前端组', 2, 2, NOW(), NOW(), 0);

-- 4. 验证修复结果
SELECT '=== 修复后的部门数据 ===' AS info;
SELECT id, name, parent_id, sort_order FROM department WHERE is_deleted = 0 ORDER BY id;

-- 5. 验证部门层级关系
SELECT '=== 技术部(id=2)的子部门 ===' AS info;
SELECT id, name, parent_id FROM department WHERE parent_id = 2 AND is_deleted = 0;

-- 6. 验证员工分布
SELECT '=== 各部门员工数量 ===' AS info;
SELECT
    d.id AS dept_id,
    d.name AS dept_name,
    COUNT(e.id) AS employee_count
FROM department d
LEFT JOIN employee e ON e.dept_id = d.id AND e.is_deleted = 0
WHERE d.is_deleted = 0
GROUP BY d.id, d.name
ORDER BY d.id;
