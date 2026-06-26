<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Post, Result } from '@/api'
import { getPost, updatePost, deletePost, checkAdmin } from '@/api'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const post = ref<Post | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const isAdmin = ref(false)
const showEdit = ref(false)
const editForm = ref({ title: '', content: '', type: 'formal', pinned: false })

onMounted(async () => {
  try {
    const id = Number(route.params.id)
    const [postRes, adminRes] = await Promise.all([
      getPost(id),
      checkAdmin()
    ])
    const p = (postRes as Result<Post>).data
    if (!p) { error.value = '帖子不存在'; return }
    post.value = p
    editForm.value = { title: p.title, content: p.content ?? '', type: p.type, pinned: p.pinned }
    isAdmin.value = (adminRes as Result<boolean>).data
  } catch (e) {
    error.value = '加载失败'
    console.error(e)
  } finally {
    loading.value = false
  }
})

function openEdit() {
  if (!post.value) return
  editForm.value = { title: post.value.title, content: post.value.content ?? '', type: post.value.type, pinned: post.value.pinned }
  showEdit.value = true
}

async function handleEdit() {
  if (!post.value) return
  try {
    await updatePost(post.value.id, editForm.value)
    const res = await getPost(post.value.id)
    post.value = (res as Result<Post>).data
    ElMessage.success('修改成功')
    showEdit.value = false
  } catch {
    ElMessage.error('修改失败')
  }
}

async function handleDelete() {
  if (!post.value) return
  try {
    await ElMessageBox.confirm('确定删除这篇帖子吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      confirmButtonClass: 'el-button--danger'
    })
    await deletePost(post.value.id)
    ElMessage.success('删除成功')
    router.push('/posts')
  } catch {
    // cancelled
  }
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN', { hour12: false })
}
</script>

<template>
  <div class="detail-page">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="post" class="detail-content">
      <div class="detail-topbar">
        <button class="back-btn" @click="router.push('/posts')">
          <el-icon :size="18"><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>

        <div v-if="isAdmin" class="settings">
          <el-dropdown trigger="click" @command="(cmd: string) => cmd === 'edit' ? openEdit() : handleDelete()">
            <button class="settings-btn">
              <el-icon :size="20"><MoreFilled /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon :size="16"><Edit /></el-icon> 修改
                </el-dropdown-item>
                <el-dropdown-item command="delete">
                  <el-icon :size="16"><Delete /></el-icon> 删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <h1 class="detail-title">{{ post.title }}</h1>

      <div class="detail-meta">
        <span class="post-type" :class="'post-type--' + post.type">
          {{ post.type === 'formal' ? '推文' : '碎碎念' }}
        </span>
        <span v-if="post.pinned" class="post-pinned">置顶</span>
        <span class="detail-date">创建于 {{ formatDate(post.createdAt) }}</span>
        <span class="detail-date" v-if="post.updatedAt !== post.createdAt">
          修改于 {{ formatDate(post.updatedAt) }}
        </span>
      </div>

      <div class="detail-body" v-if="post.content">
        {{ post.content }}
      </div>
    </div>

    <!-- Edit dialog -->
    <el-dialog v-model="showEdit" title="修改帖子" width="520px" top="15vh">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="标题">
          <el-input
            v-model="editForm.title"
            @keyup.enter="handleEdit"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="editForm.type">
            <el-radio value="formal">正规推文</el-radio>
            <el-radio value="random">碎碎念</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="editForm.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="editForm.pinned">置顶</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="handleEdit">保存</el-button>
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

.detail-title {
  font-size: 24px;
  font-weight: 700;
  color: #e8e8e8;
  line-height: 1.4;
  margin-bottom: 12px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.post-type {
  font-size: 12px;
  padding: 2px 10px;
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
  font-size: 12px;
  color: #e8a838;
  background-color: rgba(232, 168, 56, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.detail-date {
  font-size: 13px;
  color: #666;
}

.detail-body {
  font-size: 15px;
  color: #c8c8c8;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
