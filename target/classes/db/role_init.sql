-- 角色初始化脚本
-- 执行方式: 在MySQL中运行此脚本

-- 创建角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    data_scope INT NOT NULL DEFAULT 4 COMMENT '数据范围: 1全部 2本部门及以下 3本部门 4仅本人',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted INT DEFAULT 0 COMMENT '是否删除 0否 1是',
    INDEX idx_code (code),
    INDEX idx_data_scope (data_scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 先删除已有数据（避免重复）
DELETE FROM sys_role WHERE id IN (1, 2, 3, 4);

-- 初始化角色数据
INSERT INTO sys_role (id, name, code, data_scope, remark) VALUES
(1, '超级管理员', 'admin', 1, '拥有所有数据权限'),
(2, 'CEO', 'dept_CEO', 2, '查看本部门及以下数据'),
(3, '部门经理', 'dept_manager', 3, '查看本部门数据'),
(4, '普通员工', 'user', 4, '仅查看本人数据');

-- 修改用户表，添加角色字段
-- 先检查字段是否存在，如果不存在则添加
SET @exist_role_id = (SELECT COUNT(*) FROM information_schema.columns
                      WHERE table_name = 'sys_user' AND column_name = 'role_id');

SET @sql_role_id = IF(@exist_role_id = 0,
    'ALTER TABLE sys_user ADD COLUMN role_id BIGINT DEFAULT 4 COMMENT "角色ID"',
    'SELECT "role_id already exists"');
PREPARE stmt1 FROM @sql_role_id;
EXECUTE stmt1;

SET @exist_role_code = (SELECT COUNT(*) FROM information_schema.columns
                        WHERE table_name = 'sys_user' AND column_name = 'role_code');

SET @sql_role_code = IF(@exist_role_code = 0,
    'ALTER TABLE sys_user ADD COLUMN role_code VARCHAR(50) DEFAULT "user" COMMENT "角色编码"',
    'SELECT "role_code already exists"');
PREPARE stmt2 FROM @sql_role_code;
EXECUTE stmt2;

-- 创建角色部门关联表（用于自定义数据权限）
CREATE TABLE IF NOT EXISTS sys_role_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    UNIQUE KEY uk_role_dept (role_id, dept_id),
    INDEX idx_role_id (role_id),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门关联表';

-- 更新现有用户为管理员角色（测试用）
UPDATE sys_user SET role_id = 1, role_code = 'admin' WHERE username = 'admin';

-- 删除已存在的测试账号（避免重复插入报错）
DELETE FROM sys_user WHERE username IN ('manager', 'user01');

-- 插入测试数据：创建一个部门经理账号
INSERT INTO sys_user (username, password, name, role_id, role_code) VALUES
('manager', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '部门经理', 3, 'dept_manager');

-- 插入测试数据：创建一个普通员工账号
INSERT INTO sys_user (username, password, name, role_id, role_code) VALUES
('user01', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '普通员工', 4, 'user');
