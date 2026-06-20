<script setup lang="ts">
import { useRoute } from 'vue-router'

defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits<{
  toggle: []
}>()

const route = useRoute()

const navItems = [
  { label: '个人信息', icon: 'User', path: '/' },
  { label: '一些帖子', icon: 'Document', path: '/posts' },
  { label: '追番记录', icon: 'Film', path: '/anime' }
]

function isActive(path: string) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
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
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 220px;
  height: 100vh;
  background-color: #1a1a1a;
  border-right: 1px solid #2a2a2a;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  z-index: 100;
  overflow: hidden;
}

.sidebar--collapsed {
  width: 60px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid #2a2a2a;
  gap: 12px;
}

.toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  color: #888;
  flex-shrink: 0;
  transition: color 0.2s, background-color 0.2s;
}

.toggle-btn:hover {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

.logo-text {
  font-size: 14px;
  font-weight: 600;
  color: #d4d4d4;
  white-space: nowrap;
}

.nav {
  flex: 1;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  color: #888;
  transition: color 0.2s, background-color 0.2s;
  white-space: nowrap;
  text-decoration: none;
}

.nav-item:hover {
  color: #d4d4d4;
  background-color: #252525;
}

.nav-item--active {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

.nav-label {
  font-size: 14px;
}
</style>
