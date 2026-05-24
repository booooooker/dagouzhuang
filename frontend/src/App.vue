<template>
  <div class="app">
    <div class="background-effects">
      <div class="particle" v-for="n in 20" :key="n" :style="getParticleStyle(n)"></div>
    </div>

    <div class="top-bar">
      <div class="user-section">
        <div class="user-avatar" @click="startEditNickname">
          <span>👤</span>
        </div>
        <div class="user-info">
          <input v-if="editingNickname" v-model="nicknameInput" @blur="saveNickname" @keyup.enter="saveNickname" class="nickname-input" />
          <span v-else @click="startEditNickname" class="nickname">{{ currentUser?.nickname || '加载中...' }}</span>
          <span class="user-id">ID: {{ currentUser?.id || '---' }}</span>
        </div>
        <div class="edit-icon" @click="startEditNickname">✏️</div>
      </div>
      <div class="market-indicator" :class="isBullMarket ? 'bull' : 'bear'">
        <span>{{ isBullMarket ? '🐂' : '🐻' }}</span>
        <span class="market-text">{{ isBullMarket ? '牛市' : '熊市' }}</span>
      </div>
      <div class="score-badge">
        <span class="badge-label">总资产</span>
        <span class="badge-value">$ {{ currentUser?.score || 0 }}</span>
      </div>
    </div>

    <div class="main-content">
      <div class="game-container">
        <div class="game-header">
          <div class="game-stats">
            <div class="stat-item">
              <span class="stat-label">当前收益</span>
              <span class="stat-value" :class="currentGameScore >= 0 ? 'positive' : 'negative'">
                $ {{ currentGameScore >= 0 ? '+' : '' }}{{ currentGameScore }}
              </span>
            </div>
            <div class="stat-item">
              <span class="stat-label">持仓长度</span>
              <span class="stat-value">{{ snake.length }}股</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">游戏时间</span>
              <span class="stat-value">{{ formatTime(gameTime) }}</span>
            </div>
          </div>
          <div class="game-status">
            <span v-if="!gameRunning && !gameOver">等待建仓...</span>
            <span v-else-if="paused">⏸️ 暂停交易</span>
            <span v-else-if="gameRunning">📈 交易中</span>
            <span v-else>💸 已平仓</span>
          </div>
        </div>

        <div class="canvas-wrapper">
          <canvas ref="gameCanvas" :width="canvasSize" :height="canvasSize"></canvas>
          <div v-if="paused" class="pause-overlay">
            <span class="pause-icon">⏸️</span>
            <span class="pause-text">交易暂停</span>
          </div>
          <div v-if="!gameRunning" class="start-overlay">
            <button @click="startGame" class="btn-start-game">
              <span class="start-icon">{{ gameOver ? '🔄' : '🚀' }}</span>
              <span class="start-text">{{ gameOver ? '重新建仓' : '开始交易' }}</span>
            </button>
          </div>
        </div>

        <div class="mobile-controls" v-if="isMobile">
          <div class="control-row">
            <div class="control-btn" @touchstart.prevent="handleTouchMove(0, -1)">
              <span>↑</span>
            </div>
          </div>
          <div class="control-row">
            <div class="control-btn" @touchstart.prevent="handleTouchMove(-1, 0)">
              <span>←</span>
            </div>
            <div class="control-center">
              <span class="center-icon">🎮</span>
            </div>
            <div class="control-btn" @touchstart.prevent="handleTouchMove(1, 0)">
              <span>→</span>
            </div>
          </div>
          <div class="control-row">
            <div class="control-btn" @touchstart.prevent="handleTouchMove(0, 1)">
              <span>↓</span>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <button v-if="!gameRunning" @click="startGame" class="btn-primary">
            {{ gameOver ? '🔄 重新建仓' : '🚀 开始交易' }}
          </button>
          <button v-else @click="pauseGame" class="btn-secondary">
            {{ paused ? '▶️ 继续交易' : '⏸️ 暂停' }}
          </button>
          <button v-if="gameRunning" @click="endGameEarly" class="btn-danger">
            📉 割肉离场
          </button>
        </div>
      </div>
    </div>

    <div class="leaderboard-section">
      <div class="leaderboard-header">
        <h3 class="section-title">🏆 涨幅榜</h3>
        <div class="leaderboard-tabs">
          <button :class="{ active: leaderboardType === 'score' }" @click="leaderboardType = 'score'">
            💰 总积分
          </button>
          <button :class="{ active: leaderboardType === 'maxscore' }" @click="leaderboardType = 'maxscore'">
            ⭐ 最高分
          </button>
        </div>
      </div>
      <div class="leaderboard-content">
        <div class="leaderboard-item" v-for="(user, index) in leaderboard.slice(0, 10)" :key="user.id">
          <div class="rank-badge" :class="'rank-' + (index + 1)">
            {{ index + 1 }}
          </div>
          <div class="leaderboard-info">
            <span class="leaderboard-name">{{ user.nickname }}</span>
            <span class="leaderboard-detail">{{ user.totalGames }}场 | {{ user.wins }}胜</span>
          </div>
          <div class="leaderboard-score">
            $ {{ leaderboardType === 'score' ? user.score : user.maxScore }}
          </div>
        </div>
        <div v-if="leaderboard.length === 0" class="empty-leaderboard">
          <span>暂无数据</span>
        </div>
      </div>
    </div>

    <div v-if="showGameOver" class="game-over-modal">
      <div class="modal-backdrop" @click="closeGameOver"></div>
      <div class="modal-content">
        <div class="modal-icon">{{ isWin ? '🎉' : '💸' }}</div>
        <h2>{{ isWin ? '恭喜平仓成功!' : '割肉出局...' }}</h2>
        <div class="modal-stats">
          <div class="modal-stat">
            <span class="modal-stat-label">本局收益</span>
            <span class="modal-stat-value" :class="gameScore >= 0 ? 'positive' : 'negative'">
              $ {{ gameScore >= 0 ? '+' : '' }}{{ gameScore }}
            </span>
          </div>
          <div class="modal-stat">
            <span class="modal-stat-label">持仓最高</span>
            <span class="modal-stat-value">{{ maxSnakeLength }}股</span>
          </div>
          <div class="modal-stat">
            <span class="modal-stat-label">交易时长</span>
            <span class="modal-stat-value">{{ formatTime(gameTime) }}</span>
          </div>
        </div>
        <div class="modal-divider"></div>
        <div class="modal-total">
          <span class="total-label">总资产</span>
          <span class="total-value">$ {{ currentUser?.score || 0 }}</span>
        </div>
        <div class="modal-buttons">
          <button @click="closeGameOver" class="btn-primary">继续交易</button>
          <button @click="startGame" class="btn-secondary">再来一局</button>
        </div>
      </div>
    </div>

    <div class="footer">
      <span>🐕 吃狗庄，布哥出品</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { getCurrentUser, endGame, updateNickname, getLeaderboardByScore, getLeaderboardByMaxScore } from './api.js'

