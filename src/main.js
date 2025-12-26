import './assets/main.css'
import { createApp, ref } from 'vue'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router'
import Login from './components/Login.vue'
import Hall from './components/Hall.vue'
import clickSoundUrl from './assets/鼠标点击音效.mp3'
import langDict from './assets/lang.js'
import Shop from './components/shop/Shop.vue'
import SkinRepo from './components/SkinRepo.vue'

const routes = [
  { path: '/', component: Login },
  { path: '/hall', component: Hall },
  { path: '/tictactoe', component: () => import('./components/TicTacToe.vue') },
  { path: '/sudoku', component: () => import('./components/Sudoku.vue') },
  { path: '/whackamole', component: () => import('./components/WhackAMole.vue') },
  { path: '/runner', component: () => import('./components/Runner.vue') },
  { path: '/tank', component: () => import('./components/Tank.vue') },
  { path: '/bumper', component: () => import('./components/Bumper.vue') },
  { path: '/gobang', component: () => import('./components/Gobang.vue') },
  { path: '/shop', component: Shop },
  { path: '/skinrepo', component: SkinRepo },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 全局鼠标点击音效
let clickAudio = null
function playClickSound() {
  if (!clickAudio) {
    clickAudio = new Audio(clickSoundUrl)
    clickAudio.preload = 'auto'
  }
  // 立即重置到头，允许连点
  clickAudio.currentTime = 0
  clickAudio.play()
}
document.addEventListener('mousedown', playClickSound)

// 全局多语言 provide
const lang = ref(localStorage.getItem('lang') || 'zh')
function setLang(l) {
  lang.value = l
  localStorage.setItem('lang', l)
}
function t(path) {
  // 支持 t('hall.gobang') 形式
  const keys = path.split('.')
  let cur = langDict[lang.value]
  for (const k of keys) {
    if (!cur) return path
    cur = cur[k]
  }
  return cur || path
}

const app = createApp(App)
app.provide('lang', lang)
app.provide('setLang', setLang)
app.provide('t', t)
app.use(router)
app.mount('#app')
