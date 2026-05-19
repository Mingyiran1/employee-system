# 数据权限测试检查清单

## ✅ 前置检查

- [ ] 1. 执行 `update_employee_data_scope.sql` 添加字段
- [ ] 2. 执行 `test_data_scope.sql` 插入测试数据
- [ ] 3. 启动应用，无报错

## ✅ API 接口测试

### 1. 登录接口测试
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```
- [ ] 返回 token
- [ ] 返回 roleId = 1

### 2. 数据权限测试

复制以下命令逐一测试：

| # | 用户 | 角色 | 命令 | 预期结果 | 通过 |
|---|------|------|------|----------|------|
| 1 | admin | 超级管理员 | `curl -H "token:{token}" http://localhost:8080/admin/employee/page?page=1&size=10` | total >= 8 | [ ] |
| 2 | zhangsan | 技术总监 | `curl -H "token:{token}" http://localhost:8080/admin/employee/page?page=1&size=10` | total = 5 | [ ] |
| 3 | lisi | 技术经理 | `curl -H "token:{token}" http://localhost:8080/admin/employee/page?page=1&size=10` | total = 2 | [ ] |
| 4 | wangwu | 普通员工 | `curl -H "token:{token}" http://localhost:8080/admin/employee/page?page=1&size=10` | total = 2 | [ ] |
| 5 | zhaoliu | 市场经理 | `curl -H "token:{token}" http://localhost:8080/admin/employee/page?page=1&size=10` | total = 3 | [ ] |
| 6 | sunqi | 市场专员 | `curl -H "token:{token}" http://localhost:8080/admin/employee/page?page=1&size=10` | total = 0 | [ ] |

## ✅ 日志验证

查看控制台输出，确认看到以下日志：
- [ ] `当前用户是超级管理员，查看全部数据`
- [ ] `数据权限：本部门及以下，用户部门ID：...`
- [ ] `数据权限：本部门，部门ID：...`
- [ ] `数据权限：仅本人，用户ID：...`

## ✅ 单元测试

```bash
# 运行数据权限测试
mvn test -Dtest=DataScopeTest
```

- [ ] testAdminDataScope 通过
- [ ] testTechCEODataScope 通过
- [ ] testTechManagerDataScope 通过
- [ ] testNormalUserDataScope 通过

## ⚠️ 常见问题

### 问题1: 所有角色看到的数据都一样
**解决**：检查登录时是否设置了 `BaseContext.setCurrentUser()`

### 问题2: 普通员工看不到自己创建的数据
**解决**：检查 `employee.create_by` 字段是否在新增时正确设置

### 问题3: 部门权限返回空
**解决**：检查 `employee.user_id` 是否与 `sys_user.id` 正确关联

### 问题4: 切面未生效
**解决**：
1. 检查 `@EnableAspectJAutoProxy` 是否已添加
2. 检查 `DataScopeAspect` 类是否有 `@Aspect` 和 `@Component`
3. 检查 Controller 方法是否有 `@DataScope` 注解

## 📊 测试数据关系图

```
SysUser              Employee              Department
─────────────────────────────────────────────────────────
id=1(admin)          id=1(张三)            id=1(总裁办)
                     user_id=2 ─────────>  dept_id=2 ──> id=2(技术部)
                                              │
id=2(zhangsan)       id=2(李四)            id=4(后端组)
roleId=2(dept_CEO)   create_by=2            id=5(前端组)
                     user_id=3
                     
id=3(lisi)           id=3(王五)
roleId=3(manager)    create_by=2
                     user_id=4
                     
id=4(wangwu)         id=6(赵六)
roleId=4(user)       create_by=1
                     user_id=5
```

## ✅ 测试完成确认

- [ ] 所有API测试通过
- [ ] 日志输出符合预期
- [ ] 单元测试全部通过
- [ ] 代码审查完成

---
测试日期：_______  测试人：_______
