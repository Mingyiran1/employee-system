# 项目贡献指南

欢迎加入智慧交通企业员工信息管理系统开发团队！本文档帮助你快速上手项目。

## 一、项目启动

### 1. 后端启动（Spring Boot）

**环境要求：**
- JDK 17+
- MySQL 8.0
- Maven 3.8+
- Redis 7.0（可选，用于缓存）

**启动步骤：**
```bash
# 1. 克隆项目后，进入项目根目录
cd sky-employee-system

# 2. 创建数据库（使用MySQL客户端执行）
# 数据库名：sky_employee
# 字符集：utf8mb4

# 3. 修改数据库配置
# 打开 src/main/resources/application.yml
# 修改以下配置为你的本地数据库：
#   url: jdbc:mysql://localhost:3306/sky_employee?serverTimezone=Asia/Shanghai
#   username: root
#   password: 你的密码

# 4. 启动项目
# 方式一：IDEA 中右键 Application.java -> Run
# 方式二：命令行
mvn spring-boot:run

# 5. 验证启动成功
# 访问 http://localhost:8080/api/test
# 看到 {"code":200,"msg":"OK"} 表示成功
```

**后端端口：** 8080

### 2. 前端启动（Vue 3）

**环境要求：**
- Node.js 18+
- npm 9+

**启动步骤：**
```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖（第一次需要）
npm install

# 3. 启动开发服务器
npm run serve

# 4. 浏览器访问
# http://localhost:8081
```

**前端端口：** 8081

### 3. 登录测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin | 123456 | 超级管理员 |
| zhangsan | 123456 | 技术总监 |
| lisi | 123456 | 部门经理 |
| wangwu | 123456 | 普通员工 |

---

## 二、代码规范

### 1. 命名规范

**后端（Java）：**
```java
// 包名：全小写，用点分隔
com.whtc.employee.service.impl

// 类名：大驼峰（UpperCamelCase）
public class EmployeeServiceImpl { }

// 接口名：大驼峰，以 Service/Mapper/Controller 结尾
public interface EmployeeService { }

// 方法名：小驼峰（lowerCamelCase），动词开头
public Employee getById(Long id) { }
public void saveEmployee(EmployeeDTO dto) { }

// 变量名：小驼峰
private String userName;
private Long deptId;

// 常量名：全大写，下划线分隔
public static final int MAX_PAGE_SIZE = 100;
public static final String DEFAULT_PASSWORD = "123456";

// 数据库字段映射：下划线转驼峰
dept_id -> deptId
create_time -> createTime
```

**前端（Vue/JavaScript）：**
```javascript
// 组件名：大驼峰，多单词
EmployeeList.vue
UserProfile.vue

// 变量/方法：小驼峰
const userList = ref([])
const fetchData = () => { }

// 常量：全大写
const API_BASE_URL = '/api'
const PAGE_SIZE = 10

// 样式类名：小写，中划线分隔
<div class="employee-card user-info">
```

### 2. 代码格式

**后端：**
- 使用 4 个空格缩进（不要用 Tab）
- 大括号不换行
- 单行不超过 120 字符

```java
// 正确
public class UserService {
    public User getById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }
}

// 错误
public class UserService
{
    public User getById(Long id) {
        if(id==null)return null;
        return userMapper.selectById(id);
    }
}
```

**前端：**
- 使用 2 个空格缩进
- 模板中属性用双引号
- 使用单引号表示字符串

```vue
<!-- 正确 -->
<template>
  <div class="user-list">
    <el-input v-model="searchKey" placeholder="请输入关键字" />
  </div>
</template>

<script setup>
const message = 'Hello World'
</script>
```

### 3. 注释规范

**后端：**
```java
/**
 * 根据ID查询员工信息
 * @param id 员工ID
 * @return 员工信息，不存在返回null
 */
public EmployeeVO getById(Long id) {
    // 参数校验
    if (id == null) {
        return null;
    }
    return employeeMapper.selectById(id);
}
```

