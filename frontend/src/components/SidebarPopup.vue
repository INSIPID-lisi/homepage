<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { navItems } from '@/navigation'

const props = defineProps<{
  visible: boolean
  pinned: boolean
}>()

const emit = defineEmits<{
  close: []
  'toggle-pin': [value: boolean]
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

function handleClose() {
  if (!props.pinned) {
    emit('close')
  }
}

function onEscape(e: KeyboardEvent) {
  if (e.key === 'Escape') handleClose()
}

onMounted(() => document.addEventListener('keydown', onEscape))
onUnmounted(() => document.removeEventListener('keydown', onEscape))
</script>

<template>
  <Transition name="sidebar-slide">
    <div v-if="visible" class="sidebar-overlay" @click.self="handleClose">
      <aside class="sidebar-popup" :class="{ 'sidebar-popup--pinned': pinned }">
        <div class="sidebar-header">
          <span class="logo-text">个人博客</span>
          <div class="header-actions">
            <button
              class="pin-btn"
              :class="{ 'pin-btn--active': pinned }"
              :title="pinned ? '取消固定' : '固定侧边栏'"
              @click="emit('toggle-pin', !pinned)"
            >
              <el-icon :size="16"><Paperclip /></el-icon>
            </button>
            <button class="close-btn" @click="handleClose" :title="'关闭'">
              <el-icon :size="18"><Close /></el-icon>
            </button>
          </div>
        </div>

        <nav class="nav">
          <router-link
            v-for="item in navItems"
            :key="item.label"
            :to="item.path"
            class="nav-item"
            :class="{ 'nav-item--active': isActive(item.path) }"
            @click="!pinned && emit('close')"
          >
            <el-icon :size="20"><component :is="item.icon" /></el-icon>
            <span class="nav-label">{{ item.label }}</span>
          </router-link>
        </nav>

        <div class="sidebar-footer">
          <button
            v-if="token"
            class="nav-item logout-btn"
            @click="handleLogout"
          >
            <el-icon :size="20"><SwitchButton /></el-icon>
            <span class="nav-label">退出登录</span>
          </button>
          <router-link
            v-else
            to="/login"
            class="nav-item"
            @click="handleClose"
          >
            <el-icon :size="20"><User /></el-icon>
            <span class="nav-label">登录</span>
          </router-link>
        </div>
      </aside>
    </div>
  </Transition>
</template>

<style scoped>
.sidebar-overlay {
  position: fixed;
  inset: 0;
  z-index: 150;
  background: rgba(0, 0, 0, 0.3);
}

.sidebar-popup {
  position: fixed;
  top: 0;
  left: 0;
  width: 220px;
  height: 100vh;
  background-color: #1a1a1a;
  border-right: 1px solid #2a2a2a;
  display: flex;
  flex-direction: column;
  z-index: 151;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid #2a2a2a;
}

.logo-text {
  font-size: 14px;
  font-weight: 600;
  color: #d4d4d4;
  white-space: nowrap;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pin-btn,
.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  color: #888;
  transition: color 0.2s, background-color 0.2s;
}

.pin-btn:hover,
.close-btn:hover {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

.pin-btn--active {
  color: #5b8def;
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
  border-top: 1px solid #2a2a2a;
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

.logout-btn {
  width: 100%;
}

.logout-btn:hover {
  color: #ff6b6b;
}

/* Transition animations */
.sidebar-slide-enter-active,
.sidebar-slide-leave-active {
  transition: opacity 0.3s ease;
}

.sidebar-slide-enter-active .sidebar-popup,
.sidebar-slide-leave-active .sidebar-popup {
  transition: transform 0.3s ease;
}

.sidebar-slide-enter-from {
  opacity: 0;
}

.sidebar-slide-enter-from .sidebar-popup {
  transform: translateX(-100%);
}

.sidebar-slide-leave-to {
  opacity: 0;
}

.sidebar-slide-leave-to .sidebar-popup {
  transform: translateX(-100%);
}
</style>
