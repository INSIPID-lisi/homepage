<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  scrollContainer?: HTMLElement | null
}>()

const visible = ref(false)
let ticking = false

function getScrollTop(): number {
  if (props.scrollContainer) {
    return props.scrollContainer.scrollTop
  }
  return window.scrollY || document.documentElement.scrollTop
}

function onScroll() {
  if (!ticking) {
    requestAnimationFrame(() => {
      visible.value = getScrollTop() > 400
      ticking = false
    })
    ticking = true
  }
}

function scrollToTop() {
  const el = props.scrollContainer
  if (el) {
    el.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

let target: HTMLElement | Window = window

onMounted(() => {
  target = props.scrollContainer || window
  target.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => {
  target.removeEventListener('scroll', onScroll)
})
</script>

<template>
  <Transition name="back-top">
    <button v-if="visible" class="back-to-top" @click="scrollToTop" title="返回顶部">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 19V5M5 12l7-7 7 7" />
      </svg>
    </button>
  </Transition>
</template>

<style scoped>
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(42, 42, 42, 0.7);
  color: rgba(200, 200, 200, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  cursor: pointer;
  z-index: 60;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.back-to-top:hover {
  background: rgba(58, 58, 58, 0.8);
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
  color: #d4d4d4;
}

.back-top-enter-active,
.back-top-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.back-top-enter-from,
.back-top-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
</style>
