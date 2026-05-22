-- 修复密码为BCrypt加密格式
-- 密码都是：123456

-- 更新admin用户密码（BCrypt加密）
UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO' WHERE username = 'admin';

-- 更新其他用户密码（如果需要）
-- UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO' WHERE username = '其他用户名';

-- 查询验证
SELECT username, password FROM sys_user;
