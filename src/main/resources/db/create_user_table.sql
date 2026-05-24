-- 创建用户表（如果不存在）
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    name VARCHAR(50) COMMENT '显示名称',
    role VARCHAR(50) COMMENT '角色（旧字段，兼容）',
    role_id BIGINT DEFAULT 4 COMMENT '角色ID',
    role_code VARCHAR(50) DEFAULT 'user' COMMENT '角色编码',
    status INT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 插入admin用户（密码是123456的BCrypt加密）
INSERT INTO sys_user (id, username, password, real_name, name, role, role_id, role_code, status)
VALUES (1, 'admin', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '管理员', '管理员', 'admin', 1, 'admin', 1)
ON DUPLICATE KEY UPDATE
role_id = 1,
role_code = 'admin',
password = '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK';

-- 插入测试账号：部门经理（密码123456）
INSERT INTO sys_user (username, password, real_name, name, role_id, role_code, status)
VALUES ('manager', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '部门经理', '部门经理', 3, 'dept_manager', 1)
ON DUPLICATE KEY UPDATE
role_id = 3,
role_code = 'dept_manager';

-- 插入测试账号：普通员工（密码123456）
INSERT INTO sys_user (username, password, real_name, name, role_id, role_code, status)
VALUES ('user01', '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK', '普通员工', '普通员工', 4, 'user', 1)
ON DUPLICATE KEY UPDATE
role_id = 4,
role_code = 'user';

-- 创建角色部门关联表（用于自定义数据权限）
CREATE TABLE IF NOT EXISTS sys_role_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted INT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_role_dept (role_id, dept_id),
    INDEX idx_role_id (role_id),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门关联表';
