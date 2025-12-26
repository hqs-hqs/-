<template>
  <div class="shop-content">
    <div class="shop-header">
      <span class="shop-title">{{ t('shop.title') }}</span>
      <div class="shop-coins">
        <span class="coin-emoji">🪙</span>
        <span class="coin-amount">{{ coins }}</span>
      </div>
      <router-link to="/whackamole"><button class="shop-close">{{ t('shop.backToGame') }}</button></router-link>
    </div>
    <div class="shop-tabs">
      <button v-for="cat in categories" :key="cat.id" :class="['shop-tab',cat.id===activeCategory?'active':'']" @click="activeCategory=cat.id">{{cat.name}}</button>
    </div>
    <div class="shop-list">
      <div v-for="item in filteredItems" :key="item.id" class="shop-item-card">
        <div class="shop-item-img" :style="{background:item.bg}">
          <img v-if="item.image" :src="item.image" :alt="item.name" />
          <span v-else class="shop-item-emoji">{{item.emoji}}</span>
        </div>
        <div class="shop-item-info">
          <div class="shop-item-name">{{item.name}}</div>
          <div class="shop-item-price"><span class="coin-emoji">🪙</span>{{item.price}}</div>
        </div>
        <button v-if="!item.owned" class="shop-btn" :disabled="coins<item.price" @click="addToCart(item)">{{ t('shop.addToCart') }}</button>
        <span v-else class="shop-owned">{{ t('shop.owned') }}</span>
      </div>
    </div>
    <!-- 购物车弹窗 -->
    <div v-if="showCart" class="shop-cart-modal center-modal">
      <div class="shop-cart-content">
        <div class="cart-header">
          <span>{{ t('shop.cart') }}</span>
          <button class="cart-close" @click="showCart=false">×</button>
        </div>
        <div v-if="cart.length===0" class="cart-empty">
          <div class="cart-empty-icon">🛒</div>
          <div class="cart-empty-text">{{ t('shop.cartEmpty') }}</div>
          <router-link to="/whackamole">
            <button class="wam-btn back pretty-btn">{{ t('shop.backToGame') }}</button>
          </router-link>
        </div>
        <div v-else class="cart-list">
          <div v-for="(item,i) in cart" :key="item.id" class="cart-item">
            <span>{{item.name}}</span>
            <span class="cart-item-price"><span class="coin-emoji">🪙</span>{{item.price}}</span>
            <button class="cart-remove" @click="removeFromCart(i)">{{ t('shop.remove') }}</button>
          </div>
        </div>
        <div class="cart-footer" v-if="cart.length">
          <span>{{ t('shop.total') }}：<span class="coin-emoji">🪙</span>{{cartTotal}}</span>
          <button class="cart-checkout" :disabled="coins<cartTotal" @click="checkout">{{ t('shop.checkout') }}</button>
        </div>
      </div>
    </div>
    <!-- 购物车浮动按钮 -->
    <div class="shop-cart-float" @click="showCart=true">
      <span class="cart-emoji">🛒</span>
      <span class="cart-count" v-if="cart.length">{{cart.length}}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, inject } from 'vue'
import { useRoute } from 'vue-router'
import { gameAPI, shopAPI } from '@/api'
const t = inject('t')

// 检查是否使用API模式
const useAPI = import.meta.env.VITE_USE_API === 'true' || localStorage.getItem('use_api') === 'true'
// 获取当前用户ID
const currentUserId = localStorage.getItem('game_user_id') ? Number(localStorage.getItem('game_user_id')) : null

// 商品模拟数据
const categories = [
  {id:'mouse',name:t('skinrepo.mouse')},
  {id:'bg',name:t('skinrepo.bg')},
  {id:'hammer',name:t('skinrepo.hammer')}
]
const allItems = ref([
  {id:'brown',name:'棕色地鼠',category:'mouse',price:30,emoji:'🐭'},
  {id:'gold',name:'金色地鼠',category:'mouse',price:60,emoji:'🐹'},
  {id:'blue',name:'蓝色地鼠',category:'mouse',price:80,emoji:'👨‍🚀'},
  {id:'gray',name:'灰色地鼠',category:'mouse',price:40,emoji:'🐁'},
  {id:'bg1',name:'草地',category:'bg',price:40,emoji:'🌱'},
  {id:'bg2',name:'森林背景',category:'bg',price:70,emoji:'🌲'},
  {id:'hammer1',name:'木锤',category:'hammer',price:20,emoji:'🔨'},
  {id:'hammer2',name:'黄金锤',category:'hammer',price:90,emoji:'⚒️'},
])
const activeCategory = ref('mouse')

