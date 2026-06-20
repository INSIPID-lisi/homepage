<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from '@/components/Sidebar.vue'

const collapsed = ref(false)

function toggleSidebar() {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <div class="layout">
    <Sidebar :collapsed="collapsed" @toggle="toggleSidebar" />
    <div class="content" :class="{ 'content--expanded': collapsed }">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  width: 100vw;
}

.content {
  flex: 1;
  margin-left: 220px;
  overflow-y: auto;
  transition: margin-left 0.3s ease;
}

.content--expanded {
  margin-left: 60px;
}
</style>
