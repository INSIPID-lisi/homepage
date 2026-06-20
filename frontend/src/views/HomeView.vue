<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { UserProfile, SocialLink, Result } from '@/api'
import { getProfile, getSocialLinks } from '@/api'
import ProfileCard from '@/components/ProfileCard.vue'
import SocialLinks from '@/components/SocialLinks.vue'

const profile = ref<UserProfile | null>(null)
const links = ref<SocialLink[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const [profileRes, linksRes] = await Promise.all([
      getProfile(),
      getSocialLinks()
    ])
    profile.value = (profileRes as Result<UserProfile>).data
    links.value = (linksRes as Result<SocialLink[]>).data ?? []
  } catch (e) {
    error.value = 'Failed to load data'
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home">
    <div v-if="loading" class="loading">Loading...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <template v-else>
      <ProfileCard :profile="profile" />

      <div class="divider" />

      <SocialLinks :links="links" v-if="links.length" />

      <div class="divider" />

      <p class="bio" v-if="profile?.bio">{{ profile.bio }}</p>
    </template>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  max-width: 640px;
  margin: 0 auto;
  padding: 24px;
}

.divider {
  width: 80%;
  max-width: 320px;
  height: 1px;
  background-color: #2a2a2a;
  margin: 8px 0;
}

.bio {
  font-size: 15px;
  color: #999;
  text-align: center;
  line-height: 1.8;
  padding: 16px 24px 48px;
  max-width: 480px;
}

.loading,
.error {
  text-align: center;
  color: #666;
  font-size: 15px;
}
</style>
