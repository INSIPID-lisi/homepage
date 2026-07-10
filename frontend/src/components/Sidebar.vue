<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { navItems } from '@/navigation'

defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

const route = useRoute()
const router = useRouter()
const token = ref(localStorage.getItem('token'))

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  token.value = null
  router.push('/login')
}
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar--collapsed': collapsed }">
    <div class="sidebar-header">
      <button class="toggle-btn" @click="emit('toggle')" :title="collapsed ? '展开' : '收起'">
        <el-icon :size="20">
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
      </button>
      <span v-if="!collapsed" class="logo-text">个人博客</span>
    </div>

    <nav class="nav">
      <router-link
        v-for="item in navItems"
        :key="item.label"
        :to="item.path"
        class="nav-item"
        :class="{ 'nav-item--active': isActive(item.path) }"
        :title="collapsed ? item.label : undefined"
      >
        <el-icon :size="20"><component :is="item.icon" /></el-icon>
        <span v-if="!collapsed" class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <button
        v-if="token"
        class="nav-item logout-btn"
        @click="handleLogout"
        :title="collapsed ? '退出登录' : undefined"
      >
        <el-icon :size="20"><SwitchButton /></el-icon>
        <span v-if="!collapsed" class="nav-label">退出登录</span>
      </button>
      <router-link
        v-else
        to="/login"
        class="nav-item"
        :title="collapsed ? '登录' : undefined"
      >
        <el-icon :size="20"><User /></el-icon>
        <span v-if="!collapsed" class="nav-label">登录</span>
      </router-link>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 220px;
  height: 100vh;
  background: rgba(20, 20, 20, 0.9);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  transition: width 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 100;
  overflow: hidden;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.sidebar--collapsed {
  width: 60px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  gap: 12px;
}

.toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  color: rgba(136, 136, 136, 0.6);
  flex-shrink: 0;
  transition: all 0.2s;
}

.toggle-btn:hover {
  color: #d4d4d4;
  background: rgba(255, 255, 255, 0.06);
}

.logo-text {
  font-size: 14px;
  font-weight: 500;
  color: rgba(212, 212, 212, 0.8);
  white-space: nowrap;
  letter-spacing: 0.06em;
}

.nav {
  flex: 1;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-footer {
  padding: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  color: rgba(160, 160, 160, 0.8);
  transition: all 0.2s;
  white-space: nowrap;
  text-decoration: none;
}

.nav-item:hover {
  color: #d4d4d4;
  background: rgba(255, 255, 255, 0.05);
}

.nav-item--active {
  color: #d4d4d4;
  background: rgba(42, 42, 42, 0.6);
}

.nav-label {
  font-size: 14px;
  letter-spacing: 0.03em;
}

.logout-btn {
  width: 100%;
}

.logout-btn:hover {
  color: #ff6b6b;
}
</style>
