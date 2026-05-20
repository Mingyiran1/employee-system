import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Employee from '../views/Employee.vue'
import Supplier from '../views/Supplier.vue'
import Department from '../views/Department.vue'
import Approval from '../views/Approval.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
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
        path: 'employee',
        name: 'Employee',
        component: Employee,
        meta: { title: '员工管理' }
      },
      {
        path: 'supplier',
        name: 'Supplier',
        component: Supplier,
        meta: { title: '供应商管理' }
      },
      {
        path: 'department',
        name: 'Department',
        component: Department,
        meta: { title: '部门管理' }
      },
      {
        path: 'approval',
        name: 'Approval',
        component: Approval,
        meta: { title: '审批管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
