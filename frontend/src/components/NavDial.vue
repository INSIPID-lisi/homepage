<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { navItems } from '@/navigation'

const router = useRouter()
const route = useRoute()

const isExpanded = ref(false)
const rotation = ref(0)
const isDragging = ref(false)
const isLongPress = ref(false)
const isSnapping = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const longPressTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const expandTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const snapTimer = ref<ReturnType<typeof setTimeout> | null>(null)
const dialEl = ref<HTMLElement | null>(null)

/** 持续追踪上一帧鼠标相对圆心的角度，用于处理 atan2 ±180° 绕回 */
let lastAngle = 0
/** 拖拽松手后拦截浏览器派发的 click 事件，防止覆盖导航目标 */
let recentDragEnd = false

const emit = defineEmits<{
  'long-press': []
}>()

const RADIUS = 96
const itemAngles = [-75, -25, 25, 75]

const activeIndex = computed(() => {
  let minDist = Infinity
  let idx = 0
  for (let i = 0; i < itemAngles.length; i++) {
    const abs = ((itemAngles[i] + rotation.value) % 360 + 360) % 360
    const dist = Math.min(abs, 360 - abs)
    if (dist < minDist) {
      minDist = dist
      idx = i
    }
  }
  return idx
})

const itemPositions = computed(() => {
  return itemAngles.map(angle => {
    const rad = angle * Math.PI / 180
    return {
      x: Math.sin(rad) * RADIUS,
      y: -Math.cos(rad) * RADIUS
    }
  })
})

function getDialCenter() {
  if (!dialEl.value) return { x: 0, y: 0 }
  const rect = dialEl.value.getBoundingClientRect()
  return { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 }
}

function angleFromCenter(cx: number, cy: number, px: number, py: number) {
  return Math.atan2(py - cy, px - cx) * (180 / Math.PI)
}

function snapToItem(index: number) {
  const targetRotation = -itemAngles[index]
  rotation.value = targetRotation
  isSnapping.value = true
  if (snapTimer.value) clearTimeout(snapTimer.value)
  snapTimer.value = setTimeout(() => {
    isSnapping.value = false
  }, 500)
}

function startDrag(clientX: number, clientY: number) {
  isDragging.value = false
  isLongPress.value = false

  dragStartX.value = clientX
  dragStartY.value = clientY

  longPressTimer.value = setTimeout(() => {
    isLongPress.value = true
    isDragging.value = false
    emit('long-press')
  }, 600)
}

function onDrag(clientX: number, clientY: number) {
  if (isLongPress.value) return

  const dx = clientX - dragStartX.value
  const dy = clientY - dragStartY.value
  const dist = Math.sqrt(dx * dx + dy * dy)

  if (dist > 5) {
    if (!isDragging.value) {
      isDragging.value = true
      if (longPressTimer.value) {
        clearTimeout(longPressTimer.value)
        longPressTimer.value = null
      }
      const center = getDialCenter()
      lastAngle = angleFromCenter(center.x, center.y, clientX, clientY)
    }

    const center = getDialCenter()
    const currentAngle = angleFromCenter(center.x, center.y, clientX, clientY)
    let delta = currentAngle - lastAngle
    // 处理 atan2 跨 ±180° 边界时的角度绕回
    if (delta > 180) delta -= 360
    else if (delta < -180) delta += 360
    rotation.value += delta
    lastAngle = currentAngle
  }
}

function endDrag() {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }

  if (isDragging.value) {
    const targetIdx = activeIndex.value
    navigateTo(targetIdx)
    isDragging.value = false
    snapToItem(targetIdx)
    recentDragEnd = true
    requestAnimationFrame(() => { recentDragEnd = false })
  } else if (!isLongPress.value && isExpanded.value) {
    navigateTo(activeIndex.value)
  }

  isLongPress.value = false
}

function navigateTo(index: number) {
  const path = navItems[index].path
  if (route.path !== path) {
    router.push(path)
  }
}

function handleMouseEnter() {
  if (expandTimer.value) clearTimeout(expandTimer.value)
  isExpanded.value = true
}

