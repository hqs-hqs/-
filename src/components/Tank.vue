<template>
  <div class="tank-container">
    <h2>坦克大战</h2>
    <canvas ref="canvas" width="400" height="300" class="game-canvas"></canvas>
    <div class="info">
      <div>分数: {{ score }}</div>
      <button @click="start" :disabled="playing">{{ t('start') }}</button>
      <div v-if="gameOver">{{ win ? t('victory') : t('gameOver') }}</div>
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
let ctx, raf, enemyTimer
const player = ref({ x: 200, y: 250, dir: 0 })
const bullets = ref([])
const enemies = ref([])
const score = ref(0)
const playing = ref(false)
const gameOver = ref(false)
const win = ref(false)
function start() {
  player.value = { x: 200, y: 250, dir: 0 }
  bullets.value = []
  enemies.value = Array.from({ length: 5 }, () => ({ x: Math.random()*360+20, y: Math.random()*100+20, dir: Math.random()*2*Math.PI }))
  score.value = 0
  playing.value = true
  gameOver.value = false
  win.value = false
  loop()
  enemyTimer = setInterval(moveEnemies, 400)
}
function loop() {
  update()
  draw()
  if (!gameOver.value) raf = requestAnimationFrame(loop)
}
function update() {
  // 子弹移动
  for (let b of bullets.value) {
    b.x += Math.cos(b.dir) * 8
    b.y += Math.sin(b.dir) * 8
  }
  bullets.value = bullets.value.filter(b => b.x > 0 && b.x < 400 && b.y > 0 && b.y < 300)
  // 敌人碰撞
  for (let i = enemies.value.length - 1; i >= 0; i--) {
    for (let j = bullets.value.length - 1; j >= 0; j--) {
      if (Math.abs(enemies.value[i].x - bullets.value[j].x) < 18 && Math.abs(enemies.value[i].y - bullets.value[j].y) < 18) {
        enemies.value.splice(i, 1)
        bullets.value.splice(j, 1)
        score.value++
        break
      }
    }
  }
  // 玩家与敌人碰撞
  for (let e of enemies.value) {
    if (Math.abs(player.value.x - e.x) < 22 && Math.abs(player.value.y - e.y) < 22) {
      endGame(false)
      return
    }
  }
  // 胜利判定
  if (enemies.value.length === 0) {
    endGame(true)
  }
}
function draw() {
  ctx.clearRect(0, 0, 400, 300)
  // 玩家坦克
  ctx.save()
  ctx.translate(player.value.x, player.value.y)
  ctx.rotate(player.value.dir)
  ctx.fillStyle = '#3a3'
  ctx.fillRect(-15, -15, 30, 30)
  ctx.fillStyle = '#080'
  ctx.fillRect(0, -5, 20, 10)
  ctx.restore()
  // 子弹
  ctx.fillStyle = '#333'
  for (let b of bullets.value) {
    ctx.beginPath()
    ctx.arc(b.x, b.y, 5, 0, Math.PI*2)
    ctx.fill()
  }
  // 敌人
  ctx.fillStyle = '#f33'
  for (let e of enemies.value) {
    ctx.fillRect(e.x-15, e.y-15, 30, 30)
  }
}
function moveEnemies() {
  for (let e of enemies.value) {
    e.dir += (Math.random()-0.5)*0.8
    e.x += Math.cos(e.dir) * 10
    e.y += Math.sin(e.dir) * 10
    e.x = Math.max(15, Math.min(385, e.x))
    e.y = Math.max(15, Math.min(285, e.y))
  }
}
function endGame(isWin) {
  playing.value = false
  gameOver.value = true
  win.value = isWin
  cancelAnimationFrame(raf)
  clearInterval(enemyTimer)
}
function onKey(e) {
  if (!playing.value) return
  if (e.key === 'ArrowLeft') player.value.dir -= 0.2
  if (e.key === 'ArrowRight') player.value.dir += 0.2
  if (e.key === 'ArrowUp') {
    player.value.x += Math.cos(player.value.dir) * 10
    player.value.y += Math.sin(player.value.dir) * 10
    player.value.x = Math.max(15, Math.min(385, player.value.x))
    player.value.y = Math.max(15, Math.min(285, player.value.y))
  }
  if (e.key === ' ') {
    bullets.value.push({ x: player.value.x + Math.cos(player.value.dir)*20, y: player.value.y + Math.sin(player.value.dir)*20, dir: player.value.dir })
  }
}
onMounted(() => {
  ctx = canvas.value.getContext('2d')
  window.addEventListener('keydown', onKey)
})
</script>

<style scoped>
.tank-container { text-align: center; margin-top: 40px; }
.game-canvas { border: 2px solid #333; background: #eef; margin: 20px 0; }
.info { margin-top: 20px; }
button { margin-top: 10px; }
</style> 