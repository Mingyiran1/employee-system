-- ============================================
-- 审批流程V2升级脚本
-- 1. sys_user表增加managed_dept_id字段
-- 2. approval_node表增加dept_id字段
-- 3. 创建sys_message消息表
-- ============================================

-- 1. sys_user表增加managed_dept_id字段（部门经理管辖的部门）
ALTER TABLE sys_user
    ADD COLUMN managed_dept_id BIGINT NULL COMMENT '管辖部门ID（部门经理专用）' AFTER role_code;

-- 2. approval_node表增加dept_id字段（支持部门专属审批节点）
ALTER TABLE approval_node
    ADD COLUMN dept_id BIGINT NULL COMMENT '所属部门ID（null表示通用节点）' AFTER role_code;

-- 3. 创建消息通知表
CREATE TABLE IF NOT EXISTS sys_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收人ID',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type TINYINT DEFAULT 1 COMMENT '消息类型：1-审批通知 2-系统通知',
    business_type VARCHAR(50) COMMENT '关联业务类型',
    business_id BIGINT COMMENT '关联业务ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) COMMENT='系统消息通知表';

-- 4. 更新测试数据：设置部门经理的管辖部门
-- 假设：张三(user_id=2)是技术部总监，不管具体部门
--       李四(user_id=3)是技术部经理，管技术部(dept_id=2)
--       赵六(user_id=5)是市场部经理，管市场部(dept_id=3)
UPDATE sys_user SET managed_dept_id = 2 WHERE id = 3; -- 李四管技术部
UPDATE sys_user SET managed_dept_id = 3 WHERE id = 5; -- 赵六管市场部

-- 5. 更新审批节点，配置部门专属节点
-- 入职审批：技术部员工走技术部经理 -> HR，市场部员工走市场部经理 -> HR
-- 先清空现有节点，重新插入
DELETE FROM approval_node WHERE id IN (1, 2, 3, 4);

-- 技术部入职审批节点
INSERT INTO approval_node (id, process_id, node_name, role_id, role_code, dept_id, node_order, status, create_time) VALUES
(1, 1, '技术部经理审批', 3, 'dept_manager', 2, 1, 1, NOW()),
(2, 1, 'HR审批', 1, 'admin', NULL, 2, 1, NOW());

-- 市场部入职审批节点
INSERT INTO approval_node (id, process_id, node_name, role_id, role_code, dept_id, node_order, status, create_time) VALUES
(3, 1, '市场部经理审批', 3, 'dept_manager', 3, 1, 1, NOW()),
(4, 1, 'HR审批', 1, 'admin', NULL, 2, 1, NOW());

-- 6. 验证数据
SELECT '=== 用户表（查看部门经理） ===' AS info;
SELECT id, username, real_name, role_code, managed_dept_id FROM sys_user WHERE managed_dept_id IS NOT NULL;

SELECT '=== 审批节点（查看部门配置） ===' AS info;
SELECT n.id, n.node_name, n.role_code, n.dept_id, d.name AS dept_name, n.node_order
FROM approval_node n
LEFT JOIN department d ON n.dept_id = d.id
ORDER BY n.process_id, n.node_order;