const currentUser = ref(null)
const editingNickname = ref(false)
const nicknameInput = ref('')
const leaderboard = ref([])
const leaderboardType = ref('score')
const gameRunning = ref(false)
const paused = ref(false)
const gameOver = ref(false)
const showGameOver = ref(false)
const isWin = ref(false)
const gameScore = ref(0)
const currentGameScore = ref(0)
const isBullMarket = ref(true)
const gameCanvas = ref(null)
const gameTime = ref(0)
const maxSnakeLength = ref(3)
const isMobile = ref(false)

let ctx = null
let snake = []
let food = { x: 0, y: 0 }
let doge = { x: 0, y: 0, active: false }
let obstacles = []
let direction = { x: 1, y: 0 }
let nextDirection = { x: 1, y: 0 }
let gameLoop = null
let foodTimer = null
let marketTimer = null
let timeTimer = null
const gridSize = 20
const canvasSize = computed(() => isMobile.value ? 320 : 400)

function getParticleStyle(n) {
  return {
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    animationDelay: `${n * 0.2}s`,
    width: `${Math.random() * 4 + 2}px`,
    height: `${Math.random() * 4 + 2}px`
  }
}

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

onMounted(async () => {
  isMobile.value = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  
  try {
    const response = await getCurrentUser()
    currentUser.value = response.data
    await loadLeaderboard()
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }

  if (gameCanvas.value) {
    ctx = gameCanvas.value.getContext('2d')
    initGame()
  }

  window.addEventListener('keydown', handleKeydown)
  window.addEventListener('resize', handleResize)
})

