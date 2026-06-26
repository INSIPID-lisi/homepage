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
  background-color: #2a2a2a;
  color: #d4d4d4;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #3a3a3a;
  cursor: pointer;
  transition: background-color 0.2s;
  z-index: 50;
}

.fab:hover {
  background-color: #3a3a3a;
}
</style>
