<template>
  <div class="gobang-bg"></div>
  <div class="gobang-outer">
    <div class="gobang-container">
      <!-- 右上角排行榜按钮 -->
      <button class="gobang-rank-btn" @click="showRankCard=true">{{ t('gobang.rankBtn') }}</button>
      <!-- 右上角排行榜卡片 -->
      <div class="gobang-rank-card" v-if="showRankCard">
        <div class="rank-header">
          <span>{{ t('gobang.rankHeader') }}</span>
          <select v-model="rankMode" class="rank-select">
            <option value="classic">{{ t('gobang.classicMode') }}</option>
            <option value="fast">{{ t('gobang.fastMode') }}</option>
          </select>
          <button class="rank-close" @click="showRankCard=false">{{ t('gobang.close') }}</button>
        </div>
        <div class="rank-list">
          <div v-if="rankList.length===0" class="rank-empty">{{ t('gobang.rankEmpty') }}</div>
          <div v-for="(item,i) in rankList" :key="i" class="rank-item">
            <span class="rank-index">{{i+1}}.</span>
            <span class="rank-name">{{item.name}}</span>
            <span class="rank-streak">{{item.streak}} {{ t('gobang.streak') }}</span>
          </div>
        </div>
      </div>
      <div v-if="!mode" class="mode-select-modal">
        <div class="mode-select-content">
          <div class="mode-title">{{ t('gobang.modeSelectTitle') }}</div>
          <button class="mode-btn" @click="selectMode('classic')">{{ t('gobang.classicMode') }}</button>
          <button class="mode-btn" @click="selectMode('fast')">{{ t('gobang.fastMode') }}</button>
          <button class="mode-btn" style="margin-top:32px;background:linear-gradient(90deg,#ffb347,#ff7043);color:#fff;" @click="exitGame">{{ t('gobang.exitGame') }}</button>
        </div>
      </div>
      <div v-else class="gobang-main">
        <!-- 新增：右上角退出按钮 -->
        <button class="gobang-exit-btn" @click="exitGame">{{ t('gobang.back') }}</button>
        <div class="side-panel">
          <div class="player-select-row">
            <label class="select-label">{{ t('gobang.blackPlayer') }}</label>
          </div>
          <select class="player-select" v-model="blackNameSelect" @change="onBlackChange">
            <option v-for="name in playerNames" :key="name" :value="name" :disabled="name === whiteNameSelect">{{ name }}</option>
          </select>
          <div class="player-select-row" style="margin-top:8px;">
            <label class="select-label">{{ t('gobang.whitePlayer') }}</label>
          </div>
          <select class="player-select" v-model="whiteNameSelect" @change="onWhiteChange">
            <option v-for="name in playerNames" :key="name" :value="name" :disabled="name === blackNameSelect">{{ name }}</option>
          </select>
          <div class="player-select-row" style="margin-top:8px;">
            <button class="add-player-btn" @click="showAddPlayer = true">{{ t('gobang.addPlayerBtn') }}</button>
          </div>
          <div class="info info-side">
            <div v-if="winner" class="info-text">{{ winnerMsg }}</div>
            <div v-else-if="mode==='fast' && !gameStarted" class="info-text">{{ t('gobang.readyToStart') }}</div>
            <div v-else class="info-text">{{ t('gobang.currentRound') }}: {{ currentPlayer === 1 ? blackName : whiteName }}</div>
            <button v-if="mode==='fast' && !gameStarted" class="start-game-btn" @click="startGame">{{ t('gobang.startGame') }}</button>
            <button v-if="gameStarted || mode==='classic'" @click="reset">{{ t('gobang.reset') }}</button>
            <button v-if="gameStarted || mode==='classic'" class="next-btn" @click="nextGame">{{ t('gobang.nextGame') }}</button>
            <button class="exit-btn" @click="mode=''">{{ t('gobang.back') }}</button>
          </div>
        </div>
        <div class="board-bg">
          <canvas ref="canvas" width="600" height="600" class="gobang-canvas"></canvas>
          <div v-if="mode==='fast' && gameStarted && !winner" class="timer-bar">
            <span>{{ t('gobang.timer') }}: {{ timer }} {{ t('gobang.seconds') }}</span>
          </div>
        </div>
        <div v-if="showAddPlayer" class="add-player-modal">
          <div class="add-player-content">
            <div class="add-player-title">{{ t('gobang.addPlayerTitle') }}</div>
            <input v-model="newPlayerName" :placeholder="t('gobang.playerNamePlaceholder')" maxlength="10" />
            <div class="add-player-actions">
              <button @click="addPlayer">{{ t('gobang.add') }}</button>
              <button @click="showAddPlayer = false">{{ t('gobang.cancel') }}</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, watchEffect, inject } from 'vue'
