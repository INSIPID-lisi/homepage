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
      <div class="avatar-ring">
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
  padding: 64px 24px 24px;
}

.avatar-wrapper {
  margin-bottom: 28px;
  position: relative;
}

.avatar-ring {
  position: relative;
  width: 128px;
  height: 128px;
  border-radius: 50%;
  padding: 3px;
  background: linear-gradient(135deg, rgba(91, 141, 239, 0.3), rgba(166, 139, 239, 0.3), rgba(91, 141, 239, 0.1));
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.4s ease;
}

.avatar-ring:hover {
  transform: scale(1.05);
  box-shadow: 0 0 40px rgba(91, 141, 239, 0.2);
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  background: #111;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a1a2a, #111);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44px;
  color: rgba(212, 212, 212, 0.4);
  font-weight: 300;
}

.name {
  font-size: 32px;
  font-weight: 500;
  color: #e8e8e8;
  letter-spacing: 0.08em;
  margin-bottom: 8px;
}

.empty {
  padding: 48px;
  text-align: center;
  color: #666;
}
</style>
