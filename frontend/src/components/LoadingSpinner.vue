<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

const props = defineProps<{
  loading: boolean
}>()

const emit = defineEmits<{
  done: []
}>()

const visible = ref(true)
const completing = ref(false)

watch(() => props.loading, (val) => {
  if (!val) {
    completing.value = true
    setTimeout(() => {
      visible.value = false
      emit('done')
    }, 600)
  }
})

onMounted(() => {
  if (!props.loading) {
    completing.value = true
    setTimeout(() => {
      visible.value = false
      emit('done')
    }, 600)
  }
})
</script>

<template>
  <Transition name="spinner-fade">
    <div v-if="visible" class="loading-spinner" :class="{ 'loading-spinner--done': completing }">
      <div class="spinner-ring" :class="{ 'spinner-ring--complete': completing }">
        <div class="spinner-center">
          <span class="loading-text">LOADING</span>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.loading-spinner--done {
  opacity: 0;
  transform: scale(0.8);
}

.spinner-ring {
  position: relative;
  width: 96px;
  height: 96px;
}

.spinner-ring::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(
    transparent 200deg,
    rgba(91, 141, 239, 0.9) 280deg,
    rgba(166, 139, 239, 0.6) 330deg,
    transparent 360deg
  );
  animation: orbit 1.8s linear infinite;
  -webkit-mask:
    radial-gradient(farthest-side, transparent calc(100% - 6px), #000 calc(100% - 5.5px));
  mask:
    radial-gradient(farthest-side, transparent calc(100% - 6px), #000 calc(100% - 5.5px));
}

.spinner-ring--complete::before {
  animation: orbit-complete 0.6s ease-out forwards;
}

.spinner-center {
  position: absolute;
  inset: 14px;
  border-radius: 50%;
  background: radial-gradient(circle at 40% 35%, rgba(91, 141, 239, 0.2), rgba(91, 141, 239, 0.05));
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-text {
  font-size: 11px;
  font-weight: 300;
  letter-spacing: 0.35em;
  color: rgba(180, 200, 255, 0.5);
  user-select: none;
}

@keyframes orbit {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes orbit-complete {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); opacity: 0; }
}

.spinner-fade-enter-active {
  transition: opacity 0.4s ease;
}

.spinner-fade-leave-active {
  transition: opacity 0.4s ease, transform 0.4s ease;
}

.spinner-fade-enter-from,
.spinner-fade-leave-to {
  opacity: 0;
}
</style>
