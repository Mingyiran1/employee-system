-- ========================================================-- 保险员工管理系统 - 表结构重建-- 修改日期: 2026-05-23-- 说明: 根据新需求重建投保公司、保险员工、保费配置表-- ========================================================

-- ---------------------------------------------------------- 删除旧表（如果存在）-- --------------------------------------------------------
DROP TABLE IF EXISTS `insurance_company`;
DROP TABLE IF EXISTS `premium_config`;

-- ---------------------------------------------------------- 1. 投保公司表 (insured_company)-- 存储所有需要为员工购买保险的公司（A、B、C、D等）-- --------------------------------------------------------
DROP TABLE IF EXISTS `insured_company`;

CREATE TABLE `insured_company` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '公司名称',
    `code` varchar(50) DEFAULT NULL COMMENT '公司代码',
    `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `address` varchar(200) DEFAULT NULL COMMENT '公司地址',
    `status` tinyint DEFAULT '1' COMMENT '状态: 1=启用, 0=禁用',
    `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投保公司表';

-- ---------------------------------------------------------- 2. 保险员工表 (insurance_employee) - 核心表-- --------------------------------------------------------
DROP TABLE IF EXISTS `insurance_employee`;

CREATE TABLE `insurance_employee` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) NOT NULL COMMENT '员工姓名',
    `id_card` varchar(18) NOT NULL COMMENT '身份证号',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
    `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
    `company_id` bigint NOT NULL COMMENT '所属投保公司ID',
    `supplier_id` bigint DEFAULT NULL COMMENT '所属供应商ID（劳务公司）',
    `job_type` varchar(50) DEFAULT NULL COMMENT '工种: 销售/客服/理赔/管理/司机',
    `hire_date` date DEFAULT NULL COMMENT '入职日期',
    `status` tinyint DEFAULT '1' COMMENT '状态: 1=在职, 2=离职',
    `annual_premium` decimal(10,2) DEFAULT '0.00' COMMENT '年保费金额（根据工种费率自动计算）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_id_card` (`id_card`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_job_type` (`job_type`),
    KEY `idx_status` (`status`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保险员工表';

-- ---------------------------------------------------------- 3. 保费配置表 (premium_config) - 按工种配置费率-- --------------------------------------------------------
DROP TABLE IF EXISTS `premium_config`;

CREATE TABLE `premium_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `job_type` varchar(50) NOT NULL COMMENT '工种: 销售/客服/理赔/管理/司机',
    `rate` decimal(5,4) DEFAULT '0.0150' COMMENT '保费费率(如0.015=1.5%)',
    `base_salary` decimal(10,2) DEFAULT '10000.00' COMMENT '基数(年薪基数，元)',
    `annual_premium` decimal(10,2) GENERATED ALWAYS AS (round(`base_salary` * `rate`, 2)) STORED COMMENT '年保费(自动计算)',
    `status` tinyint DEFAULT '1' COMMENT '状态: 1=启用, 0=禁用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_type` (`job_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保费配置表-按工种配置';

-- ========================================================-- 插入基础数据-- ========================================================

-- ---------------------------------------------------------- 插入默认保费配置（按工种）-- --------------------------------------------------------
INSERT INTO `premium_config` (`job_type`, `rate`, `base_salary`, `status`) VALUES
('销售', 0.0150, 12000.00, 1),
('客服', 0.0120, 10000.00, 1),
('理赔', 0.0180, 15000.00, 1),
('管理', 0.0200, 20000.00, 1),
('司机', 0.0250, 8000.00, 1);

-- ---------------------------------------------------------- 插入示例投保公司数据-- --------------------------------------------------------
INSERT INTO `insured_company` (`name`, `code`, `contact_name`, `contact_phone`, `address`, `status`) VALUES
('智慧交通集团', 'ZHJT', '张经理', '13800138001', '北京市朝阳区', 1),
('平安人寿合作单位', 'PARENER01', '李经理', '13800138002', '上海市浦东新区', 1),
('太平洋保险合作单位', 'TPPARTNER', '王经理', '13800138003', '广州市天河区', 1);

-- ---------------------------------------------------------- 插入示例保险员工数据-- --------------------------------------------------------
INSERT INTO `insurance_employee` (`name`, `id_card`, `phone`, `email`, `company_id`, `supplier_id`, `job_type`, `hire_date`, `status`, `annual_premium`, `remark`, `create_by`) VALUES
('张伟', '110101199001011234', '13812345601', 'zhangwei@test.com', 1, 1, '销售', '2023-03-15', 1, 180.00, '资深销售代表', 1),
('李娜', '110101199002021235', '13812345602', 'lina@test.com', 1, 1, '客服', '2023-06-20', 1, 120.00, '客服主管', 1),
('王强', '110101199003031236', '13812345603', 'wangqiang@test.com', 2, 2, '理赔', '2022-09-10', 1, 270.00, '理赔专员', 1),
('刘芳', '110101199004041237', '13812345604', 'liufang@test.com', 2, 2, '管理', '2021-05-08', 1, 400.00, '部门经理', 1),
('陈明', '110101199005051238', '13812345605', 'chenming@test.com', 3, 1, '司机', '2023-01-12', 1, 200.00, '专职司机', 1);
