-- ============================================================
-- 保险员工管理系统数据库重构脚本
-- 创建日期: 2025-05-22
-- 说明: 将通用员工表重构为保险员工专用表
-- ============================================================

-- 1. 保险公司表
CREATE TABLE IF NOT EXISTS insurance_company (
    id bigint PRIMARY KEY AUTO_INCREMENT,
    name varchar(100) NOT NULL COMMENT '公司名称',
    code varchar(50) COMMENT '公司代码',
    contact_name varchar(50) COMMENT '联系人',
    contact_phone varchar(20) COMMENT '联系电话',
    address varchar(200) COMMENT '公司地址',
    status tinyint DEFAULT 1 COMMENT '状态: 1=启用 0=禁用',
    create_time datetime DEFAULT CURRENT_TIMESTAMP,
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保险公司信息表';

-- 2. 供应商表 - 保留现有supplier表，添加code字段
SET @exist := (SELECT COUNT(*) FROM information_schema.columns
               WHERE table_schema = DATABASE()
               AND table_name = 'supplier'
               AND column_name = 'code');
SET @sql := IF(@exist = 0, 'ALTER TABLE supplier ADD COLUMN code varchar(50) COMMENT "供应商代码" AFTER name', 'SELECT "code column already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 保费计算配置表
CREATE TABLE IF NOT EXISTS premium_config (
    id bigint PRIMARY KEY AUTO_INCREMENT,
    config_key varchar(50) NOT NULL COMMENT '配置键名',
    config_value varchar(200) COMMENT '配置值',
    calc_type tinyint DEFAULT 1 COMMENT '计算类型: 1=按年费率计算 2=按日费率直接设置',
    annual_rate decimal(5,4) COMMENT '年费率（如0.015表示1.5%）',
    daily_rate decimal(10,2) COMMENT '日费率金额',
    description varchar(200) COMMENT '配置说明',
    create_time datetime DEFAULT CURRENT_TIMESTAMP,
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保费计算配置表';

-- 插入默认保费计算配置（年费率模式）
INSERT IGNORE INTO premium_config (config_key, config_value, calc_type, annual_rate, daily_rate, description)
VALUES ('default_premium_calc', '年费率计算', 1, 0.0150, NULL, '默认保费计算方式：按年费率1.5%计算');

-- 4. 保险员工表（替代现有employee表）
-- 先备份原employee表结构
CREATE TABLE IF NOT EXISTS employee_backup LIKE employee;
INSERT IGNORE INTO employee_backup SELECT * FROM employee;

-- 创建新的保险员工表
CREATE TABLE IF NOT EXISTS insurance_employee (
    id bigint PRIMARY KEY AUTO_INCREMENT,
    name varchar(50) NOT NULL COMMENT '姓名',
    phone varchar(20) COMMENT '手机号',
    email varchar(100) COMMENT '邮箱',
    id_card varchar(18) COMMENT '身份证号',
    job_type varchar(50) COMMENT '岗位类型（销售/客服/理赔等）',
    company_id bigint COMMENT '所属保险公司ID',
    supplier_id bigint COMMENT '所属供应商ID',
    annual_premium decimal(10,2) COMMENT '年保费',
    daily_premium decimal(10,2) COMMENT '日保费（可选，如果直接设置）',
    premium_calc_type tinyint DEFAULT 1 COMMENT '保费计算类型: 1=年费率计算 2=日费率直接设置',
    hire_date date COMMENT '入职日期',
    status tinyint DEFAULT 1 COMMENT '状态: 1=在职 2=离职',
    remark varchar(500) COMMENT '备注',
    create_by bigint COMMENT '创建人ID',
    create_time datetime DEFAULT CURRENT_TIMESTAMP,
    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted tinyint DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    INDEX idx_company_id (company_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_status (status),
    INDEX idx_job_type (job_type),
    INDEX idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保险员工信息表';

-- 将原employee表数据迁移到新表
INSERT IGNORE INTO insurance_employee (
    id, name, phone, email, id_card,
    job_type, company_id, supplier_id,
    annual_premium, daily_premium, premium_calc_type,
    hire_date, status, remark,
    create_by, create_time, update_time, is_deleted
)
SELECT
    e.id, e.name, e.phone, e.email, e.id_card,
    e.position AS job_type,
    NULL AS company_id,
    NULL AS supplier_id,
    NULL AS annual_premium,
    NULL AS daily_premium,
    1 AS premium_calc_type,
    e.entry_date AS hire_date,
    CASE WHEN e.status = 1 THEN 1 ELSE 2 END AS status,
    e.address AS remark,
    e.create_by, e.create_time, e.update_time, e.is_deleted
FROM employee e
WHERE e.is_deleted = 0;

-- 5. 添加示例保险公司数据
INSERT IGNORE INTO insurance_company (name, code, contact_name, contact_phone, address, status)
VALUES
    ('中国平安保险', 'PAIC', '张经理', '13800138001', '深圳市福田区', 1),
    ('中国人寿保险', 'CLIC', '李经理', '13800138002', '北京市西城区', 1),
    ('太平洋保险', 'CPIC', '王经理', '13800138003', '上海市浦东新区', 1);

-- 6. 添加示例保险员工数据（如果表为空）
INSERT INTO insurance_employee (name, phone, email, id_card, job_type, company_id, supplier_id,
    annual_premium, premium_calc_type, hire_date, status, create_by)
SELECT '保险员工示例', '13800138000', 'demo@insurance.com', '110101199001011234',
    '销售', 1, NULL, 50000.00, 1, '2024-01-01', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM insurance_employee LIMIT 1);
