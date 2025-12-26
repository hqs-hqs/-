<template>
  <div class="skinrepo-content">
    <h2>{{ t('skinrepo.title') }}</h2>
    <div class="wam-skin-tabs">
      <button v-for="cat in skinCategories" :key="cat.id" :class="['wam-skin-tab',skinActiveCategory===cat.id&&'active']" @click="skinActiveCategory=cat.id">{{cat.name}}</button>
    </div>
    <div class="wam-skin-list">
      <div v-for="skin in skins.filter(s=>s.category===skinActiveCategory)" :key="skin.id" :class="['wam-skin-item', tempSkin===skin.id && 'selected', !ownedSkins.includes(skin.id) && 'locked']" @click="ownedSkins.includes(skin.id) ? tempSkin=skin.id : null">
        <div class="wam-skin-demo" :style="{background: skin.color}">
          <span v-if="skin.emoji" style="font-size:1.8em;line-height:38px;">{{skin.emoji}}</span>
        </div>
        <div class="wam-skin-name">{{skin.name}}</div>
        <div v-if="!ownedSkins.includes(skin.id)" class="wam-skin-locked">未拥有</div>
        <button v-else-if="skin.category==='bg'" class="wam-btn" :disabled="selectedBg===skin.id" @click.stop="setBgSkin(skin.id)">
          {{ selectedBg===skin.id ? t('skinrepo.used') : t('skinrepo.useBg') }}
        </button>
      </div>
    </div>
    <button class="wam-btn" @click="confirmSkin">{{ t('skinrepo.confirm') }}</button>
    <router-link to="/whackamole"><button class="wam-btn back">{{ t('skinrepo.backToGame') }}</button></router-link>
  </div>
</template>
<script setup>
import { ref, onMounted, watch, inject } from 'vue'
import { useRoute } from 'vue-router'
import { gameAPI } from '@/api'
const t = inject('t')

// 检查是否使用API模式
const useAPI = import.meta.env.VITE_USE_API === 'true' || localStorage.getItem('use_api') === 'true'
// 获取当前用户ID
const currentUserId = localStorage.getItem('game_user_id') ? Number(localStorage.getItem('game_user_id')) : null

// 皮肤分类Tab
const skinCategories = [
  {id:'mouse',name:t('skinrepo.mouse')},
  {id:'bg',name:t('skinrepo.bg')},
  {id:'hammer',name:t('skinrepo.hammer')}
]
const skinActiveCategory = ref('mouse')
const skins = [
  {id: 'brown', name: '棕色地鼠', color: '#a86c3c', category: 'mouse', emoji: '🐭'},
  {id: 'gray', name: '灰色地鼠', color: '#888', category: 'mouse', emoji: '🐁'},
  {id: 'gold', name: '金色地鼠', color: 'gold', category: 'mouse', emoji: '🐹'},
  {id: 'blue', name: '蓝色地鼠', color: '#4a90e2', category: 'mouse', emoji: '👨‍🚀'},
  {id: 'bg1', name: '草地', category: 'bg', emoji: '🌱'},
  {id: 'bg2', name: '森林背景', category: 'bg', emoji: '🌲'},
  {id: 'bg3', name: '太空背景', category: 'bg', emoji: '🌌'},
  {id: 'hammer1', name: '木锤', category: 'hammer', emoji: '🔨'},
  {id: 'hammer2', name: '黄金锤', category: 'hammer', emoji: '⚒️'},
]
const selectedBg = ref(localStorage.getItem('wam_bg') || 'bg1')
const selectedSkin = ref(localStorage.getItem('wam_skin') || 'brown')
const selectedHammer = ref(localStorage.getItem('wam_hammer') || 'hammer1')
const tempSkin = ref('brown')
const route = useRoute()

// 默认免费皮肤（所有用户初始拥有）
const defaultFreeSkins = ['brown', 'hammer1', 'bg1']

// 拥有的皮肤列表
const ownedSkins = ref([])

// 根据当前分类获取当前选中的皮肤
function getCurrentSelectedSkin() {
  if (skinActiveCategory.value === 'mouse') {
    return selectedSkin.value
  } else if (skinActiveCategory.value === 'bg') {
    return selectedBg.value
  } else if (skinActiveCategory.value === 'hammer') {
    return selectedHammer.value
  }
  return 'brown'
}

// 加载游戏数据（包括选中的皮肤）
async function loadGameData() {
  if (useAPI && currentUserId) {
    try {
      const response = await gameAPI.getUserGameData(currentUserId, 'whackamole')
      if (response.success && response.data) {
        const data = response.data
        // 加载选中的皮肤
        if (data.selectedSkin) {
          selectedSkin.value = data.selectedSkin
        }
        if (data.selectedBg) {
          selectedBg.value = data.selectedBg
        }
        if (data.selectedHammer) {
          selectedHammer.value = data.selectedHammer
        }
        // 加载拥有的皮肤
        if (data.ownedSkins) {
          let skins = Array.isArray(data.ownedSkins) ? data.ownedSkins : []
          // 确保默认免费皮肤包含在内
          defaultFreeSkins.forEach(skinId => {
            if (!skins.includes(skinId)) {
              skins.push(skinId)
            }
          })
          ownedSkins.value = skins
        } else {
          ownedSkins.value = [...defaultFreeSkins]
          // 保存默认值到数据库
          await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
            ownedSkins: defaultFreeSkins
          })
        }
        // 根据当前分类设置 tempSkin
        tempSkin.value = getCurrentSelectedSkin()
        return
      }
    } catch (error) {
      console.error('获取游戏数据失败:', error)
    }
  }
  // localStorage模式（兼容旧数据）
  selectedSkin.value = localStorage.getItem('wam_skin') || 'brown'
  selectedBg.value = localStorage.getItem('wam_bg') || 'bg1'
  selectedHammer.value = localStorage.getItem('wam_hammer') || 'hammer1'
  let arr = []
  try { arr = JSON.parse(localStorage.getItem('wam_skins_owned')||'[]') } catch { arr = [] }
  // 确保默认免费皮肤包含在内
  defaultFreeSkins.forEach(skinId => {
    if (!arr.includes(skinId)) {
      arr.push(skinId)
    }
  })
  ownedSkins.value = arr
  tempSkin.value = getCurrentSelectedSkin()
}

