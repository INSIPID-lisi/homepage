<script setup lang="ts">
import { ref, computed } from 'vue'
import NavDial from '@/components/NavDial.vue'
import SidebarPopup from '@/components/SidebarPopup.vue'
import BackToTop from '@/components/BackToTop.vue'
import { useKeyboardShortcuts } from '@/composables/useKeyboardShortcuts'

useKeyboardShortcuts()

const sidebarVisible = ref(false)
const sidebarPinned = ref(localStorage.getItem('sidebarPinned') === 'true')
const showNavDial = computed(() => !sidebarPinned.value && !sidebarVisible.value)
const contentRef = ref<HTMLElement | null>(null)

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
    <BackToTop :scroll-container="contentRef" />
    <div ref="contentRef" class="content" :class="{ 'content--with-sidebar': sidebarVisible }">
      <router-view v-slot="{ Component }">
        <Transition name="page" mode="out-in">
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
  position: relative;
  z-index: 1;
}

.content {
  height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
  transition: margin-left 0.4s cubic-bezier(0.34, 1.56, 0.64, 1),
              width 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.content--with-sidebar {
  margin-left: 220px;
  width: calc(100vw - 220px);
}
</style>
