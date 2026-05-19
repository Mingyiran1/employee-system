-- 数据权限功能升级：为 employee 表添加必要字段

-- 1. 添加 create_by 字段（用于"仅本人"数据权限）
ALTER TABLE employee ADD COLUMN create_by BIGINT COMMENT '创建人ID（关联sys_user.id）';

-- 2. 添加 user_id 字段（用于关联SysUser和Employee）
ALTER TABLE employee ADD COLUMN user_id BIGINT COMMENT '关联的系统用户ID（sys_user.id）';

-- 3. 创建索引优化查询性能
CREATE INDEX idx_employee_create_by ON employee(create_by);
CREATE INDEX idx_employee_user_id ON employee(user_id);
CREATE INDEX idx_employee_dept_id ON employee(dept_id);

-- 4. 更新现有数据：假设当前所有员工的create_by为1（admin）
-- 实际生产环境需要根据业务逻辑调整
UPDATE employee SET create_by = 1 WHERE create_by IS NULL;

-- 5. 可选：建立user_id和sys_user的关联（如果有对应关系）
-- UPDATE employee e SET e.user_id = (SELECT id FROM sys_user WHERE username = e.name) WHERE e.user_id IS NULL;

-- 验证字段添加成功
DESCRIBE employee;