function handleMouseLeave() {
  if (longPressTimer.value) {
    clearTimeout(longPressTimer.value)
    longPressTimer.value = null
  }
  expandTimer.value = setTimeout(() => {
    if (!isDragging.value) {
      isExpanded.value = false
    }
  }, 1200)
}

function onGlobalMouseMove(e: MouseEvent) {
  onDrag(e.clientX, e.clientY)
}

function onGlobalMouseUp() {
  endDrag()
  document.removeEventListener('mousemove', onGlobalMouseMove)
  document.removeEventListener('mouseup', onGlobalMouseUp)
}

function onDialMouseDown(e: MouseEvent) {
  startDrag(e.clientX, e.clientY)
  document.addEventListener('mousemove', onGlobalMouseMove)
  document.addEventListener('mouseup', onGlobalMouseUp)
}

function onGlobalTouchMove(e: TouchEvent) {
  const touch = e.touches[0]
  onDrag(touch.clientX, touch.clientY)
}

function onGlobalTouchEnd() {
  endDrag()
  document.removeEventListener('touchmove', onGlobalTouchMove)
  document.removeEventListener('touchend', onGlobalTouchEnd)
}

function onDialTouchStart(e: TouchEvent) {
  const touch = e.touches[0]
  startDrag(touch.clientX, touch.clientY)
  document.addEventListener('touchmove', onGlobalTouchMove, { passive: true })
  document.addEventListener('touchend', onGlobalTouchEnd)
}

function handleFanItemClick(index: number) {
  if (isDragging.value || isLongPress.value || recentDragEnd) return
  snapToItem(index)
  const path = navItems[index].path
  if (route.path !== path) {
    router.push(path)
  }
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onGlobalMouseMove)
  document.removeEventListener('mouseup', onGlobalMouseUp)
  document.removeEventListener('touchmove', onGlobalTouchMove)
  document.removeEventListener('touchend', onGlobalTouchEnd)
  if (snapTimer.value) clearTimeout(snapTimer.value)
})
</script>

<template>
  <div
    ref="dialEl"
    class="nav-dial"
    :class="{ 'nav-dial--expanded': isExpanded }"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @mousedown="onDialMouseDown"
    @touchstart.prevent="onDialTouchStart"
  >
    <!-- Pointer indicator -->
    <div class="dial-pointer" :class="{ 'dial-pointer--snap': isSnapping }" v-show="isExpanded">
      <svg width="16" height="12" viewBox="0 0 14 10">
        <polygon points="7,10 0,0 14,0" fill="currentColor" />
      </svg>
    </div>

    <!-- Rotating assembly: sectors -->
    <div
      class="dial-rotator"
      :class="{ 'dial-rotator--hidden': !isExpanded, 'dial-rotator--no-transition': isDragging }"
      :style="{ transform: 'rotate(' + rotation + 'deg)' }"
    >
      <div
        v-for="(item, index) in navItems"
        :key="item.label"
        class="fan-sector"
        :class="{
          'fan-sector--snapping': isSnapping && index === activeIndex
        }"
        :style="{ transform: 'rotate(' + itemAngles[index] + 'deg)' }"
        @click.stop="handleFanItemClick(index)"
      >
        <div
          class="fan-sector__stroke"
          :class="{ 'fan-sector__stroke--active': index === activeIndex }"
        ></div>
        <div
          class="fan-sector__bg"
          :class="{ 'fan-sector__bg--active': index === activeIndex }"
        ></div>
        <div class="fan-sector__content">
          <el-icon :size="16"><component :is="item.icon" /></el-icon>
          <span class="fan-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- Fixed center (does not rotate) -->
    <div class="dial-center">
      <svg class="dial-compass" width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10" opacity="0.4" />
        <polygon points="12,3 8.5,12 12,21 15.5,12" fill="#5b8def" stroke="none" />
        <circle cx="12" cy="12" r="2" fill="#d4d4d4" opacity="0.6" />
      </svg>
    </div>
  </div>
</template>

