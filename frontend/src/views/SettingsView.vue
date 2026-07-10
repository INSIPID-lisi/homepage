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
  position: relative;
  z-index: 1;
}

.page-title {
  font-size: 20px;
  font-weight: 500;
  color: rgba(222, 222, 222, 0.95);
  margin-bottom: 32px;
  letter-spacing: 0.06em;
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
  padding: 16px 20px;
  border-radius: 14px;
  background: rgba(26, 26, 26, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  color: rgba(210, 210, 210, 0.85);
  font-size: 15px;
  cursor: pointer;
  transition: all 0.25s ease;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.settings-item:hover {
  color: #d4d4d4;
  background: rgba(34, 34, 34, 0.6);
  border-color: rgba(255, 255, 255, 0.12);
  transform: translateX(4px);
}

.settings-item:active {
  background: rgba(37, 37, 37, 0.7);
}

.item-label {
  flex: 1;
  text-align: left;
  letter-spacing: 0.02em;
}

.item-arrow {
  color: rgba(85, 85, 85, 0.5);
  transition: color 0.2s, transform 0.25s ease;
}

.item-arrow--open {
  transform: rotate(90deg);
  color: rgba(136, 136, 136, 0.6);
}

.settings-item:hover .item-arrow {
  color: rgba(136, 136, 136, 0.7);
}

/* ─── password panel ─── */
.password-panel {
  padding: 20px;
  background: rgba(26, 26, 26, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 14px;
  margin-top: -4px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
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
  gap: 4px;
  background: rgba(26, 26, 26, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  padding: 3px;
  margin-bottom: 16px;
}

.mode-tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 13px;
  color: rgba(136, 136, 136, 0.7);
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
  border-radius: 6px;
}

.mode-tab:hover {
  color: #d4d4d4;
  background: rgba(255, 255, 255, 0.04);
}

.mode-tab--active {
  color: #d4d4d4;
  background: rgba(42, 42, 42, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* ─── form ─── */
.form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form :deep(.el-input__wrapper) {
  background-color: rgba(15, 15, 15, 0.5) !important;
  border-radius: 10px;
}

.submit-btn {
  width: 100%;
  padding: 12px 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #5b8def, #4a7de0);
  color: #fff;
  font-size: 14px;
  font-weight: 400;
  cursor: pointer;
  transition: all 0.25s ease;
  margin-top: 4px;
  border: none;
  letter-spacing: 0.04em;
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #6b9df5, #5b8def);
  box-shadow: 0 4px 20px rgba(91, 141, 239, 0.3);
}

.submit-btn:disabled {
  opacity: 0.5;
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
  background-color: rgba(15, 15, 15, 0.5) !important;
  border-radius: 10px;
}

.send-code-btn {
  flex-shrink: 0;
  padding: 0 16px;
  border-radius: 10px;
  background: rgba(42, 42, 42, 0.8);
  color: #c8c8c8;
  font-size: 13px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: all 0.25s ease;
  white-space: nowrap;
}

.send-code-btn:hover:not(:disabled) {
  background: rgba(58, 58, 58, 0.9);
  color: #d4d4d4;
  border-color: rgba(255, 255, 255, 0.12);
}

.send-code-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
