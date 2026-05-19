# 数据权限测试指南

## 一、测试前准备

### 1. 执行数据库脚本
```bash
# 1. 添加字段
mysql -u root -p sky_employee_system < src/main/resources/db/update_employee_data_scope.sql

# 2. 插入测试数据
mysql -u root -p sky_employee_system < src/main/resources/db/test_data_scope.sql
```

### 2. 确认测试数据
| 用户名 | 密码 | 角色 | 部门 | 期望看到的数据 |
|--------|------|------|------|----------------|
| admin | 123456 | admin(1) | 总裁办 | 全部8条员工数据 |
| zhangsan | 123456 | dept_CEO(2) | 技术部 | 技术部+后端组+前端组(5条) |
| lisi | 123456 | dept_manager(3) | 技术部 | 技术部本部门(2条) |
| wangwu | 123456 | user(4) | 后端组 | 张三创建的数据(2条) |
| zhaoliu | 123456 | dept_manager(3) | 市场部 | 市场部本部门(3条) |
| sunqi | 123456 | user(4) | 市场部 | 赵六创建的数据(0条，因为赵六创建的是6号) |

## 二、API测试步骤

### Step 1: 登录获取Token
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}'
```

返回示例：
```json
{
  "code": 1,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 2,
    "roleId": 2,
    "roleCode": "dept_CEO"
  }
}
```

### Step 2: 测试员工列表查询
```bash
curl -X GET "http://localhost:8080/admin/employee/page?page=1&size=10" \
  -H "token: eyJhbGciOiJIUzI1NiJ9..."
```

## 三、不同角色测试结果验证

### 1. admin 登录测试
```bash
# 登录
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 查询员工列表 - 应该返回全部8条数据
curl -X GET "http://localhost:8080/admin/employee/page?page=1&size=10" \
  -H "token: {admin_token}"
```
**预期结果**: total = 8

### 2. 张三(技术总监)登录测试
```bash
# 登录
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}'

# 查询员工列表 - 应该返回技术部及子部门的数据
curl -X GET "http://localhost:8080/admin/employee/page?page=1&size=10" \
  -H "token: {zhangsan_token}"
```
**预期结果**: total = 5（张三、李四、王五、员工A、员工B）

### 3. 李四(技术经理)登录测试
```bash
# 登录
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"lisi","password":"123456"}'

# 查询员工列表 - 应该只返回技术部本部门数据
curl -X GET "http://localhost:8080/admin/employee/page?page=1&size=10" \
  -H "token: {lisi_token}"
```
**预期结果**: total = 2（张三、李四）

### 4. 王五(普通员工)登录测试
```bash
# 登录
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"wangwu","password":"123456"}'

# 查询员工列表 - 应该只返回create_by=2的数据
curl -X GET "http://localhost:8080/admin/employee/page?page=1&size=10" \
  -H "token: {wangwu_token}"
```
**预期结果**: total = 2（张三创建的李四、王五）

### 5. 赵六(市场经理)登录测试
```bash
# 登录
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhaoliu","password":"123456"}'

# 查询员工列表 - 应该只返回市场部数据
curl -X GET "http://localhost:8080/admin/employee/page?page=1&size=10" \
  -H "token: {zhaoliu_token}"
```
**预期结果**: total = 3（赵六、孙七、员工C）

## 四、日志验证

查看控制台日志，确认数据权限生效：
```
# 应该看到类似日志：
数据权限：本部门及以下，用户部门ID：2，角色：dept_CEO
或
数据权限：本部门，部门ID：2
或
数据权限：仅本人，用户ID：4
```

## 五、问题排查

### 问题1: 所有用户都能看到全部数据
**排查**：
1. 检查 `@DataScope` 注解是否添加到 Controller 方法
2. 检查切面类是否被扫描（添加 `@Aspect` 和 `@Component`）
3. 检查登录后是否设置了 BaseContext.setCurrentUser()

### 问题2: 普通员工看不到任何数据
**排查**：
1. 检查 employee.create_by 字段是否正确设置
2. 检查 employee.user_id 是否与 sys_user.id 关联

### 问题3: 部门权限不生效
**排查**：
1. 检查 employee.user_id 是否关联了 sys_user.id
2. 检查 DepartmentService.getChildDeptIds() 是否正确返回子部门

## 六、Postman 测试集合

1. 创建 Collection：数据权限测试
2. 创建 Folder：登录
   - POST /login - admin登录
   - POST /login - 张三登录
   - POST /login - 李四登录
   - POST /login - 王五登录
3. 创建 Folder：员工查询
   - GET /admin/employee/page - 使用不同token
4. 设置环境变量：token、userId
