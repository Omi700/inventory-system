<script setup lang="ts">
import { ref } from 'vue'

// 表单输入框绑定的数据
const username = ref('')
const password = ref('')

// 页面状态
const loading = ref(false)
const errorMessage = ref('')
const currentUser = ref<any>(null)

// 登录方法：点击 Login 按钮时执行
async function handleLogin() {
  errorMessage.value = ''

  if (!username.value || !password.value) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true

  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },

      // 请求体字段必须和后端 LoginRequest 对应：
      // username -> 用户名
      // password -> 密码
      body: JSON.stringify({
        username: username.value,
        password: password.value,
      }),
    })

    const result = await response.json()

    if (result.code !== 0) {
      errorMessage.value = result.message || '登录失败'
      return
    }

    // 后端返回的 token 保存到 localStorage。
    // 后续访问 /api/auth/me、/api/products 时，都要把它放进 Authorization 请求头。
    localStorage.setItem('token', result.data.token)

    currentUser.value = result.data.user
  } catch (error) {
    errorMessage.value = '无法连接后端服务'
  } finally {
    loading.value = false
  }
}

// 测试 token 是否能访问 /api/auth/me
async function loadCurrentUser() {
  const token = localStorage.getItem('token')

  if (!token) {
    errorMessage.value = '请先登录'
    return
  }

  const response = await fetch('/api/auth/me', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })

  const result = await response.json()

  if (result.code === 0) {
    currentUser.value = result.data
  } else {
    errorMessage.value = result.message || '获取当前用户失败'
  }
}
</script>

<template>
  <div class="page">
    <header class="topbar">
      <div class="brand">
        <span class="brand-icon">📦</span>
        <span>Inventory System</span>
      </div>

      <nav class="nav">
        <a>Products</a>
        <a>Warehouses</a>
        <a>Inventory</a>
        <button class="signup-button">Sign Up</button>
      </nav>
    </header>

    <main class="login-area">
      <section class="login-card">
        <div class="mascot">📦</div>

        <h1>Log In</h1>
        <p class="subtitle">With your Inventory Account</p>

        <label>
          <span>Username</span>
          <input v-model="username" placeholder="Username" />
        </label>

        <label>
          <span>Password</span>
          <input v-model="password" type="password" placeholder="Password" />
        </label>

        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

        <button class="login-button" :disabled="loading" @click="handleLogin">
          {{ loading ? 'Logging in...' : 'Login' }}
        </button>

        <button class="secondary-button" @click="loadCurrentUser">
          Test /api/auth/me
        </button>

        <p v-if="currentUser" class="success">
          当前用户：{{ currentUser.nickname }} / {{ currentUser.role }}
        </p>
      </section>
    </main>
  </div>
</template>