import { useRouter } from 'vue-router'
import { rankAPI } from '@/api'
const lang = inject('lang')
const setLang = inject('setLang')
const t = inject('t')
const router = useRouter()

// 检查是否使用API模式
const useAPI = import.meta.env.VITE_USE_API === 'true' || localStorage.getItem('use_api') === 'true'

const size = 15
const board = ref(Array.from({ length: size }, () => Array(size).fill(0))) // 0空 1黑 2白
const currentPlayer = ref(1) // 1黑 2白
const winner = ref(0)
const canvas = ref(null)
let ctx

// 玩家相关
const playerNames = ref(JSON.parse(localStorage.getItem('gobang_players') || '["玩家A","玩家B"]'))
const currentUser = ref(playerNames.value[0])
// 新增：黑白双方可选
const blackNameSelect = ref(playerNames.value[0])
const whiteNameSelect = ref(playerNames.value[1] || playerNames.value[0])
const blackName = ref(blackNameSelect.value)
const whiteName = ref(whiteNameSelect.value)

const showAddPlayer = ref(false)
const newPlayerName = ref('')
const expandLeaderboard = ref(false)
const mode = ref('') // ''=未选，classic=经典，fast=快棋
const timer = ref(20)
let timerInterval = null
const gameStarted = ref(false) // 快棋模式游戏是否已开始
const showRankCard = ref(false)
const rankMode = ref('classic')
const rankList = ref([])

function addPlayer() {
  const name = newPlayerName.value.trim()
  if (!name || playerNames.value.includes(name)) return
  playerNames.value.push(name)
  localStorage.setItem('gobang_players', JSON.stringify(playerNames.value))
  // 如果原来只有一个玩家，自动补充白方
  if (playerNames.value.length === 2) {
    whiteName.value = playerNames.value[1]
  }
  showAddPlayer.value = false
  newPlayerName.value = ''
}

function swapPlayers() {
  // 只交换黑白身份，不动playerNames顺序
  [blackName.value, whiteName.value] = [whiteName.value, blackName.value]
}

// 连胜记录
function getStreak(name) {
  return Number(localStorage.getItem('gobang_win_streak_' + name) || 0)
}
function setStreak(name, streak) {
  localStorage.setItem('gobang_win_streak_' + name, streak)
}
function addWin(name) {
  setStreak(name, getStreak(name) + 1)
}
function resetStreak(name) {
  setStreak(name, 0)
}

function onBlackChange() {
  if (blackNameSelect.value === whiteNameSelect.value) {
    // 自动切换白方为下一个不同的玩家
    const idx = playerNames.value.indexOf(blackNameSelect.value)
    whiteNameSelect.value = playerNames.value[(idx + 1) % playerNames.value.length]
  }
  blackName.value = blackNameSelect.value
  whiteName.value = whiteNameSelect.value
  currentPlayer.value = 1
  board.value = Array.from({ length: size }, () => Array(size).fill(0))
  winner.value = 0
  drawBoard()
}
function onWhiteChange() {
  if (whiteNameSelect.value === blackNameSelect.value) {
    // 自动切换黑方为下一个不同的玩家
    const idx = playerNames.value.indexOf(whiteNameSelect.value)
    blackNameSelect.value = playerNames.value[(idx + 1) % playerNames.value.length]
  }
  blackName.value = blackNameSelect.value
  whiteName.value = whiteNameSelect.value
  currentPlayer.value = 1
  board.value = Array.from({ length: size }, () => Array(size).fill(0))
  winner.value = 0
  drawBoard()
}

