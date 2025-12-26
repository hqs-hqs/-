<template>
  <div class="runner-container" :class="theme">
    <div class="canvas-wrap">
      <canvas ref="canvas" :width="winW" :height="winH" class="game-canvas"></canvas>
      <div class="pause-btn" @click="pauseGame" v-if="playing && !paused">
        <span class="pause-icon">⏸</span>
      </div>
    </div>
    <!-- 得分板 -->
    <transition name="score-pop">
      <div class="score-board" v-show="playing || paused || gameOver">
        <span>分数：</span><span class="score-anim" :class="{pop: scorePop}">{{ displayScore }}</span>
        <span class="best-score">最佳：{{ bestScore }}</span>
      </div>
    </transition>
    <!-- 开始按钮 -->
    <transition name="fade">
      <div v-if="!playing && !gameOver && !paused" class="start-panel">
        <div class="start-content">
          <div class="start-icon"></div>
          <button class="start-btn" @click="start">{{ t('runner.start') }}</button>
        </div>
      </div>
    </transition>
    <!-- 暂停菜单 -->
    <transition name="modal-fade">
      <div v-if="paused" class="pause-modal">
        <div class="pause-content">
          <h3>{{ t('runner.paused') }}</h3>
          <div class="pause-score">{{ t('runner.currentScore') }}: {{ displayScore }}</div>
          <div class="pause-score">{{ t('runner.bestScore') }}: {{ bestScore }}</div>
          <button @click="resumeGame">{{ t('runner.resume') }}</button>
          <button @click="restartGame">{{ t('runner.restart') }}</button>
          <button @click="exitGame">{{ t('runner.exit') }}</button>
        </div>
      </div>
    </transition>
    <!-- 游戏结束界面 -->
    <transition name="modal-fade">
      <div v-if="gameOver" class="gameover-modal">
        <div class="gameover-content">
          <div class="gameover-icon"></div>
          <h2>{{ t('runner.gameOver') }}</h2>
          <div class="pause-score">{{ t('runner.currentScore') }}: {{ displayScore }}</div>
          <div class="pause-score">{{ t('runner.bestScore') }}: {{ bestScore }}</div>
          <button @click="restartGame">{{ t('runner.restart') }}</button>
          <button @click="exitGame">{{ t('runner.exit') }}</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, inject } from 'vue'
import { useRouter } from 'vue-router'
const lang = inject('lang')
const setLang = inject('setLang')
const t = inject('t')
const router = useRouter()
const winW = ref(window.innerWidth)
const winH = ref(window.innerHeight)
const canvas = ref(null)
let ctx, raf
const score = ref(0)
const displayScore = ref(0)
const bestScore = ref(Number(localStorage.getItem('runner_best_score') || 0))
const playing = ref(false)
const paused = ref(false)
const gameOver = ref(false)
const platformMinW = 120, platformMaxW = 200, platformH = 20, platformGap = 80
const groundH = 72
const groundPoints = ref([])
const platforms = ref([])
const ball = ref({ x: 100, y: 0, vx: 0, vy: 0, r: 18, onGround: false })
const cameraX = ref(0)
// 键盘状态
const keyState = { a: false, d: false }
const theme = ref('theme-day') // 预留主题切换
const scorePop = ref(false)
const obstacles = ref([]) // 新增障碍物数组
const weatherTypes = ['none', 'rain', 'snow', 'fog']
const weather = ref('none')
const weatherTimer = ref(null)
const weatherParticles = ref([])
const bricks = ref([]) // 砖块和问号箱数组