watch(leaderboardType, async () => {
  await loadLeaderboard()
})

async function loadLeaderboard() {
  try {
    const response = leaderboardType.value === 'score'
      ? await getLeaderboardByScore()
      : await getLeaderboardByMaxScore()
    leaderboard.value = response.data
  } catch (error) {
    console.error('获取排行榜失败:', error)
  }
}

function handleResize() {
  isMobile.value = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

function startEditNickname() {
  editingNickname.value = true
  nicknameInput.value = currentUser.value?.nickname || ''
}

async function saveNickname() {
  if (nicknameInput.value.trim() && currentUser.value) {
    try {
      const response = await updateNickname(currentUser.value.id, nicknameInput.value.trim())
      currentUser.value = response.data
    } catch (error) {
      console.error('更新昵称失败:', error)
    }
  }
  editingNickname.value = false
}

function initGame() {
  snake = [
    { x: Math.floor((canvasSize.value / gridSize) / 2) - 2, y: Math.floor((canvasSize.value / gridSize) / 2) },
    { x: Math.floor((canvasSize.value / gridSize) / 2) - 3, y: Math.floor((canvasSize.value / gridSize) / 2) },
    { x: Math.floor((canvasSize.value / gridSize) / 2) - 4, y: Math.floor((canvasSize.value / gridSize) / 2) }
  ]
  direction = { x: 1, y: 0 }
  nextDirection = { x: 1, y: 0 }
  obstacles = []
  generateObstacles()
  spawnFood()
  doge.active = false
  gameTime.value = 0
  maxSnakeLength.value = 3

  if (foodTimer) clearInterval(foodTimer)
  if (marketTimer) clearInterval(marketTimer)
  if (timeTimer) clearInterval(timeTimer)

  foodTimer = setInterval(() => {
    if (gameRunning.value && !paused.value) {
      isBullMarket.value = !isBullMarket.value
    }
  }, 12000)

  marketTimer = setInterval(() => {
    if (gameRunning.value && !paused.value) {
      addObstacle()
    }
  }, 15000)

  draw()
}

function generateObstacles() {
  const numObstacles = 5 + Math.floor(Math.random() * 5)
  for (let i = 0; i < numObstacles; i++) {
    addObstacle()
  }
}

function addObstacle() {
  let validPosition = false
  let attempts = 0
  while (!validPosition && attempts < 100) {
    const obs = {
      x: Math.floor(Math.random() * (canvasSize.value / gridSize)),
      y: Math.floor(Math.random() * (canvasSize.value / gridSize)),
      type: Math.random() > 0.5 ? 'bomb' : 'block'
    }
    
    validPosition = true
    for (let segment of snake) {
      if (obs.x === segment.x && obs.y === segment.y) {
        validPosition = false
        break
      }
    }
    
    if (validPosition) {
      for (let o of obstacles) {
        if (obs.x === o.x && obs.y === o.y) {
          validPosition = false
          break
        }
      }
    }
    
    if (validPosition) {
      if (obs.x === food.x && obs.y === food.y) {
        validPosition = false
      }
    }
    
    if (validPosition) {
      if (doge.active && obs.x === doge.x && obs.y === doge.y) {
        validPosition = false
      }
    }
    
    if (validPosition) {
      const minDist = 5
      for (let segment of snake) {
        if (Math.abs(obs.x - segment.x) < minDist && Math.abs(obs.y - segment.y) < minDist) {
          validPosition = false
          break
        }
      }
    }
    
    if (validPosition) {
      obstacles.push(obs)
    }
    
    attempts++
  }
}

function startGame() {
  initGame()
  gameRunning.value = true
  paused.value = false
  gameOver.value = false
  isWin.value = false
  currentGameScore.value = 0

  gameLoop = setInterval(update, isMobile.value ? 180 : 150)
  timeTimer = setInterval(() => {
    if (gameRunning.value && !paused.value) {
      gameTime.value++
    }
  }, 1000)
}

function pauseGame() {
  paused.value = !paused.value
}

function endGameEarly() {
  if (confirm('确定要割肉离场吗？当前收益将不会保存！')) {
    endCurrentGame(false)
  }
}

function update() {
  if (paused.value) return

  direction = nextDirection
  const head = { x: snake[0].x + direction.x, y: snake[0].y + direction.y }

  if (head.x < 0 || head.x >= canvasSize.value / gridSize ||
      head.y < 0 || head.y >= canvasSize.value / gridSize) {
    endCurrentGame(false)
    return
  }

  for (let segment of snake) {
    if (head.x === segment.x && head.y === segment.y) {
      endCurrentGame(false)
      return
    }
  }

  for (let obs of obstacles) {
    if (head.x === obs.x && head.y === obs.y) {
      endCurrentGame(false)
      return
    }
  }

  snake.unshift(head)

  if (head.x === food.x && head.y === food.y) {
    currentGameScore.value += 10
    if (snake.length > maxSnakeLength.value) {
      maxSnakeLength.value = snake.length
    }
    spawnFood()
  } else if (doge.active && head.x === doge.x && head.y === doge.y) {
    currentGameScore.value += 50
    doge.active = false
    spawnDoge()
  } else {
    snake.pop()
  }

  if (Math.random() < 0.015 && !doge.active) {
    spawnDoge()
  }

  draw()
}

function spawnFood() {
  let validPosition = false
  while (!validPosition) {
    food.x = Math.floor(Math.random() * (canvasSize.value / gridSize))
    food.y = Math.floor(Math.random() * (canvasSize.value / gridSize))
    validPosition = !snake.some(s => s.x === food.x && s.y === food.y) &&
                    !(doge.active && doge.x === food.x && doge.y === food.y) &&
                    !obstacles.some(o => o.x === food.x && o.y === food.y)
  }
}

function spawnDoge() {
  let validPosition = false
  while (!validPosition) {
    doge.x = Math.floor(Math.random() * (canvasSize.value / gridSize))
    doge.y = Math.floor(Math.random() * (canvasSize.value / gridSize))
    validPosition = !snake.some(s => s.x === doge.x && s.y === doge.y) &&
                    !(food.x === doge.x && food.y === doge.y) &&
                    !obstacles.some(o => o.x === doge.x && o.y === doge.y)
  }
  doge.active = true
  setTimeout(() => {
    if (doge.active) {
      doge.active = false
    }
  }, 6000)
}

function draw() {
  if (!ctx) return

  const bgGradient = ctx.createLinearGradient(0, 0, canvasSize.value, canvasSize.value)
  if (isBullMarket.value) {
    bgGradient.addColorStop(0, '#0a1628')
    bgGradient.addColorStop(1, '#0d1f3c')
  } else {
    bgGradient.addColorStop(0, '#1a0a0a')
    bgGradient.addColorStop(1, '#2d1515')
  }
  ctx.fillStyle = bgGradient
  ctx.fillRect(0, 0, canvasSize.value, canvasSize.value)

  ctx.strokeStyle = isBullMarket.value ? 'rgba(0, 212, 255, 0.1)' : 'rgba(255, 107, 107, 0.1)'
  ctx.lineWidth = 0.5
  for (let i = 0; i <= canvasSize.value; i += gridSize) {
    ctx.beginPath()
    ctx.moveTo(i, 0)
    ctx.lineTo(i, canvasSize.value)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(0, i)
    ctx.lineTo(canvasSize.value, i)
    ctx.stroke()
  }

  obstacles.forEach(obs => {
    if (obs.type === 'bomb') {
      ctx.shadowBlur = 15
      ctx.shadowColor = '#ff0000'
      ctx.fillStyle = '#ff4444'
      ctx.font = `${gridSize - 4}px Arial`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText('💣', obs.x * gridSize + gridSize / 2, obs.y * gridSize + gridSize / 2)
    } else {
      ctx.shadowBlur = 10
      ctx.shadowColor = '#666666'
      ctx.fillStyle = '#444444'
      ctx.beginPath()
      ctx.roundRect(obs.x * gridSize + 2, obs.y * gridSize + 2, gridSize - 4, gridSize - 4, 4)
      ctx.fill()
      ctx.fillStyle = '#666666'
      ctx.font = `${gridSize - 8}px Arial`
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.fillText('⚠️', obs.x * gridSize + gridSize / 2, obs.y * gridSize + gridSize / 2)
    }
  })

  const snakeGradient = ctx.createLinearGradient(0, 0, canvasSize.value, canvasSize.value)
  if (isBullMarket.value) {
    snakeGradient.addColorStop(0, '#00d4ff')
    snakeGradient.addColorStop(1, '#00ff88')
  } else {
    snakeGradient.addColorStop(0, '#ff6b6b')
    snakeGradient.addColorStop(1, '#ffd93d')
  }

  snake.forEach((segment, index) => {
    if (index === 0) {
      ctx.fillStyle = snakeGradient
      ctx.shadowBlur = 20
      ctx.shadowColor = isBullMarket.value ? '#00ffff' : '#ffaa00'
      ctx.beginPath()
      ctx.roundRect(segment.x * gridSize + 2, segment.y * gridSize + 2, gridSize - 4, gridSize - 4, 4)
      ctx.fill()
      
      ctx.fillStyle = '#fff'
      ctx.shadowBlur = 0
      ctx.beginPath()
      ctx.arc(segment.x * gridSize + 6, segment.y * gridSize + 6, 3, 0, Math.PI * 2)
      ctx.fill()
      ctx.beginPath()
      ctx.arc(segment.x * gridSize + gridSize - 6, segment.y * gridSize + 6, 3, 0, Math.PI * 2)
      ctx.fill()
    } else {
      ctx.fillStyle = isBullMarket.value ? 'rgba(0, 170, 255, 0.7)' : 'rgba(255, 149, 0, 0.7)'
      ctx.shadowBlur = 5
      ctx.shadowColor = isBullMarket.value ? '#00aaff' : '#ff9500'
      ctx.beginPath()
      ctx.roundRect(segment.x * gridSize + 3, segment.y * gridSize + 3, gridSize - 6, gridSize - 6, 3)
      ctx.fill()
    }
  })

  ctx.shadowBlur = 30
  ctx.shadowColor = '#ffd700'
  ctx.font = `${gridSize - 4}px Arial`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('🐕', food.x * gridSize + gridSize / 2, food.y * gridSize + gridSize / 2)

  if (doge.active) {
    ctx.shadowBlur = 35
    ctx.shadowColor = '#ff00ff'
    ctx.font = `${gridSize - 2}px Arial`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText('🐕', doge.x * gridSize + gridSize / 2, doge.y * gridSize + gridSize / 2)
  }

  ctx.shadowBlur = 0
}

async function endCurrentGame(isWinGame) {
  clearInterval(gameLoop)
  clearInterval(timeTimer)
  gameRunning.value = false
  gameOver.value = true
  isWin.value = isWinGame
  gameScore.value = currentGameScore.value

  if (currentUser.value && currentGameScore.value > 0) {
    try {
      const response = await endGame(currentUser.value.id, currentGameScore.value, isWinGame)
      if (response.data.success) {
        currentUser.value.score = response.data.totalScore
        currentUser.value.totalGames = response.data.totalGames
        currentUser.value.wins = response.data.wins
        currentUser.value.maxScore = response.data.maxScore
        await loadLeaderboard()
      }
    } catch (error) {
      console.error('保存游戏结果失败:', error)
    }
  }

  showGameOver.value = true
}

function closeGameOver() {
  showGameOver.value = false
}

function handleKeydown(e) {
  if (e.code === 'Space') {
    e.preventDefault()
    if (gameRunning.value) {
      pauseGame()
    } else if (!gameOver.value) {
      startGame()
    }
    return
  }

  if (!gameRunning.value || paused.value) return

  switch (e.code) {
    case 'ArrowUp':
    case 'KeyW':
      if (direction.y !== 1) nextDirection = { x: 0, y: -1 }
      break
    case 'ArrowDown':
    case 'KeyS':
      if (direction.y !== -1) nextDirection = { x: 0, y: 1 }
      break
    case 'ArrowLeft':
    case 'KeyA':
      if (direction.x !== 1) nextDirection = { x: -1, y: 0 }
      break
    case 'ArrowRight':
    case 'KeyD':
      if (direction.x !== -1) nextDirection = { x: 1, y: 0 }
      break
  }
}

function handleTouchMove(dx, dy) {
  if (!gameRunning.value || paused.value) return
  
  if (direction.x !== -dx) nextDirection = { x: dx, y: dy }
}

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
  window.removeEventListener('resize', handleResize)
  if (gameLoop) clearInterval(gameLoop)
  if (foodTimer) clearInterval(foodTimer)
  if (marketTimer) clearInterval(marketTimer)
  if (timeTimer) clearInterval(timeTimer)
})
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1628 0%, #1a1a2e 50%, #0f3460 100%);
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60px;
  display: flex;
  flex-direction: column;
}