function drawBoard() {
  if (!ctx) return;
  ctx.clearRect(0, 0, 600, 600)
  ctx.save()
  ctx.fillStyle = '#f8f8e8'
  ctx.fillRect(0, 0, 600, 600)
  ctx.restore()
  drawGrid()
  drawPieces()
}
function drawGrid() {
  ctx.save()
  ctx.strokeStyle = '#333'
  ctx.lineWidth = 2
  for (let i = 0; i < size; i++) {
    ctx.beginPath()
    ctx.moveTo(20 + i * 40, 20)
    ctx.lineTo(20 + i * 40, 580)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(20, 20 + i * 40)
    ctx.lineTo(580, 20 + i * 40)
    ctx.stroke()
  }
  ctx.restore()
}
function drawPieces() {
  for (let i = 0; i < size; i++) {
    for (let j = 0; j < size; j++) {
      if (board.value[i][j] !== 0) {
        ctx.beginPath()
        ctx.arc(20 + j * 40, 20 + i * 40, 16, 0, Math.PI * 2)
        ctx.fillStyle = board.value[i][j] === 1 ? '#222' : '#fff'
        ctx.shadowColor = board.value[i][j] === 1 ? '#555' : '#aaa'
        ctx.shadowBlur = 8
        ctx.fill()
        ctx.shadowBlur = 0
      }
    }
  }
}
function handleClick(e) {
  if (winner.value) return
  if (mode.value === 'fast' && !gameStarted.value) return // 快棋模式未开始不能下棋
  const rect = canvas.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const j = Math.round((x - 20) / 40)
  const i = Math.round((y - 20) / 40)
  if (i < 0 || i >= size || j < 0 || j >= size) return
  if (board.value[i][j] !== 0) return
  board.value[i][j] = currentPlayer.value
  if (checkWin(i, j, currentPlayer.value)) {
    winner.value = currentPlayer.value
    stopTimer()
  } else {
    currentPlayer.value = 3 - currentPlayer.value
    if (mode.value === 'fast' && gameStarted.value) {
      startTimer()
    }
  }
  drawBoard()
}
function checkWin(x, y, player) {
  const dirs = [
    [1, 0], [0, 1], [1, 1], [1, -1]
  ]
  for (let [dx, dy] of dirs) {
    let count = 1
    for (let d = 1; d < 5; d++) {
      const nx = x + dx * d, ny = y + dy * d
      if (nx < 0 || nx >= size || ny < 0 || ny >= size) break
      if (board.value[nx][ny] === player) count++
      else break
    }
    for (let d = 1; d < 5; d++) {
      const nx = x - dx * d, ny = y - dy * d
      if (nx < 0 || nx >= size || ny < 0 || ny >= size) break
      if (board.value[nx][ny] === player) count++
      else break
    }
    if (count >= 5) return true
  }
  return false
}
function reset() {
  board.value = Array.from({ length: size }, () => Array(size).fill(0))
  winner.value = 0
  currentPlayer.value = 1
  gameStarted.value = false
  timer.value = 20
  stopTimer()
  drawBoard()
}
function nextGame() {
  board.value = Array.from({ length: size }, () => Array(size).fill(0))
  winner.value = 0
  currentPlayer.value = 1
  gameStarted.value = false
  timer.value = 20
  swapPlayers() // 交换先后手
  stopTimer()
  drawBoard()
}
function exitGame() {
  router.push('/hall')
}
const winnerMsg = computed(() => {
  if (!winner.value) return ''
  return t('gobang.winner') + '：' + (winner.value === 1 ? blackName.value : whiteName.value)
})
watch(winner, (val) => {
  if (val) {
    stopTimer()
    if (val === 1) {
      addWin(blackName.value)
      resetStreak(whiteName.value)
      updateRankList(mode.value, blackName.value, getStreak(blackName.value))
    } else {
      addWin(whiteName.value)
      resetStreak(blackName.value)
      updateRankList(mode.value, whiteName.value, getStreak(whiteName.value))
    }
  }
})
watchEffect(() => {
  if (canvas.value) {
    ctx = canvas.value.getContext('2d')
    drawBoard()
    canvas.value.addEventListener('click', handleClick)
    // 只绑定一次事件
  }
})
watch(mode, (val) => {
  if (val && ctx) {
    gameStarted.value = false
    reset()
  }
})

function selectMode(m) {
  mode.value = m
}

function startGame() {
  gameStarted.value = true
  startTimer()
}

function startTimer() {
  clearInterval(timerInterval)
  timer.value = 20
  timerInterval = setInterval(() => {
    timer.value--
    if (timer.value <= 0) {
      clearInterval(timerInterval)
      // 超时自动判负
      winner.value = currentPlayer.value === 1 ? 2 : 1
    }
  }, 1000)
}
function stopTimer() {
  clearInterval(timerInterval)
}