// 默认免费皮肤
const defaultFreeSkins = ['brown', 'hammer1', 'bg1']

async function loadOwnedSkins() {
  if (useAPI && currentUserId) {
    try {
      const response = await gameAPI.getUserGameData(currentUserId, 'whackamole')
      if (response.success && response.data && response.data.ownedSkins) {
        let skins = Array.isArray(response.data.ownedSkins) ? response.data.ownedSkins : []
        // 确保默认免费皮肤包含在内
        defaultFreeSkins.forEach(skinId => {
          if (!skins.includes(skinId)) {
            skins.push(skinId)
          }
        })
        return skins
      }
    } catch (error) {
      console.error('获取拥有皮肤失败:', error)
    }
  }
  // localStorage模式（兼容）
  let arr = []
  try { arr = JSON.parse(localStorage.getItem('wam_skins_owned')||'[]') } catch { arr = [] }
  // 确保默认免费皮肤包含在内
  defaultFreeSkins.forEach(skinId => {
    if (!arr.includes(skinId)) {
      arr.push(skinId)
    }
  })
  return arr
}

const ownedSkins = ref([])

// 金币余额
const coins = ref(9999)

// 从数据库加载金币
async function loadCoins() {
  if (useAPI && currentUserId) {
    try {
      const response = await shopAPI.getCoins(currentUserId)
      if (response.success && response.data !== undefined) {
        coins.value = Number(response.data) || 9999
        // 同步到 localStorage 作为备份
        localStorage.setItem('wam_coins', String(coins.value))
        return
      }
    } catch (error) {
      console.error('获取金币失败:', error)
    }
  }
  // localStorage模式（兼容旧数据）
  coins.value = Number(localStorage.getItem('wam_coins') || 9999)
}

// 保存金币到数据库
async function saveCoins(newCoins) {
  if (useAPI && currentUserId) {
    try {
      const response = await shopAPI.updateCoins(currentUserId, newCoins)
      if (response.success) {
        console.log('金币已保存到数据库:', newCoins)
        // 同步到 localStorage 作为备份
        localStorage.setItem('wam_coins', String(newCoins))
        return
      }
    } catch (error) {
      console.error('保存金币失败:', error)
    }
  }
  // localStorage模式（兼容）
  localStorage.setItem('wam_coins', String(newCoins))
}

// 页面每次进入都刷新金币和拥有的皮肤
onMounted(async () => {
  ownedSkins.value = await loadOwnedSkins()
  await loadCoins()
  document.addEventListener('visibilitychange', handleVisibility)
})
onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibility)
})
function handleVisibility() {
  if (document.visibilityState === 'visible') {
    loadCoins()
  }
}

const route = useRoute()
watch(
  () => route.fullPath,
  async () => {
    ownedSkins.value = await loadOwnedSkins()
    await loadCoins()
  }
)
// 监听金币变化并保存
watch(coins, (newValue) => {
  saveCoins(newValue)
})

