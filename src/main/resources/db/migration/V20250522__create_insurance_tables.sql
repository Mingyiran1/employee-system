-- ========================================================
-- 保险员工管理系统 - 数据库初始化脚本
-- 创建日期: 2026-05-22
-- 说明: 创建保险公司、供应商、保险员工及保费配置表
-- ========================================================

-- --------------------------------------------------------
-- 1. 保险公司表 (insurance_company)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `insurance_company`;

CREATE TABLE `insurance_company` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '公司名称',
    `code` varchar(50) DEFAULT NULL COMMENT '公司代码',
    `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `address` varchar(200) DEFAULT NULL COMMENT '公司地址',
    `status` tinyint DEFAULT '1' COMMENT '状态: 1=启用, 0=禁用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_company_code` (`code`),
    KEY `idx_company_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保险公司表';

-- --------------------------------------------------------
-- 2. 供应商表 (supplier)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `supplier`;

CREATE TABLE `supplier` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) NOT NULL COMMENT '供应商名称',
    `code` varchar(50) DEFAULT NULL COMMENT '供应商代码',
    `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `address` varchar(200) DEFAULT NULL COMMENT '地址',
    `status` tinyint DEFAULT '1' COMMENT '状态: 1=启用, 0=禁用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_supplier_code` (`code`),
    KEY `idx_supplier_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- --------------------------------------------------------
-- 3. 保险员工表 (insurance_employee) - 核心表
-- --------------------------------------------------------
DROP TABLE IF EXISTS `insurance_employee`;

CREATE TABLE `insurance_employee` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(50) NOT NULL COMMENT '员工姓名',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
    `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
    `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
    `job_type` varchar(50) DEFAULT NULL COMMENT '岗位类型: 销售/客服/理赔/管理',
    `company_id` bigint DEFAULT NULL COMMENT '所属保险公司ID',
    `supplier_id` bigint DEFAULT NULL COMMENT '所属供应商ID',
    `annual_premium` decimal(10,2) DEFAULT '0.00' COMMENT '年保费金额',
    `daily_premium` decimal(10,2) DEFAULT '0.00' COMMENT '日保费金额（计算字段）',
    `premium_calc_type` tinyint DEFAULT '1' COMMENT '保费计算类型: 1=年费率, 2=日费率',
    `hire_date` date DEFAULT NULL COMMENT '入职日期',
    `status` tinyint DEFAULT '1' COMMENT '状态: 1=在职, 2=离职',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除: 0=未删除, 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_employee_name` (`name`),
    KEY `idx_employee_phone` (`phone`),
    KEY `idx_employee_company` (`company_id`),
    KEY `idx_employee_supplier` (`supplier_id`),
    KEY `idx_employee_status` (`status`),
    KEY `idx_employee_job_type` (`job_type`),
    KEY `idx_employee_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保险员工表';

-- --------------------------------------------------------
-- 4. 保费计算配置表 (premium_config)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `premium_config`;

CREATE TABLE `premium_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` varchar(50) NOT NULL COMMENT '配置键',
    `config_value` varchar(200) DEFAULT NULL COMMENT '配置值',
    `calc_type` tinyint DEFAULT '1' COMMENT '计算类型: 1=年费率, 2=日费率',
    `annual_rate` decimal(5,4) DEFAULT '0.0150' COMMENT '年费率 (如0.015表示1.5%)',
    `daily_rate` decimal(10,2) DEFAULT '0.00' COMMENT '日费率金额',
    `description` varchar(200) DEFAULT NULL COMMENT '配置说明',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    KEY `idx_config_calc_type` (`calc_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保费计算配置表';

-- ========================================================
-- 插入基础数据
-- ========================================================

-- --------------------------------------------------------
-- 插入保险公司数据
-- --------------------------------------------------------
INSERT INTO `insurance_company` (`name`, `code`, `contact_name`, `contact_phone`, `address`, `status`) VALUES
('中国平安保险', 'PINGAN', '张经理', '13800138001', '深圳市福田区平安金融中心', 1),
('中国人寿保险', 'CHINALIFE', '李经理', '13800138002', '北京市西城区金融大街16号', 1),
('太平洋保险', 'CPIC', '王经理', '13800138003', '上海市浦东新区银城中路190号', 1),
('新华保险', 'NCI', '赵经理', '13800138004', '北京市朝阳区建国门外大街甲12号', 1),
('泰康保险', 'TAIKANG', '陈经理', '13800138005', '北京市朝阳区建国路甲92号', 1);

