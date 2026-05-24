-- 修复 admin 密码为 123456
-- 在 DataGrip 中选择 employee_system 数据库后执行

-- 更新 admin 用户密码
UPDATE sys_user
SET password = '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK'
WHERE username = 'admin';

-- 同时更新测试账号密码（方便测试）
UPDATE sys_user
SET password = '$2a$12$B3vMXj3WNXuTTUD/R/QsXuMH/XegkB6CbWcd..AphAnXlSaC87RcK'
WHERE username IN ('manager', 'user01');

-- 验证更新结果
SELECT username, password FROM sys_user WHERE username IN ('admin', 'manager', 'user01');
