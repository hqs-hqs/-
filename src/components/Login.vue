<template>
  <div class="login-container">
    <div class="login-bg"></div>
    <div class="login-box">
      <div class="lang-switch">
        <button :class="{active: lang==='zh'}" @click="setLang('zh')">中文</button>
        <span>|</span>
        <button :class="{active: lang==='en'}" @click="setLang('en')">English</button>
      </div>
      <h2 v-if="mode==='login'">{{ t('login.title') }}</h2>
      <h2 v-else-if="mode==='register'">{{ t('login.register') }}</h2>
      <h2 v-else>{{ t('login.forgot') }}</h2>
      <input v-model="username" :placeholder="t('login.username')" />
      <input v-if="mode!=='forgot'" v-model="password" type="password" :placeholder="t('login.password')" />
      <input v-if="mode==='forgot'" v-model="newPassword" type="password" :placeholder="t('login.newPassword')" />
      <div class="btn-group">
        <button v-if="mode==='login'" @click="login">{{ t('login.login') }}</button>
        <button v-if="mode==='register'" @click="register">{{ t('login.register') }}</button>
        <button v-if="mode==='forgot'" @click="resetPwd">{{ t('login.resetPwd') }}</button>
      </div>
      <div class="switch-group">
        <span v-if="mode==='login'">
          <a href="#" @click.prevent="mode='register'">{{ t('login.switchToRegister') }}</a> |
          <a href="#" @click.prevent="mode='forgot'">{{ t('login.switchToForgot') }}</a>
        </span>
        <span v-else-if="mode==='register'">
          <a href="#" @click.prevent="mode='login'">{{ t('login.switchToLogin') }}</a>
        </span>
        <span v-else>
          <a href="#" @click.prevent="mode='login'">{{ t('login.back') }}</a>
        </span>
      </div>
      <div class="clear-data-group">
        <button class="clear-data-btn" @click="clearAllData">{{ t('login.clearAllData') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { userAPI } from '@/api'

const lang = inject('lang')
const setLang = inject('setLang')
const t = inject('t')
const username = ref('')
const password = ref('')
const newPassword = ref('')
const mode = ref('login') // login/register/forgot
const router = useRouter()

// 检查是否使用API模式（通过环境变量或配置）
const useAPI = import.meta.env.VITE_USE_API === 'true' || localStorage.getItem('use_api') === 'true'

// 调试信息
console.log('API模式:', useAPI)
console.log('环境变量 VITE_USE_API:', import.meta.env.VITE_USE_API)
console.log('API基础URL:', import.meta.env.VITE_API_BASE_URL)

// 登录函数 - 支持API和localStorage两种模式
async function login() {
  if (!username.value.trim() || !password.value) {
    alert(t('login.alertEmpty'))
    return
  }
  
  if (useAPI) {
    try {
      const response = await userAPI.login(username.value, password.value)
      if (response.success) {
        // 保存token和用户信息
        localStorage.setItem('game_token', response.token)
        localStorage.setItem('game_username', username.value)
        localStorage.setItem('game_user_id', response.user.id)
        router.push('/hall')
      } else {
        alert(response.message || t('login.alertPwdWrong'))
      }
    } catch (error) {
      console.error('Login error:', error)
      alert(error.message || t('login.alertPwdWrong'))
    }
  } else {
    // 使用localStorage模式（原有逻辑）
    const user = localStorage.getItem('game_user_' + username.value)
    if (!user) {
      alert(t('login.alertNoUser'))
      return
    }
    const { pwd } = JSON.parse(user)
    if (pwd !== password.value) {
      alert(t('login.alertPwdWrong'))
      return
    }
    localStorage.setItem('game_username', username.value)
    router.push('/hall')
  }
}

// 注册函数 - 支持API和localStorage两种模式
async function register() {
  if (!username.value.trim() || !password.value) {
    alert(t('login.alertEmpty'))
    return
  }
  
  if (useAPI) {
    try {
      console.log('正在注册用户:', username.value)
      const response = await userAPI.register(username.value, password.value)
      console.log('注册响应:', response)
      if (response.success) {
        alert(t('login.alertRegisterOk'))
        mode.value = 'login'
        // 清空输入框
        username.value = ''
        password.value = ''
      } else {
        // 如果用户已存在，询问是否重置密码
        if (response.message && response.message.includes('已存在')) {
          if (confirm(t('login.alertUserExistsConfirm'))) {
            await resetPwd()
          }
        } else {
          alert(response.message || '注册失败')
        }
      }
    } catch (error) {
      console.error('Register error:', error)
      console.error('Error details:', error.response || error)
      // 检查是否是响应错误
      const errorMessage = error.response?.data?.message || error.message || '注册失败'
      if (errorMessage.includes('已存在')) {
        if (confirm(t('login.alertUserExistsConfirm'))) {
          await resetPwd()
        }
      } else {
        alert(errorMessage)
      }
    }
  } else {
    // 使用localStorage模式（原有逻辑）
    if (localStorage.getItem('game_user_' + username.value)) {
      if (confirm(t('login.alertUserExistsConfirm'))) {
        localStorage.setItem('game_user_' + username.value, JSON.stringify({ pwd: password.value }))
        alert(t('login.alertRegisterOk'))
        mode.value = 'login'
      }
      return
    }
    localStorage.setItem('game_user_' + username.value, JSON.stringify({ pwd: password.value }))
    alert(t('login.alertRegisterOk'))
    mode.value = 'login'
  }
}

// 重置密码函数
async function resetPwd() {
  if (!username.value.trim() || !newPassword.value) {
    alert(t('login.alertEmpty'))
    return
  }
  
  if (useAPI) {
    try {
      const response = await userAPI.resetPassword(username.value, newPassword.value)
      if (response.success) {
        alert(t('login.alertResetOk'))
        mode.value = 'login'
        newPassword.value = ''
      } else {
        alert(response.message || '重置失败')
      }
    } catch (error) {
      console.error('Reset password error:', error)
      alert(error.message || t('login.alertNoUser'))
    }
  } else {
    // 使用localStorage模式（原有逻辑）
    if (!localStorage.getItem('game_user_' + username.value)) {
      alert(t('login.alertNoUser'))
      return
    }
    localStorage.setItem('game_user_' + username.value, JSON.stringify({ pwd: newPassword.value }))
    alert(t('login.alertResetOk'))
    mode.value = 'login'
    newPassword.value = ''
  }
}

// 清除所有数据
function clearAllData() {
  if (confirm(t('login.clearAllDataConfirm'))) {
    // 清除所有以 game_ 开头的 localStorage 数据
    const keys = Object.keys(localStorage)
    keys.forEach(key => {
      if (key.startsWith('game_')) {
        localStorage.removeItem(key)
      }
    })
    alert(t('login.clearAllDataSuccess'))
    // 刷新页面
    location.reload()
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  left: 0; top: 0;
  overflow: hidden;
  z-index: 0;
}
.login-bg {
  position: fixed;
  left: 0; top: 0; width: 100vw; height: 100vh;
  z-index: 0;
  background: url('@/assets/dragonball.jpg') center center / cover no-repeat;
}
.login-bg::after {
  content: '';
  position: absolute;
  left: 0; top: 0; width: 100vw; height: 100vh;
  background: linear-gradient(135deg, #e0e7ff88 0%, #fdf6e388 100%);
  z-index: 1;
}
.login-box {
  background: rgba(255,255,255,0.85);
  padding: 48px 40px 32px 40px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 350px;
  z-index: 2;
  position: relative;
}
h2 {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 32px;
  color: #222;
}
input {
  margin: 14px 0 0 0;
  padding: 12px;
  font-size: 18px;
  width: 260px;
  border: 1px solid #bbb;
  border-radius: 6px;
  background: #f9f9f9;
  transition: border 0.2s;
}
input:focus {
  border: 1.5px solid #1976d2;
  outline: none;
  background: #fff;
}
.btn-group {
  display: flex;
  gap: 24px;
  margin-top: 24px;
  width: 100%;
  justify-content: center;
}
button {
  padding: 10px 36px;
  font-size: 18px;
  border-radius: 6px;
  border: 1px solid #1976d2;
  background: #1976d2;
  color: #fff;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s, color 0.2s;
}
button:hover {
  background: #145ea8;
}
.switch-group {
  margin-top: 28px;
  font-size: 16px;
}
a {
  color: #1976d2;
  text-decoration: underline;
  cursor: pointer;
}
.clear-data-group {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.clear-data-btn {
  padding: 8px 20px;
  font-size: 14px;
  border-radius: 6px;
  border: 1px solid #f44336;
  background: #f44336;
  color: #fff;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.2s, color 0.2s;
}
.clear-data-btn:hover {
  background: #d32f2f;
}
.lang-switch {
  position: absolute;
  top: 18px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 10;
}
.lang-switch button {
  background: none;
  border: none;
  color: #1976d2;
  font-size: 1.05rem;
  font-weight: bold;
  cursor: pointer;
  padding: 2px 8px;
  border-radius: 6px;
  transition: background 0.18s, color 0.18s;
}
.lang-switch button.active, .lang-switch button:hover {
  background: #e3f2fd;
  color: #145ea8;
}
</style> 