<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updatePassword, sendCode } from '@/api'

const router = useRouter()

const showPasswordForm = ref(false)
const passwordMode = ref<'old' | 'code'>('old')

// Password change form
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const email = ref('')
const code = ref('')

const sendingCode = ref(false)
const countdown = ref(0)
const submitting = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function togglePasswordForm() {
  showPasswordForm.value = !showPasswordForm.value
}

async function handleSendCode() {
  if (!email.value) {
    ElMessage.warning('请输入邮箱')
    return
  }
  sendingCode.value = true
  try {
    await sendCode({ email: email.value })
    ElMessage.success('验证码已发送')
    countdown.value = 60
    if (timer) clearInterval(timer)
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && timer) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message
    ElMessage.error(msg || '发送失败')
  } finally {
    sendingCode.value = false
  }
}

async function handleChangePassword() {
  if (submitting.value) return

  if (!newPassword.value || !confirmPassword.value) {
    ElMessage.warning('请填写新密码和确认密码')
    return
  }
  if (newPassword.value.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  if (passwordMode.value === 'old') {
    if (!oldPassword.value) {
      ElMessage.warning('请输入旧密码')
      return
    }
  } else {
    if (!email.value) {
      ElMessage.warning('请输入邮箱')
      return
    }
    if (!code.value) {
      ElMessage.warning('请输入验证码')
      return
    }
  }

  submitting.value = true
  try {
    const payload: Record<string, string> = {
      newPassword: newPassword.value,
      confirmPassword: confirmPassword.value
    }
    if (passwordMode.value === 'old') {
      payload.oldPassword = oldPassword.value
    } else {
      payload.email = email.value
      payload.code = code.value
    }
    await updatePassword(payload)
    ElMessage.success('密码修改成功')
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
    email.value = ''
    code.value = ''
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message
    ElMessage.error(msg || '修改失败')
  } finally {
    submitting.value = false
  }
}

function handleSwitchAccount() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  ElMessage.info('请登录其他账号')
  router.push('/login')
}

function handleLogout() {
  ElMessageBox.confirm('确认退出登录？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
    // cancelled
  })
}
</script>

<template>
  <div class="settings-page">
    <h2 class="page-title">设置</h2>

    <div class="settings-list">
      <!-- Password Change -->
      <button class="settings-item" @click="togglePasswordForm">
        <el-icon :size="20"><Lock /></el-icon>
        <span class="item-label">修改密码</span>
        <el-icon
          class="item-arrow"
          :class="{ 'item-arrow--open': showPasswordForm }"
          :size="16"
        ><ArrowRight /></el-icon>
      </button>

      <Transition name="slide">
        <div v-show="showPasswordForm" class="password-panel">
          <div class="mode-tabs">
            <span
              class="mode-tab"
              :class="{ 'mode-tab--active': passwordMode === 'old' }"
              @click="passwordMode = 'old'"
            >旧密码验证</span>
            <span
              class="mode-tab"
              :class="{ 'mode-tab--active': passwordMode === 'code' }"
              @click="passwordMode = 'code'"
            >邮箱验证码</span>
          </div>

          <div class="form">
            <!-- Mode 1: Old password -->
            <el-input
              v-if="passwordMode === 'old'"
              v-model="oldPassword"
              type="password"
              placeholder="旧密码"
              show-password
            />

            <!-- Mode 2: Email + Code -->
            <template v-if="passwordMode === 'code'">
              <el-input v-model="email" placeholder="邮箱" />
              <div class="code-row">
                <el-input v-model="code" placeholder="验证码" class="code-input" />
                <button
                  class="send-code-btn"
                  :disabled="sendingCode || countdown > 0"
                  @click="handleSendCode"
                >{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</button>
              </div>
            </template>

            <!-- Common: new password + confirm -->
            <el-input
              v-model="newPassword"
              type="password"
              placeholder="新密码"
              show-password
            />
            <el-input
              v-model="confirmPassword"
              type="password"
              placeholder="确认新密码"
              show-password
              @keyup.enter="handleChangePassword"
            />

            <button
              class="submit-btn"
              :disabled="submitting"
              @click="handleChangePassword"
            >{{ submitting ? '处理中...' : '确认修改' }}</button>
          </div>
        </div>
      </Transition>

      <!-- Switch account -->
      <button class="settings-item" @click="handleSwitchAccount">
        <el-icon :size="20"><Switch /></el-icon>
        <span class="item-label">切换账号</span>
        <el-icon class="item-arrow" :size="16"><ArrowRight /></el-icon>
      </button>

      <!-- Logout -->
      <button class="settings-item" @click="handleLogout">
        <el-icon :size="20"><SwitchButton /></el-icon>
        <span class="item-label">退出登录</span>
        <el-icon class="item-arrow" :size="16"><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<style scoped>
.settings-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #d4d4d4;
  margin-bottom: 32px;
}

/* ─── settings list ─── */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.settings-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  border-radius: 10px;
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  color: #c8c8c8;
  font-size: 15px;
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s, border-color 0.2s;
}

.settings-item:hover {
  color: #d4d4d4;
  background-color: #222;
  border-color: #3a3a3a;
}

.settings-item:active {
  background-color: #252525;
}

.item-label {
  flex: 1;
  text-align: left;
}

.item-arrow {
  color: #555;
  transition: color 0.2s, transform 0.25s ease;
}

.item-arrow--open {
  transform: rotate(90deg);
  color: #888;
}

.settings-item:hover .item-arrow {
  color: #888;
}

/* ─── password panel ─── */
.password-panel {
  padding: 16px;
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
  border-radius: 10px;
  margin-top: -4px;
}

/* slide transition */
.slide-enter-active,
.slide-leave-active {
  transition: opacity 0.25s ease, max-height 0.35s ease, margin-top 0.35s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
  margin-top: 0;
  padding-top: 0;
  padding-bottom: 0;
  border-width: 0;
}

.slide-enter-to,
.slide-leave-from {
  opacity: 1;
  max-height: 500px;
}

/* ─── mode tabs ─── */
.mode-tabs {
  display: flex;
  border: 1px solid #2a2a2a;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 16px;
}

.mode-tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 13px;
  color: #888;
  background-color: #1a1a1a;
  cursor: pointer;
  transition: color 0.2s, background-color 0.2s;
  user-select: none;
}

.mode-tab:hover {
  color: #d4d4d4;
  background-color: #222;
}

.mode-tab--active {
  color: #d4d4d4;
  background-color: #2a2a2a;
}

/* ─── form ─── */
.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form :deep(.el-input__wrapper) {
  background-color: #0f0f0f;
  border-radius: 8px;
}

.submit-btn {
  width: 100%;
  padding: 12px 0;
  border-radius: 8px;
  background-color: #5b8def;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-top: 4px;
  border: none;
}

.submit-btn:hover:not(:disabled) {
  background-color: #4a7de0;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.code-row {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-input :deep(.el-input__wrapper) {
  background-color: #0f0f0f;
  border-radius: 8px;
}

.send-code-btn {
  flex-shrink: 0;
  padding: 0 16px;
  border-radius: 8px;
  background-color: #2a2a2a;
  color: #c8c8c8;
  font-size: 13px;
  border: 1px solid #3a3a3a;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
  white-space: nowrap;
}

.send-code-btn:hover:not(:disabled) {
  background-color: #3a3a3a;
  color: #d4d4d4;
}

.send-code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
