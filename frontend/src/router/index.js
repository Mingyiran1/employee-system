import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import Employee from '../views/Employee.vue'
import Supplier from '../views/Supplier.vue'
import Department from '../views/Department.vue'

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
    redirect: '/employee',
    children: [
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
