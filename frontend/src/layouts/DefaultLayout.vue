<script setup lang="ts">
import { ref, computed } from 'vue'
import NavDial from '@/components/NavDial.vue'
import SidebarPopup from '@/components/SidebarPopup.vue'
import { useKeyboardShortcuts } from '@/composables/useKeyboardShortcuts'

useKeyboardShortcuts()

const sidebarVisible = ref(false)
const sidebarPinned = ref(localStorage.getItem('sidebarPinned') === 'true')
const showNavDial = computed(() => !sidebarPinned.value && !sidebarVisible.value)

function onLongPress() {
  if (sidebarPinned.value) return
  sidebarVisible.value = !sidebarVisible.value
}

function closeSidebar() {
  if (!sidebarPinned.value) {
    sidebarVisible.value = false
  }
}

function onTogglePin(pinned: boolean) {
  sidebarPinned.value = pinned
  localStorage.setItem('sidebarPinned', String(pinned))
  if (pinned) {
    sidebarVisible.value = true
  } else {
    sidebarVisible.value = false
  }
}
</script>

<template>
  <div class="layout">
    <NavDial v-if="showNavDial" @long-press="onLongPress" />
    <SidebarPopup
      :visible="sidebarVisible"
      :pinned="sidebarPinned"
      @close="closeSidebar"
      @toggle-pin="onTogglePin"
    />
    <div class="content" :class="{ 'content--with-sidebar': sidebarVisible }">
      <router-view v-slot="{ Component }">
        <Transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
.layout {
  width: 100vw;
  height: 100vh;
}

.content {
  height: 100vh;
  overflow-y: auto;
  transition: margin-left 0.3s ease, width 0.3s ease;
}

.content--with-sidebar {
  margin-left: 220px;
  width: calc(100vw - 220px);
}

.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
}
</style>