.background-effects {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.particle {
  position: absolute;
  background: rgba(255, 215, 0, 0.3);
  border-radius: 50%;
  animation: float 15s infinite ease-in-out;
}

@keyframes float {
  0%, 100% { transform: translateY(0) translateX(0); opacity: 0.3; }
  50% { transform: translateY(-100px) translateX(20px); opacity: 0.8; }
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(15px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 10;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  border-radius: 50%;
  font-size: 1.3em;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.nickname-container {
  position: relative;
}

.nickname {
  font-size: 1.1em;
  font-weight: bold;
  color: #fff;
  cursor: pointer;
  transition: color 0.3s ease;
}

.nickname:hover {
  color: #ffd700;
}

.nickname-input {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid #ffd700;
  color: #fff;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 1em;
  width: 150px;
}

.user-id {
  font-size: 0.75em;
  color: #888;
}

.edit-icon {
  cursor: pointer;
  color: #888;
  transition: color 0.3s ease;
  font-size: 0.9em;
}

.edit-icon:hover {
  color: #ffd700;
}

.market-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
}

.market-indicator.bull {
  background: rgba(0, 255, 136, 0.2);
  color: #00ff88;
}

.market-indicator.bear {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
}

.market-text {
  font-size: 0.9em;
}

.score-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.2), rgba(255, 107, 107, 0.2));
  padding: 8px 16px;
  border-radius: 12px;
}