async function loadRankList(mode) {
  if (useAPI) {
    try {
      console.log('加载排行榜 - 使用API模式, mode:', mode)
      const response = await rankAPI.getRankList('gobang', mode, 10)
      console.log('排行榜API响应:', response)
      if (response.success) {
        // 转换格式：{username, streak} -> {name, streak}
        rankList.value = response.data.map(item => ({
          name: item.username,
          streak: item.streak || 0
        }))
        console.log('排行榜加载成功:', rankList.value)
        // 如果没有数据，不写入默认数据（让用户自己玩）
        return
      } else {
        console.warn('排行榜API返回失败:', response)
      }
    } catch (error) {
      console.error('获取排行榜失败:', error)
      // API失败时回退到localStorage
    }
  } else {
    console.log('加载排行榜 - 使用localStorage模式, mode:', mode)
  }
  
  // localStorage模式（原有逻辑）
  let arr = []
  try {
    arr = JSON.parse(localStorage.getItem('gobang_rank_' + mode)||'[]')
    if (!Array.isArray(arr)) arr = []
  } catch { arr = [] }
  // 如果没有数据，强制写入一条默认数据
  if (arr.length === 0) {
    arr = [{name: '玩家A', streak: 1}]
    localStorage.setItem('gobang_rank_' + mode, JSON.stringify(arr))
  }
  rankList.value = arr
}

async function updateRankList(mode, name, streak) {
  console.log('更新排行榜 - useAPI:', useAPI, 'mode:', mode, 'name:', name, 'streak:', streak)
  if (useAPI) {
    try {
      // 先从数据库获取当前玩家的连胜数，确保使用最新值
      let currentStreak = streak
      try {
        const rankResponse = await rankAPI.getRankList('gobang', mode, 100) // 获取更多记录以找到当前玩家
        if (rankResponse.success && rankResponse.data) {
          const playerRecord = rankResponse.data.find(item => item.username === name)
          if (playerRecord && playerRecord.streak != null) {
            // 如果数据库中有记录，使用数据库中的值加1
            currentStreak = playerRecord.streak + 1
            console.log('从数据库获取当前连胜数:', playerRecord.streak, '更新为:', currentStreak)
          } else {
            // 如果数据库中没有记录，使用传入的值（应该是1）
            console.log('数据库中没有该玩家记录，使用传入的连胜数:', currentStreak)
          }
        }
      } catch (err) {
        console.warn('获取当前玩家记录失败，使用传入的连胜数:', err)
      }
      
      // 提交到API
      const response = await rankAPI.submitRank('gobang', {
        username: name,
        mode: mode,
        streak: currentStreak
      })
      console.log('提交排行榜API响应:', response)
      if (response.success) {
        console.log('排行榜已更新到数据库:', response.data)
        // 更新localStorage以保持同步
        setStreak(name, currentStreak)
        // 重新加载排行榜
        await loadRankList(mode)
        return
      } else {
        console.warn('排行榜API提交失败:', response)
      }
    } catch (error) {
      console.error('提交排行榜失败:', error)
      console.error('错误详情:', error.message, error.stack)
      // API失败时回退到localStorage
    }
  } else {
    console.log('更新排行榜 - 使用localStorage模式')
  }
  
  // localStorage模式（原有逻辑）
  let arr = []
  try {
    arr = JSON.parse(localStorage.getItem('gobang_rank_' + mode)||'[]')
    if (!Array.isArray(arr)) arr = []
  } catch { arr = [] }
  // 如果已存在则更新，否则插入
  const idx = arr.findIndex(item=>item.name===name)
  if (idx>-1) {
    if (streak>arr[idx].streak) arr[idx].streak = streak
  } else {
    arr.push({name, streak})
  }
  arr.sort((a,b)=>b.streak-a.streak)
  arr = arr.slice(0,10)
  localStorage.setItem('gobang_rank_' + mode, JSON.stringify(arr))
  rankList.value = arr
}
watch(rankMode, (val)=>{ loadRankList(val) })
watch(showRankCard, v=>{ if(v) loadRankList(rankMode.value) })

onMounted(() => {
  loadRankList(rankMode.value)
})
</script>

