# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **智慧交通企业员工信息管理系统** (Smart Transportation Enterprise Employee Management System) - a full-stack Java Spring Boot + Vue 3 application with role-based access control, data scope filtering, approval workflows, and data masking.

## Build Commands

### Backend (Spring Boot)

```bash
# Compile and package
mvn clean compile

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=DataScopeTest
mvn test -Dtest=PasswordEncoderTest

# Run the application
mvn spring-boot:run

# Build production JAR
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

### Frontend (Vue 3)

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (port 3000, proxies /api to localhost:8080)
npm run serve

# Build for production
npm run build
```

### Full Development Setup

```bash
# 1. Start MySQL and Redis
# 2. Run backend (port 8080)
mvn spring-boot:run

# 3. In another terminal, run frontend (port 3000)
cd frontend && npm run serve

# 4. Access application at http://localhost:3000
```

## Architecture Overview

### Tech Stack
- **Backend**: Spring Boot 3.2.5, Java 17, MyBatis Plus 3.5.6, MySQL 8.0, Redis 7.0
- **Frontend**: Vue 3.3.4, Element Plus 2.3.14, Vue Router 4, Axios, ECharts
- **Security**: JWT (jjwt 0.12.3), BCrypt password encoding
- **Export**: EasyExcel 3.3.4 for async Excel exports

### Key Architectural Patterns

#### 1. Data Scope Permission System (@DataScope)
Located in `aspect/DataScopeAspect.java`, this AOP-based system filters data based on user role:

- **1 (全部数据)**: Admin sees all data
- **2 (本部门及以下)**: Role sees own department + sub-departments
- **3 (本部门)**: Role sees only own department
- **4 (仅本人)**: User sees only their own data

Usage: Add `@DataScope(deptField = "dept_id", userField = "create_by")` to service methods. The aspect automatically injects QueryWrapper conditions via ThreadLocal (`DataScopeContext`).

#### 2. Data Masking (@DataMasking)
Located in `jackson/DataMaskingSerializer.java`, automatically masks sensitive fields in API responses:

- Types: PHONE, EMAIL, ID_CARD, NAME, ADDRESS, BANK_CARD
- Admin users (roleId=1) see plaintext; others see masked data
- Usage: `@DataMasking(MaskingType.PHONE)` on entity fields

#### 3. Approval Workflow System
Located in `service/ApprovalService.java`, implements multi-level approval flows:

- Business types: EMPLOYEE_ENTRY (入职), EMPLOYEE_LEAVE (离职)
- Flow: Applicant submits → Direct manager approves → Department head approves → Complete
- Status tracking: 0-Pending, 1-Approved, 2-Rejected
- Tables: `approval_process` (流程定义), `approval_node` (节点定义), `approval_record` (实例记录), `approval_history` (审批历史)

#### 4. ThreadLocal Context Management
`BaseContext` stores current user info in ThreadLocal:
- `setCurrentUserId()` / `getCurrentUserId()`: Current user ID
- `setCurrentUser()` / `getCurrentUser()`: Full user object with role
- Populated by `JwtTokenInterceptor` from JWT claims

#### 5. JWT Authentication Flow
`JwtTokenInterceptor` validates tokens on every request:
- Token blacklist stored in Redis (logout invalidation)
- Claims include empId for user identification
- Detailed error codes: 401001 (missing), 401002 (expired), 401003 (invalid), etc.

## Code Style Conventions

### Backend (Java)
- **Indent**: 4 spaces (no tabs)
- **Braces**: Same line, Egyptian style
- **Naming**:
  - Packages: `com.whtc.employee.service.impl`
  - Classes: `EmployeeServiceImpl` (UpperCamelCase)
  - Methods: `getById()`, `saveEmployee()` (lowerCamelCase, verb prefix)
  - Constants: `MAX_PAGE_SIZE`, `DEFAULT_PASSWORD` (UPPER_SNAKE_CASE)
- **Line length**: Max 120 characters

### Frontend (Vue/JavaScript)
- **Indent**: 2 spaces
- **Quotes**: Single for JS, double for template attributes
- **Component names**: `EmployeeList.vue`, `UserProfile.vue` (UpperCamelCase)

## Database Schema Highlights

Key tables:
- `employee`: Core employee data (linked to `sys_user` via `user_id`)
- `sys_user`: Login accounts with role
- `sys_role`: Role definitions with `data_scope` field (1-4)
- `department`: Department tree structure with `parent_id`
- `insurance_employee`: Employee insurance records
- `supplier`: Supplier management
- `export_task`: Async export task queue

## Testing Accounts

| Username | Password | Role | Data Scope |
|----------|----------|------|------------|
| admin | 123456 | 超级管理员 | All data |
| zhangsan | 123456 | 技术总监 | Dept + children |
| lisi | 123456 | 部门经理 | Own dept only |
| wangwu | 123456 | 普通员工 | Self only |

## Common Development Tasks

### Adding a New API Endpoint
1. Create DTO in `dto/` for request params
2. Create VO in `vo/` for response data
3. Add controller method in `controller/admin/`
4. Implement service interface and impl
5. Add mapper interface (MyBatis Plus - no XML needed for simple CRUD)

### Running Data Scope Tests
```bash
# Run the data scope unit test
mvn test -Dtest=DataScopeTest

# Or test via curl
curl -X POST http://localhost:8080/login -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}'
curl -H "token:YOUR_TOKEN" "http://localhost:8080/admin/employee/page?page=1&size=10"
```

### Testing Approval Flow
1. Create employee (sets status=0 pending)
2. Call `POST /admin/approval/start` with businessType="EMPLOYEE_ENTRY"
3. Approver calls `POST /admin/approval/process` with approvalStatus=1 (pass) or 2 (reject)
4. Query status via `GET /admin/approval/status`

## Important Notes

- **Git workflow**: Always `git pull` before `git push` (see CONTRIBUTING.md)
- **SQL injection prevention**: DataScopeAspect validates field names against `^[a-zA-Z_][a-zA-Z0-9_]*$` pattern
- **BCrypt passwords**: Use `PasswordEncoderTest` to generate hashes for test data
- **Async exports**: Large exports use `ExportTask` queue; files stored in `./temp/exports`
- **Environment variables**: `MYSQL_PASSWORD`, `REDIS_PASSWORD`, `JWT_SECRET_KEY` can override defaults
