<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { AnimeDetail, AnimeReview, Result } from '@/api'
import { getAnimeDetail, createAnimeReview, updateAnimeReview, deleteAnimeReview, updateAnime, uploadFile, checkAdmin } from '@/api'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const anime = ref<AnimeDetail | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const isAdmin = ref(false)
const newReview = ref('')
const editingReviewId = ref<number | null>(null)
const editingReviewContent = ref('')
const showEditAnime = ref(false)
const editAnimeForm = ref({ name: '', coverUrl: '', description: '' })
const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    const [animeRes, adminRes] = await Promise.all([
      getAnimeDetail(id),
      checkAdmin()
    ])
    const a = (animeRes as Result<AnimeDetail>).data
    if (!a) { error.value = '番剧不存在'; return }
    anime.value = a
    editAnimeForm.value = { name: a.name, coverUrl: a.coverUrl ?? '', description: a.description ?? '' }
    isAdmin.value = (adminRes as Result<boolean>).data
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
})

function getCoverUrl(url: string | null) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return '/uploads/' + url
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { hour12: false })
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
      editAnimeForm.value.coverUrl = filename
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
  editAnimeForm.value.coverUrl = ''
}

async function handleAddReview() {
  if (!anime.value || !newReview.value.trim()) return
  try {
    await createAnimeReview(anime.value.id, { content: newReview.value.trim() })
    ElMessage.success('评价添加成功')
    newReview.value = ''
    const res = await getAnimeDetail(anime.value.id)
    anime.value = (res as Result<AnimeDetail>).data
  } catch {
    ElMessage.error('添加失败')
  }
}

function startEditReview(review: AnimeReview) {
  editingReviewId.value = review.id
  editingReviewContent.value = review.content
}

function cancelEditReview() {
  editingReviewId.value = null
  editingReviewContent.value = ''
}

async function handleEditReview() {
  if (!anime.value || !editingReviewContent.value.trim() || editingReviewId.value === null) return
  try {
    await updateAnimeReview(anime.value.id, editingReviewId.value, { content: editingReviewContent.value.trim() })
    ElMessage.success('修改成功')
    editingReviewId.value = null
    editingReviewContent.value = ''
    const res = await getAnimeDetail(anime.value.id)
    anime.value = (res as Result<AnimeDetail>).data
  } catch {
    ElMessage.error('修改失败')
  }
}