-- --------------------------------------------------------
-- 插入供应商数据（劳务公司）
-- --------------------------------------------------------
INSERT INTO `supplier` (`name`, `code`, `contact_name`, `contact_phone`, `address`, `status`) VALUES
('前程无忧劳务公司', 'QIANCHENG', '刘经理', '13900139001', '上海市浦东新区张江高科技园区', 1),
('智联招聘劳务公司', 'ZHAOPIN', '周经理', '13900139002', '北京市朝阳区酒仙桥路10号', 1),
('猎聘网劳务公司', 'LIEPIN', '吴经理', '13900139003', '北京市朝阳区望京SOHO', 1);

-- --------------------------------------------------------
-- 插入默认保费配置
-- --------------------------------------------------------
INSERT INTO `premium_config` (`config_key`, `config_value`, `calc_type`, `annual_rate`, `daily_rate`, `description`) VALUES
('DEFAULT_PREMIUM_RATE', '默认保费费率', 1, 0.0150, 0.00, '系统默认保费计算费率，年费率1.5%');

-- ========================================================
-- 插入测试员工数据（可选，用于开发测试）
-- ========================================================

-- --------------------------------------------------------
-- 插入测试员工数据（15条）
-- --------------------------------------------------------
INSERT INTO `insurance_employee` (`name`, `phone`, `email`, `id_card`, `job_type`, `company_id`, `supplier_id`, `annual_premium`, `daily_premium`, `premium_calc_type`, `hire_date`, `status`, `remark`, `create_by`) VALUES
('张伟', '13812345601', 'zhangwei@example.com', '110101199001011234', '销售', 1, 1, 15000.00, 41.10, 1, '2023-03-15', 1, '资深销售代表', 1),
('李娜', '13812345602', 'lina@example.com', '110101199002021235', '客服', 1, 1, 12000.00, 32.88, 1, '2023-06-20', 1, '客服主管', 1),
('王强', '13812345603', 'wangqiang@example.com', '110101199003031236', '理赔', 2, 2, 18000.00, 49.32, 1, '2022-09-10', 1, '理赔专员', 1),
('刘芳', '13812345604', 'liufang@example.com', '110101199004041237', '管理', 2, 2, 25000.00, 68.50, 1, '2021-05-08', 1, '部门经理', 1),
('陈明', '13812345605', 'chenming@example.com', '110101199005051238', '销售', 3, 1, 16000.00, 43.84, 1, '2023-01-12', 1, '销售经理', 1),
('杨丽', '13812345606', 'yangli@example.com', '110101199006061239', '客服', 3, 3, 11000.00, 30.14, 1, '2023-08-25', 1, '客服专员', 1),
('赵军', '13812345607', 'zhaojun@example.com', '110101199007071240', '理赔', 4, 2, 19000.00, 52.05, 1, '2022-11-30', 1, '高级理赔员', 1),
('黄婷', '13812345608', 'huangting@example.com', '110101199008081241', '销售', 4, 1, 14000.00, 38.36, 1, '2023-04-18', 1, '销售代表', 1),
('周杰', '13812345609', 'zhoujie@example.com', '110101199009091242', '管理', 5, 3, 28000.00, 76.72, 1, '2020-12-05', 1, '区域总监', 1),
('吴敏', '13812345610', 'wumin@example.com', '110101199010101243', '客服', 5, 3, 13000.00, 35.62, 1, '2023-07-22', 1, '客服组长', 1),
('郑涛', '13812345611', 'zhengtao@example.com', '110101199011111244', '销售', 1, 1, 17000.00, 46.58, 1, '2022-06-15', 2, '已离职', 1),
('孙雪', '13812345612', 'sunxue@example.com', '110101199012121245', '理赔', 2, 2, 20000.00, 54.80, 1, '2021-09-08', 1, '理赔主管', 1),
('朱辉', '13812345613', 'zhuhui@example.com', '110101199101011246', '销售', 3, 1, 15500.00, 42.47, 1, '2023-02-28', 1, '销售专员', 1),
('徐静', '13812345614', 'xujing@example.com', '110101199102021247', '客服', 4, 2, 12500.00, 34.25, 1, '2023-05-20', 1, '客服专员', 1),
('马鹏', '13812345615', 'mapeng@example.com', '110101199103031248', '管理', 5, 3, 30000.00, 82.20, 1, '2020-08-01', 1, '总经理助理', 1);
