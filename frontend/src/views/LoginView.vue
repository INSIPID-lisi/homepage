<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { login, register, loginByCode, sendCode, getProfile } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const siteName = ref('')
const isRegister = ref(false)
const loginMode = ref<'password' | 'code'>('password')
const email = ref('')
const password = ref('')
const code = ref('')
const sendingCode = ref(false)
const countdown = ref(0)
const loading = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  if (localStorage.getItem('token')) {
    router.replace('/')
    return
  }
  try {
    const res = await getProfile() as any
    siteName.value = res.data?.name || '...'
  } catch {
    siteName.value = '...'
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function switchMode(mode: 'login' | 'register') {
  isRegister.value = mode === 'register'
  password.value = ''
  code.value = ''
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

async function handleSubmit() {
  if (loading.value) return
  if (!email.value) { ElMessage.warning('请输入邮箱'); return }

  if (isRegister.value) {
    if (!password.value || !code.value) { ElMessage.warning('请填写完整'); return }
  } else if (loginMode.value === 'password') {
    if (!password.value) { ElMessage.warning('请输入密码'); return }
  } else {
    if (!code.value) { ElMessage.warning('请输入验证码'); return }
  }

  loading.value = true
  try {
    let res: any
    if (isRegister.value) {
      res = await register({ email: email.value, password: password.value, code: code.value })
    } else if (loginMode.value === 'password') {
      res = await login({ email: email.value, password: password.value })
    } else {
      res = await loginByCode({ email: email.value, code: code.value })
    }
    const data = res.data as { token: string; role: string } | undefined
    if (data?.token) {
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', data.role)
      ElMessage.success('登录成功')
      router.push('/')
    }
  } catch (e: any) {
    const msg = e?.response?.data?.message || ''
    if (msg) {
      ElMessage.error(msg)
    } else {
      ElMessage.error(isRegister.value ? '注册失败' : '登录失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="welcome-title">欢迎来到 {{ siteName }} 的个人博客</h1>

      <div class="mode-tabs">
        <span
          class="mode-tab"
          :class="{ 'mode-tab--active': !isRegister }"
          @click="switchMode('login')"
        >登录</span>
        <span
          class="mode-tab"
          :class="{ 'mode-tab--active': isRegister }"
          @click="switchMode('register')"
        >注册</span>
      </div>

      <div class="form">
        <el-input v-model="email" placeholder="邮箱" class="form-input" />

        <el-input
          v-if="isRegister || loginMode === 'password'"
          v-model="password"
          type="password"
          placeholder="密码"
          class="form-input"
          show-password
          @keyup.enter="handleSubmit"
        />

        <div class="mode-toggle" v-if="!isRegister">
          <span
            :class="{ 'toggle-active': loginMode === 'password' }"
            @click="loginMode = 'password'"
          >密码登录</span>
          <span class="toggle-sep">|</span>
          <span
            :class="{ 'toggle-active': loginMode === 'code' }"
            @click="loginMode = 'code'"
          >验证码登录</span>
        </div>

        <template v-if="isRegister || loginMode === 'code'">
          <div class="code-row">
            <el-input v-model="code" placeholder="验证码" class="code-input" @keyup.enter="handleSubmit" />
            <button
              class="send-code-btn"
              :disabled="sendingCode || countdown > 0"
              @click="handleSendCode"
            >{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</button>
          </div>
        </template>

        <button class="submit-btn" :disabled="loading" @click="handleSubmit">
          {{ loading ? '处理中...' : isRegister ? '注册' : '登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1;
}

.login-card {
  width: 440px;
  padding: 48px 40px;
  background: rgba(20, 20, 20, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.3);
}

.welcome-title {
  font-size: 20px;
  font-weight: 500;
  color: rgba(224, 224, 224, 0.95);
  text-align: center;
  margin-bottom: 36px;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.mode-tabs {
  display: flex;
  gap: 4px;
  background: rgba(26, 26, 26, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  padding: 3px;
  margin-bottom: 28px;
}

.mode-tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  font-size: 14px;
  color: rgba(160, 160, 160, 0.8);
  cursor: pointer;
  transition: all 0.25s ease;
  user-select: none;
  border-radius: 7px;
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

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-input :deep(.el-input__wrapper) {
  background-color: rgba(26, 26, 26, 0.5) !important;
  border-radius: 10px;
}

.mode-toggle {
  display: flex;
  justify-content: center;
  gap: 0;
  font-size: 13px;
  color: rgba(102, 102, 102, 0.6);
}

.mode-toggle span {
  cursor: pointer;
  transition: color 0.2s;
}

.mode-toggle span:hover {
  color: #999;
}

.toggle-active {
  color: #5b8def !important;
}

.toggle-sep {
  margin: 0 12px;
  color: rgba(51, 51, 51, 0.5);
  cursor: default !important;
}

.code-row {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.code-input :deep(.el-input__wrapper) {
  background-color: rgba(26, 26, 26, 0.5) !important;
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

.submit-btn {
  width: 100%;
  padding: 12px 0;
  border-radius: 10px;
  background: linear-gradient(135deg, #5b8def, #4a7de0);
  color: #fff;
  font-size: 15px;
  font-weight: 400;
  cursor: pointer;
  transition: all 0.25s ease;
  margin-top: 4px;
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
</style>
