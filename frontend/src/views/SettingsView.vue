<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

function handleSwitchAccount() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  ElMessage.info('请登录其他账号')
  router.push('/login')
}

function handleLogout() {
  ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
    // cancelled
  })
}
</script>

<template>
  <div class="settings-page">
    <h2 class="page-title">设置</h2>
    <div class="settings-list">
      <button class="settings-item" @click="handleSwitchAccount">
        <el-icon :size="20"><Switch /></el-icon>
        <span class="item-label">切换账号</span>
        <el-icon class="item-arrow" :size="16"><ArrowRight /></el-icon>
      </button>
      <button class="settings-item" @click="handleLogout">
        <el-icon :size="20"><SwitchButton /></el-icon>
        <span class="item-label">退出登录</span>
        <el-icon class="item-arrow" :size="16"><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #d4d4d4;
  margin-bottom: 24px;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border-radius: 10px;
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  color: #c8c8c8;
  font-size: 15px;
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s, border-color 0.2s;
}

.settings-item:hover {
  color: #d4d4d4;
  background-color: #222;
  border-color: #3a3a3a;
}

.settings-item:active {
  background-color: #252525;
}

.item-label {
  flex: 1;
  text-align: left;
}

.item-arrow {
  color: #555;
  transition: color 0.2s;
}

.settings-item:hover .item-arrow {
  color: #888;
}
</style>
