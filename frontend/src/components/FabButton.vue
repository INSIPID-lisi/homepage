<script setup lang="ts">
defineProps<{
  icon?: string
  iconSize?: number
}>()

defineEmits<{
  click: []
}>()
</script>

<template>
  <button class="fab" @click="$emit('click')">
    <el-icon :size="iconSize ?? 28"><component :is="icon || 'Plus'" /></el-icon>
  </button>
</template>

<style scoped>
.fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: rgba(42, 42, 42, 0.7);
  color: #d4d4d4;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 50;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.fab:hover {
  background: rgba(58, 58, 58, 0.8);
  border-color: rgba(255, 255, 255, 0.15);
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
}
</style>