**前端：**
```javascript
/**
 * 获取员工列表
 * @param {Object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页数量
 * @returns {Promise<Object>} 分页数据
 */
const getEmployeeList = async (params) => {
  return await request.get('/employee/page', { params })
}
```

### 4. Git 提交规范

**提交信息格式：**
```
类型: 简短描述

详细描述（可选）
```

**类型说明：**
| 类型 | 含义 |
|------|------|
| feat | 新功能 |
| fix | 修复bug |
| docs | 文档更新 |
| style | 代码格式调整 |
| refactor | 重构代码 |
| test | 添加测试 |
| chore | 构建/工具变动 |

**示例：**
```bash
git commit -m "feat: 添加员工导出Excel功能"
git commit -m "fix: 修复登录验证码错误不提示的问题"
git commit -m "docs: 更新API接口文档"
```

---

## 三、代码提交流程

### 重要：提交前必须先 pull！

```bash
# 1. 保存你的修改
git add .

# 2. 提交到本地（可以先不push）
git commit -m "feat: 你的修改说明"

# 3. 【关键步骤】先拉取最新代码
git pull origin main

# 4. 如果有冲突，解决冲突（见下方说明）

# 5. 推送到远程
git push origin main
```

### 冲突解决

如果 `git pull` 后出现冲突：

```bash
# 1. 查看冲突文件
git status

# 2. 打开冲突文件，找到类似以下内容：
<<<<<<< HEAD
你的代码
=======
别人的代码
>>>>>>> origin/main

# 3. 手动修改，保留正确代码，删除标记行

# 4. 重新提交
git add .
git commit -m "fix: 解决合并冲突"
git push origin main
```

### 每日工作流程

```bash
# 早上开始工作时
git pull origin main

# ... 写代码 ...

# 中午/晚上提交
git add .
git commit -m "feat: xxx"
git pull origin main
# 解决冲突（如有）
git push origin main
```

---

## 四、项目结构说明

```
sky-employee-system/
├── src/main/java/com/whtc/employee/     # 后端代码
│   ├── annotation/                      # 自定义注解（如@DataScope）
│   ├── aspect/                          # AOP切面（数据权限等）
│   ├── config/                          # 配置类
│   ├── context/                         # 上下文（当前用户等）
│   ├── controller/                      # 控制器（接收请求）
│   ├── dto/                             # 数据传输对象（入参）
│   ├── entity/                          # 实体类（对应数据库表）
│   ├── exception/                       # 异常处理
│   ├── mapper/                          # MyBatis Mapper接口
│   ├── service/                         # 业务逻辑层
│   └── vo/                              # 视图对象（出参）
├── src/main/resources/
│   ├── db/                              # SQL脚本
│   ├── mapper/                          # MyBatis XML文件
│   └── application.yml                  # 配置文件
├── frontend/                            # 前端Vue项目
│   ├── src/
│   │   ├── api/                         # API请求封装
│   │   ├── components/                  # 公共组件
│   │   ├── router/                      # 路由配置
│   │   ├── views/                       # 页面组件
│   │   └── utils/                       # 工具函数
│   └── package.json
└── pom.xml                              # Maven配置
```

---

## 五、常见问题

**Q: 后端启动报错 "数据库连接失败"？**
A: 检查 application.yml 中的数据库配置，确保 MySQL 已启动且数据库已创建。

**Q: 前端启动报错 "Cannot find module"？**
A: 删除 node_modules 文件夹，重新运行 `npm install`。

**Q: 登录时接口返回 401？**
A: 检查后端是否正常启动，检查前端代理配置（vue.config.js）。

**Q: 提交代码时提示 "rejected"？**
A: 你一定忘了先 `git pull`！先 pull 解决冲突后再 push。

---

## 六、联系队长

遇到问题先自己尝试解决（百度/Google），30分钟解决不了再联系队长。

**沟通方式：**
- 技术问题：截图 + 错误信息
- 需求问题：先描述你的理解，再确认是否正确

---

祝大家开发顺利！🚀
