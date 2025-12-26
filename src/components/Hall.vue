<template>
  <div class="hall-bg">
    <div class="hall-container">
      <div class="lang-switch">
        <button :class="{active: lang==='zh'}" @click="setLang('zh')">中文</button>
        <span>|</span>
        <button :class="{active: lang==='en'}" @click="setLang('en')">English</button>
      </div>
      <h2>{{ t('hall.welcome') }}</h2>
      <div class="btn-group">
        <div class="game-item">
          <img src="@/assets/jzq.png" alt="五子棋" />
          <button @click="goTo('gobang')">{{ t('hall.gobang') }}</button>
        </div>
        <div class="game-item">
          <img src="@/assets/shudu.jpg" alt="数独" />
          <button @click="goTo('sudoku')">{{ t('hall.sudoku') }}</button>
        </div>
        <div class="game-item">
          <img src="@/assets/dds.jpg" alt="打地鼠" />
          <button @click="goTo('whackamole')">{{ t('hall.whackamole') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import { useRouter } from 'vue-router'
const lang = inject('lang')
const setLang = inject('setLang')
const t = inject('t')
const router = useRouter()
function goTo(game) {
  router.push('/' + game)
}
</script>

<style scoped>
.hall-bg {
  min-height: 100vh;
  width: 100vw;
  position: fixed;
  left: 0; top: 0;
  background: url('@/assets/b2.png') center center / cover no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hall-container {
  background: rgba(255,255,255,0.85);
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  padding: 48px 60px 40px 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 600px;
  z-index: 2;
}
h2 {
  font-size: 1.6rem;
  font-weight: bold;
  margin-bottom: 32px;
  color: #222;
}
.btn-group {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 48px;
  margin-top: 40px;
  width: 100%;
  flex-wrap: wrap;
}
.game-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.game-item img {
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(25,118,210,0.15);
  background: #fff;
  transition: transform 0.2s, box-shadow 0.2s;
}
.game-item:hover img {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(25,118,210,0.25);
}
.game-item button {
  padding: 14px 0;
  font-size: 18px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(90deg, #6a8dff 0%, #a084ee 100%);
  color: #fff;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(25,118,210,0.2);
  width: 140px;
  letter-spacing: 1px;
}
.game-item button:hover {
  background: linear-gradient(90deg, #4e6edb 0%, #7c5fd3 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(25,118,210,0.3);
}
.game-item button:active {
  transform: translateY(0);
}
.lang-switch {
  position: absolute;
  top: 18px;
  right: 32px;
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