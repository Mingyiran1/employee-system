-- 报表导出任务表
CREATE TABLE IF NOT EXISTS export_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    user_id BIGINT NOT NULL COMMENT '创建用户ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-等待中 1-执行中 2-成功 3-失败',
    total_count INT DEFAULT 0 COMMENT '总记录数',
    filter_params JSON COMMENT '筛选参数(JSON)',
    export_fields JSON COMMENT '导出的字段列表',
    error_msg VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    finish_time DATETIME COMMENT '完成时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表导出任务表';
