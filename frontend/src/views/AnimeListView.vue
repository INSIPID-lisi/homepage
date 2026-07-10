<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Anime, Result } from '@/api'
import { getAnimeList, createAnime, deleteAnime, uploadFile, checkAdmin } from '@/api'
import FabButton from '@/components/FabButton.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import { getImageUrl } from '@/utils'
import { ElMessageBox, ElMessage } from 'element-plus'

const router = useRouter()
const animeList = ref<Anime[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const isAdmin = ref(false)
const showCreate = ref(false)
const createForm = ref({ name: '', coverUrl: '', description: '' })
const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

onMounted(async () => {
  try {
    const [listRes, adminRes] = await Promise.all([
      getAnimeList(),
      checkAdmin()
    ])
    animeList.value = (listRes as Result<Anime[]>).data ?? []
    isAdmin.value = (adminRes as Result<boolean>).data
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
})

function goDetail(id: number) {
  router.push(`/anime/${id}`)
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
      createForm.value.coverUrl = filename
      ElMessage.success('封面上传成功')
    }
  } catch {
    ElMessage.error('封面上传失败')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function removeCover() {
  createForm.value.coverUrl = ''
}

async function handleCreate() {
  try {
    const data: Record<string, string> = { name: createForm.value.name }
    if (createForm.value.coverUrl) data.coverUrl = createForm.value.coverUrl
    if (createForm.value.description) data.description = createForm.value.description
    await createAnime(data)
    ElMessage.success('添加成功')
    showCreate.value = false
    createForm.value = { name: '', coverUrl: '', description: '' }
    const res = await getAnimeList()
    animeList.value = (res as Result<Anime[]>).data ?? []
  } catch {
    ElMessage.error('添加失败')
  }
}

async function handleDelete(id: number, e: Event) {
  e.stopPropagation()
  try {
    await ElMessageBox.confirm('确定删除这部番剧吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteAnime(id)
    ElMessage.success('删除成功')
    const res = await getAnimeList()
    animeList.value = (res as Result<Anime[]>).data ?? []
  } catch {
    // cancelled
  }
}


</script>

<template>
  <div class="anime-page">
    <template v-if="loading">
      <LoadingSpinner :loading="loading" />
    </template>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="!animeList.length" class="empty">
      <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" opacity="0.4">
        <rect x="2" y="2" width="20" height="20" rx="2.18" ry="2.18" />
        <line x1="7" y1="2" x2="7" y2="22" />
        <line x1="17" y1="2" x2="17" y2="22" />
        <line x1="2" y1="12" x2="22" y2="12" />
        <line x1="2" y1="7" x2="7" y2="7" />
        <line x1="2" y1="17" x2="7" y2="17" />
        <line x1="17" y1="7" x2="22" y2="7" />
        <line x1="17" y1="17" x2="22" y2="17" />
      </svg>
      <span>暂无番剧记录</span>
    </div>
    <div v-else class="anime-grid">
      <div
        v-for="anime in animeList"
        :key="anime.id"
        class="anime-card"
        @click="goDetail(anime.id)"
      >
        <div class="cover-wrapper">
          <img
            v-if="anime.coverUrl"
            :src="getImageUrl(anime.coverUrl)"
            :alt="anime.name"
            class="cover-img"
          />
          <div v-else class="cover-placeholder">
            <el-icon :size="40"><Picture /></el-icon>
          </div>
        </div>
        <div class="card-body">
          <h3 class="anime-name">{{ anime.name }}</h3>
          <p v-if="anime.latestReview" class="latest-review">{{ anime.latestReview }}</p>
        </div>
        <button
          v-if="isAdmin"
          class="delete-btn"
          @click="handleDelete(anime.id, $event)"
          title="删除"
        >
          <el-icon :size="16"><Delete /></el-icon>
        </button>
      </div>
    </div>

    <FabButton v-if="isAdmin" @click="showCreate = true" />

    <el-dialog v-model="showCreate" title="添加番剧" width="480px" top="15vh">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="番名">
          <el-input v-model="createForm.name" placeholder="输入番名" @keyup.enter="handleCreate" />
        </el-form-item>
        <el-form-item label="封面图">
          <div class="cover-upload">
            <div v-if="createForm.coverUrl" class="cover-preview">
              <img :src="getImageUrl(createForm.coverUrl)" alt="封面预览" />
              <button class="cover-remove" @click="removeCover" title="移除">
                <el-icon :size="16"><Close /></el-icon>
              </button>
            </div>
            <div v-else class="upload-placeholder" @click="onPickFile">
              <el-icon :size="24" v-if="!uploading"><Plus /></el-icon>
              <el-icon :size="24" v-else class="upload-spin"><Loading /></el-icon>
              <span>{{ uploading ? '上传中...' : '点击上传封面' }}</span>
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
        <el-form-item label="简介">
          <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="输入简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleCreate">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.anime-page {
  padding: 32px 40px;
  max-width: 960px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.anime-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.anime-card {
  background: rgba(26, 26, 26, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.anime-card:hover {
  border-color: rgba(255, 255, 255, 0.12);
  background: rgba(32, 32, 32, 0.6);
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.3);
}

.cover-wrapper {
  width: 100%;
  aspect-ratio: 3 / 4;
  overflow: hidden;
  background-color: rgba(17, 17, 17, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.anime-card:hover .cover-img {
  transform: scale(1.05);
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(68, 68, 68, 0.5);
}

.card-body {
  padding: 14px;
}

.anime-name {
  font-size: 14px;
  font-weight: 600;
  color: rgba(232, 232, 232, 0.95);
  margin-bottom: 6px;
  line-height: 1.4;
  letter-spacing: 0.03em;
}

.latest-review {
  font-size: 12px;
  color: rgba(160, 160, 160, 0.7);
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-weight: 400;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #ff4d4f;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.anime-card:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(255, 77, 79, 0.25);
}

.cover-upload {
  width: 100%;
}

.cover-preview {
  position: relative;
  width: 140px;
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(17, 17, 17, 0.5);
  aspect-ratio: 3 / 4;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-remove {
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

.cover-remove:hover {
  background-color: rgba(255, 77, 79, 0.8);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 140px;
  aspect-ratio: 3 / 4;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  border-radius: 8px;
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