// 购物车
const cart = ref([])
const showCart = ref(false)
const cartTotal = computed(()=>cart.value.reduce((sum,i)=>sum+i.price,0))
function addToCart(item) {
  if (!cart.value.find(i=>i.id===item.id)) cart.value.push(item)
}
function removeFromCart(idx) {
  cart.value.splice(idx,1)
}
async function checkout() {
  if (coins.value<cartTotal.value) return
  
  // 收集购买的皮肤ID
  const purchasedSkinIds = []
  cart.value.forEach(item=>{
    const found = allItems.value.find(i=>i.id===item.id)
    if (found) {
      found.owned = true
      purchasedSkinIds.push(item.id)
    }
  })
  
  // 更新拥有的皮肤列表
  purchasedSkinIds.forEach(skinId => {
    if (!ownedSkins.value.includes(skinId)) {
      ownedSkins.value.push(skinId)
    }
  })
  
  // 保存到数据库（如果使用API）
  if (useAPI && currentUserId && purchasedSkinIds.length > 0) {
    try {
      // 获取当前游戏数据
      const response = await gameAPI.getUserGameData(currentUserId, 'whackamole')
      let currentData = {}
      if (response.success && response.data) {
        currentData = response.data
      }
      // 更新拥有的皮肤列表
      const updatedOwnedSkins = [...ownedSkins.value]
      // 保存到数据库
      await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
        ...currentData,
        ownedSkins: updatedOwnedSkins
      })
      console.log('购买的皮肤已保存到数据库:', purchasedSkinIds)
    } catch (error) {
      console.error('保存购买的皮肤到数据库失败:', error)
    }
  }
  
  // localStorage模式（兼容）
  let owned = []
  try { owned = JSON.parse(localStorage.getItem('wam_skins_owned') || '[]') } catch { owned = [] }
  purchasedSkinIds.forEach(skinId => {
    if (!owned.includes(skinId)) {
      owned.push(skinId)
    }
  })
  localStorage.setItem('wam_skins_owned', JSON.stringify(owned))
  
  // 更新金币（扣款）
  const newCoins = coins.value - cartTotal.value
  coins.value = newCoins
  // 保存金币到数据库
  await saveCoins(newCoins)
  
  cart.value = []
}

// 计算商品是否已拥有
const itemsWithOwned = computed(() => allItems.value.map(item => ({
  ...item,
  owned: ownedSkins.value.includes(item.id)
})))
const filteredItems = computed(()=>itemsWithOwned.value.filter(i=>i.category===activeCategory.value))
</script>