async function handleDeleteReview(reviewId: number) {
  if (!anime.value) return
  try {
    await ElMessageBox.confirm('确定删除这条评价吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteAnimeReview(anime.value.id, reviewId)
    ElMessage.success('删除成功')
    const res = await getAnimeDetail(anime.value.id)
    anime.value = (res as Result<AnimeDetail>).data
  } catch {
    // cancelled
  }
}

async function handleEditAnime() {
  if (!anime.value) return
  try {
    const data: Record<string, string> = {}
    if (editAnimeForm.value.name !== anime.value.name) data.name = editAnimeForm.value.name
    if (editAnimeForm.value.coverUrl !== (anime.value.coverUrl ?? '')) data.coverUrl = editAnimeForm.value.coverUrl
    if (editAnimeForm.value.description !== (anime.value.description ?? '')) data.description = editAnimeForm.value.description
    if (Object.keys(data).length === 0) { showEditAnime.value = false; return }
    await updateAnime(anime.value.id, data)
    ElMessage.success('修改成功')
    showEditAnime.value = false
    const res = await getAnimeDetail(anime.value.id)
    anime.value = (res as Result<AnimeDetail>).data
    editAnimeForm.value = { name: anime.value.name, coverUrl: anime.value.coverUrl ?? '', description: anime.value.description ?? '' }
  } catch {
    ElMessage.error('修改失败')
  }
}
</script>

<template>
  <div class="detail-page">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="anime" class="detail-content">
      <div class="detail-topbar">
        <button class="back-btn" @click="router.push('/anime')">
          <el-icon :size="18"><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>
        <div v-if="isAdmin" class="settings">
          <el-dropdown trigger="click" @command="() => showEditAnime = true">
            <button class="settings-btn">
              <el-icon :size="20"><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon :size="16"><Edit /></el-icon> 修改
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="anime-header">
        <div class="anime-cover">
          <img
            v-if="anime.coverUrl"
            :src="getCoverUrl(anime.coverUrl)"
            :alt="anime.name"
          />
          <div v-else class="cover-placeholder">
            <el-icon :size="48"><Picture /></el-icon>
          </div>
        </div>
        <div class="anime-info">
          <h1 class="anime-title">{{ anime.name }}</h1>
          <p v-if="anime.description" class="anime-desc">{{ anime.description }}</p>
        </div>
      </div>

      <div class="reviews-section">
        <h2 class="section-title">评价</h2>

        <div v-if="isAdmin" class="add-review">
          <el-input
            v-model="newReview"
            type="textarea"
            :rows="3"
            placeholder="添加评价..."
          />
          <el-button
            type="primary"
            class="add-review-btn"
            :disabled="!newReview.trim()"
            @click="handleAddReview"
          >添加</el-button>
        </div>

        <div v-if="!anime.reviews.length" class="no-reviews">暂无评价</div>
        <div v-else class="review-list">
          <div v-for="review in anime.reviews" :key="review.id" class="review-item">
            <div v-if="editingReviewId === review.id" class="review-edit">
              <el-input
                v-model="editingReviewContent"
                type="textarea"
                :rows="3"
              />
              <div class="review-edit-actions">
                <el-button size="small" @click="cancelEditReview">取消</el-button>
                <el-button size="small" type="primary" @click="handleEditReview">保存</el-button>
              </div>
            </div>
            <div v-else class="review-display">
              <div class="review-time">{{ formatDate(review.createdTime) }}</div>
              <div class="review-content">{{ review.content }}</div>
              <div v-if="isAdmin" class="review-actions">
                <button class="review-action-btn" @click="startEditReview(review)" title="修改">
                  <el-icon :size="14"><Edit /></el-icon>
                </button>
                <button class="review-action-btn review-action-btn--delete" @click="handleDeleteReview(review.id)" title="删除">
                  <el-icon :size="14"><Delete /></el-icon>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit anime dialog -->
    <el-dialog v-model="showEditAnime" title="修改番剧信息" width="480px" top="15vh">
      <el-form :model="editAnimeForm" label-position="top">
        <el-form-item label="番名">
          <el-input v-model="editAnimeForm.name" @keyup.enter="handleEditAnime" />
        </el-form-item>
        <el-form-item label="封面图">
          <div class="cover-upload">
            <div v-if="editAnimeForm.coverUrl" class="cover-preview">
              <img :src="getCoverUrl(editAnimeForm.coverUrl)" alt="封面预览" />
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
          <el-input v-model="editAnimeForm.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditAnime = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleEditAnime">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-page {
  padding: 32px 40px;
  max-width: 720px;
  margin: 0 auto;
}

.loading,
.error {
  text-align: center;
  color: #666;
  font-size: 15px;
  padding: 48px 0;
}

.detail-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #888;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: color 0.2s, background-color 0.2s;
}

.back-btn:hover {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

.settings-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: #888;
  transition: color 0.2s, background-color 0.2s;
}

.settings-btn:hover {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

.anime-header {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}

.anime-cover {
  width: 180px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background-color: #111;
  aspect-ratio: 3 / 4;
}

.anime-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #444;
}

.anime-info {
  flex: 1;
  min-width: 0;
}

.anime-title {
  font-size: 22px;
  font-weight: 700;
  color: #e8e8e8;
  margin-bottom: 12px;
}

.anime-desc {
  font-size: 14px;
  color: #999;
  line-height: 1.7;
  white-space: pre-wrap;
}

.reviews-section {
  margin-top: 8px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #d4d4d4;
  margin-bottom: 16px;
}

.add-review {
  margin-bottom: 20px;
}

.add-review-btn {
  margin-top: 8px;
}

.no-reviews {
  text-align: center;
  color: #666;
  font-size: 14px;
  padding: 24px 0;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  background-color: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 8px;
  padding: 16px;
}

.review-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.review-content {
  font-size: 14px;
  color: #c8c8c8;
  line-height: 1.7;
  white-space: pre-wrap;
}

.review-actions {
  display: flex;
  gap: 6px;
  margin-top: 8px;
  justify-content: flex-end;
}

.review-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 4px;
  color: #888;
  transition: color 0.2s, background-color 0.2s;
}

.review-action-btn:hover {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

.review-action-btn--delete:hover {
  color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.1);
}

.review-edit-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-end;
}

.cover-upload {
  width: 100%;
}

.cover-preview {
  position: relative;
  width: 140px;
  border-radius: 6px;
  overflow: hidden;
  background-color: #111;
  aspect-ratio: 3 / 4;
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
  border: 1px dashed #3a3a3a;
  border-radius: 6px;
  color: #666;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;
}

.upload-placeholder:hover {
  border-color: #5b8def;
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
