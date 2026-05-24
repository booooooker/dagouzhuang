<template>
  <div class="admin-page">
    <div v-if="!isLoggedIn" class="login-container">
      <div class="login-box">
        <h2>🔐 管理员登录</h2>
        <div class="login-form">
          <input 
            v-model="password" 
            type="password" 
            placeholder="请输入管理员密码"
            @keyup.enter="handleLogin"
            class="password-input"
          />
          <button @click="handleLogin" class="login-btn">登录</button>
        </div>
        <div v-if="loginError" class="error-message">❌ {{ loginError }}</div>
      </div>
    </div>

    <div v-else class="admin-panel">
      <div class="admin-header">
        <h1>🐕 吃狗庄 - 管理后台</h1>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>

      <div class="admin-stats">
        <div class="stat-card">
          <span class="stat-value">{{ users.length }}</span>
          <span class="stat-label">总用户数</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ totalScore }}</span>
          <span class="stat-label">总积分</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ totalGames }}</span>
          <span class="stat-label">总游戏场数</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ winRate }}%</span>
          <span class="stat-label">胜率</span>
        </div>
      </div>

      <div class="users-table-container">
        <div class="table-header">
          <h3>用户列表</h3>
          <div class="search-bar">
            <input v-model="searchQuery" placeholder="搜索昵称或IP..." class="search-input" />
          </div>
        </div>
        <div class="users-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>昵称</th>
                <th>IP地址</th>
                <th>积分</th>
                <th>最高分</th>
                <th>总场次</th>
                <th>胜率</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in filteredUsers" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.nickname }}</td>
                <td>{{ user.ipAddress }}</td>
                <td class="score-cell">{{ user.score }}</td>
                <td class="max-score-cell">{{ user.maxScore }}</td>
                <td>{{ user.totalGames }}</td>
                <td>{{ user.totalGames > 0 ? Math.round((user.wins / user.totalGames) * 100) : 0 }}%</td>
                <td>{{ formatDate(user.createdAt) }}</td>
                <td>
                  <button @click="deleteUser(user.id)" class="delete-btn">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="filteredUsers.length === 0" class="empty-table">
            暂无数据
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const isLoggedIn = ref(false)
const password = ref('')
const loginError = ref('')
const users = ref([])
const searchQuery = ref('')

const filteredUsers = computed(() => {
  if (!Array.isArray(users.value)) return []
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter(user => 
    user && user.nickname && user.ipAddress && (
      user.nickname.toLowerCase().includes(query) ||
      user.ipAddress.toLowerCase().includes(query)
    )
  )
})

const totalScore = computed(() => {
  if (!Array.isArray(users.value)) return 0
  return users.value.reduce((sum, user) => sum + (user.score || 0), 0)
})

const totalGames = computed(() => {
  if (!Array.isArray(users.value)) return 0
  return users.value.reduce((sum, user) => sum + (user.totalGames || 0), 0)
})

const winRate = computed(() => {
  if (!Array.isArray(users.value)) return 0
  const total = users.value.reduce((sum, user) => sum + (user.totalGames || 0), 0)
  const wins = users.value.reduce((sum, user) => sum + (user.wins || 0), 0)
  return total > 0 ? Math.round((wins / total) * 100) : 0
})

async function handleLogin() {
  if (!password.value) {
    loginError.value = '请输入密码'
    return
  }
  
  try {
    const response = await fetch('/api/admin/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: `password=${encodeURIComponent(password.value)}`
    })
    const data = await response.json()
    if (data.success) {
      isLoggedIn.value = true
      loginError.value = ''
      await loadUsers()
    } else {
      loginError.value = data.message
    }
  } catch (error) {
    loginError.value = '登录失败，请重试'
  }
}

function handleLogout() {
  isLoggedIn.value = false
  password.value = ''
  users.value = []
}

async function loadUsers() {
  try {
    const response = await fetch('/api/admin/users')
    users.value = await response.json()
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

async function deleteUser(userId) {
  if (!confirm('确定要删除该用户吗？此操作不可撤销。')) {
    return
  }
  
  try {
    const response = await fetch(`/api/admin/user/${userId}`, {
      method: 'DELETE'
    })
    const data = await response.json()
    if (data.success) {
      users.value = users.value.filter(user => user.id !== userId)
    }
  } catch (error) {
    console.error('删除用户失败:', error)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  if (isLoggedIn.value) {
    loadUsers()
  }
})
</script>

<style scoped>
.admin-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}

.login-box {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.login-box h2 {
  color: #ffd700;
  margin-bottom: 30px;
  font-size: 1.8em;
}

.password-input {
  width: 100%;
  padding: 15px 20px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 1.1em;
  margin-bottom: 20px;
  box-sizing: border-box;
}

.password-input::placeholder {
  color: #888;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  color: #1a1a2e;
  border: none;
  border-radius: 12px;
  font-size: 1.1em;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(255, 215, 0, 0.4);
}

.error-message {
  color: #ff6b6b;
  margin-top: 15px;
  font-weight: bold;
}

.admin-panel {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.admin-header h1 {
  color: #ffd700;
  font-size: 1.8em;
}

.logout-btn {
  padding: 10px 20px;
  background: rgba(255, 107, 107, 0.3);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.5);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 107, 107, 0.5);
}

.admin-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-value {
  font-size: 2em;
  font-weight: bold;
  color: #ffd700;
  display: block;
}

.stat-label {
  color: #888;
  font-size: 0.9em;
}

.users-table-container {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.table-header h3 {
  color: #fff;
  font-size: 1.2em;
  margin: 0;
}

.search-input {
  padding: 10px 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  width: 200px;
}

.search-input::placeholder {
  color: #888;
}

.users-table {
  overflow-x: auto;
}

.users-table table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.users-table th {
  color: #888;
  font-weight: bold;
  font-size: 0.9em;
}

.users-table td {
  color: #fff;
}

.score-cell {
  color: #00ff88;
  font-weight: bold;
}

.max-score-cell {
  color: #00d4ff;
  font-weight: bold;
}

.delete-btn {
  padding: 6px 12px;
  background: rgba(255, 107, 107, 0.3);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.5);
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85em;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: rgba(255, 107, 107, 0.5);
}

.empty-table {
  padding: 40px;
  text-align: center;
  color: #666;
}

@media (max-width: 768px) {
  .admin-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .users-table {
    font-size: 0.8em;
  }
  
  .users-table th,
  .users-table td {
    padding: 10px 5px;
  }
  
  .search-input {
    width: 100%;
    margin-top: 10px;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
