-- ============================================
-- 审批流程测试数据
-- 插入默认审批流程和节点
-- ============================================

-- 1. 插入审批流程定义
INSERT INTO approval_process (id, process_name, process_type, status, create_time, update_time) VALUES
(1, '员工入职审批', 'EMPLOYEE_ENTRY', 1, NOW(), NOW()),
(2, '员工离职审批', 'EMPLOYEE_LEAVE', 1, NOW(), NOW());

-- 2. 插入审批节点
-- 员工入职审批流程节点：部门经理审批 → HR审批
INSERT INTO approval_node (id, process_id, node_name, role_id, role_code, node_order, status, create_time) VALUES
(1, 1, '部门经理审批', 3, 'dept_manager', 1, 1, NOW()),
(2, 1, 'HR审批', 1, 'admin', 2, 1, NOW());

-- 员工离职审批流程节点：部门经理审批 → HR审批
INSERT INTO approval_node (id, process_id, node_name, role_id, role_code, node_order, status, create_time) VALUES
(3, 2, '部门经理审批', 3, 'dept_manager', 1, 1, NOW()),
(4, 2, 'HR审批', 1, 'admin', 2, 1, NOW());

-- 3. 验证数据
SELECT '=== 审批流程定义 ===' AS info;
SELECT id, process_name, process_type, status FROM approval_process WHERE status = 1;

SELECT '=== 审批节点 ===' AS info;
SELECT n.id, n.node_name, p.process_name, n.role_code, n.node_order
FROM approval_node n
LEFT JOIN approval_process p ON n.process_id = p.id
WHERE n.status = 1
ORDER BY n.process_id, n.node_order;
