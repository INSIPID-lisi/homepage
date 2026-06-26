<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Post, Result } from '@/api'
import { getPosts, createPost, checkAdmin } from '@/api'
import FabButton from '@/components/FabButton.vue'
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

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="!posts.length" class="empty">暂无帖子</div>
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
  padding: 24px 32px;
  max-width: 720px;
  margin: 0 auto;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.type-tabs {
  display: flex;
  gap: 0;
  border: 1px solid #2a2a2a;
  border-radius: 6px;
  overflow: hidden;
}

.type-tab {
  padding: 6px 18px;
  font-size: 13px;
  color: #888;
  background-color: #1a1a1a;
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s;
  border-right: 1px solid #2a2a2a;
  user-select: none;
}

.type-tab:last-child {
  border-right: none;
}

.type-tab:hover {
  color: #d4d4d4;
  background-color: #222;
}

.type-tab--active {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

.search-input {
  width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #1a1a1a;
  border-radius: 6px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  background-color: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 10px;
  padding: 20px;
  cursor: pointer;
  transition: border-color 0.2s, background-color 0.2s;
}

.post-card:hover {
  border-color: #444;
  background-color: #1e1e1e;
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
  color: #e0e0e0;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.post-type {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.post-type--formal {
  color: #5b8def;
  background-color: rgba(91, 141, 239, 0.1);
}

.post-type--random {
  color: #a68bef;
  background-color: rgba(166, 139, 239, 0.1);
}

.post-pinned {
  font-size: 11px;
  color: #e8a838;
  background-color: rgba(232, 168, 56, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.post-date {
  font-size: 12px;
  color: #666;
}

.post-excerpt {
  margin-top: 10px;
  font-size: 14px;
  color: #999;
  line-height: 1.6;
}

</style>
