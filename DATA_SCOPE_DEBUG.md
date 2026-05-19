# 数据权限调试指南

## 问题：所有用户看到的数据都一样

### 第一步：检查数据库

请执行以下 SQL 脚本验证数据：

```bash
mysql -u root -p sky_employee_system < src/main/resources/db/verify_data_scope.sql
```

**关键检查点：**

1. **employee 表必须有 user_id 字段** - 用于关联 sys_user.id
2. **employee 表必须有 create_by 字段** - 用于"仅本人"权限
3. **employee.user_id 必须正确关联到 sys_user.id**

### 第二步：检查日志输出

启动后端后，在控制台应该看到：

```
【数据权限】当前用户：zhangsan，角色：dept_CEO，角色ID：2
【数据权限】找到QueryWrapper，准备添加过滤条件
【数据权限】数据范围值：2，部门字段：dept_id，用户字段：create_by
【数据权限】查询用户 2 的部门ID
【数据权限】用户 2 的部门ID：2
【数据权限】本部门及以下，用户所属部门ID：2
【数据权限】部门及子部门ID列表：[2, 4, 5]
```

**如果看不到这些日志，说明：**
1. JwtTokenInterceptor 没有正确执行
2. DataScopeAspect 没有被触发

### 第三步：常见问题和解决

#### 问题1：看不到数据权限日志

**原因**：JwtTokenInterceptor 中的依赖没有注入

**解决**：检查 WebMvcConfiguration 中是否正确注入了 jwtProperties 和 sysUserService

#### 问题2：日志显示"用户未关联员工信息"

**原因**：employee.user_id 为 null 或与 sys_user.id 不匹配

**解决**：
```sql
-- 检查关联关系
SELECT su.id, su.username, e.user_id, e.name
FROM sys_user su
LEFT JOIN employee e ON e.user_id = su.id
WHERE su.username IN ('zhangsan', 'lisi', 'wangwu');
```

如果没有关联，执行测试数据脚本：
```sql
-- 重新插入测试数据
DELETE FROM employee WHERE id <= 10;

INSERT INTO employee (id, name, gender, phone, email, dept_id, position, entry_date, status, address, create_by, user_id, create_time, update_time, is_deleted) VALUES
(1, '张三', 1, '13800138001', 'zhangsan@company.com', 2, '技术总监', '2020-01-01', 1, '北京', 1, 2, NOW(), NOW(), 0),
(2, '李四', 1, '13800138002', 'lisi@company.com', 2, '技术经理', '2021-03-15', 1, '北京', 2, 3, NOW(), NOW(), 0),
(3, '王五', 1, '13800138003', 'wangwu@company.com', 4, '后端工程师', '2022-06-01', 1, '北京', 2, 4, NOW(), NOW(), 0);
```

#### 问题3：admin 能看到全部，但其他角色也能看到全部

**原因**：getDataScopeByRole 方法返回的值不对

**检查**：确认 sys_user.role_id 字段值是否正确
- 1 = admin (全部)
- 2 = dept_CEO (本部门及以下)
- 3 = dept_manager (本部门)
- 4 = user (仅本人)

### 第四步：API 测试

使用 curl 测试不同用户的返回数据量：

```bash
# 1. 登录获取 token
TOKEN=$(curl -s -X POST http://localhost:8080/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"123456"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 2. 查询员工列表
curl -s "http://localhost:8080/admin/employee/page?page=1&size=10" \
  -H "token: $TOKEN" | grep -o '"total":[0-9]*'
```

**预期结果：**
| 用户名 | 角色 | 应返回数据条数 |
|--------|------|----------------|
| admin | 超级管理员 | 8条 |
| zhangsan | 技术总监 | 5条 (部门2,4,5) |
| lisi | 技术经理 | 2条 (部门2) |
| wangwu | 普通员工 | 2条 (create_by=2) |

### 第五步：如果还是不行

1. **检查切面是否被扫描到**
   - 确认 DataScopeAspect 类上有 `@Aspect` 和 `@Component`
   - 确认 EmployeeSystemApplication 上有 `@EnableAspectJAutoProxy`

2. **检查 Controller 方法**
   - 确认 EmployeeController.page() 方法上有 `@DataScope` 注解
   - 确认方法参数中有 `QueryWrapper<Employee>`

3. **手动调试**
   在 DataScopeAspect.before() 方法第一行添加断点，查看是否被触发。