.badge-label {
  font-size: 0.7em;
  color: #888;
}

.badge-value {
  font-size: 1.2em;
  font-weight: bold;
  color: #ffd700;
}

.main-content {
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.game-container {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  padding: 20px;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  width: 100%;
  max-width: 500px;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.game-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

.stat-label {
  font-size: 0.7em;
  color: #888;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1em;
  font-weight: bold;
  color: #fff;
}

.stat-value.positive {
  color: #00ff88;
}

.stat-value.negative {
  color: #ff6b6b;
}

.game-status {
  padding: 8px 16px;
  background: rgba(0, 212, 255, 0.2);
  border-radius: 20px;
  color: #00d4ff;
  font-weight: bold;
  font-size: 0.9em;
}

.canvas-wrapper {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

canvas {
  border-radius: 12px;
  border: 2px solid rgba(255, 215, 0, 0.3);
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.1);
}

.pause-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 30px 40px;
  background: rgba(0, 0, 0, 0.8);
  border-radius: 16px;
  border: 2px solid #ffd700;
}

.pause-icon {
  font-size: 3em;
}

.pause-text {
  color: #ffd700;
  font-weight: bold;
  font-size: 1.2em;
}

.start-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 12px;
  backdrop-filter: blur(5px);
}

.btn-start-game {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 25px 45px;
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  border: none;
  border-radius: 20px;
  cursor: pointer;
  box-shadow: 0 10px 40px rgba(255, 215, 0, 0.4);
  animation: pulse 2s infinite;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.btn-start-game:hover {
  transform: scale(1.05);
  box-shadow: 0 15px 50px rgba(255, 215, 0, 0.6);
}

.btn-start-game:active {
  transform: scale(0.98);
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 10px 40px rgba(255, 215, 0, 0.4);
  }
  50% {
    box-shadow: 0 15px 50px rgba(255, 215, 0, 0.6);
  }
}

