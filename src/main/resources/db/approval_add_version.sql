-- 为审批记录表添加乐观锁版本号字段
ALTER TABLE approval_record ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER is_deleted;

-- 更新现有数据的版本号为0
UPDATE approval_record SET version = 0 WHERE version IS NULL;
