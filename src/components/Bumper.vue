<template>
  <div class="bumper-container">
    <h2>碰碰车</h2>
    <canvas ref="canvas" width="600" height="400" class="game-canvas"></canvas>
    <div class="info">
      <div>得分: {{ score }}</div>
      <button @click="start" :disabled="playing">{{ t('bumper.start') }}</button>
      <div v-if="gameOver">{{ t('bumper.gameOver') }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, inject } from 'vue'
import { useRouter } from 'vue-router'
const lang = inject('lang')
const setLang = inject('setLang')
const t = inject('t')
const router = useRouter()
const canvas = ref(null)
let ctx, raf, aiTimer
const player = ref({ x: 100, y: 200, vx: 0, vy: 0, r: 22, color: '#1976d2', score: 0 })
const ais = ref([])
const obstacles = ref([])
const score = ref(0)
const playing = ref(false)
const gameOver = ref(false)
function start() {
  cancelAnimationFrame(raf)
  clearInterval(aiTimer)
  player.value = { x: 100, y: 200, vx: 0, vy: 0, r: 22, color: '#1976d2', score: 0 }
  ais.value = [
    { x: 500, y: 100, vx: 2, vy: 1.5, r: 22, color: '#e53935', score: 0 },
    { x: 500, y: 300, vx: -2, vy: -1.5, r: 22, color: '#43a047', score: 0 }
  ]
  obstacles.value = [
    { x: 300, y: 200, r: 30 }
  ]
  score.value = 0
  playing.value = true
  gameOver.value = false
  loop()
  aiTimer = setInterval(moveAI, 100)
}
function loop() {
  update()
  draw()
  if (!gameOver.value) raf = requestAnimationFrame(loop)
}
function update() {
  // 玩家移动
  player.value.x += player.value.vx
  player.value.y += player.value.vy
  keepInBounds(player.value)
  // AI移动
  for (let ai of ais.value) {
    ai.x += ai.vx
    ai.y += ai.vy
    keepInBounds(ai)
  }
  // 碰撞检测
  for (let ai of ais.value) {
    if (circleCollide(player.value, ai)) {
      score.value++
      ai.vx = -ai.vx
      ai.vy = -ai.vy
    }
  }
  for (let ai of ais.value) {
    for (let other of ais.value) {
      if (ai !== other && circleCollide(ai, other)) {
        ai.vx = -ai.vx
        ai.vy = -ai.vy
      }
    }
  }
  for (let obs of obstacles.value) {
    if (circleCollide(player.value, obs)) {
      endGame()
      return
    }
    for (let ai of ais.value) {
      if (circleCollide(ai, obs)) {
        ai.vx = -ai.vx
        ai.vy = -ai.vy
      }
    }
  }
}
function draw() {
  ctx.clearRect(0, 0, 600, 400)
  // 障碍物
  ctx.fillStyle = '#888'
  for (let obs of obstacles.value) {
    ctx.beginPath()
    ctx.arc(obs.x, obs.y, obs.r, 0, Math.PI*2)
    ctx.fill()
  }
  // AI车
  for (let ai of ais.value) {
    ctx.beginPath()
    ctx.arc(ai.x, ai.y, ai.r, 0, Math.PI*2)
    ctx.fillStyle = ai.color
    ctx.fill()
    ctx.strokeStyle = '#222'
    ctx.stroke()
  }
  // 玩家车
  ctx.beginPath()
  ctx.arc(player.value.x, player.value.y, player.value.r, 0, Math.PI*2)
  ctx.fillStyle = player.value.color
  ctx.fill()
  ctx.strokeStyle = '#222'
  ctx.stroke()
}
function keepInBounds(car) {
  if (car.x < car.r) { car.x = car.r; car.vx = -car.vx }
  if (car.x > 600-car.r) { car.x = 600-car.r; car.vx = -car.vx }
  if (car.y < car.r) { car.y = car.r; car.vy = -car.vy }
  if (car.y > 400-car.r) { car.y = 400-car.r; car.vy = -car.vy }
}
function circleCollide(a, b) {
  const dx = a.x - b.x, dy = a.y - b.y
  return Math.sqrt(dx*dx+dy*dy) < a.r + b.r
}
function endGame() {
  playing.value = false
  gameOver.value = true
  cancelAnimationFrame(raf)
  clearInterval(aiTimer)
}
function moveAI() {
  for (let ai of ais.value) {
    if (Math.random() < 0.3) {
      ai.vx += (Math.random()-0.5)*1.2
      ai.vy += (Math.random()-0.5)*1.2
      ai.vx = Math.max(-3, Math.min(3, ai.vx))
      ai.vy = Math.max(-3, Math.min(3, ai.vy))
    }
  }
}
function onKey(e) {
  if (!playing.value) return
  if (e.key === 'ArrowLeft') player.value.vx = -3
  if (e.key === 'ArrowRight') player.value.vx = 3
  if (e.key === 'ArrowUp') player.value.vy = -3
  if (e.key === 'ArrowDown') player.value.vy = 3
}
function onKeyUp(e) {
  if (!playing.value) return
  if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') player.value.vx = 0
  if (e.key === 'ArrowUp' || e.key === 'ArrowDown') player.value.vy = 0
}
onMounted(() => {
  ctx = canvas.value.getContext('2d')
  window.addEventListener('keydown', onKey)
  window.addEventListener('keyup', onKeyUp)
})
onUnmounted(() => {
  cancelAnimationFrame(raf)
  clearInterval(aiTimer)
  window.removeEventListener('keydown', onKey)
  window.removeEventListener('keyup', onKeyUp)
})
</script>

<style scoped>
.bumper-container { text-align: center; margin-top: 40px; }
.game-canvas { border: 2px solid #333; background: #eef; margin: 20px 0; }
.info { margin-top: 20px; }
button { margin-top: 10px; }
</style> 