<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { UserProfile, SocialLink, WeatherData, Result } from '@/api'
import { getProfile, getSocialLinks, checkAdmin, updateProfile, updateSocialLinks, uploadFile, getWeather } from '@/api'
import { ElMessage } from 'element-plus'
import ProfileCard from '@/components/ProfileCard.vue'
import SocialLinks from '@/components/SocialLinks.vue'
import WeatherCard from '@/components/WeatherCard.vue'
import FabButton from '@/components/FabButton.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import { getImageUrl } from '@/utils'

const profile = ref<UserProfile | null>(null)
const links = ref<SocialLink[]>([])
const weatherData = ref<WeatherData | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const isAdmin = ref(false)
const showEdit = ref(false)
const editForm = ref({
  name: '',
  avatar: '',
  bio: ''
})
const editLinks = ref<{ platform: string; url: string; icon: string }[]>([])
const saving = ref(false)
const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

onMounted(async () => {
  try {
    const [profileRes, linksRes, adminRes] = await Promise.all([
      getProfile(),
      getSocialLinks(),
      checkAdmin()
    ])
    profile.value = (profileRes as Result<UserProfile>).data
    links.value = (linksRes as Result<SocialLink[]>).data ?? []
    isAdmin.value = (adminRes as Result<boolean>).data
  } catch (e) {
    error.value = 'Failed to load data'
    console.error(e)
  } finally {
    loading.value = false
  }

  // weather — fire-and-forget, don't block the main content
  try {
    const res = await getWeather()
    weatherData.value = (res as Result<WeatherData>).data
  } catch (e) {
    console.warn('Weather fetch failed (backend may be down):', e)
  }
})

function openEdit() {
  editForm.value = {
    name: profile.value?.name ?? '',
    avatar: profile.value?.avatar ?? '',
    bio: profile.value?.bio ?? ''
  }
  editLinks.value = links.value.map(l => ({
    platform: l.platform,
    url: l.url,
    icon: l.icon ?? ''
  }))
  if (editLinks.value.length === 0) {
    editLinks.value.push({ platform: '', url: '', icon: '' })
  }
  showEdit.value = true
}

function addLink() {
  editLinks.value.push({ platform: '', url: '', icon: '' })
}

function removeLink(index: number) {
  editLinks.value.splice(index, 1)
}

function onPickFile() {
  fileInput.value?.click()
}

async function onFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadFile(file)
    const filename = (res as Result<string>).data
    if (filename) {
      editForm.value.avatar = filename
      ElMessage.success('头像上传成功')
    }
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function removeAvatar() {
  editForm.value.avatar = ''
}


async function handleSave() {
  saving.value = true
  try {
    const filteredLinks = editLinks.value.filter(l => l.platform && l.url)
    const linkData = filteredLinks.map(l => ({
      platform: l.platform,
      url: l.url,
      ...(l.icon ? { icon: l.icon } : {})
    }))

    await Promise.all([
      updateProfile({
        name: editForm.value.name,
        ...(editForm.value.avatar ? { avatar: editForm.value.avatar } : {}),
        ...(editForm.value.bio ? { bio: editForm.value.bio } : {})
      }),
      updateSocialLinks(linkData as { platform: string; url: string; icon?: string }[])
    ])

    ElMessage.success('保存成功')
    showEdit.value = false

    const [profileRes, linksRes] = await Promise.all([
      getProfile(),
      getSocialLinks()
    ])
    profile.value = (profileRes as Result<UserProfile>).data
    links.value = (linksRes as Result<SocialLink[]>).data ?? []
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="home">
    <WeatherCard :data="weatherData" />

    <template v-if="loading">
      <LoadingSpinner :loading="loading" />
    </template>
    <div v-else-if="error" class="error">{{ error }}</div>
    <template v-else>
      <ProfileCard :profile="profile" />

      <div class="divider" />

      <SocialLinks :links="links" v-if="links.length" />

      <div class="divider" />

      <p class="bio" v-if="profile?.bio">{{ profile.bio }}</p>
    </template>

    <FabButton v-if="isAdmin" icon="Edit" :icon-size="24" @click="openEdit" />

    <el-dialog v-model="showEdit" title="修改个人信息" width="520px" top="10vh">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" placeholder="输入名称" />
        </el-form-item>

        <el-form-item label="头像">
          <div class="avatar-upload">
            <div v-if="editForm.avatar" class="avatar-preview">
              <img :src="getImageUrl(editForm.avatar)" alt="头像预览" />
              <button class="avatar-remove" @click="removeAvatar" title="移除">
                <el-icon :size="16"><Close /></el-icon>
              </button>
            </div>
            <div v-else class="upload-placeholder" @click="onPickFile">
              <el-icon :size="24" v-if="!uploading"><Plus /></el-icon>
              <el-icon :size="24" v-else class="upload-spin"><Loading /></el-icon>
              <span>{{ uploading ? '上传中...' : '点击上传头像' }}</span>
            </div>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="onFileChange"
          />
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="4" placeholder="输入个人简介" />
        </el-form-item>

        <el-divider />

        <div class="links-header">
          <span class="links-title">社交链接</span>
          <el-button size="small" @click="addLink">添加链接</el-button>
        </div>
        <div v-for="(link, index) in editLinks" :key="index" class="link-row">
          <el-input v-model="link.platform" placeholder="平台 (如 GitHub)" class="link-input" />
          <el-input v-model="link.url" placeholder="链接地址" class="link-input" />
          <el-input v-model="link.icon" placeholder="图标标识 (可选)" class="link-input link-input--narrow" />
          <el-button size="small" type="danger" @click="removeLink(index)" :disabled="editLinks.length <= 1">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
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
  position: relative;
  z-index: 1;
}

.divider {
  width: 60px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  margin: 12px 0;
}

.bio {
  font-size: 14px;
  color: rgba(180, 180, 180, 0.8);
  text-align: center;
  line-height: 1.8;
  padding: 16px 24px 48px;
  max-width: 480px;
  letter-spacing: 0.04em;
  font-weight: 400;
}


.links-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.links-title {
  font-size: 14px;
  font-weight: 500;
  color: rgba(224, 224, 224, 0.7);
}

.link-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.link-input {
  flex: 1;
}

.link-input--narrow {
  flex: 0.6;
}

.avatar-upload {
  width: 100%;
}

.avatar-preview {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #111;
  border: 2px solid rgba(255, 255, 255, 0.06);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.avatar-remove:hover {
  background-color: rgba(255, 77, 79, 0.8);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  color: #666;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}

.upload-placeholder:hover {
  border-color: rgba(91, 141, 239, 0.5);
  color: #5b8def;
}

.upload-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
