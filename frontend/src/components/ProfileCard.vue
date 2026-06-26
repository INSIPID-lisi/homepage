<script setup lang="ts">
import type { UserProfile } from '@/api'
import { getImageUrl } from '@/utils'

defineProps<{
  profile: UserProfile | null
}>()
</script>

<template>
  <div class="profile-card" v-if="profile">
    <div class="avatar-wrapper">
      <img
        v-if="profile.avatar"
        :src="getImageUrl(profile.avatar)"
        :alt="profile.name"
        class="avatar"
      />
      <div v-else class="avatar-placeholder">
        {{ profile.name.charAt(0) }}
      </div>
    </div>
    <h1 class="name">{{ profile.name }}</h1>
  </div>
  <div class="empty" v-else>
    <p>Loading...</p>
  </div>
</template>

<style scoped>
.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 48px 24px 32px;
}

.avatar-wrapper {
  margin-bottom: 24px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #2a2a2a;
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: #2a2a2a;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #666;
  border: 3px solid #333;
}

.name {
  font-size: 28px;
  font-weight: 700;
  color: #e8e8e8;
  margin-bottom: 12px;
  letter-spacing: 0.5px;
}

.empty {
  padding: 48px;
  text-align: center;
  color: #666;
}
</style>
