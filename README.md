# 企业员工审批管理系统

基于 Spring Boot 开发的企业员工与审批管理系统，支持员工信息管理与多层级审批流程。系统采用前后端分离架构，后端提供 RESTful API，前端基于 Vue 3 实现。

## 技术栈

| 分类 | 技术 |
|------|------|
| 后端框架 | Spring Boot |
| ORM 框架 | MyBatis |
| 数据库 | MySQL |
| 认证方案 | JWT Token |
| 权限模型 | RBAC（基于角色的访问控制） |
| 前端框架 | Vue 3 |
| 构建工具 | Maven |

## 核心功能

### 员工信息管理
- 员工信息的增删改查与分页查询
- 员工与部门的关联管理
- 支持按部门、姓名等条件筛选

### 审批流程模块（核心）
- **两级审批链路**：将原"员工申请直达 HR"的单级流程调整为"部门经理 → HR"两级审批，使流程贴合企业实际审批场景
- **四张核心数据表**：审批流程、审批节点、审批记录、审批权限，支持审批节点配置、进度跟踪与审批留痕
- 支持发起审批、处理审批（通过/驳回）、撤销审批等操作
- 驳回场景下的状态一致性处理，确保审批流程数据准确

### RBAC 权限控制
- **角色体系**：超级管理员、部门经理、普通员工
- **JWT 认证**：基于 JWT 实现 Token 签发与拦截器校验
- **权限隔离**：不同角色仅能操作权限范围内的审批节点，确保数据安全

### 部门管理
- 支持树形多级部门结构
- 部门的增删改查与树形展示

## 项目结构

```
sky-employee-system/
├── sky-common/                  # 公共模块
│   └── src/main/java/com/sky/employee/
│       ├── annotation/          # 自定义注解
│       ├── constant/            # 常量定义
│       ├── context/             # 上下文（当前用户信息）
│       ├── enums/               # 枚举类
│       ├── exception/           # 异常定义
│       ├── properties/          # 配置属性类
│       └── utils/               # 工具类（JWT 等）
│
├── sky-pojo/                    # 数据对象模块
│   └── src/main/java/com/sky/employee/
│       ├── dto/                 # 数据传输对象（请求入参）
│       ├── entity/              # 实体类（对应数据库表）
│       └── vo/                  # 视图对象（响应出参）
│
├── sky-server/                  # 服务端模块
│   └── src/main/java/com/sky/employee/
│       ├── config/              # 配置类
│       ├── controller/          # 控制器（接收请求）
│       │   └── admin/           # 管理端接口
│       ├── interceptor/         # JWT 拦截器
│       ├── mapper/              # MyBatis Mapper 接口
│       ├── service/             # 业务逻辑层
│       │   └── impl/            # 服务实现
│       └── exception/           # 全局异常处理
│
├── frontend/                    # 前端 Vue 3 项目
│   └── src/
│       ├── api/                 # API 请求封装
│       ├── views/               # 页面组件
│       ├── router/              # 路由配置
│       └── utils/               # 工具函数
│
└── pom.xml                      # Maven 父级配置
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.8+
- Node.js 18+（前端）

### 后端启动

1. 克隆项目后进入根目录
2. 创建数据库 `employee_system`，字符集 `utf8mb4`
3. 执行 `src/main/resources/db/` 目录下的 SQL 脚本初始化表结构和测试数据
4. 修改 `src/main/resources/application.yml` 中的数据库连接配置
5. 运行 `EmployeeSystemApplication.java` 或执行 `mvn spring-boot:run`
6. 验证：访问 `http://localhost:8080/api/test`，返回 `{"code":200,"msg":"OK"}` 表示启动成功

### 前端启动

1. 进入前端目录 `cd frontend`
2. 安装依赖 `npm install`
3. 启动开发服务器 `npm run serve`
4. 浏览器访问 `http://localhost:8081`

### 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 超级管理员 |
| lisi | 123456 | 部门经理 |
| wangwu | 123456 | 普通员工 |

## 接口概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/admin/auth/login` | 用户登录，返回 JWT Token |
| 认证 | POST | `/admin/auth/logout` | 登出 |
| 员工 | GET | `/admin/employee/page` | 分页查询员工 |
| 员工 | POST | `/admin/employee` | 新增员工 |
| 员工 | PUT | `/admin/employee` | 更新员工 |
| 员工 | DELETE | `/admin/employee/{id}` | 删除员工 |
| 审批 | POST | `/admin/approval/start` | 发起审批 |
| 审批 | POST | `/admin/approval/process/{recordId}` | 处理审批（通过/驳回） |
| 审批 | GET | `/admin/approval/pending` | 我的待审批列表 |
| 审批 | GET | `/admin/approval/my` | 我发起的审批列表 |
| 审批 | GET | `/admin/approval/detail/{recordId}` | 审批详情（含历史） |
| 审批 | POST | `/admin/approval/cancel/{recordId}` | 撤销审批 |
| 部门 | GET | `/admin/department/tree` | 部门树形结构 |
| 部门 | POST | `/admin/department` | 新增部门 |
| 部门 | DELETE | `/admin/department/{id}` | 删除部门 |