<style scoped>
.gobang-bg {
  position: fixed;
  left: 0; top: 0; width: 100vw; height: 100vh;
  background: url('@/assets/wzq.png') center center/cover no-repeat;
  z-index: 0;
}
.gobang-outer {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gobang-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  width: 100vw;
  position: relative;
}
.gobang-main {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100vw;
  height: 100vh;
}
.side-panel {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-right: 32px;
  min-width: 220px;
  justify-content: center;
  height: 600px;
}
.player-select-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  justify-content: flex-start;
}
.select-label {
  font-size: 1.18rem;
  font-weight: bold;
  letter-spacing: 2px;
  color: #1a3d2e;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  margin-bottom: 4px;
  margin-right: 8px;
  text-shadow: 0 1px 2px rgba(255,255,255,0.8);
}
.player-select {
  width: 100%;
  min-width: 160px;
  font-size: 1.12rem;
  border-radius: 12px;
  border: 2px solid #6ec6a6;
  background: linear-gradient(135deg, #ffffff 60%, #f0f8f5 100%);
  color: #1a3d2e;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  padding: 8px 12px;
  outline: none;
  transition: box-shadow 0.2s, border 0.2s;
  box-shadow: 0 2px 10px rgba(110,198,166,0.10);
  margin-bottom: 12px;
  margin-top: 0;
  font-weight: 500;
}
.player-select:focus {
  box-shadow: 0 4px 18px rgba(110,198,166,0.18);
  border-color: #4cae8a;
}
.leaderboard-label {
  font-size: 1.1rem;
  font-weight: bold;
  letter-spacing: 2px;
  margin-bottom: 8px;
}
.leaderboard-select {
  margin-bottom: 18px;
  margin-top: 0;
  font-size: 1.08rem;
  border-radius: 12px;
  border: 2px solid #6ec6a6;
  background: linear-gradient(135deg, #ffffff 60%, #f0f8f5 100%);
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  box-shadow: 0 2px 10px rgba(110,198,166,0.10);
  padding: 8px 12px;
  outline: none;
  transition: box-shadow 0.2s, border 0.2s;
  font-weight: 500;
}
.leaderboard-select:focus {
  box-shadow: 0 4px 18px rgba(110,198,166,0.18);
  border-color: #4cae8a;
}
.info.info-side {
  margin-top: 12px;
  min-width: 160px;
  background: none;
  box-shadow: none;
  border-radius: 0;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-end;
}
.board-bg {
  width: 600px;
  height: 600px;
  position: relative;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.10);
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  opacity: 0.75;
}
.gobang-canvas {
  background: transparent;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(25,118,210,0.10);
  z-index: 1;
  position: relative;
}
.info-text {
  font-size: 1.35rem;
  color: #0d2818;
  margin-bottom: 32px;
  font-weight: bold;
  letter-spacing: 2px;
  text-shadow: 0 1px 3px rgba(255,255,255,0.9);
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
}
button {
  padding: 10px 38px;
  font-size: 18px;
  border-radius: 18px;
  border: 2px solid #6ec6a6;
  background: linear-gradient(135deg, #e6f9f2 60%, #b2e2c7 100%);
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 2px 8px rgba(110,198,166,0.10);
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border 0.2s;
  margin-bottom: 12px;
}
button:hover {
  background: linear-gradient(135deg, #b2e2c7 60%, #6ec6a6 100%);
  color: #0a1f14;
  border-color: #4cae8a;
}
.next-btn, .exit-btn {
  padding: 10px 38px;
  font-size: 18px;
  border-radius: 18px;
  border: 2px solid #6ec6a6;
  background: linear-gradient(135deg, #e6f9f2 60%, #b2e2c7 100%);
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 2px 8px rgba(110,198,166,0.10);
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border 0.2s;
  margin-bottom: 12px;
  margin-top: 0;
  margin-right: 0;
  margin-left: 0;
  display: block;
}
.next-btn:hover, .exit-btn:hover {
  background: linear-gradient(135deg, #b2e2c7 60%, #6ec6a6 100%);
  color: #0a1f14;
  border-color: #4cae8a;
}
.start-game-btn {
  padding: 14px 48px;
  font-size: 1.2rem;
  border-radius: 18px;
  border: 2px solid #4cae8a;
  background: linear-gradient(135deg, #4cae8a 60%, #6ec6a6 100%);
  color: #fff;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 4px 12px rgba(110,198,166,0.3);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 12px;
  width: 100%;
  min-width: 160px;
}
.start-game-btn:hover {
  background: linear-gradient(135deg, #6ec6a6 60%, #4cae8a 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(110,198,166,0.4);
}
.start-game-btn:active {
  transform: translateY(0);
}
.add-player-btn {
  margin-left: 12px;
  padding: 4px 16px;
  font-size: 1rem;
  border-radius: 8px;
  border: 1.5px solid #6ec6a6;
  background: #e6f9f2;
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border 0.2s;
  font-weight: 500;
}
.add-player-btn:hover {
  background: #b2e2c7;
  color: #0a1f14;
  border-color: #4cae8a;
}
.add-player-modal {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.add-player-content {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
  padding: 32px 36px 24px 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.add-player-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 18px;
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
}
.add-player-content input {
  font-size: 1.1rem;
  padding: 8px 16px;
  border-radius: 8px;
  border: 1.5px solid #6ec6a6;
  margin-bottom: 18px;
  outline: none;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
}
.add-player-actions {
  display: flex;
  gap: 18px;
}
.add-player-actions button {
  padding: 8px 24px;
  font-size: 1rem;
  border-radius: 8px;
  border: 1.5px solid #6ec6a6;
  background: #e6f9f2;
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border 0.2s;
  font-weight: 500;
}
.add-player-actions button:hover {
  background: #b2e2c7;
  color: #0a1f14;
  border-color: #4cae8a;
}
.mode-select-modal {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.mode-select-content {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
  padding: 48px 56px 36px 56px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.mode-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 32px;
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
}
.mode-btn {
  padding: 16px 48px;
  font-size: 1.18rem;
  border-radius: 18px;
  border: 2px solid #6ec6a6;
  background: linear-gradient(135deg, #e6f9f2 60%, #b2e2c7 100%);
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  font-weight: bold;
  letter-spacing: 2px;
  box-shadow: 0 2px 8px rgba(110,198,166,0.10);
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border 0.2s;
  margin-bottom: 18px;
}
.mode-btn:hover {
  background: linear-gradient(135deg, #b2e2c7 60%, #6ec6a6 100%);
  color: #0a1f14;
  border-color: #4cae8a;
}
.timer-bar {
  position: absolute;
  left: 50%;
  top: 18px;
  transform: translateX(-50%);
  background: rgba(255,255,255,0.95);
  border-radius: 12px;
  padding: 8px 32px;
  font-size: 1.25rem;
  color: #0d2818;
  font-family: 'STKaiti', 'KaiTi', 'FZKai-Z03', '楷体', serif;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(110,198,166,0.15);
  z-index: 10;
  border: 1px solid rgba(110,198,166,0.2);
}
.gobang-rank-btn {
  position: absolute;
  top: 32px;
  right: 48px;
  z-index: 9999;
  background: linear-gradient(90deg,#7b8aff,#a6e3ff);
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 1.1rem;
  box-shadow: 0 2px 8px #0002;
  cursor: pointer;
  transition: box-shadow .2s;
}
.gobang-rank-btn:hover { box-shadow: 0 4px 16px #0003; }
.gobang-rank-card {
  position: absolute;
  top: 70px;
  right: 48px;
  z-index: 9999;
  width: 280px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px #0003;
  padding: 18px 18px 12px 18px;
  animation: fadeIn .3s;
}
.rank-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
  font-size: 1.1rem;
  margin-bottom: 10px;
}
.rank-select {
  border-radius: 8px;
  border: 1px solid #bcd;
  padding: 2px 8px;
  margin: 0 8px;
}
.rank-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  color: #888;
  cursor: pointer;
}
.rank-list {
  min-height: 120px;
}
.rank-item {
  display: flex;
  align-items: center;
  font-size: 1rem;
  margin-bottom: 4px;
  padding: 2px 0;
}
.rank-index {
  width: 2em;
  color: #7b8aff;
  font-weight: bold;
}
.rank-name {
  color: #333;
  font-weight: bold;
  margin-right: 12px;
}
.rank-streak {
  color: #4cae8a;
  font-weight: bold;
}
.rank-empty {
  color: #aaa;
  text-align: center;
  margin-top: 30px;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: none; }
}
.gobang-exit-btn {
  position: absolute;
  top: 32px;
  right: 180px;
  z-index: 9999;
  background: linear-gradient(90deg,#ffb347,#ff7043);
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 1.1rem;
  box-shadow: 0 2px 8px #0002;
  cursor: pointer;
  transition: box-shadow .2s;
}
.gobang-exit-btn:hover { box-shadow: 0 4px 16px #ff7043aa; }
</style> 