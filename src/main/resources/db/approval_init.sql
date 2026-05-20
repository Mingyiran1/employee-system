-- 审批流程定义表
CREATE TABLE IF NOT EXISTS approval_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_name VARCHAR(100) NOT NULL COMMENT '流程名称（如：员工入职审批）',
    process_type VARCHAR(50) NOT NULL COMMENT '流程类型（ENTRY-入职/LEAVE-离职）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '审批流程定义表';

-- 审批节点表
CREATE TABLE IF NOT EXISTS approval_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_id BIGINT NOT NULL COMMENT '关联流程ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称（如：部门经理审批）',
    role_id BIGINT COMMENT '审批角色ID（关联sys_role）',
    role_code VARCHAR(50) COMMENT '审批角色编码',
    node_order INT DEFAULT 1 COMMENT '节点顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (process_id) REFERENCES approval_process(id)
) COMMENT '审批节点表';

-- 审批记录表
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_id BIGINT NOT NULL COMMENT '流程ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型（EMPLOYEE_ENTRY/EMPLOYEE_LEAVE）',
    business_id BIGINT NOT NULL COMMENT '业务ID（如：employee.id）',
    current_node_id BIGINT COMMENT '当前节点ID',
    current_role_id BIGINT COMMENT '当前审批角色ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    approver_id BIGINT COMMENT '实际审批人ID',
    approval_status TINYINT DEFAULT 0 COMMENT '状态：0-待审批 1-已通过 2-已拒绝',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
) COMMENT '审批记录表';

-- 审批历史表（记录每个节点的审批历史）
CREATE TABLE IF NOT EXISTS approval_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL COMMENT '审批记录ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    approver_id BIGINT COMMENT '审批人ID',
    approval_status TINYINT COMMENT '审批结果：1-通过 2-拒绝',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (record_id) REFERENCES approval_record(id)
) COMMENT '审批历史表';