<style scoped>
.shop-content {
  width: 100vw;
  height: 100vh;
  min-width: 100vw;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: none !important;
}
.shop-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.shop-title {
  font-size: 2rem;
  font-weight: bold;
  color: #3a7bd5;
  letter-spacing: 2px;
}
.shop-coins {
  display: flex;
  align-items: center;
  font-size: 1.18rem;
  font-weight: bold;
  color: #ffb300;
  background: #fffbe6;
  border-radius: 18px;
  padding: 4px 16px;
  box-shadow: 0 2px 8px #ffd70022;
  margin-right: 12px;
}
.coin-emoji { font-size: 1.3em; margin-right: 4px; }
.coin-amount { font-size: 1.1em; }
.shop-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: #888;
  cursor: pointer;
  margin-left: 8px;
}
.shop-tabs {
  display: flex;
  gap: 18px;
  margin-bottom: 18px;
}
.shop-tab {
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  border: none;
  border-radius: 18px;
  padding: 8px 28px;
  font-size: 1.08rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.shop-tab.active {
  background: linear-gradient(90deg, #ffd700, #ffb347);
  color: #374151;
  box-shadow: 0 2px 12px #ffd70033;
  transform: scale(1.08);
}
.shop-list {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  justify-content: center;
  margin-bottom: 32px;
}
.shop-item-card {
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 4px 18px #7b8aff22;
  padding: 18px 18px 12px 18px;
  min-width: 120px;
  max-width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  transition: box-shadow .18s, transform .18s;
}
.shop-item-card:hover {
  box-shadow: 0 8px 32px #7b8aff44;
  transform: scale(1.06) rotate(-2deg);
  z-index: 2;
}
.shop-item-img {
  width: 60px; height: 60px;
  border-radius: 16px;
  background: #e3f0ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  font-size: 2.2rem;
  box-shadow: 0 2px 8px #7b8aff11;
}
.shop-item-emoji { font-size: 2.2rem; }
.shop-item-info {
  text-align: center;
  margin-bottom: 8px;
}
.shop-item-name {
  font-size: 1.08rem;
  color: #234d3c;
  font-weight: 600;
  letter-spacing: 1px;
}
.shop-item-price {
  font-size: 1.02rem;
  color: #ffb300;
  font-weight: bold;
  margin-top: 2px;
}
.shop-btn {
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  border: none;
  border-radius: 18px;
  padding: 7px 18px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.shop-btn:disabled {
  background: #eee;
  color: #bbb;
  cursor: not-allowed;
}
.shop-owned {
  color: #42a5f5;
  font-weight: bold;
  margin-top: 8px;
}
.shop-cart-float {
  position: fixed;
  right: 48px;
  bottom: 48px;
  background: linear-gradient(135deg,#ffd700 60%,#ffb347 100%);
  color: #fff;
  border-radius: 50%;
  width: 64px; height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.1rem;
  box-shadow: 0 4px 18px #ffd70033;
  cursor: pointer;
  z-index: 999;
  transition: box-shadow .18s, transform .18s;
}
.shop-cart-float:hover {
  box-shadow: 0 8px 32px #ffd70044;
  transform: scale(1.10) rotate(6deg);
}
.cart-emoji { font-size: 2.1rem; }
.cart-count {
  position: absolute;
  right: 10px; top: 10px;
  background: #f44336;
  color: #fff;
  border-radius: 50%;
  width: 22px; height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  font-weight: bold;
  box-shadow: 0 2px 8px #f4433622;
}
.shop-cart-modal {
  position: fixed;
  right: 0; bottom: 0;
  width: 340px;
  backdrop-filter: blur(8px) saturate(1.2);
  background: linear-gradient(135deg,rgba(255,255,255,0.98) 60%,rgba(173,216,255,0.92) 100%);
  border-radius: 32px;
  box-shadow: 0 12px 48px 0 rgba(80,120,255,0.18), 0 2px 12px 0 rgba(80,120,255,0.10);
  padding: 28px 24px 18px 24px;
  z-index: 2000;
  animation: fadeIn .3s;
}
.shop-cart-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cart-header {
  font-size: 1.35rem;
  font-weight: 800;
  color: #3a7bd5;
  letter-spacing: 1px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.cart-header::before {
  content: '🛒';
  font-size: 1.5em;
  margin-right: 6px;
}
.cart-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #888;
  cursor: pointer;
}
.cart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bbb;
  margin: 32px 0 18px 0;
  gap: 12px;
}
.cart-empty-icon {
  font-size: 3.2rem;
  margin-bottom: 8px;
  opacity: 0.7;
}
.cart-empty-text {
  font-size: 1.18rem;
  font-weight: 600;
  color: #7a8cff;
  margin-bottom: 10px;
}
.pretty-btn {
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  border: none;
  border-radius: 22px;
  padding: 10px 32px;
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 2px 8px rgba(106,141,255,0.10);
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
  margin-top: 8px;
}
.pretty-btn:hover {
  background: linear-gradient(90deg, #4e6edb 0%, #7c5fd3 100%);
  box-shadow: 0 4px 16px rgba(106,141,255,0.13);
  transform: translateY(-2px) scale(1.04);
}
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.cart-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f5faff;
  border-radius: 12px;
  padding: 6px 12px;
  font-size: 1.05rem;
}
.cart-item-price {
  color: #ffb300;
  font-weight: bold;
  margin-left: 8px;
}
.cart-remove {
  background: none;
  border: none;
  color: #f44336;
  font-size: 1.1rem;
  cursor: pointer;
  margin-left: 8px;
}
.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 1.08rem;
}
.cart-checkout {
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  border: none;
  border-radius: 18px;
  padding: 7px 18px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  margin-left: 18px;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.cart-checkout:disabled {
  background: #eee;
  color: #bbb;
  cursor: not-allowed;
}
@keyframes fadeIn {
  0% { opacity: 0; transform: translateY(40px); }
  100% { opacity: 1; transform: translateY(0); }
}
.center-modal {
  position: fixed !important;
  left: 50% !important;
  top: 50% !important;
  transform: translate(-50%, -50%) !important;
  right: auto !important;
  bottom: auto !important;
  z-index: 2000;
}
</style> 