function resetGround() {
  groundPoints.value = []
  let x = 0, y = winH.value - groundH - 10
  while (x < 10000) {
    // 更大幅度高低起伏
    let delta = rand(-80, 80)
    if (Math.random() < 0.12) delta += rand(-60, 60) // 偶尔大幅波动
    y = Math.max(winH.value - groundH - 120, Math.min(winH.value - groundH + 60, y + delta))
    // 偶尔生成高台
    if (Math.random() < 0.08) y -= rand(40, 100)
    // 偶尔生成小坑
    if (Math.random() < 0.07) y += rand(40, 80)
    groundPoints.value.push({ x, y })
    x += rand(120, 320)
  }
}
function getGroundY(x) {
  // 线性插值获取地面y
  for (let i = 1; i < groundPoints.value.length; i++) {
    if (x < groundPoints.value[i].x) {
      const p1 = groundPoints.value[i - 1], p2 = groundPoints.value[i]
      const t = (x - p1.x) / (p2.x - p1.x)
      return p1.y * (1 - t) + p2.y * t
    }
  }
  return winH.value - groundH
}
function randomWeather() {
  // 随机切换天气
  weather.value = weatherTypes[Math.floor(Math.random() * weatherTypes.length)]
  weatherParticles.value = []
  if (weather.value === 'rain') {
    for (let i = 0; i < 60; i++) {
      weatherParticles.value.push({ x: Math.random()*winW.value, y: Math.random()*winH.value, l: 18+Math.random()*12, v: 8+Math.random()*6 })
    }
  } else if (weather.value === 'snow') {
    for (let i = 0; i < 40; i++) {
      weatherParticles.value.push({ x: Math.random()*winW.value, y: Math.random()*winH.value, r: 2+Math.random()*2, v: 1+Math.random()*1.5 })
    }
  } else if (weather.value === 'fog') {
    for (let i = 0; i < 8; i++) {
      weatherParticles.value.push({ x: Math.random()*winW.value, y: Math.random()*winH.value, r: 80+Math.random()*60, o: 0.08+Math.random()*0.08 })
    }
  }
}
function resetPlatforms() {
  resetGround()
  platforms.value = []
  obstacles.value = []
  bricks.value = []
  let x = 0, y = getGroundY(0) - 60
  for (let i = 0; i < 8; i++) {
    // 平台宽度和间距变化更大
    const w = rand(platformMinW-40, platformMaxW+80)
    const slope = Math.random() < 0.35 ? (Math.random()*0.6-0.3) : 0
    // 有概率让平台直接贴地
    let groundY = getGroundY(x + w/2)
    let stickToGround = Math.random() < 0.38
    let platY = stickToGround ? groundY - platformH : y
    platforms.value.push({ x, y: platY, w, h: platformH, slope, stickToGround })
    if (i > 1 && Math.random() < 0.22) {
      const y2 = y - rand(80, 120)
      platforms.value.push({ x, y: y2, w, h: platformH, slope: Math.random() < 0.5 ? (Math.random()*0.6-0.3) : 0, stickToGround: false })
    }
    // 障碍物
    if (i > 1 && Math.random() < 0.5) {
      const type = Math.random() < 0.5 ? 'block' : (Math.random() < 0.5 ? 'rotator' : 'spring')
      if (type === 'block') {
        obstacles.value.push({ type, x: x + w/2, y: y - 32, w: 36, h: 36, vx: 0, vy: 0 })
      } else if (type === 'rotator') {
        obstacles.value.push({ type, x: x + w/2, y: y - 32, r: 32, angle: 0, speed: 0.05 + Math.random()*0.04 })
      } else if (type === 'spring') {
        obstacles.value.push({ type, x: x + w/2, y: y - 16, w: 32, h: 12, bounce: true })
      }
    }
    // 砖块和问号箱
    if (i > 1 && Math.random() < 0.7) {
      const brickCount = rand(1, Math.floor(w/48))
      for (let j = 0; j < brickCount; j++) {
        const bx = x + 24 + j*48
        const by = y - 56 - (Math.random()<0.3?rand(0,32):0)
        const type = Math.random() < 0.7 ? 'brick' : 'question'
        bricks.value.push({ type, x: bx, y: by, w: 44, h: 44, hit: false, anim: 0 })
      }
    }
    x += w + rand(platformGap-30, platformGap + 120)
    y = Math.max(120, Math.min(getGroundY(x) - 60, y + rand(-80, 80)))
  }
}
function addPlatform() {
  const last = platforms.value[platforms.value.length - 1]
  let x = last.x + last.w + rand(platformGap-30, platformGap + 120)
  let y = Math.max(120, Math.min(getGroundY(x) - 60, last.y + rand(-80, 80)))
  const w = rand(platformMinW-40, platformMaxW+80)
  const slope = Math.random() < 0.35 ? (Math.random()*0.6-0.3) : 0
  // 有概率让平台直接贴地
  let groundY = getGroundY(x + w/2)
  let stickToGround = Math.random() < 0.38
  let platY = stickToGround ? groundY - platformH : y
  platforms.value.push({ x, y: platY, w, h: platformH, slope, stickToGround })
  if (Math.random() < 0.18) {
    const y2 = y - rand(80, 120)
    platforms.value.push({ x, y: y2, w, h: platformH, slope: Math.random() < 0.5 ? (Math.random()*0.6-0.3) : 0, stickToGround: false })
  }
  if (Math.random() < 0.4) {
    const type = Math.random() < 0.5 ? 'block' : (Math.random() < 0.5 ? 'rotator' : 'spring')
    if (type === 'block') {
      obstacles.value.push({ type, x: x + w/2, y: y - 32, w: 36, h: 36, vx: 0, vy: 0 })
    } else if (type === 'rotator') {
      obstacles.value.push({ type, x: x + w/2, y: y - 32, r: 32, angle: 0, speed: 0.05 + Math.random()*0.04 })
    } else if (type === 'spring') {
      obstacles.value.push({ type, x: x + w/2, y: y - 16, w: 32, h: 12, bounce: true })
    }
  }
  // 砖块和问号箱
  if (Math.random() < 0.7) {
    const brickCount = rand(1, Math.floor(w/48))
    for (let j = 0; j < brickCount; j++) {
      const bx = x + 24 + j*48
      const by = y - 56 - (Math.random()<0.3?rand(0,32):0)
      const type = Math.random() < 0.7 ? 'brick' : 'question'
      bricks.value.push({ type, x: bx, y: by, w: 44, h: 44, hit: false, anim: 0 })
    }
  }
}
function rand(a, b) { return Math.floor(Math.random() * (b - a + 1)) + a }
function start() {
  winW.value = window.innerWidth
  winH.value = window.innerHeight
  resetGround()
  resetPlatforms()
  ball.value = { x: platforms.value[0].x + 40, y: platforms.value[0].y - 20, vx: 0, vy: 0, r: 18, onGround: false }
  cameraX.value = 0
  score.value = 0
  displayScore.value = 0
  playing.value = true
  paused.value = false
  gameOver.value = false
  obstacles.value = [] // 重置障碍物
  bricks.value = []
  loop()
}
function loop() {
  if (!playing.value || paused.value) return
  update()
  draw()
  if (!gameOver.value) raf = requestAnimationFrame(loop)
}
function update() {
  // 球物理
  ball.value.x += ball.value.vx
  ball.value.y += ball.value.vy
  ball.value.vy += 0.6
  // 平台碰撞（只允许从上落下）
  let onPlat = false
  for (let pf of platforms.value) {
    if (
      ball.value.x + ball.value.r > pf.x &&
      ball.value.x - ball.value.r < pf.x + pf.w &&
      ball.value.y + ball.value.r > pf.y &&
      ball.value.y + ball.value.r < pf.y + pf.h + 8 &&
      ball.value.vy >= 0 &&
      ball.value.y + ball.value.r - ball.value.vy <= pf.y // 上一帧在平台上方
    ) {
      ball.value.y = pf.y - ball.value.r
      ball.value.vy = 0
      onPlat = true
    }
  }
  // 地面碰撞（只允许从上落下）
  const gy = getGroundY(ball.value.x)
  if (
    ball.value.y + ball.value.r > gy &&
    ball.value.vy >= 0 &&
    ball.value.y + ball.value.r - ball.value.vy <= gy
  ) {
    ball.value.y = gy - ball.value.r
    ball.value.vy = 0
    onPlat = true
  }
  ball.value.onGround = onPlat
  // 边界
  if (ball.value.x < platforms.value[0].x + ball.value.r) ball.value.x = platforms.value[0].x + ball.value.r
  if (ball.value.y > winH.value) {
    endGame()
  }
  // 相机跟随
  const centerX = winW.value / 2
  if (ball.value.x > cameraX.value + centerX) cameraX.value = ball.value.x - centerX
  // 平台生成
  while (platforms.value[platforms.value.length - 1].x < cameraX.value + winW.value + 200) {
    addPlatform()
  }
  // 障碍物动画
  for (let obs of obstacles.value) {
    if (obs.type === 'rotator') {
      obs.angle += obs.speed
    }
  }
  // 主角与障碍物碰撞
  for (let obs of obstacles.value) {
    if (obs.type === 'block') {
      if (
        ball.value.x + ball.value.r > obs.x - obs.w/2 &&
        ball.value.x - ball.value.r < obs.x + obs.w/2 &&
        ball.value.y + ball.value.r > obs.y - obs.h/2 &&
        ball.value.y - ball.value.r < obs.y + obs.h/2
      ) {
        endGame()
        return
      }
    } else if (obs.type === 'rotator') {
      // 旋转锤碰撞（简化为圆形）
      const dx = ball.value.x - obs.x, dy = ball.value.y - obs.y
      if (Math.sqrt(dx*dx + dy*dy) < obs.r + ball.value.r - 8) {
        endGame()
        return
      }
    } else if (obs.type === 'spring') {
      // 弹跳板
      if (
        ball.value.x + ball.value.r > obs.x - obs.w/2 &&
        ball.value.x - ball.value.r < obs.x + obs.w/2 &&
        Math.abs(ball.value.y + ball.value.r - obs.y) < 10 &&
        ball.value.vy > 0
      ) {
        ball.value.vy = -14
      }
    }
  }
  // 主角顶砖块/问号箱
  for (let brick of bricks.value) {
    if (!brick.hit &&
      ball.value.x + ball.value.r > brick.x - brick.w/2 &&
      ball.value.x - ball.value.r < brick.x + brick.w/2 &&
      Math.abs(ball.value.y - ball.value.r - brick.y - brick.h/2) < 8 &&
      ball.value.vy < 0
    ) {
      brick.hit = true
      brick.anim = 1
      ball.value.vy = 4.5 // 反弹
    }
    if (brick.anim > 0) {
      brick.anim += 0.3
      if (brick.anim > 8) brick.anim = 0
    }
  }
  // 得分
  score.value = Math.floor(ball.value.x - platforms.value[0].x)
}
function draw() {
  ctx.clearRect(0, 0, winW.value, winH.value)
  // 天空
  ctx.fillStyle = '#4ecfff'
  ctx.fillRect(0, 0, winW.value, winH.value)
  // 天气特效
  if (weather.value === 'rain') {
    ctx.save()
    ctx.strokeStyle = 'rgba(80,180,255,0.55)'
    ctx.lineWidth = 2
    for (let drop of weatherParticles.value) {
      ctx.beginPath()
      ctx.moveTo(drop.x, drop.y)
      ctx.lineTo(drop.x, drop.y + drop.l)
      ctx.stroke()
      drop.y += drop.v
      if (drop.y > winH.value) { drop.y = -drop.l; drop.x = Math.random()*winW.value }
    }
    ctx.restore()
  } else if (weather.value === 'snow') {
    ctx.save()
    ctx.fillStyle = 'rgba(255,255,255,0.85)'
    for (let snow of weatherParticles.value) {
      ctx.beginPath()
      ctx.arc(snow.x, snow.y, snow.r, 0, Math.PI*2)
      ctx.fill()
      snow.y += snow.v
      snow.x += Math.sin(Date.now()/400 + snow.y/40)
      if (snow.y > winH.value) { snow.y = -snow.r; snow.x = Math.random()*winW.value }
    }
    ctx.restore()
  } else if (weather.value === 'fog') {
    ctx.save()
    for (let fog of weatherParticles.value) {
      ctx.globalAlpha = fog.o
      ctx.beginPath()
      ctx.arc(fog.x, fog.y, fog.r, 0, Math.PI*2)
      ctx.fillStyle = '#dde6ee'
      ctx.fill()
    }
    ctx.globalAlpha = 1
    ctx.restore()
  }
  // 美化地面：泥土块+草皮+地垒凸起
  drawMarioGround(ctx)
  // 砖块和问号箱
  for (let brick of bricks.value) {
    if (brick.x - cameraX.value < -60 || brick.x - cameraX.value > winW.value+60) continue
    if (brick.type === 'brick') {
      drawMarioBrick(ctx, brick.x - brick.w/2 - cameraX.value, brick.y - brick.h/2 - (brick.anim>0?brick.anim:0), brick.w, brick.h, brick.hit)
    } else if (brick.type === 'question') {
      drawMarioQuestion(ctx, brick.x - brick.w/2 - cameraX.value, brick.y - brick.h/2 - (brick.anim>0?brick.anim:0), brick.w, brick.h, brick.hit)
    }
  }
  // 平台
  for (let pf of platforms.value) {
    if (pf.x + pf.w < cameraX.value - 100) continue
    ctx.save()
    // 坡道
    if (pf.slope) {
      ctx.transform(1, pf.slope, 0, 1, 0, -pf.slope * (pf.x - cameraX.value))
    }
    ctx.fillStyle = '#8d5c1c'
    ctx.fillRect(pf.x - cameraX.value, pf.y, pf.w, pf.h)
    ctx.fillStyle = '#6fff4e'
    ctx.fillRect(pf.x - cameraX.value, pf.y, pf.w, 8)
    ctx.beginPath()
    ctx.arc(pf.x - cameraX.value + 18, pf.y + 8, 8, Math.PI, 2 * Math.PI)
    ctx.fillStyle = '#4edc2f'
    ctx.fill()
    ctx.restore()
    // 视觉引导线
    ctx.save()
    ctx.setLineDash([8, 8])
    ctx.strokeStyle = 'rgba(80,80,255,0.18)'
    ctx.lineWidth = 3
    ctx.beginPath()
    ctx.moveTo(pf.x - cameraX.value + pf.w/2, pf.y - 18)
    ctx.lineTo(pf.x - cameraX.value + pf.w/2, pf.y + pf.h + 18)
    ctx.stroke()
    ctx.restore()
  }
  // 障碍物绘制
  for (let obs of obstacles.value) {
    if (obs.x - cameraX.value < -100 || obs.x - cameraX.value > winW.value + 100) continue
    if (obs.type === 'block') {
      ctx.save()
      ctx.fillStyle = '#444';
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 3;
      ctx.beginPath()
      ctx.rect(obs.x - obs.w/2 - cameraX.value, obs.y - obs.h/2, obs.w, obs.h)
      ctx.fill()
      ctx.stroke()
      ctx.restore()
    } else if (obs.type === 'rotator') {
      ctx.save()
      ctx.translate(obs.x - cameraX.value, obs.y)
      ctx.rotate(obs.angle)
      ctx.fillStyle = '#f90';
      ctx.beginPath()
      ctx.arc(0, 0, obs.r, 0, Math.PI*2)
      ctx.fill()
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 4;
      ctx.beginPath()
      ctx.moveTo(0, 0)
      ctx.lineTo(obs.r, 0)
      ctx.stroke()
      ctx.restore()
    } else if (obs.type === 'spring') {
      ctx.save()
      ctx.fillStyle = '#3af';
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 2;
      ctx.beginPath()
      ctx.rect(obs.x - obs.w/2 - cameraX.value, obs.y - obs.h/2, obs.w, obs.h)
      ctx.fill()
      ctx.stroke()
      // 弹簧动画
      ctx.beginPath()
      for (let i = 0; i < 8; i++) {
        const px = obs.x - obs.w/2 + (i/7)*obs.w - cameraX.value
        const py = obs.y - obs.h/2 + Math.sin(Date.now()/120 + i)*4 + obs.h/2
        if (i === 0) ctx.moveTo(px, py)
        else ctx.lineTo(px, py)
      }
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 1.5;
      ctx.stroke()
      ctx.restore()
    }
  }
  // 球
  ctx.beginPath()
  ctx.arc(ball.value.x - cameraX.value, ball.value.y, ball.value.r, 0, Math.PI*2)
  ctx.fillStyle = '#e33'
  ctx.fill()
}
function endGame() {
  playing.value = false
  gameOver.value = true
  cancelAnimationFrame(raf)
  if (score.value > bestScore.value) {
    bestScore.value = score.value
    localStorage.setItem('runner_best_score', score.value)
  }
}
function pauseGame() {
  paused.value = true
}
function resumeGame() {
  paused.value = false
  loop()
}
function restartGame() {
  gameOver.value = false
  paused.value = false
  start()
}
function exitGame() {
  playing.value = false
  paused.value = false
  gameOver.value = false
  cancelAnimationFrame(raf)
  router.push('/hall')
}
function onKey(e) {
  if (!playing.value || paused.value) return
  if (e.key === 'a' || e.key === 'A') {
    keyState.a = true
    ball.value.vx = -2.1 // 降低左右速度
  }
  if (e.key === 'd' || e.key === 'D') {
    keyState.d = true
    ball.value.vx = 2.1 // 降低左右速度
  }
  if ((e.key === 'w' || e.key === 'W') && ball.value.onGround) {
    ball.value.vy = -7.5 // 降低跳跃速度
    ball.value.onGround = false
  }
}
function onKeyUp(e) {
  if (e.key === 'a' || e.key === 'A') {
    keyState.a = false
    if (keyState.d) ball.value.vx = 2.1
    else ball.value.vx = 0
  }
  if (e.key === 'd' || e.key === 'D') {
    keyState.d = false
    if (keyState.a) ball.value.vx = -2.1
    else ball.value.vx = 0
  }
}
function onResize() {
  winW.value = window.innerWidth
  winH.value = window.innerHeight
  resetGround()
}
// 动态分数动画
watch(score, (val) => {
  if (val > displayScore.value) {
    animateScore(val)
    scorePop.value = true
    setTimeout(() => (scorePop.value = false), 300)
  }
})
function animateScore(target) {
  if (displayScore.value === target) return
  let step = Math.max(1, Math.floor((target - displayScore.value) / 8))
  const timer = setInterval(() => {
    if (displayScore.value < target) displayScore.value += step
    else displayScore.value = target
    if (displayScore.value >= target) clearInterval(timer)
  }, 18)
}
// 马里奥砖块绘制
function drawMarioBrick(ctx, x, y, w, h, broken) {
  ctx.save()
  ctx.fillStyle = broken ? '#b97a56' : '#d2691e'
  ctx.fillRect(x, y, w, h)
  ctx.strokeStyle = '#fff6'
  ctx.lineWidth = 2
  for (let i = 1; i < 4; i++) {
    ctx.beginPath()
    ctx.moveTo(x + i*w/4, y)
    ctx.lineTo(x + i*w/4, y + h)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(x, y + i*h/4)
    ctx.lineTo(x + w, y + i*h/4)
    ctx.stroke()
  }
  // 高光
  ctx.beginPath()
  ctx.moveTo(x+4, y+4)
  ctx.lineTo(x+w-4, y+4)
  ctx.strokeStyle = '#fff8'
  ctx.lineWidth = 3
  ctx.stroke()
  ctx.restore()
}
// 马里奥问号箱绘制
function drawMarioQuestion(ctx, x, y, w, h, hit) {
  ctx.save()
  ctx.fillStyle = hit ? '#e2c04c' : '#f7d560'
  ctx.fillRect(x, y, w, h)
  ctx.strokeStyle = '#fff6'
  ctx.lineWidth = 2
  for (let i = 1; i < 4; i++) {
    ctx.beginPath()
    ctx.moveTo(x + i*w/4, y)
    ctx.lineTo(x + i*w/4, y + h)
    ctx.stroke()
    ctx.beginPath()
    ctx.moveTo(x, y + i*h/4)
    ctx.lineTo(x + w, y + i*h/4)
    ctx.stroke()
  }
  // 问号
  ctx.font = `${Math.floor(w*0.7)}px Arial Black`
  ctx.fillStyle = hit ? '#bfae3a' : '#b97a1e'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('?', x + w/2, y + h/2)
  // 高光
  ctx.beginPath()
  ctx.moveTo(x+4, y+4)
  ctx.lineTo(x+w-4, y+4)
  ctx.strokeStyle = '#fff8'
  ctx.lineWidth = 3
  ctx.stroke()
  ctx.restore()
}
// 美化地面绘制函数
function drawMarioGround(ctx) {
  ctx.save()
  const groundThickness = 160 // 厚泥土
  const grassThickness = 8 // 草坪厚度固定
  // 1. 画泥土主线
  ctx.beginPath()
  ctx.moveTo(groundPoints.value[0].x - cameraX.value, winH.value - groundThickness)
  for (let i = 0; i < groundPoints.value.length; i++) {
    let y = groundPoints.value[i].y
    for (let pf of platforms.value) {
      if (pf.stickToGround && Math.abs(pf.x - groundPoints.value[i].x) < pf.w/2) {
        y = Math.min(y, pf.y + pf.h)
      }
    }
    ctx.lineTo(groundPoints.value[i].x - cameraX.value, y)
  }
  ctx.lineTo(groundPoints.value[groundPoints.value.length - 1].x - cameraX.value, winH.value - groundThickness)
  ctx.closePath()
  ctx.fillStyle = '#a86f2a'
  ctx.fill()
  // 2. 画草坪（厚度始终一致）
  ctx.beginPath()
  for (let i = 0; i < groundPoints.value.length; i++) {
    let y = groundPoints.value[i].y
    for (let pf of platforms.value) {
      if (pf.stickToGround && Math.abs(pf.x - groundPoints.value[i].x) < pf.w/2) {
        y = Math.min(y, pf.y + pf.h)
      }
    }
    ctx.lineTo(groundPoints.value[i].x - cameraX.value, y - grassThickness)
  }
  for (let i = groundPoints.value.length - 1; i >= 0; i--) {
    let y = groundPoints.value[i].y
    for (let pf of platforms.value) {
      if (pf.stickToGround && Math.abs(pf.x - groundPoints.value[i].x) < pf.w/2) {
        y = Math.min(y, pf.y + pf.h)
      }
    }
    ctx.lineTo(groundPoints.value[i].x - cameraX.value, y)
  }
  ctx.closePath()
  ctx.fillStyle = '#5fff3a'
  ctx.shadowColor = '#fff'
  ctx.shadowBlur = 8
  ctx.fill()
  ctx.shadowBlur = 0
  // 3. 泥土砖块纹理
  for (let i = 0; i < groundPoints.value.length - 1; i++) {
    const x0 = groundPoints.value[i].x - cameraX.value
    const x1 = groundPoints.value[i+1].x - cameraX.value
    const y0 = groundPoints.value[i].y
    const y1 = groundPoints.value[i+1].y
    for (let bx = x0; bx < x1; bx += 44) {
      for (let by = Math.max(y0, y1); by < winH.value - groundThickness; by += 44) {
        drawMarioBrick(ctx, bx, by, 44, 44)
      }
    }
  }
  ctx.restore()
}
onMounted(() => {
  ctx = canvas.value.getContext('2d')
  window.addEventListener('keydown', onKey)
  window.addEventListener('keyup', onKeyUp)
  window.addEventListener('resize', onResize)
  // 定时切换天气
  randomWeather()
  if (weatherTimer.value) clearInterval(weatherTimer.value)
  weatherTimer.value = setInterval(randomWeather, 18000)
})
onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
  window.removeEventListener('keyup', onKeyUp)
  window.removeEventListener('resize', onResize)
  cancelAnimationFrame(raf)
  if (weatherTimer.value) clearInterval(weatherTimer.value)
})
</script>

