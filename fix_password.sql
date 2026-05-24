-- Fix admin password to BCrypt format
-- Password is: 123456
-- Generated with BCryptPasswordEncoder (strength 10)

UPDATE sys_user SET password = '$2a$10$7JB720yubVS1vai/5EjiueVwDkB.qD6kz.EicKnx.v/aK.vB5tEJq' WHERE username = 'admin';

-- Verify
SELECT username, password FROM sys_user WHERE username = 'admin';
