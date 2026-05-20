-- 1. 查看部门层级
SELECT id, name, parent_id FROM department ORDER BY id;

-- 2. 查看审批节点
SELECT n.id, n.node_name, n.role_id, n.role_code, n.dept_id, d.name as dept_name
FROM approval_node n
LEFT JOIN department d ON n.dept_id = d.id
ORDER BY n.node_order;

-- 3. 查看用户角色
SELECT id, username, real_name, role_id, role_code, managed_dept_id
FROM sys_user WHERE role_id IN (1, 2, 3);