.start-icon {
  font-size: 3.5em;
}

.start-text {
  font-size: 1.4em;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.mobile-controls {
  display: none;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin: 20px 0;
}

@media (max-width: 600px) {
  .mobile-controls {
    display: flex;
  }
}

.control-row {
  display: flex;
  gap: 10px;
}

.control-btn {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 1.5em;
  transition: all 0.2s ease;
}

.control-btn:active {
  background: rgba(255, 215, 0, 0.3);
  transform: scale(0.95);
}

.control-center {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 215, 0, 0.1);
  border-radius: 15px;
  border: 2px solid rgba(255, 215, 0, 0.3);
}

.center-icon {
  font-size: 1.2em;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary, .btn-secondary, .btn-danger {
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 0.95em;
  font-weight: bold;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  color: #1a1a2e;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(255, 215, 0, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
}

.btn-danger {
  background: rgba(255, 107, 107, 0.3);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.5);
}

.btn-danger:hover {
  background: rgba(255, 107, 107, 0.5);
}

.leaderboard-section {
  padding: 0 20px 20px 20px;
  max-width: 600px;
  width: 100%;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.leaderboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.leaderboard-tabs {
  display: flex;
  gap: 10px;
}

.leaderboard-tabs button {
  padding: 6px 16px;
  border-radius: 20px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #888;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.85em;
}

.leaderboard-tabs button.active {
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  color: #1a1a2e;
  font-weight: bold;
}

.leaderboard-content {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  padding: 10px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 15px;
  border-radius: 10px;
  transition: background 0.3s ease;
}

.leaderboard-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.rank-badge {
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: bold;
  font-size: 1.1em;
  background: rgba(255, 255, 255, 0.1);
  color: #888;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffaa00);
  color: #1a1a2e;
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
  color: #1a1a2e;
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #b87333);
  color: #fff;
}

.leaderboard-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.leaderboard-name {
  font-weight: bold;
  color: #fff;
  font-size: 0.95em;
}

.leaderboard-detail {
  font-size: 0.75em;
  color: #666;
}

.leaderboard-score {
  font-size: 1.2em;
  font-weight: bold;
  color: #00d4ff;
}

.empty-leaderboard {
  padding: 30px;
  text-align: center;
  color: #666;
}

.game-over-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(5px);
}

