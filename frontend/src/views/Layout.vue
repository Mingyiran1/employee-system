<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">
        <el-icon size="28"><Management /></el-icon>
        <span>员工管理系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/employee">
          <el-icon><User /></el-icon>
          <span>员工管理</span>
        </el-menu-item>
        <el-menu-item index="/supplier">
          <el-icon><OfficeBuilding /></el-icon>
          <span>供应商管理</span>
        </el-menu-item>
        <el-menu-item index="/department">
          <el-icon><School /></el-icon>
          <span>部门管理</span>
        </el-menu-item>
        <el-menu-item index="/approval">
          <el-icon><DocumentChecked /></el-icon>
          <span>审批管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-right">
          <!-- 消息通知铃铛 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="message-badge" type="danger">
            <el-icon class="message-icon" @click="handleShowMessages"><Bell /></el-icon>
          </el-badge>

          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><UserFilled /></el-icon>
              {{ userInfo.realName || userInfo.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="messages">
                  <el-icon><Bell /></el-icon>
                  消息通知
                  <el-badge v-if="unreadCount > 0" :value="unreadCount" type="danger" />
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Management, User, OfficeBuilding, School, DocumentChecked, UserFilled, ArrowDown, Bell, SwitchButton } from '@element-plus/icons-vue'
import { getUnreadCount } from '@/api/message'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('user') || '{}'))
const unreadCount = ref(0)
let messageTimer = null

// 获取未读消息数
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    console.error('获取未读消息失败', error)
  }
}

// 显示消息列表（可以跳转到消息页面或显示下拉菜单）
const handleShowMessages = () => {
  // 这里可以显示消息下拉菜单或跳转到消息页面
  ElMessage.info(`您有 ${unreadCount.value} 条未读消息`)
  // 标记为已读后刷新
  if (unreadCount.value > 0) {
    // 可选：跳转到消息中心页面
    // router.push('/messages')
  }
}

// 下拉菜单处理
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  } else if (command === 'messages') {
    handleShowMessages()
  }
}

// 定期刷新未读消息数
onMounted(() => {
  fetchUnreadCount()
  // 每30秒刷新一次
  messageTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (messageTimer) {
    clearInterval(messageTimer)
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #1f2d3d;
}

.logo .el-icon {
  margin-right: 10px;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 消息铃铛样式 */
.message-badge {
  cursor: pointer;
}

.message-icon {
  font-size: 20px;
  color: #606266;
  cursor: pointer;
  transition: color 0.3s;
}

.message-icon:hover {
  color: #409EFF;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