async function confirmSkin() {
  if (skinActiveCategory.value === 'mouse') {
    selectedSkin.value = tempSkin.value
    if (useAPI && currentUserId) {
      try {
        await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
          selectedSkin: tempSkin.value
        })
      } catch (error) {
        console.error('保存皮肤失败:', error)
      }
    }
    localStorage.setItem('wam_skin', tempSkin.value)
    alert('地鼠皮肤已切换！')
  } else if (skinActiveCategory.value === 'bg') {
    selectedBg.value = tempSkin.value
    if (useAPI && currentUserId) {
      try {
        await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
          selectedBg: tempSkin.value
        })
      } catch (error) {
        console.error('保存背景皮肤失败:', error)
      }
    }
    localStorage.setItem('wam_bg', tempSkin.value)
    alert('背景皮肤已切换！')
  } else if (skinActiveCategory.value === 'hammer') {
    selectedHammer.value = tempSkin.value
    if (useAPI && currentUserId) {
      try {
        await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
          selectedHammer: tempSkin.value
        })
      } catch (error) {
        console.error('保存锤子皮肤失败:', error)
      }
    }
    localStorage.setItem('wam_hammer', tempSkin.value)
    alert('锤子皮肤已切换！')
  }
}

async function setBgSkin(id) {
  if (id === 'bg1' || id === 'bg2' || id === 'bg3') {
    selectedBg.value = id
    tempSkin.value = id
    if (useAPI && currentUserId) {
      try {
        await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
          selectedBg: id
        })
      } catch (error) {
        console.error('保存背景皮肤失败:', error)
      }
    }
    localStorage.setItem('wam_bg', id)
    alert('背景皮肤已切换！')
  }
}

// 当切换分类时，更新 tempSkin 为当前分类的选中皮肤
watch(skinActiveCategory, () => {
  tempSkin.value = getCurrentSelectedSkin()
})

onMounted(() => {
  loadGameData()
})

watch(
  () => route.fullPath,
  () => {
    loadGameData()
  }
)
</script>
<style scoped>
.skinrepo-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  width: 100vw;
}
.wam-skin-tabs {
  display: flex;
  gap: 18px;
  margin-bottom: 12px;
  justify-content: center;
}
.wam-skin-tab {
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  border: none;
  border-radius: 18px;
  padding: 6px 24px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.wam-skin-tab.active {
  background: linear-gradient(90deg, #ffd700, #ffb347);
  color: #374151;
  box-shadow: 0 2px 12px #ffd70033;
  transform: scale(1.08);
}
.wam-skin-list {
  display: flex;
  flex-direction: row;
  gap: 28px;
  margin: 28px 0 18px 0;
}
.wam-skin-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  border-radius: 18px;
  background: #f5faff;
  box-shadow: 0 2px 10px #7b8aff11;
  padding: 18px 22px 12px 22px;
  transition: box-shadow .18s, transform .18s, border .18s;
  border: 2.5px solid transparent;
  min-width: 80px;
}
.wam-skin-item.selected {
  border: 2.5px solid #42a5f5;
  box-shadow: 0 4px 18px #42a5f533;
  transform: scale(1.08) rotate(-2deg);
}
.wam-skin-item.locked {
  opacity: 0.5;
  cursor: not-allowed;
  position: relative;
}
.wam-skin-item.locked::after {
  content: '🔒';
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 1.2rem;
}
.wam-skin-locked {
  color: #999;
  font-size: 0.9rem;
  margin-top: 8px;
  font-weight: 600;
}
.wam-skin-demo {
  width: 38px; height: 38px;
  border-radius: 50%;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px #aaa2;
  border: 2px solid #fff;
}
.wam-skin-name {
  font-size: 1.08rem;
  color: #234d3c;
  font-weight: 600;
  letter-spacing: 1px;
}
.wam-btn {
  font-size: 1.08rem;
  font-weight: 600;
  border: none;
  border-radius: 18px;
  padding: 10px 32px;
  cursor: pointer;
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  box-shadow: 0 2px 8px rgba(66,165,245,0.10);
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
  margin-top: 18px;
}
.wam-btn.back {
  background: linear-gradient(90deg, #bdbdbd, #90caf9);
  color: #374151;
  margin-left: 18px;
}
.wam-btn:hover {
  background: linear-gradient(90deg, #7e57c2, #42a5f5);
  transform: scale(1.06);
}
</style> 