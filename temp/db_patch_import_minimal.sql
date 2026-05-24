
SET @supplier_status_sql = (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'supplier' AND column_name = 'status'
    ),
    'SELECT 1',
    'ALTER TABLE supplier ADD COLUMN status TINYINT DEFAULT 1 AFTER cooperation_status'
  )
);
PREPARE stmt FROM @supplier_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE supplier SET status = 1 WHERE status IS NULL;

CREATE TABLE IF NOT EXISTS insured_company (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) DEFAULT NULL,
    contact_name VARCHAR(50) DEFAULT NULL,
    contact_phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(200) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS premium_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_type VARCHAR(50) NOT NULL,
    rate DECIMAL(5,4) DEFAULT NULL,
    base_salary DECIMAL(10,2) DEFAULT NULL,
    annual_premium DECIMAL(10,2) DEFAULT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_type (job_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS insurance_employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    id_card VARCHAR(18) NOT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL,
    company_id BIGINT NOT NULL,
    supplier_id BIGINT DEFAULT NULL,
    job_type VARCHAR(50) DEFAULT NULL,
    hire_date DATE DEFAULT NULL,
    leave_date DATE DEFAULT NULL,
    status TINYINT DEFAULT 1,
    annual_premium DECIMAL(10,2) DEFAULT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    create_by BIGINT DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_company_id (company_id),
    KEY idx_supplier_id (supplier_id),
    KEY idx_job_type (job_type),
    KEY idx_status (status),
    KEY idx_is_deleted (is_deleted),
    KEY idx_create_by (create_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO insured_company (name, code, contact_name, contact_phone, address, status, create_by, is_deleted)
SELECT 'A公司', 'COMPANY_A', '张经理', '13800138001', '北京市朝阳区A大厦', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM insured_company WHERE name = 'A公司' AND is_deleted = 0);
INSERT INTO insured_company (name, code, contact_name, contact_phone, address, status, create_by, is_deleted)
SELECT 'B公司', 'COMPANY_B', '李经理', '13800138002', '上海市浦东新区B大厦', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM insured_company WHERE name = 'B公司' AND is_deleted = 0);
INSERT INTO insured_company (name, code, contact_name, contact_phone, address, status, create_by, is_deleted)
SELECT 'C公司', 'COMPANY_C', '王经理', '13800138003', '广州市天河区C大厦', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM insured_company WHERE name = 'C公司' AND is_deleted = 0);
INSERT INTO insured_company (name, code, contact_name, contact_phone, address, status, create_by, is_deleted)
SELECT 'D公司', 'COMPANY_D', '赵经理', '13800138004', '深圳市南山区D大厦', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM insured_company WHERE name = 'D公司' AND is_deleted = 0);

INSERT INTO supplier (name, contact_name, contact_phone, email, address, business_scope, cooperation_status, status, remark, is_deleted)
SELECT '供应商A', '刘经理', '13900139001', 'supplierA@example.com', '北京市海淀区A路1号', '保险员工服务', 1, 1, '导入测试基础数据', 0
WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE name = '供应商A' AND is_deleted = 0);
INSERT INTO supplier (name, contact_name, contact_phone, email, address, business_scope, cooperation_status, status, remark, is_deleted)
SELECT '供应商B', '陈经理', '13900139002', 'supplierB@example.com', '上海市静安区B路2号', '保险员工服务', 1, 1, '导入测试基础数据', 0
WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE name = '供应商B' AND is_deleted = 0);
INSERT INTO supplier (name, contact_name, contact_phone, email, address, business_scope, cooperation_status, status, remark, is_deleted)
SELECT '供应商C', '杨经理', '13900139003', 'supplierC@example.com', '广州市越秀区C路3号', '保险员工服务', 1, 1, '导入测试基础数据', 0
WHERE NOT EXISTS (SELECT 1 FROM supplier WHERE name = '供应商C' AND is_deleted = 0);

INSERT INTO premium_config (job_type, rate, base_salary, annual_premium, status)
VALUES
    ('一类', 0.0150, 120000.00, 1800.00, 1),
    ('二类', 0.0200, 120000.00, 2400.00, 1),
    ('三类', 0.0250, 120000.00, 3000.00, 1)
ON DUPLICATE KEY UPDATE
    rate = VALUES(rate),
    base_salary = VALUES(base_salary),
    annual_premium = VALUES(annual_premium),
    status = VALUES(status);

UPDATE sys_user target
JOIN (SELECT password FROM sys_user WHERE username = 'admin') source
SET target.password = source.password,
    target.role_id = 4,
    target.role_code = 'user',
    target.status = 1
WHERE target.username = 'wangwu';
