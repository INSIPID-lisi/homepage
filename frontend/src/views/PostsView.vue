<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Post, Result } from '@/api'
import { getPosts, createPost, checkAdmin } from '@/api'
import FabButton from '@/components/FabButton.vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const posts = ref<Post[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const activeType = ref<string | undefined>(undefined)
const searchText = ref('')
const isAdmin = ref(false)
const showCreate = ref(false)
const createForm = ref({ title: '', content: '', type: 'formal', pinned: false })
let searchTimer: ReturnType<typeof setTimeout> | null = null

async function loadPosts() {
  error.value = null
  loading.value = true
  try {
    const res = await getPosts(activeType.value, searchText.value || undefined)
    posts.value = (res as Result<Post[]>).data ?? []
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const [postsRes, adminRes] = await Promise.all([
      getPosts(),
      checkAdmin()
    ])
    posts.value = (postsRes as Result<Post[]>).data ?? []
    isAdmin.value = (adminRes as Result<boolean>).data
  } catch (e) {
    error.value = '加载失败'
    console.error(e)
  } finally {
    loading.value = false
  }
})

function filterByType(type?: string) {
  activeType.value = type
  loadPosts()
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadPosts()
  }, 300)
}

function goDetail(id: number) {
  router.push(`/posts/${id}`)
}

async function handleCreate() {
  try {
    await createPost(createForm.value)
    ElMessage.success('发布成功')
    showCreate.value = false
    createForm.value = { title: '', content: '', type: 'formal', pinned: false }
    loadPosts()
  } catch {
    ElMessage.error('发布失败')
  }
}

function formatDate(dateStr: string) {
  return dateStr.slice(0, 10)
}
</script>

<template>
  <div class="posts-page">
    <div class="top-bar">
      <div class="type-tabs">
        <span
          class="type-tab"
          :class="{ 'type-tab--active': !activeType }"
          @click="filterByType(undefined)"
        >全部</span>
        <span
          class="type-tab"
          :class="{ 'type-tab--active': activeType === 'formal' }"
          @click="filterByType('formal')"
        >正规推文</span>
        <span
          class="type-tab"
          :class="{ 'type-tab--active': activeType === 'random' }"
          @click="filterByType('random')"
        >碎碎念</span>
      </div>
      <el-input
        v-model="searchText"
        class="search-input"
        placeholder="搜索帖子..."
        prefix-icon="Search"
        clearable
        @input="onSearchInput"
        @clear="loadPosts"
      />
    </div>

    <template v-if="loading">
      <LoadingSpinner :loading="loading" />
    </template>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="!posts.length" class="empty">
      <svg class="empty-icon" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" opacity="0.4">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
        <polyline points="14 2 14 8 20 8" />
        <line x1="9" y1="13" x2="15" y2="13" />
        <line x1="9" y1="17" x2="13" y2="17" />
      </svg>
      <span>暂无帖子</span>
    </div>
    <div v-else class="post-list">
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-card"
        @click="goDetail(post.id)"
      >
        <div class="post-card-header">
          <h3 class="post-title">{{ post.title }}</h3>
          <div class="post-meta">
            <span class="post-type" :class="'post-type--' + post.type">
              {{ post.type === 'formal' ? '推文' : '碎碎念' }}
            </span>
            <span v-if="post.pinned" class="post-pinned">置顶</span>
            <span class="post-date">{{ formatDate(post.createdAt) }}</span>
          </div>
        </div>
        <p class="post-excerpt" v-if="post.content">
          {{ post.content.length > 120 ? post.content.slice(0, 120) + '...' : post.content }}
        </p>
      </div>
    </div>

    <!-- Floating create button -->
    <FabButton v-if="isAdmin" @click="showCreate = true" />

    <!-- Create dialog -->
    <el-dialog v-model="showCreate" title="发布帖子" width="520px" top="15vh">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="标题">
          <el-input
            v-model="createForm.title"
            placeholder="输入标题"
            @keyup.enter="handleCreate"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="createForm.type">
            <el-radio value="formal">正规推文</el-radio>
            <el-radio value="random">碎碎念</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正文">
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="6"
            placeholder="输入正文"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="createForm.pinned">置顶</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.posts-page {
  padding: 32px 40px;
  max-width: 720px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 28px;
}

.type-tabs {
  display: flex;
  gap: 4px;
  background: rgba(26, 26, 26, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  padding: 3px;
}

.type-tab {
  padding: 6px 18px;
  font-size: 13px;
  color: rgba(160, 160, 160, 0.8);
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
  border-radius: 7px;
}

.type-tab:hover {
  color: #d4d4d4;
  background: rgba(255, 255, 255, 0.04);
}

.type-tab--active {
  color: #d4d4d4;
  background: rgba(42, 42, 42, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: rgba(26, 26, 26, 0.5) !important;
  border-radius: 10px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.post-card {
  background: rgba(26, 26, 26, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  padding: 22px 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.post-card:hover {
  border-color: rgba(255, 255, 255, 0.12);
  background: rgba(32, 32, 32, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.post-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(232, 232, 232, 0.95);
  line-height: 1.4;
  letter-spacing: 0.02em;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.post-type {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 999px;
  letter-spacing: 0.03em;
}

.post-type--formal {
  color: #7ba5f5;
  background: rgba(91, 141, 239, 0.1);
  border: 1px solid rgba(91, 141, 239, 0.15);
}

.post-type--random {
  color: #b8a3f5;
  background: rgba(166, 139, 239, 0.1);
  border: 1px solid rgba(166, 139, 239, 0.15);
}

.post-pinned {
  font-size: 11px;
  color: #f0c050;
  background: rgba(232, 168, 56, 0.1);
  border: 1px solid rgba(232, 168, 56, 0.15);
  padding: 2px 10px;
  border-radius: 999px;
}

.post-date {
  font-size: 12px;
  color: rgba(140, 140, 140, 0.8);
}

.post-excerpt {
  margin-top: 12px;
  font-size: 14px;
  color: rgba(170, 170, 170, 0.75);
  line-height: 1.7;
  font-weight: 400;
}
</style>