<style scoped>
.runner-container {
  text-align: center; margin: 0; padding: 0;
  min-height: 100vh;
  transition: background 0.6s;
}
.runner-container.theme-day { background: linear-gradient(180deg, #b3e0ff 0%, #e6f7ff 100%); }
.runner-container.theme-night { background: linear-gradient(180deg, #232946 0%, #3e497a 100%); }
.runner-container.theme-forest { background: linear-gradient(180deg, #a8e063 0%, #56ab2f 100%); }
.runner-container.theme-desert { background: linear-gradient(180deg, #fceabb 0%, #f8b500 100%); }
.canvas-wrap { position: fixed; left: 0; top: 0; width: 100vw; height: 100vh; z-index: 1; }
.game-canvas { display: block; width: 100vw; height: 100vh; background: #eef; margin: 0; border: none; }
.pause-btn {
  position: absolute;
  right: 32px; top: 32px;
  width: 48px; height: 48px;
  background: rgba(255,255,255,0.85);
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: #333;
  cursor: pointer;
  z-index: 2;
  transition: background 0.18s;
}
.pause-btn:hover { background: #ffe7b3; }
/* 得分板 */
.score-board {
  position: fixed; left: 48px; top: 32px; z-index: 20;
  background: rgba(255,255,255,0.92);
  border-radius: 18px;
  box-shadow: 0 2px 16px 0 rgba(80,80,160,0.18), 0 0 32px 0 #fff8;
  padding: 18px 38px 18px 38px;
  font-size: 2.1rem;
  font-weight: 800;
  color: #4e6edb;
  letter-spacing: 2px;
  display: flex; align-items: center; gap: 32px;
  animation: fadeInDown 0.7s;
}
@keyframes fadeInDown {
  0% { opacity: 0; transform: translateY(-40px); }
  100% { opacity: 1; transform: translateY(0); }
}
.score-anim {
  font-size: 2.5rem;
  color: #e33;
  font-weight: 900;
  margin-left: 8px;
  transition: color 0.2s, transform 0.2s;
}
.score-anim.pop {
  color: #ffb300;
  animation: popScore 0.3s;
}
@keyframes popScore {
  0% { transform: scale(1); }
  60% { transform: scale(1.35); }
  100% { transform: scale(1); }
}
.best-score {
  font-size: 1.2rem;
  color: #a084ee;
  font-weight: 600;
  margin-left: 18px;
}
/* 开始按钮 */
.start-panel {
  position: fixed; left: 0; top: 0; width: 100vw; height: 100vh; z-index: 30;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.18);
  backdrop-filter: blur(2px);
  animation: fadeIn 0.7s;
}
@keyframes fadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}
.start-content {
  display: flex; flex-direction: column; align-items: center;
}
.start-icon {
  width: 90px; height: 90px;
  background: url('https://img.icons8.com/color/96/000000/rocket--v2.png') no-repeat center/contain;
  margin-bottom: 32px;
  animation: rocketFloat 1.5s infinite alternate;
}
@keyframes rocketFloat {
  0% { transform: translateY(0); }
  100% { transform: translateY(-18px); }
}
.start-btn {
  font-size: 2.2rem;
  font-weight: 900;
  color: #fff;
  background: linear-gradient(90deg, #6a8dff 0%, #a084ee 100%);
  border: none;
  border-radius: 32px;
  padding: 28px 80px;
  box-shadow: 0 4px 32px rgba(106,141,255,0.13);
  cursor: pointer;
  animation: jump 1.2s infinite alternate;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.start-btn:hover {
  background: linear-gradient(90deg, #4e6edb 0%, #7c5fd3 100%);
  box-shadow: 0 8px 32px rgba(106,141,255,0.18);
  transform: scale(1.06);
}
@keyframes jump {
  0% { transform: translateY(0); }
  100% { transform: translateY(-18px); }
}
.fade-enter-active, .fade-leave-active, .modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.4s; }
.fade-enter-from, .fade-leave-to, .modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
/* 暂停/结束菜单 */
.pause-modal, .gameover-modal {
  position: fixed; left: 0; top: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.18); z-index: 100;
  display: flex; align-items: center; justify-content: center;
  animation: modalFadeIn 0.5s;
  backdrop-filter: blur(2px);
}
@keyframes modalFadeIn {
  0% { opacity: 0; }
  100% { opacity: 1; }
}
.pause-content, .gameover-content {
  background: #fff; border-radius: 28px; padding: 48px 68px; box-shadow: 0 8px 40px 0 rgba(80,80,160,0.18);
  text-align: center;
  min-width: 340px;
  animation: panelPop 0.5s;
}
@keyframes panelPop {
  0% { transform: scale(0.8); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.gameover-icon {
  width: 80px; height: 80px;
  background: url('https://img.icons8.com/color/96/000000/trophy.png') no-repeat center/contain;
  margin: 0 auto 18px auto;
  animation: trophyPop 1.2s;
}
@keyframes trophyPop {
  0% { transform: scale(0.2); opacity: 0; }
  80% { transform: scale(1.2); opacity: 1; }
  100% { transform: scale(1); }
}
.pause-score {
  font-size: 1.4rem;
  color: #4e6edb;
  font-weight: 700;
  margin: 12px 0 0 0;
}
</style> 