.modal-content {
  position: relative;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border: 2px solid #ffd700;
  border-radius: 24px;
  padding: 40px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-icon {
  font-size: 4em;
  margin-bottom: 20px;
}

.modal-content h2 {
  font-size: 1.6em;
  color: #ffd700;
  margin-bottom: 25px;
}

.modal-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 25px;
}

.modal-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.modal-stat-label {
  font-size: 0.75em;
  color: #888;
  margin-bottom: 6px;
}

.modal-stat-value {
  font-size: 1.2em;
  font-weight: bold;
  color: #fff;
}

.modal-stat-value.positive {
  color: #00ff88;
}

.modal-stat-value.negative {
  color: #ff6b6b;
}

.modal-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 20px 0;
}

.modal-total {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 30px;
}

.total-label {
  font-size: 1em;
  color: #888;
}

.total-value {
  font-size: 2.2em;
  font-weight: bold;
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.modal-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 15px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 0.9em;
  color: #888;
  z-index: 10;
}

@media (max-width: 600px) {
  .top-bar {
    padding: 10px 15px;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .user-section {
    gap: 8px;
  }
  
  .user-avatar {
    width: 40px;
    height: 40px;
    font-size: 1.1em;
  }
  
  .nickname {
    font-size: 0.95em;
  }
  
  .nickname-input {
    width: 120px;
  }
  
  .market-indicator {
    padding: 5px 10px;
    font-size: 0.8em;
  }
  
  .score-badge {
    padding: 5px 10px;
  }
  
  .badge-value {
    font-size: 1em;
  }
  
  .game-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .game-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stat-item {
    min-width: 80px;
  }
  
  .footer {
    font-size: 0.85em;
  }
}
</style>
