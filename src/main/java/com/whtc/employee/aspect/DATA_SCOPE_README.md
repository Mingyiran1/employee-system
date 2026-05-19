# 数据权限框架使用说明

## 概述

数据权限框架基于 Spring AOP 实现，通过 `@DataScope` 注解自动对查询结果进行权限过滤。

## 核心组件

### 1. @DataScope 注解

```java
@DataScope(deptField = "dept_id", userField = "create_by")
```

参数说明：
- `deptField`: 部门ID字段名（用于部门级数据权限）
- `userField`: 用户ID字段名（用于本人数据权限）
- `deptAlias`: 表别名（用于多表关联查询）

### 2. SysUser 与 Employee 关联

数据权限通过 `employee.user_id` 字段关联 `sys_user.id`：

```
sys_user (登录用户) -> employee (员工) -> dept_id (部门)
       id  ---------------->  user_id
```

这样可以根据当前登录用户找到其所属部门，进而实现：
- **全部数据**：超级管理员
- **本部门及以下**：部门总监
- **本部门**：部门经理
- **仅本人**：普通员工

## 使用方法

### 1. 数据库准备

执行 `db/update_employee_data_scope.sql` 添加必要字段：
- `create_by`: 创建人ID（用于本人权限）
- `user_id`: 关联系统用户ID

### 2. 关联用户与员工

在创建员工时设置 `user_id` 字段，将员工与系统用户关联：

```java
employee.setUserId(sysUserId);  // 关联系统用户
```

### 3. 添加数据权限注解

在 Controller 的查询方法上添加 `@DataScope`：

```java
@GetMapping("/page")
@DataScope(deptField = "dept_id", userField = "create_by")
public Result<PageResult> page(EmployeePageQueryDTO dto) {
    QueryWrapper<Employee> wrapper = new QueryWrapper<>();
    PageResult result = employeeService.pageQuery(dto, wrapper);
    return Result.success(result);
}
```

### 4. Service 层处理

Service 方法接收 QueryWrapper 参数：

```java
@Override
public PageResult pageQuery(EmployeePageQueryDTO dto, QueryWrapper<Employee> wrapper) {
    // wrapper 已由切面添加数据权限条件
    // 继续添加业务查询条件...
    wrapper.like("name", dto.getName());
    return this.page(pageInfo, wrapper);
}
```

## 权限规则

| 角色ID | 角色 | 数据范围 |
|-------|------|---------|
| 1 | admin | 全部数据 |
| 2 | dept_CEO | 本部门及以下 |
| 3 | dept_manager | 本部门 |
| 4 | user | 仅本人 |

## 数据权限过滤逻辑

1. 切面拦截 `@DataScope` 注解的方法
2. 获取当前登录用户（从 BaseContext）
3. 超级管理员直接放行
4. 根据用户角色确定数据范围
5. 获取用户所属部门（通过 employee.user_id 关联）
6. 向 QueryWrapper 添加对应的过滤条件

## 注意事项

1. **必须关联用户**：员工表需要设置 `user_id` 才能正确获取部门信息
2. **创建人设置**：保存数据时自动设置 `create_by` 字段
3. **Wrapper 传递**：Controller 需要创建 QueryWrapper 并传给 Service
4. **性能优化**：已为 dept_id、user_id、create_by 添加索引
