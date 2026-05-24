import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import InsuranceEmployee from '../views/InsuranceEmployee.vue'
import InsuredCompany from '../views/InsuredCompany.vue'
import PremiumConfig from '../views/PremiumConfig.vue'
import Supplier from '../views/Supplier.vue'
import Approval from '../views/Approval.vue'
import Message from '../views/Message.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '数据仪表盘' }
      },
      {
        path: 'insurance-employee',
        name: 'InsuranceEmployee',
        component: InsuranceEmployee,
        meta: { title: '保险员工管理' }
      },
      {
        path: 'insurance-company',
        name: 'InsuredCompany',
        component: InsuredCompany,
        meta: { title: '投保公司管理' }
      },
      {
        path: 'premium-config',
        name: 'PremiumConfig',
        component: PremiumConfig,
        meta: { title: '保费配置' }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: Supplier,
        meta: { title: '供应商管理' }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: Approval,
        meta: { title: '审批管理' }
      },
      {
        path: 'messages',
        name: 'Message',
        component: Message,
        meta: { title: '消息通知' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 增强版权限校验
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  const userStr = sessionStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null

  // 公开页面直接放行
  if (to.meta?.public) {
    // 已登录用户访问登录页，重定向到首页
    if (token && user && to.path === '/login') {
      next('/dashboard')
      return
    }
    next()
    return
  }

  // 未登录用户重定向到登录页
  if (!token || !user) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // 检查角色权限
  if (to.meta?.roles && to.meta.roles.length > 0) {
    const hasPermission = to.meta.roles.includes(user.roleId)
    if (!hasPermission) {
      ElMessage.error('没有权限访问该页面')
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