<style scoped>
.nav-dial {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 54px;
  height: 54px;
  z-index: 200;
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
}

.nav-dial:active {
  cursor: grabbing;
}

/* ─── pointer ─── */
.dial-pointer {
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  color: #5b8def;
  z-index: 3;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.nav-dial--expanded .dial-pointer {
  opacity: 1;
}

.dial-pointer--snap {
  animation: pointer-bounce 0.5s ease-out;
}

@keyframes pointer-bounce {
  0% { transform: translateX(-50%) translateY(0); }
  30% { transform: translateX(-50%) translateY(-4px); }
  60% { transform: translateX(-50%) translateY(2px); }
  100% { transform: translateX(-50%) translateY(0); }
}

/* ─── rotating assembly ─── */
.dial-rotator {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  z-index: 1;
  opacity: 0;
  transform: rotate(0deg) scale(0.6);
  transition: opacity 0.35s ease, transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  pointer-events: none;
}

.dial-rotator--hidden {
  opacity: 0;
  transform: rotate(0deg) scale(0.6);
  pointer-events: none;
}

.dial-rotator--no-transition {
  transition: none !important;
}
.dial-rotator--no-transition .fan-sector__stroke,
.dial-rotator--no-transition .fan-sector__bg {
  transition: none !important;
}

.nav-dial--expanded .dial-rotator {
  opacity: 1;
  transform: rotate(0deg) scale(1);
  pointer-events: auto;
}

/* ─── wedge sectors ─── */
.fan-sector {
  position: absolute;
  top: calc(50% - 35px);
  left: 0;
  width: 96px;
  height: 70px;
  transform-origin: left center;
  pointer-events: auto;
  cursor: pointer;
}

/* ─── stroke layer (white outline) ─── */
.fan-sector__stroke {
  position: absolute;
  inset: 0;
  clip-path: polygon(0 50%, 100% -17%, 100% 117%);
  background: rgba(212, 212, 212, 0.25);
  pointer-events: none;
  transition: background 0.25s;
}

.fan-sector__stroke--active {
  background: rgba(91, 141, 239, 0.35);
}

/* ─── solid background with 1.5px inset reveals the stroke ─── */
.fan-sector__bg {
  position: absolute;
  inset: 1.5px;
  clip-path: polygon(0 50%, 100% -17%, 100% 117%);
  background: #1a1a1a;
  pointer-events: none;
  transition: background 0.25s;
}

.fan-sector__bg--active {
  background: linear-gradient(135deg, rgba(91, 141, 239, 0.15), rgba(91, 141, 239, 0.05));
}

.fan-sector__content {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 6px;
  color: #888;
  pointer-events: none;
  white-space: nowrap;
}

.fan-sector__stroke--active ~ .fan-sector__content {
  color: #d4d4d4;
}

.fan-sector--snapping .fan-sector__bg {
  animation: sector-glow 0.5s ease-out;
}

.fan-sector--snapping .fan-sector__stroke {
  animation: sector-stroke-glow 0.5s ease-out;
}

@keyframes sector-glow {
  0% { filter: brightness(1); }
  35% { filter: brightness(1.4); }
  70% { filter: brightness(1.1); }
  100% { filter: brightness(1); }
}

@keyframes sector-stroke-glow {
  0% { background: rgba(91,141,239,0.35); }
  35% { background: rgba(91,141,239,0.7); }
  70% { background: rgba(91,141,239,0.4); }
  100% { background: rgba(91,141,239,0.35); }
}

.fan-label {
  font-size: 13px;
  font-weight: 500;
}

/* ─── center button ─── */
.dial-center {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(26, 26, 26, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1.5px solid rgba(212, 212, 212, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}

.nav-dial--expanded .dial-center {
  box-shadow: 0 0 24px rgba(91, 141, 239, 0.15);
  border-color: rgba(91, 141, 239, 0.3);
}

.dial-compass {
  color: #999;
  transition: transform 0.5s ease;
}

.nav-dial--expanded .dial-compass {
  transform: rotate(45deg);
}
</style>
