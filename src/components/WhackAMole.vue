<template>
  <div class="wam-fullscreen" :class="selectedBg">
    <!-- SVG背景：动态云朵、蝴蝶、花朵、石头 -->
    <svg class="wam-bg-svg" viewBox="0 0 1920 1080" preserveAspectRatio="none">
      <defs>
        <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#b3e0ff"/>
          <stop offset="100%" stop-color="#e0f7fa"/>
        </linearGradient>
        <linearGradient id="grass" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#a8e063"/>
          <stop offset="100%" stop-color="#56ab2f"/>
        </linearGradient>
        <radialGradient id="holeShadow" cx="0.5" cy="0.6" r="0.5">
          <stop offset="60%" stop-color="#222" stop-opacity="0.7"/>
          <stop offset="100%" stop-color="transparent"/>
        </radialGradient>
        <linearGradient id="grassfade" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#fff" stop-opacity="0.7"/>
          <stop offset="100%" stop-color="transparent"/>
        </linearGradient>
      </defs>
      <!-- 天空 -->
      <rect x="0" y="0" width="1920" height="650" fill="url(#sky)"/>
      <!-- 动态云朵 -->
      <g v-for="(cloud, i) in clouds" :key="i">
        <g :style="{ transform: `translateX(${cloud.x}px)` }" class="cloud-anim" :class="'cloud-'+i">
          <ellipse :cx="cloud.cx" :cy="cloud.cy" :rx="cloud.rx" :ry="cloud.ry" fill="#fff" :opacity="cloud.opacity"/>
          <ellipse :cx="cloud.cx+cloud.rx*0.7" :cy="cloud.cy-cloud.ry*0.3" :rx="cloud.rx*0.7" :ry="cloud.ry*0.7" fill="#fff" :opacity="cloud.opacity*0.8"/>
        </g>
      </g>
      <!-- 草地 -->
      <rect x="0" y="650" width="1920" height="430" fill="url(#grass)"/>
      <!-- 草地底部柔和渐变 -->
      <rect x="0" y="950" width="1920" height="80" fill="url(#grassfade)" opacity="0.5"/>
      
      <!-- 森林皮肤专属装饰：只在底部用一丛矮草和一棵树 -->
      <g v-if="selectedBg === 'bg2'">
        <image :href="tree2Png" x="70" y="750" width="128" height="179" />
        <image :href="tree2Png" x="140" y="650" width="115" height="170" />
        <image :href="tree2Png" x="310" y="850" width="122" height="180" />
        <image :href="tree2Png" x="600" y="650" width="130" height="165" />
        <image :href="tree2Png" x="600" y="860" width="118" height="178" />
        <image :href="tree2Png" x="960" y="740" width="125" height="172" />
        <image :href="tree2Png" x="1250" y="600" width="120" height="180" />
        <image :href="tree2Png" x="1390" y="870" width="112" height="168" />
        <image :href="tree2Png" x="1510" y="740" width="129" height="175" />
        <image :href="tree2Png" x="1800" y="600" width="117" height="169" />
        <image :href="tree2Png" x="1820" y="900" width="126" height="177" />
      </g>
      
      <!-- 花朵装饰 -->
      <g>
        <circle cx="300" cy="1000" r="12" fill="#ffb6c1"/>
        <circle cx="320" cy="1010" r="8" fill="#ffd700"/>
        <circle cx="340" cy="990" r="10" fill="#b39ddb"/>
        <circle cx="1600" cy="1000" r="12" fill="#ffb6c1"/>
        <circle cx="1580" cy="1020" r="8" fill="#ffd700"/>
        <circle cx="1560" cy="990" r="10" fill="#b39ddb"/>
      </g>
      <!-- 石头装饰 -->
      <g>
        <ellipse cx="600" cy="980" rx="30" ry="12" fill="#bbb"/>
        <ellipse cx="1200" cy="1000" rx="40" ry="16" fill="#888"/>
        <ellipse cx="1700" cy="950" rx="30" ry="14" fill="#bbb"/>
      </g>
      <!-- 动态蝴蝶 -->
      <g v-for="(butterfly, i) in butterflies" :key="i">
        <g :style="{ transform: `translate(${butterfly.x}px, ${butterfly.y}px)` }" class="butterfly-anim" :class="'butterfly-'+i">
          <ellipse cx="0" cy="0" rx="10" ry="18" fill="#ffb6c1"/>
          <ellipse cx="20" cy="0" rx="10" ry="18" fill="#ffd700"/>
          <circle cx="10" cy="0" r="6" fill="#6a4"/>
        </g>
      </g>
    </svg>
    <!-- 森林皮肤专属装饰：用 img 绝对定位渲染 -->
    <!-- 已移除 -->
    <!-- 入口页：活泼圆形按钮布局 -->
    <div v-if="stage==='entry'" class="wam-entry-panel lively">
      <h2>{{ t('whackamole.title') }}</h2>
      <div class="wam-btn-circle-row">
        <button class="wam-btn-circle start" @click="closeAllPopupsAndGoMode">
          <span class="icon">🎮</span>
          <span>{{ t('whackamole.start') }}</span>
        </button>
        <button class="wam-btn-circle exit" @click="exitGame">
          <span class="icon">❌</span>
          <span>{{ t('whackamole.exit') }}</span>
        </button>
      </div>
      <div class="wam-btn-circle-row">
        <router-link to="/skinrepo" custom v-slot="{navigate}">
          <button class="wam-btn-circle skin" @click="navigate">
            <span class="icon">🎨</span>
            <span>{{ t('whackamole.skin') }}</span>
          </button>
        </router-link>
        <router-link to="/shop" custom v-slot="{navigate}">
          <button class="wam-btn-circle shop" @click="navigate">
            <span class="icon">🛒</span>
            <span>{{ t('whackamole.shop') }}</span>
          </button>
        </router-link>
      </div>
    </div>
    <!-- 商城弹窗 -->
    <div v-if="showShopModal" class="wam-shop-modal">
      <div class="wam-shop-content">
        <Shop @close="showShopModal=false" />
      </div>
    </div>
    <!-- 皮肤选择弹窗 -->
    <div v-if="showSkinModal" class="wam-skin-modal">
      <div class="wam-skin-content">
        <button class="wam-skin-close" @click="showSkinModal=false">×</button>
        <h3>选择你的地鼠皮肤</h3>
        <div class="wam-skin-tabs">
          <button v-for="cat in skinCategories" :key="cat.id" :class="['wam-skin-tab',skinActiveCategory===cat.id&&'active']" @click="skinActiveCategory=cat.id">{{cat.name}}</button>
        </div>
        <div class="wam-skin-list">
          <div v-for="skin in skins.filter(s=>s.category===skinActiveCategory && ownedSkins.includes(s.id))" :key="skin.id" :class="['wam-skin-item', tempSkin===skin.id && 'selected']" @click="tempSkin=skin.id">
            <div class="wam-skin-demo" :style="{background: skin.color}"></div>
            <div class="wam-skin-name">{{skin.name}}</div>
          </div>
        </div>
        <button class="wam-btn" @click="confirmSkin">确认</button>
      </div>
    </div>
    <!-- 模式选择页 -->
    <div v-if="stage==='mode'" class="wam-mode-panel">
      <h2>{{ t('whackamole.levelSelect') }}</h2>
      <div class="mode-list">
        <div :class="['mode-item', modeSel==='endless'&&'selected']" @click="modeSel='endless'">
          <div class="mode-title">{{ t('whackamole.endless') }}</div>
          <div class="mode-desc">{{ t('whackamole.endlessDesc') }}</div>
        </div>
        <div :class="['mode-item', modeSel==='stage'&&'selected']" @click="modeSel='stage'">
          <div class="mode-title">{{ t('whackamole.level') }}</div>
          <div class="mode-desc">{{ t('whackamole.levelDesc') }}</div>
        </div>
      </div>
      <div class="mode-btn-row">
        <button class="ui-btn start" @click="startSelectedMode">{{ t('whackamole.start') }}</button>
        <button class="ui-btn back" @click="stage='entry'">{{ t('whackamole.back') }}</button>
      </div>
    </div>
    <!-- 无尽模式游戏页 -->
    <div v-if="stage==='endless'" class="wam-game-panel">
      <div class="wam-ui-corner card-ui">
        <div class="ui-title">{{ t('whackamole.endless') }}</div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.score') }}：</span><span class="ui-value score">{{ score }}</span></div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.miss') }}：</span><span class="ui-value miss">{{ endlessMissRounds }}/10</span></div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.timeUsed') }}：</span><span class="ui-value time">{{ endlessTimeStr }}</span></div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.bestScore') }}：</span><span class="ui-value best">{{ endlessBest }}</span></div>
        <div class="ui-rank-row">
          <label class="ui-label" for="endless-rank">{{ t('whackamole.leaderboard') }}：</label>
          <select id="endless-rank" class="ui-rank-select">
            <option v-for="(item, i) in (endlessRankList || [])" :key="i">
              {{ i+1 }}. {{ item.score }}{{ lang.value === 'zh' ? '分' : ' pts' }} / {{ item.time }}s
            </option>
          </select>
        </div>
        <div class="ui-btn-row">
          <button v-if="!playing" @click="startEndless" class="ui-btn start">{{ t('whackamole.startBtn') }}</button>
          <button v-if="!playing" @click="stage='mode'" class="ui-btn back">{{ t('whackamole.back') }}</button>
          <button v-if="playing && !paused" @click="pauseGame" class="ui-btn pause">{{ t('common.close') }}</button>
        </div>
        <div v-if="paused && playing" class="pause-modal">
          <div class="pause-content">
            <h3>{{ t('whackamole.gamePaused') }}</h3>
            <div class="pause-btns">
              <button class="resume-btn" @click="resumeGame">{{ t('whackamole.continue') }}</button>
              <button class="end-btn" @click="endGame">{{ t('whackamole.exitGame') }}</button>
            </div>
          </div>
        </div>
      </div>
      <!-- 洞口和地鼠 -->
      <div v-for="(hole, idx) in holes" :key="idx" :class="'wam-hole-abs hole-' + idx">
        <svg width="140" height="90" viewBox="0 0 140 90">
          <ellipse cx="70" cy="60" rx="60" ry="25" fill="#7a5c2e"/>
          <ellipse cx="70" cy="60" rx="55" ry="22" fill="#bfa76f"/>
          <ellipse cx="70" cy="60" rx="40" ry="15" fill="#222" fill-opacity="0.7"/>
          <ellipse cx="70" cy="60" rx="60" ry="25" fill="url(#holeShadow)"/>
        </svg>
        <svg v-if="moleIdxArr.includes(idx)" class="wam-mole" width="80" height="80" viewBox="0 0 80 80">
          <ellipse cx="40" cy="75" rx="22" ry="7" fill="#222" fill-opacity="0.25"/>
          <ellipse cx="40" cy="48" rx="24" ry="28" :fill="getSkinColor()"/>
          <ellipse cx="40" cy="38" rx="18" ry="16" :fill="getSkinColor()"/>
          <ellipse cx="22" cy="22" rx="7" ry="10" :fill="getSkinColor()"/>
          <ellipse cx="58" cy="22" rx="7" ry="10" :fill="getSkinColor()"/>
          <ellipse cx="33" cy="38" rx="3" ry="4" fill="#fff"/>
          <ellipse cx="47" cy="38" rx="3" ry="4" fill="#fff"/>
          <ellipse cx="33" cy="39" rx="1.2" ry="2" fill="#222"/>
          <ellipse cx="47" cy="39" rx="1.2" ry="2" fill="#222"/>
          <ellipse cx="40" cy="46" rx="2.5" ry="2" fill="#e57373"/>
          <path d="M37 52 Q40 56 43 52" stroke="#222" stroke-width="2" fill="none"/>
          <g v-if="showStars[idx]">
            <g v-for="n in 5" :key="n" :style="starStyle(n)">
              <polygon points="40,18 42,24 48,24 43,28 45,34 40,30 35,34 37,28 32,24 38,24" fill="#ffd600" stroke="#ff9800" stroke-width="1"/>
            </g>
          </g>
        </svg>
        <svg v-if="hammer.show && hammer.idx === idx" class="wam-hammer" width="60" height="90" viewBox="0 0 60 90">
          <g :class="{ 'hammer-anim': hammer.animating }" transform="rotate(180 30 80)">
            <rect x="26" y="20" width="8" height="40" rx="4" :fill="selectedHammer==='hammer2' ? '#FFD700' : '#a0522d'"/>
            <rect x="15" y="10" width="30" height="18" rx="6" :fill="selectedHammer==='hammer2' ? '#FFD700' : '#e53935'" stroke="#b71c1c" stroke-width="3"/>
            <rect x="22" y="15" width="16" height="7" rx="3" :fill="selectedHammer==='hammer2' ? '#FFFACD' : '#ffd600'"/>
          </g>
        </svg>
        <div class="wam-hole-click" @click.stop="handleHit(idx)"></div>
        <transition name="combo-fade">
          <div v-if="comboShow && comboIdx === idx" class="combo-effect">Combo x{{ comboCount }}</div>
        </transition>
      </div>
      <!-- 游戏结束弹窗 -->
      <transition name="modal-fade">
        <div v-if="showGameOver && stage==='endless'" class="gameover-modal">
          <div class="gameover-content">
            <h3>{{ t('whackamole.gameOver') }}</h3>
            <div class="gameover-row">{{ t('whackamole.currentScore') }}：<span class="score">{{ score }}</span></div>
            <div class="gameover-row">{{ t('whackamole.bestScore') }}：<span class="best">{{ endlessBest }}</span></div>
            <div class="gameover-row">{{ t('whackamole.currentTime') }}：<span class="time">{{ endlessTimeStr }}</span></div>
            <div class="gameover-btns">
              <button class="restart-btn" @click="startEndless">{{ t('whackamole.tryAgain') }}</button>
              <button class="exit-btn" @click="stage='mode'">{{ t('whackamole.back') }}</button>
            </div>
          </div>
        </div>
      </transition>
    </div>
    <!-- 闯关模式游戏页 -->
    <div v-if="stage==='stage'" class="wam-game-panel">
      <div class="wam-ui-corner card-ui">
        <div class="ui-title">{{ t('whackamole.level') }}</div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.stage') }}：</span><span class="ui-value score">{{ stageLevel }}/{{ stageMax }}</span></div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.molesHit') }}：</span><span class="ui-value score">{{ stageHit }}/20</span></div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.timeLeft') }}：</span><span class="ui-value time">{{ stageTime }}s</span></div>
        <div class="ui-row"><span class="ui-label">{{ t('whackamole.bestTime') }}：</span><span class="ui-value best">{{ stageBestTimeStr }}</span></div>
        <div class="ui-rank-row">
          <label class="ui-label" for="stage-rank">{{ t('whackamole.stageLeaderboard') }}：</label>
          <select id="stage-rank" class="ui-rank-select">
            <option v-for="(item, i) in (stageRankList || [])" :key="i">
              {{ i+1 }}. {{ item.time }}s
            </option>
          </select>
        </div>
        <div class="ui-btn-row">
          <button v-if="!playing" @click="startStage" class="ui-btn start">{{ t('whackamole.startBtn') }}</button>
          <button v-if="!playing" @click="stage='stageSelect'" class="ui-btn back">{{ t('whackamole.back') }}</button>
          <button v-if="playing && !paused" @click="pauseGame" class="ui-btn pause">{{ t('common.close') }}</button>
        </div>
        <div v-if="paused && playing" class="pause-modal">
          <div class="pause-content">
            <h3>{{ t('whackamole.gamePaused') }}</h3>
            <div class="pause-btns">
              <button class="resume-btn" @click="resumeGame">{{ t('whackamole.continue') }}</button>
              <button class="end-btn" @click="endGame">{{ t('whackamole.exitGame') }}</button>
            </div>
          </div>
        </div>
      </div>
      <div v-for="(hole, idx) in holes" :key="idx" :class="'wam-hole-abs hole-' + idx">
        <svg width="140" height="90" viewBox="0 0 140 90">
          <ellipse cx="70" cy="60" rx="60" ry="25" fill="#7a5c2e"/>
          <ellipse cx="70" cy="60" rx="55" ry="22" fill="#bfa76f"/>
          <ellipse cx="70" cy="60" rx="40" ry="15" fill="#222" fill-opacity="0.7"/>
          <ellipse cx="70" cy="60" rx="60" ry="25" fill="url(#holeShadow)"/>
        </svg>
        <svg v-if="moleIdxArr.includes(idx)" class="wam-mole" width="80" height="80" viewBox="0 0 80 80">
          <ellipse cx="40" cy="75" rx="22" ry="7" fill="#222" fill-opacity="0.25"/>
          <ellipse cx="40" cy="48" rx="24" ry="28" :fill="getSkinColor()"/>
          <ellipse cx="40" cy="38" rx="18" ry="16" :fill="getSkinColor()"/>
          <ellipse cx="22" cy="22" rx="7" ry="10" :fill="getSkinColor()"/>
          <ellipse cx="58" cy="22" rx="7" ry="10" :fill="getSkinColor()"/>
          <ellipse cx="33" cy="38" rx="3" ry="4" fill="#fff"/>
          <ellipse cx="47" cy="38" rx="3" ry="4" fill="#fff"/>
          <ellipse cx="33" cy="39" rx="1.2" ry="2" fill="#222"/>
          <ellipse cx="47" cy="39" rx="1.2" ry="2" fill="#222"/>
          <ellipse cx="40" cy="46" rx="2.5" ry="2" fill="#e57373"/>
          <path d="M37 52 Q40 56 43 52" stroke="#222" stroke-width="2" fill="none"/>
          <g v-if="showStars[idx]">
            <g v-for="n in 5" :key="n" :style="starStyle(n)">
              <polygon points="40,18 42,24 48,24 43,28 45,34 40,30 35,34 37,28 32,24 38,24" fill="#ffd600" stroke="#ff9800" stroke-width="1"/>
            </g>
          </g>
        </svg>
        <svg v-if="hammer.show && hammer.idx === idx" class="wam-hammer" width="60" height="90" viewBox="0 0 60 90">
          <g :class="{ 'hammer-anim': hammer.animating }" transform="rotate(180 30 80)">
            <rect x="26" y="20" width="8" height="40" rx="4" :fill="selectedHammer==='hammer2' ? '#FFD700' : '#a0522d'"/>
            <rect x="15" y="10" width="30" height="18" rx="6" :fill="selectedHammer==='hammer2' ? '#FFD700' : '#e53935'" stroke="#b71c1c" stroke-width="3"/>
            <rect x="22" y="15" width="16" height="7" rx="3" :fill="selectedHammer==='hammer2' ? '#FFFACD' : '#ffd600'"/>
          </g>
        </svg>
        <div class="wam-hole-click" @click.stop="handleHit(idx)"></div>
        <transition name="combo-fade">
          <div v-if="comboShow && comboIdx === idx" class="combo-effect">Combo x{{ comboCount }}</div>
        </transition>
      </div>
      <!-- 游戏结束弹窗 -->
      <transition name="modal-fade">
        <div v-if="showGameOver && stage==='stage'" class="gameover-modal">
          <div class="gameover-content">
            <h3>{{ t('whackamole.gameOver') }}</h3>
            <div class="gameover-row">{{ t('whackamole.currentTime') }}：<span class="score">{{ stageTotalTime }}</span>s</div>
            <div class="gameover-row">{{ t('whackamole.bestTime') }}：<span class="best">{{ stageBestTimeStr }}</span></div>
            <div class="gameover-btns">
              <button class="restart-btn" @click="goNextStage">{{ t('whackamole.nextStage') }}</button>
              <button class="exit-btn" @click="stage='stageSelect'">{{ t('whackamole.back') }}</button>
            </div>
          </div>
        </div>
      </transition>
    </div>
    <!-- 闯关失败弹窗 -->
    <transition name="modal-fade">
      <div v-if="showStageFail" class="gameover-modal">
        <div class="gameover-content">
          <h3>{{ t('whackamole.fail') }}</h3>
          <div class="gameover-row">{{ t('whackamole.fail') }}</div>
          <div class="gameover-btns">
            <button class="restart-btn" @click="retryStage">{{ t('whackamole.retryStage') }}</button>
            <button class="exit-btn" @click="stage='stageSelect';showStageFail=false">{{ t('whackamole.backToStageSelect') }}</button>
          </div>
        </div>
      </div>
    </transition>
    <!-- 关卡选择页面 -->
    <div v-if="stage==='stageSelect'" class="stage-select-bg">
      <div class="stage-select-card">
        <h2>{{ t('whackamole.selectStage') }}</h2>
        <div class="stage-btn-list">
          <button v-for="n in stageMax" :key="n"
            :class="['stage-btn', isStageUnlocked(n)?'unlocked':'locked', n===stageLevel && 'selected']"
            :disabled="!isStageUnlocked(n)"
            @click="chooseStage(n)">
            {{ t('whackamole.stageNum').replace('{0}', n) }}
          </button>
        </div>
        <div class="stage-select-actions">
          <button class="ui-btn start" @click="startStage" v-if="!playing">开始</button>
          <button class="ui-btn pause" @click="pauseGame" v-if="playing && !paused">暂停</button>
          <button class="ui-btn resume" @click="resumeGame" v-if="playing && paused">继续</button>
        </div>
        <button class="ui-btn back" @click="stage='mode'">返回</button>
      </div>
      <div v-if="showLockTip" class="lock-tip-modal">
        <div class="lock-tip-content">
          <div>{{ t('whackamole.completePrevious') }}</div>
          <button @click="showLockTip=false">{{ t('common.ok') }}</button>
        </div>
      </div>
      <!-- 暂停弹窗 -->
      <div v-if="paused && playing" class="pause-modal">
        <div class="pause-content">
          <h3>{{ t('whackamole.gamePaused') }}</h3>
          <div class="pause-btns">
            <button class="resume-btn" @click="resumeGame">{{ t('whackamole.continue') }}</button>
            <button class="end-btn" @click="endGame">{{ t('whackamole.exitGame') }}</button>
          </div>
        </div>
      </div>
    </div>
    <!-- 排行榜页 -->
    <div v-if="stage==='rank'" class="wam-rank-panel">
      <h2>{{ rankType==='endless' ? t('whackamole.endless') : t('whackamole.level') }} {{ t('whackamole.leaderboard') }}</h2>
      <ol>
        <li v-for="(item,i) in rankList" :key="i">{{ item }}</li>
      </ol>
      <button class="wam-btn" @click="stage='mode'">返回</button>
    </div>
    <!-- 右上角排行榜卡片（按钮和数据合一） -->
    <div class="wam-rank-float" :class="{open: showRankCard}" >
      <button class="wam-rank-btn" @click="showRankCard=!showRankCard"><span class="icon">🏆</span>{{ t('whackamole.leaderboard') }}</button>
      <transition name="fade">
        <div v-if="showRankCard" class="wam-rank-card">
          <div class="rank-header">
            <span>{{ t('whackamole.leaderboard') }}</span>
            <select v-model="rankType" class="rank-select">
              <option value="endless">{{ t('whackamole.endless') }}</option>
              <option v-for="n in stageMax" :key="n" :value="'stage-'+n">{{ t('whackamole.stageNum').replace('{0}', n) }}</option>
            </select>
            <button class="rank-close" @click="showRankCard=false">×</button>
          </div>
          <div class="rank-list">
            <div v-if="rankList.length===0" class="rank-empty">{{ t('whackamole.noData') }}</div>
            <div v-for="(item,i) in rankList" :key="i" class="rank-item">
              <template v-if="rankType==='endless'">
                <span class="rank-index">{{ t('whackamole.rank').replace('{0}', i+1) }}：</span>
                <span class="rank-score">{{item.score}}{{ lang.value === 'zh' ? '分' : ' pts' }}</span>
                <span class="rank-time">/ {{item.time}}s</span>
              </template>
              <template v-else>
                <span class="rank-index">{{ t('whackamole.rank').replace('{0}', i+1) }}：</span>
                <span class="rank-score">{{ lang.value === 'zh' ? '用时' : 'Time' }}</span>
                <span class="rank-time">{{item.time}}s</span>
              </template>
            </div>
          </div>
        </div>
      </transition>
    </div>
    <!-- 闯关成功弹窗 -->
    <transition name="modal-fade">
      <div v-if="showStageSuccess" class="gameover-modal">
        <div class="gameover-content">
          <h3>{{ t('whackamole.success') }}</h3>
          <div class="gameover-row">{{ t('whackamole.currentTime') }}：<span class="score">{{ stageTotalTime }}</span>s</div>
          <div class="gameover-row">{{ t('whackamole.bestTime') }}：<span class="best">{{ stageBestTimeStr }}</span></div>
          <div class="gameover-btns">
            <button class="restart-btn" @click="goNextStage">{{ t('whackamole.nextStage') }}</button>
            <button class="exit-btn" @click="stage='stageSelect';showStageSuccess=false">{{ t('whackamole.backToStageSelect') }}</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, inject } from 'vue'
import { useRoute } from 'vue-router'
import Shop from './shop/Shop.vue'
import { rankAPI, gameAPI } from '@/api'
const lang = inject('lang')
const setLang = inject('setLang')
const t = inject('t')

// 检查是否使用API模式
const useAPI = import.meta.env.VITE_USE_API === 'true' || localStorage.getItem('use_api') === 'true'
// 获取当前用户ID和用户名
const currentUserId = localStorage.getItem('game_user_id') ? Number(localStorage.getItem('game_user_id')) : null
const currentUsername = localStorage.getItem('game_username') || '游客'
// 洞口更集中布局
const holes = [
  { left: '32%', top: '62%' },
  { left: '42%', top: '68%' },
  { left: '52%', top: '62%' },
  { left: '62%', top: '68%' },
  { left: '37%', top: '78%' },
  { left: '47%', top: '80%' },
  { left: '57%', top: '80%' },
  { left: '67%', top: '78%' },
  { left: '47%', top: '70%' },
]
let timer = null, moleTimer = null
const score = ref(0)
const time = ref(30)
const moleIdxArr = ref([])
const playing = ref(false)
const paused = ref(false)
const showStars = reactive(Array(holes.length).fill(false))
const hammer = reactive({ show: false, idx: -1, animating: false })
const hammerAnimating = ref(false) // 防止重复触发动画
let moleStay = 1000 // 地鼠初始停留时间(ms)
// 连击相关
const comboCount = ref(0)
const comboShow = ref(false)
const comboIdx = ref(-1)
let comboTimer = null
let lastHitTime = 0
const currentWaveHit = ref(false) // 当前波次是否被击中
// 动态云朵/蝴蝶数据
const clouds = ref([
  { cx: 400, cy: 120, rx: 90, ry: 40, opacity: 0.8, x: 0 },
  { cx: 1500, cy: 180, rx: 100, ry: 45, opacity: 0.8, x: 0 },
  { cx: 800, cy: 100, rx: 70, ry: 30, opacity: 0.7, x: 0 },
  { cx: 1200, cy: 220, rx: 80, ry: 35, opacity: 0.6, x: 0 },
  { cx: 1700, cy: 80, rx: 60, ry: 25, opacity: 0.5, x: 0 },
])
const butterflies = ref([
  { x: 800, y: 200, dir: 1 },
  { x: 1400, y: 300, dir: -1 },
  { x: 600, y: 150, dir: 1 },
  { x: 1100, y: 250, dir: -1 },
  { x: 1600, y: 180, dir: 1 },
])
const showGameOver = ref(false)
// 全局AudioContext复用，兼容Safari
let audioCtx = null
function getAudioCtx() {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  }
  // 兼容Safari自动恢复
  if (audioCtx.state === 'suspended') {
    audioCtx.resume();
  }
  return audioCtx;
}
function playDong() {
  const ctx = getAudioCtx();
  const o = ctx.createOscillator();
  const g = ctx.createGain();
  o.type = 'sine';
  o.frequency.setValueAtTime(180, ctx.currentTime);
  o.frequency.linearRampToValueAtTime(80, ctx.currentTime + 0.18);
  g.gain.setValueAtTime(0.5, ctx.currentTime);
  g.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.18);
  o.connect(g).connect(ctx.destination);
  o.start();
  o.stop(ctx.currentTime + 0.18);
}
const stage = ref('entry') // entry/mode/endless/stage/rank
const modeSel = ref('endless')
const rankType = ref('endless')
const rankList = ref([])
// 无尽模式数据
const endlessTime = ref(0)
const endlessTimer = ref(null)
const endlessBest = ref(0)
const endlessRounds = ref(0)
const endlessMissRounds = ref(0)
const endlessTimeStr = computed(()=>{
  const m = Math.floor(endlessTime.value/60)
  const s = endlessTime.value%60
  return `${m}:${s.toString().padStart(2,'0')}`
})
const endlessRankList = ref([])
// 闯关模式数据
const stageLevel = ref(1)
const stageMax = 10
const stageHit = ref(0)
const stageTime = ref(60)
const stageTimer = ref(null)
const stageBest = ref(1)
const stageTotalTime = ref(0)
const stageTotalTimeStr = computed(()=>{
  const m = Math.floor(stageTotalTime.value/60)
  const s = stageTotalTime.value%60
  return `${m}:${s.toString().padStart(2,'0')}`
})
const stageRankList = ref([])
const stageBestTime = ref(null)
const stageBestTimeStr = computed(()=>stageBestTime.value==null?'--':(stageBestTime.value+'s'))
const showRankCard = ref(false)
const maxPassed = ref(0) // 0表示未通关任何关卡，只解锁第1关
const unlockedStages = ref([1])
const showLockTip = ref(false)
const showStageFail = ref(false)
const showStageSuccess = ref(false)
const showSkinModal = ref(false)
const skins = [
  {id: 'brown', name: '棕色地鼠', color: '#a86c3c', category: 'mouse'},
  {id: 'gray', name: '灰色地鼠', color: '#888', category: 'mouse'},
  {id: 'gold', name: '金色地鼠', color: 'gold', category: 'mouse'},
  {id: 'blue', name: '蓝色地鼠', color: '#4a90e2', category: 'mouse'},
  {id: 'bg1', name: '背景皮肤1', category: 'bg'},
  {id: 'bg2', name: '背景皮肤2', category: 'bg'},
  {id: 'hammer1', name: '锤子皮肤1', category: 'hammer'},
  {id: 'hammer2', name: '锤子皮肤2', category: 'hammer'},
]
const selectedSkin = ref('brown')
const tempSkin = ref(selectedSkin.value)
const showShopModal = ref(false)
// 皮肤分类Tab
const skinCategories = [
  {id:'mouse',name:'小鼠皮肤'},
  {id:'bg',name:'背景皮肤'},
  {id:'hammer',name:'锤子皮肤'}
]
const skinActiveCategory = ref('mouse')
// 拥有的皮肤列表
const ownedSkins = ref([])
// 默认免费皮肤（所有用户初始拥有）
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
        ownedSkins.value = skins
        // 如果数据库中没有 ownedSkins，保存默认值
        if (!response.data.ownedSkins || response.data.ownedSkins.length === 0) {
          await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
            ownedSkins: defaultFreeSkins
          })
        }
        return
      }
      // 如果数据库中没有游戏数据，初始化默认免费皮肤
      await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
        ownedSkins: defaultFreeSkins
      })
      ownedSkins.value = [...defaultFreeSkins]
      return
    } catch (error) {
      console.error('获取拥有皮肤失败:', error)
    }
  }
  // localStorage模式（兼容旧数据）
  let arr = []
  try { arr = JSON.parse(localStorage.getItem('wam_skins_owned')||'[]') } catch { arr = [] }
  // 确保默认免费皮肤包含在内
  defaultFreeSkins.forEach(skinId => {
    if (!arr.includes(skinId)) {
      arr.push(skinId)
    }
  })
  ownedSkins.value = arr
}
async function updateOwnedSkins(newId) {
  if (!ownedSkins.value.includes(newId)) {
    ownedSkins.value.push(newId)
    if (useAPI && currentUserId) {
      try {
        await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
          ownedSkins: ownedSkins.value
        })
        return
      } catch (error) {
        console.error('保存拥有皮肤失败:', error)
      }
    }
    // localStorage模式（兼容）
    localStorage.setItem('wam_skins_owned', JSON.stringify(ownedSkins.value))
  }
}
function start() {
  score.value = 0
  time.value = 30
  playing.value = true
  paused.value = false
  moleStay = 1000
  comboCount.value = 0
  comboShow.value = false
  comboIdx.value = -1
  currentWaveHit.value = false
  showGameOver.value = false
  nextMole()
  timer = setInterval(() => {
    if (!paused.value) {
      time.value--
      if (time.value <= 0) {
        clearInterval(timer)
        clearInterval(moleTimer)
        playing.value = false
        moleIdxArr.value = []
        showGameOver.value = true
      }
    }
  }, 1000)
}
function nextMole() {
  if (paused.value) return
  // 检查上一波是否被击中，如果没被击中则重置连击
  if (moleIdxArr.value.length > 0 && !currentWaveHit.value) {
    comboCount.value = 0
  }
  // 重置当前波次击中状态
  currentWaveHit.value = false
  // 随机出现1~3只地鼠
  const count = Math.floor(Math.random() * 3) + 1
  const idxs = []
  while (idxs.length < count) {
    const idx = Math.floor(Math.random() * holes.length)
    if (!idxs.includes(idx)) idxs.push(idx)
  }
  moleIdxArr.value = idxs
  moleStay = Math.max(700, moleStay - 30)
  moleTimer = setTimeout(nextMole, moleStay)
}
function handleHit(idx) {
  if (!playing.value || paused.value) return
  // 防止在动画进行中重复触发
  if (hammerAnimating.value && hammer.idx === idx) return
  
  // 检查是否击中地鼠，并且该地鼠还没有被击中过
  const isHit = moleIdxArr.value.includes(idx)
  
  // 只有在击中地鼠时才显示锤子动画和星星动画
  if (isHit) {
    // 立即从数组中移除，防止重复点击同一只地鼠
    moleIdxArr.value = moleIdxArr.value.filter(i => i !== idx)
    
    // 防止重复触发动画
    if (hammerAnimating.value) return
    hammerAnimating.value = true
    
    // 显示锤子动画（只在击中时显示）
    hammer.show = true
    hammer.idx = idx
    hammer.animating = true
    setTimeout(() => {
      hammer.animating = false
      hammer.show = false
      hammerAnimating.value = false
    }, 180)
    
    if (stage.value==='endless' && typeof handleHit._onEndlessHit === 'function') handleHit._onEndlessHit()
    playDong(); // 命中时播放咚音效
    score.value++
    // 标记当前波次被击中
    currentWaveHit.value = true
    // 连击判定：如果当前波次被击中，连击数+1
    comboCount.value++
    comboShow.value = true
    comboIdx.value = idx
    clearTimeout(comboTimer)
    comboTimer = setTimeout(() => {
      comboShow.value = false
      comboIdx.value = -1
    }, 700)
    // 显示星星动画（确保不会重复设置）
    if (!showStars[idx]) {
      showStars[idx] = true
      setTimeout(() => {
        showStars[idx] = false
      }, 700)
    }
    if (stage.value==='stage') {
      stageHit.value++
      if (stageHit.value>=20) {
        // 过关，弹出成功弹窗
        updateStageRank(stageLevel.value, stageTotalTime.value)
        unlockNextStage()
        playing.value = false
        clearInterval(stageTimer.value)
        showStageSuccess.value = true
        return
      }
    }
  }
}
// 星星绕圈动画样式
function starStyle(n) {
  const deg = (n - 1) * 72
  return {
    position: 'absolute',
    left: '40px',
    top: '18px',
    transform: `rotate(${deg}deg) translate(0, -22px) rotate(-${deg}deg)`,
    transition: 'transform 0.7s cubic-bezier(.68,-0.55,.27,1.55)',
  }
}
// 暂停/继续
function pauseGame() {
  paused.value = true
  clearTimeout(moleTimer)
  clearInterval(endlessTimer.value) // 暂停计时
}
function resumeGame() {
  paused.value = false
  nextMoleEndless()
  endlessTimer.value = setInterval(()=>{ endlessTime.value++ }, 1000) // 恢复计时
}
function endGame() {
  playing.value = false
  paused.value = false
  time.value = 0
  moleIdxArr.value = []
  showGameOver.value = false
  clearInterval(endlessTimer.value)
  clearInterval(stageTimer.value) // 保证闯关模式定时器彻底清理
  if (stage.value==='endless') {
    if (score.value>endlessBest.value) {
      endlessBest.value = score.value
      saveGameData({ endlessBest: endlessBest.value })
    }
    updateEndlessRank(score.value, endlessTime.value)
  } else if (stage.value==='stage') {
    updateStageRank(stageLevel.value, stageTotalTime.value)
  }
}
function exitGame() {
  // 跳转回大厅
  window.location.href = '/hall'
}
function chooseMode() {
  if (modeSel.value==='endless') stage.value='endless'
  else if (modeSel.value==='stage') stage.value='stage'
}
async function toRank(type) {
  rankType.value = type
  if (type==='endless') {
    if (useAPI) {
      try {
        const response = await rankAPI.getRankList('whackamole', 'endless', 10)
        if (response.success) {
          rankList.value = response.data.map((x,i)=>`${i+1}. ${x.score}分 ${x.time}s`)
          return
        }
      } catch (error) {
        console.error('获取排行榜失败:', error)
      }
    }
    const arr = JSON.parse(localStorage.getItem('wam_endless_rank')||'[]')
    rankList.value = arr.slice(0,10).map((x,i)=>`${i+1}. ${x.score}分 ${x.time}s`)
  } else {
    if (useAPI) {
      try {
        // 闯关模式需要指定关卡，这里先获取总的排行榜
        const response = await rankAPI.getRankList('whackamole', 'stage', 10)
        if (response.success) {
          rankList.value = response.data.map((x,i)=>`${i+1}. ${x.level}关 ${x.time}s`)
          return
        }
      } catch (error) {
        console.error('获取排行榜失败:', error)
      }
    }
    const arr = JSON.parse(localStorage.getItem('wam_stage_rank')||'[]')
    rankList.value = arr.slice(0,10).map((x,i)=>`${i+1}. ${x.level}关 ${x.time}s`)
  }
  stage.value = 'rank'
}
// 无尽模式主流程
function startEndless() {
  score.value = 0
  endlessTime.value = 0
  endlessRounds.value = 0
  endlessMissRounds.value = 0
  comboCount.value = 0
  currentWaveHit.value = false
  playing.value = true
  showGameOver.value = false
  nextMoleEndless()
  endlessTimer.value = setInterval(()=>{ endlessTime.value++ }, 1000)
}
function nextMoleEndless() {
  if (!playing.value || paused.value) return
  // 检查上一波是否被击中，如果没被击中则重置连击
  if (moleIdxArr.value.length > 0 && !currentWaveHit.value) {
    comboCount.value = 0
  }
  // 重置当前波次击中状态
  currentWaveHit.value = false
  // 随机出现1~3只地鼠
  const count = Math.floor(Math.random()*3)+1
  const idxs = []
  while (idxs.length<count) {
    const idx = Math.floor(Math.random()*holes.length)
    if (!idxs.includes(idx)) idxs.push(idx)
  }
  moleIdxArr.value = idxs
  let hit = false
  // 命中时hit=true
  handleHit._onEndlessHit = () => { hit = true }
  setTimeout(()=>{
    handleHit._onEndlessHit = null // 解绑，防止下轮误判
    if (!hit) endlessMissRounds.value++
    if (endlessMissRounds.value>=10) {
      endGame()
      return
    }
    nextMoleEndless()
  }, Math.max(700, 1200-score.value*8))
}
// 闯关模式主流程
function startStage() {
  // 修复：每次开始前清理旧定时器，防止多重倒计时
  if (stageTimer.value) clearInterval(stageTimer.value);
  score.value = 0
  stageHit.value = 0
  stageTime.value = 60
  comboCount.value = 0
  currentWaveHit.value = false
  playing.value = true
  showGameOver.value = false
  showStageFail.value = false
  stageTotalTime.value = 0
  updateStageRank(stageLevel.value, 9999) // 初始化排行榜
  nextMoleStage()
  stageTimer.value = setInterval(()=>{
    stageTime.value--
    stageTotalTime.value++
    if (stageTime.value<=0) {
      if (stageHit.value<20) {
        // 只弹一次失败弹窗
        if (!showStageFail.value) {
          playing.value = false
          clearInterval(stageTimer.value)
          showStageFail.value = true
        }
        return
      }
      endGame()
    }
  }, 1000)
}
function nextMoleStage() {
  if (!playing.value) return
  // 检查上一波是否被击中，如果没被击中则重置连击
  if (moleIdxArr.value.length > 0 && !currentWaveHit.value) {
    comboCount.value = 0
  }
  // 重置当前波次击中状态
  currentWaveHit.value = false
  // 随机出现1~3只地鼠
  const count = Math.floor(Math.random()*3)+1
  const idxs = []
  while (idxs.length<count) {
    const idx = Math.floor(Math.random()*holes.length)
    if (!idxs.includes(idx)) idxs.push(idx)
  }
  moleIdxArr.value = idxs
  setTimeout(()=>{
    nextMoleStage()
  }, Math.max(700, 1200-stageLevel.value*30))
}
// 动态云朵/蝴蝶动画
let cloudAnim, butterflyAnim
onMounted(async () => {
  // 加载游戏数据（从数据库）
  await loadGameData()
  
  // 加载关卡进度
  await loadMaxPassed()
  
  // 启动云朵和蝴蝶动画
  cloudAnim = setInterval(() => {
    clouds.value.forEach((cloud, i) => {
      cloud.x += (i % 2 === 0 ? 0.18 : -0.12)
      if (cloud.x > 400) cloud.x = -400
      if (cloud.x < -400) cloud.x = 400
    })
  }, 30)
  butterflyAnim = setInterval(() => {
    butterflies.value.forEach((b, i) => {
      b.x += b.dir * (0.7 + 0.3 * Math.sin(Date.now() / 800 + i))
      b.y += Math.sin(Date.now() / 600 + i) * 0.7
      if (b.x > 1800) b.x = 100
      if (b.x < 100) b.x = 1800
    })
  }, 30)
  
  // 加载无尽模式排行榜
  if (useAPI) {
    try {
      const response = await rankAPI.getRankList('whackamole', 'endless', 10)
      if (response.success && response.data) {
        endlessRankList.value = response.data.map(item => ({ score: item.score, time: item.time }))
      }
    } catch (error) {
      console.error('加载无尽模式排行榜失败:', error)
      // 回退到localStorage
      let arr = []
      try {
        arr = JSON.parse(localStorage.getItem('wam_endless_rank')||'[]')
        if (!Array.isArray(arr)) arr = []
      } catch { arr = [] }
      endlessRankList.value = Array.isArray(arr) ? arr : []
    }
  } else {
    // localStorage模式
    let arr = []
    try {
      arr = JSON.parse(localStorage.getItem('wam_endless_rank')||'[]')
      if (!Array.isArray(arr)) arr = []
    } catch { arr = [] }
    endlessRankList.value = Array.isArray(arr) ? arr : []
  }
  
  // 加载排行榜
  await loadRankList(rankType.value)
  
  // 初始化皮肤弹窗位置
  skinModalPos.value.x = window.innerWidth/2-220
  skinModalPos.value.y = window.innerHeight/2-160
  
  // 如果进入闯关模式，初始化排行榜
  if (stage.value==='stage') updateStageRank(stageLevel.value, 9999)
})
onUnmounted(() => {
  clearInterval(cloudAnim)
  clearInterval(butterflyAnim)
  clearTimeout(moleTimer)
  clearInterval(timer)
})
async function updateEndlessRank(score, time) {
  if (useAPI && currentUserId) {
    try {
      const response = await rankAPI.submitRank('whackamole', {
        userId: currentUserId,
        username: currentUsername,
        mode: 'endless',
        score: score,
        time: time
      })
      if (response.success) {
        console.log('排行榜已更新:', response.data)
        // 重新加载排行榜
        if (stage.value === 'rank' && rankType.value === 'endless') {
          await toRank('endless')
        }
        return
      }
    } catch (error) {
      console.error('提交排行榜失败:', error)
    }
  }
  
  // localStorage模式（兼容，仅在没有API时使用）
  let arr = []
  try {
    arr = JSON.parse(localStorage.getItem('wam_endless_rank')||'[]')
    if (!Array.isArray(arr)) arr = []
  } catch { arr = [] }
  // 只保留前10名
  if (arr.length < 10 || score > arr[arr.length-1].score) {
    arr.push({score, time})
    arr.sort((a,b)=>b.score-a.score)
    arr = arr.slice(0,10)
    localStorage.setItem('wam_endless_rank', JSON.stringify(arr))
  }
  endlessRankList.value = Array.isArray(arr) ? arr : []
}
async function updateStageRank(level, time) {
  // 无论时间多少，都尝试保存到数据库（如果使用API模式）
  if (useAPI && currentUserId) {
    try {
      const response = await rankAPI.submitRank('whackamole', {
        userId: currentUserId,
        username: currentUsername,
        mode: 'stage',
        level: level,
        time: time
      })
      if (response.success) {
        console.log('排行榜已更新到数据库:', response.data)
        // 重新加载排行榜
        if (stage.value === 'rank' && rankType.value === `stage-${level}`) {
          await loadRankList(`stage-${level}`)
        }
        // 更新最佳时间
        const bestResponse = await rankAPI.getRankListByLevel('whackamole', level, 'stage', 1)
        if (bestResponse.success && bestResponse.data.length > 0) {
          stageBestTime.value = bestResponse.data[0].time
        }
        // 从数据库重新加载排行榜
        const rankResponse = await rankAPI.getRankListByLevel('whackamole', level, 'stage', 10)
        if (rankResponse.success && rankResponse.data) {
          stageRankList.value = rankResponse.data.map(item => ({ time: item.time }))
          stageBestTime.value = rankResponse.data.length > 0 ? rankResponse.data[0].time : null
        }
        return
      }
    } catch (error) {
      console.error('提交排行榜到数据库失败:', error)
    }
  }
  
  // localStorage模式（兼容，仅在没有API时使用）
  const key = `wam_stage_rank_${level}`
  let arr = []
  try {
    arr = JSON.parse(localStorage.getItem(key)||'[]')
    if (!Array.isArray(arr)) arr = []
  } catch { arr = [] }
  // 只保留用时>=15秒的通关数据
  arr = arr.filter(item => item.time >= 15)
  if ((arr.length < 10 || time < arr[arr.length-1].time) && time >= 15) {
    arr.push({time})
    arr.sort((a,b)=>a.time-b.time)
    arr = arr.slice(0,10)
    localStorage.setItem(key, JSON.stringify(arr))
  }
  stageRankList.value = arr
  stageBestTime.value = arr.length>0 ? arr[0].time : null
}
function nextStage() {
  showGameOver.value = false
  if (stageLevel.value >= stageMax) {
    stage.value = 'stageSelect'
    return
  }
  stageLevel.value++
  startStage()
}
watch(rankType, (val)=>{
  loadRankList(val)
})
async function loadRankList(type) {
  if (type==='endless') {
    if (useAPI) {
      try {
        const response = await rankAPI.getRankList('whackamole', 'endless', 10)
        if (response.success) {
          rankList.value = response.data
          return
        }
      } catch (error) {
        console.error('获取排行榜失败:', error)
      }
    }
    let arr = []
    try {
      arr = JSON.parse(localStorage.getItem('wam_endless_rank')||'[]')
      if (!Array.isArray(arr)) arr = []
    } catch { arr = [] }
    rankList.value = arr
  } else if (type.startsWith('stage-')) {
    const level = parseInt(type.split('-')[1])
    if (useAPI) {
      try {
        const response = await rankAPI.getRankListByLevel('whackamole', level, 'stage', 10)
        if (response.success) {
          rankList.value = response.data
          return
        }
      } catch (error) {
        console.error('获取排行榜失败:', error)
      }
    }
    const key = `wam_stage_rank_${level}`
    let arr = []
    try {
      arr = JSON.parse(localStorage.getItem(key)||'[]')
      if (!Array.isArray(arr)) arr = []
    } catch { arr = [] }
  rankList.value = arr
}
}
async function loadMaxPassed() {
  if (useAPI && currentUserId) {
    try {
      const response = await gameAPI.getUserGameData(currentUserId, 'whackamole')
      if (response.success) {
        // maxPassed表示已通关的最大关卡数，0表示未通关任何关卡（只解锁第1关）
        // 如果API返回空数据或没有maxPassed字段，默认为0
        let max = 0
        if (response.data && response.data.maxPassed !== undefined && response.data.maxPassed !== null) {
          max = parseInt(response.data.maxPassed) || 0
        }
        maxPassed.value = max
        // 解锁1~(max+1)关：maxPassed=0时只解锁第1关，maxPassed=1时解锁第1和第2关
        unlockedStages.value = []
        for(let i=1;i<=Math.min(max+1,stageMax);i++) unlockedStages.value.push(i)
        // 如果使用API，清理localStorage中的旧数据，避免混用
        if (localStorage.getItem('wam_stage_max_passed')) {
          localStorage.removeItem('wam_stage_max_passed')
        }
        return
      }
    } catch (error) {
      console.error('获取关卡进度失败:', error)
    }
  }
  
  // localStorage模式（原有逻辑）
  let max = 0 // 初始为0，表示未通关任何关卡
  try {
    const raw = localStorage.getItem('wam_stage_max_passed')
    if (raw) {
      const parsed = parseInt(raw)
      // 兼容旧数据：如果localStorage中有值，使用它；否则默认为0
      max = isNaN(parsed) ? 0 : parsed
    }
  } catch { max = 0 }
  maxPassed.value = max
  // 解锁1~(max+1)关
  unlockedStages.value = []
  for(let i=1;i<=Math.min(max+1,stageMax);i++) unlockedStages.value.push(i)
}
async function saveMaxPassed(newMax) {
  if (useAPI && currentUserId) {
    try {
      const response = await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
        maxPassed: newMax
      })
      if (response.success) {
        console.log('关卡进度已保存:', response.data)
        return
      }
    } catch (error) {
      console.error('保存关卡进度失败:', error)
    }
  }
  
  // localStorage模式（原有逻辑）
  localStorage.setItem('wam_stage_max_passed', String(newMax))
}
function isStageUnlocked(n) {
  return unlockedStages.value.includes(n)
}
async function unlockNextStage() {
  // 只在真正通关时调用，顺序解锁
  if (stageLevel.value > maxPassed.value) {
    maxPassed.value = stageLevel.value
    await saveMaxPassed(maxPassed.value)
    // 自动解锁1~(max+1)关
    unlockedStages.value = []
    for(let i=1;i<=Math.min(maxPassed.value+1,stageMax);i++) unlockedStages.value.push(i)
  }
}
function enterStageSelect() {
  // 自动选中最新解锁的关卡
  stageLevel.value = Math.min(maxPassed.value + 1, stageMax)
  stage.value = 'stageSelect'
}
function chooseStage(n) {
  if (!isStageUnlocked(n)) {
    showLockTip.value = true
    return
  }
  // 修复：切换关卡前清理定时器
  if (stageTimer.value) clearInterval(stageTimer.value);
  stageLevel.value = n
  stage.value = 'stage' // 进入游戏页面，但不自动开始
  playing.value = false // 未开始
  paused.value = false
  stageHit.value = 0
  stageTime.value = 60
  stageTotalTime.value = 0
  moleIdxArr.value = []
}
function retryStage() {
  // 修复：重试前清理定时器
  if (stageTimer.value) clearInterval(stageTimer.value);
  showStageFail.value = false
  startStage()
}
function startSelectedMode() {
  if (modeSel.value==='endless') {
    stage.value = 'endless'
    // 不立即开始游戏，让玩家点击"开始"按钮
    playing.value = false
    paused.value = false
    score.value = 0
    endlessTime.value = 0
    endlessRounds.value = 0
    endlessMissRounds.value = 0
    showGameOver.value = false
  } else if (modeSel.value==='stage') {
    enterStageSelect()
  }
}
function goNextStage() {
  showStageSuccess.value = false
  // 修复：下一关前清理定时器
  if (stageTimer.value) clearInterval(stageTimer.value);
  if (stageLevel.value < stageMax) {
    stageLevel.value++
    startStage()
  } else {
    enterStageSelect()
  }
}
async function confirmSkin() {
  selectedSkin.value = tempSkin.value
  await saveGameData({ selectedSkin: selectedSkin.value })
  showSkinModal.value = false
}
function getSkinColor() {
  const skin = skins.find(s=>s.id===selectedSkin.value)
  return skin ? skin.color : '#a86c3c'
}
function closeAllPopupsAndGoMode() {
  showSkinModal.value = false
  showShopModal.value = false
  showStageFail.value = false
  showStageSuccess.value = false
  showGameOver.value = false
  stage.value = 'mode'
  console.log('切换stage:', stage.value)
}
// 拖拽皮肤仓库弹窗
const skinModalPos = ref({x:0, y:0, dragging:false, offsetX:0, offsetY:0, moved:false})
function onSkinModalMouseDown(e) {
  skinModalPos.value.dragging = true
  skinModalPos.value.offsetX = e.clientX - skinModalPos.value.x
  skinModalPos.value.offsetY = e.clientY - skinModalPos.value.y
  skinModalPos.value.moved = true
  document.addEventListener('mousemove', onSkinModalMouseMove)
  document.addEventListener('mouseup', onSkinModalMouseUp)
}
function onSkinModalMouseMove(e) {
  if (!skinModalPos.value.dragging) return
  skinModalPos.value.x = e.clientX - skinModalPos.value.offsetX
  skinModalPos.value.y = e.clientY - skinModalPos.value.offsetY
}
function onSkinModalMouseUp() {
  skinModalPos.value.dragging = false
  document.removeEventListener('mousemove', onSkinModalMouseMove)
  document.removeEventListener('mouseup', onSkinModalMouseUp)
}
// 新增：背景皮肤切换
const selectedBg = ref('bg1')
const selectedHammer = ref('hammer1')

function setBgSkin(id) {
  selectedBg.value = id
  saveGameData({ selectedBg: id })
}
// 调试：输出 selectedBg 的变化
watch(selectedBg, v => { console.log('当前selectedBg:', v) })

// 森林皮肤图片静态资源
import bushPng from '../assets/bush.png'
import grass1Png from '../assets/grass1.png'
import grass2Png from '../assets/grass2.png'
import tree1Png from '../assets/tree1.png'
import tree2Png from '../assets/tree2.png'

console.log('bushPng', bushPng)
console.log('grass1Png', grass1Png)
console.log('grass2Png', grass2Png)
console.log('tree1Png', tree1Png)
console.log('tree2Png', tree2Png)

const route = useRoute()

// 加载游戏数据（从数据库）
async function loadGameData() {
  if (useAPI && currentUserId) {
    try {
      const response = await gameAPI.getUserGameData(currentUserId, 'whackamole')
      if (response.success && response.data) {
        const data = response.data
        // 加载无尽模式最佳分数
        if (data.endlessBest !== undefined) {
          endlessBest.value = Number(data.endlessBest) || 0
        }
        // 加载闯关模式最佳关卡
        if (data.stageBest !== undefined) {
          stageBest.value = Number(data.stageBest) || 1
        }
        // 加载皮肤选择
        if (data.selectedSkin) {
          selectedSkin.value = data.selectedSkin
          tempSkin.value = data.selectedSkin
        }
        // 加载背景选择
        if (data.selectedBg) {
          selectedBg.value = data.selectedBg
        }
        // 加载锤子选择
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
          // 如果没有 ownedSkins 数据，使用默认免费皮肤
          ownedSkins.value = [...defaultFreeSkins]
          // 保存默认值到数据库
          try {
            await gameAPI.updateUserGameData(currentUserId, 'whackamole', {
              ownedSkins: defaultFreeSkins
            })
          } catch (error) {
            console.error('保存默认皮肤失败:', error)
          }
        }
        return
      }
    } catch (error) {
      console.error('加载游戏数据失败:', error)
    }
  }
  
  // localStorage模式（兼容旧数据，仅在没有API或API失败时使用）
  endlessBest.value = Number(localStorage.getItem('wam_endless_best')||0)
  stageBest.value = Number(localStorage.getItem('wam_stage_best')||1)
  selectedSkin.value = localStorage.getItem('wam_skin') || 'brown'
  tempSkin.value = selectedSkin.value
  selectedBg.value = localStorage.getItem('wam_bg') || 'bg1'
  selectedHammer.value = localStorage.getItem('wam_hammer') || 'hammer1'
  await loadOwnedSkins()
}

// 保存游戏数据（到数据库）
async function saveGameData(data) {
  if (useAPI && currentUserId) {
    try {
      // 获取当前数据，合并新数据
      const currentResponse = await gameAPI.getUserGameData(currentUserId, 'whackamole')
      let currentData = {}
      if (currentResponse.success && currentResponse.data) {
        currentData = currentResponse.data
      }
      // 合并数据
      const mergedData = { ...currentData, ...data }
      const response = await gameAPI.updateUserGameData(currentUserId, 'whackamole', mergedData)
      if (response.success) {
        console.log('游戏数据已保存:', response.data)
        return
      }
    } catch (error) {
      console.error('保存游戏数据失败:', error)
    }
  }
  
  // localStorage模式（兼容）
  if (data.endlessBest !== undefined) {
    localStorage.setItem('wam_endless_best', String(data.endlessBest))
  }
  if (data.stageBest !== undefined) {
    localStorage.setItem('wam_stage_best', String(data.stageBest))
  }
  if (data.selectedSkin) {
    localStorage.setItem('wam_skin', data.selectedSkin)
  }
  if (data.selectedBg) {
    localStorage.setItem('wam_bg', data.selectedBg)
  }
  if (data.selectedHammer) {
    localStorage.setItem('wam_hammer', data.selectedHammer)
  }
}
</script>

<style scoped>
.wam-fullscreen {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  z-index: 10;
}
.wam-bg-svg {
  position: absolute;
  left: 0; top: 0;
  width: 100vw;
  height: 100vh;
  z-index: -1;
  pointer-events: none;
}
.wam-hole-abs {
  position: absolute;
  width: 140px;
  height: 90px;
  z-index: 2;
}
.hole-0 { left: 15%; top: 62%; }
.hole-1 { left: 32%; top: 68%; }
.hole-2 { left: 50%; top: 60%; }
.hole-3 { left: 68%; top: 70%; }
.hole-4 { left: 82%; top: 65%; }
.hole-5 { left: 22%; top: 80%; }
.hole-6 { left: 40%; top: 82%; }
.hole-7 { left: 60%; top: 80%; }
.hole-8 { left: 78%; top: 82%; }
.wam-mole {
  position: absolute;
  left: 30px;
  top: 0;
  animation: pop 0.3s cubic-bezier(.68,-0.55,.27,1.55) !important;
}
@keyframes pop {
  0% { transform: translateY(40px) scaleY(0.2); opacity: 0; }
  80% { transform: translateY(-8px) scaleY(1.1); opacity: 1; }
  100% { transform: translateY(0) scaleY(1); opacity: 1; }
}
.wam-hole-click {
  position: absolute;
  left: 0; top: 0;
  width: 140px;
  height: 90px;
  cursor: pointer;
  background: transparent;
  z-index: 3;
}
.wam-hammer {
  position: absolute;
  left: 24px;
  top: -20px;
  z-index: 10;
  pointer-events: none;
  transform-origin: 50% 80px;
  /* 旋转中心在锤头末端（靠近锤头） */
  transform: scale(1.0) rotate(-90deg) translateY(-60px);
}
.hammer-anim {
  animation: hammerSwing 0.18s cubic-bezier(.68,-0.55,.27,1.55);
}
@keyframes hammerSwing {
  0% { transform: scale(1.0) rotate(-90deg) translateY(-60px); }
  60% { transform: scale(1.0) rotate(-20deg) translateY(0); }
  100% { transform: scale(1.0) rotate(-90deg) translateY(-60px); }
}
.wam-ui-corner {
  position: absolute;
  left: 32px;
  top: 32px;
  background: rgba(255,255,255,0.82) !important;
  border-radius: 28px !important;
  box-shadow: 0 8px 40px 0 rgba(80,80,160,0.13) !important;
  backdrop-filter: blur(12px) saturate(1.2);
  padding: 18px 28px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  z-index: 20;
  min-width: 160px;
}
.title {
  font-size: 1.6rem;
  font-weight: bold;
  margin-bottom: 10px;
  color: #374151;
  letter-spacing: 2px;
}
.score, .timer {
  font-size: 1.1rem;
  font-weight: bold;
  margin: 4px 0;
  color: #7e57c2;
}
.score span, .timer span {
  color: #42a5f5;
  font-weight: bold;
  font-size: 1.2em;
}
.start-btn {
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  border: none;
  border-radius: 18px;
  padding: 8px 22px;
  font-size: 1.1rem;
  margin-top: 12px;
  box-shadow: 0 2px 8px rgba(66,165,245,0.18);
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}
.start-btn:hover {
  background: linear-gradient(90deg, #7e57c2, #42a5f5);
  transform: scale(1.05);
}
.pause-btn {
  position: fixed;
  right: 32px; top: 32px;
  width: 56px; height: 56px;
  background: none;
  border: none;
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(66,165,245,0.18);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  z-index: 20;
  transition: transform 0.18s, box-shadow 0.18s;
  animation: floatPauseBtn 2s infinite alternate;
  outline: none;
  padding: 0;
}
.pause-btn:hover {
  transform: scale(1.12) rotate(-8deg);
  box-shadow: 0 8px 32px rgba(126,87,194,0.18);
}
@keyframes floatPauseBtn {
  0% { transform: translateY(0);}
  100% { transform: translateY(-6px);}
}
.pause-modal {
  position: fixed; left: 0; top: 0; width: 100vw; height: 100vh; z-index: 100;
  background: rgba(0,0,0,0.18); display: flex; align-items: center; justify-content: center;
}
.pause-content {
  background: #fff; border-radius: 22px; padding: 38px 48px; box-shadow: 0 8px 40px 0 rgba(80,80,160,0.18), 0 1.5px 8px 0 rgba(80,80,160,0.10);
  text-align: center;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.pause-content h3 {
  font-size: 1.6rem;
  font-weight: bold;
  color: #374151;
  margin-bottom: 28px;
}
.pause-btns {
  display: flex;
  gap: 32px;
  justify-content: center;
  margin-top: 8px;
}
.resume-btn, .end-btn {
  min-width: 120px;
  font-size: 1.1rem;
  padding: 12px 0;
  border-radius: 24px;
  border: none;
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 2px 8px rgba(66,165,245,0.10);
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.resume-btn:hover, .end-btn:hover {
  background: linear-gradient(90deg, #7e57c2, #42a5f5);
  box-shadow: 0 4px 16px rgba(126,87,194,0.13);
  transform: translateY(-2px) scale(1.04);
}
.end-btn {
  background: linear-gradient(90deg, #f44336, #ff9800);
}
.end-btn:hover {
  background: linear-gradient(90deg, #ff9800, #f44336);
  box-shadow: 0 4px 16px rgba(255,152,0,0.13);
  transform: translateY(-2px) scale(1.04);
}
.combo-effect {
  position: absolute;
  left: 50%; top: -30px;
  transform: translateX(-50%);
  font-size: 2.1rem;
  color: #ff9800;
  font-weight: bold;
  text-shadow: 0 2px 8px #fff8, 0 0 12px #ffb300;
  animation: comboPop 0.7s;
  pointer-events: none;
  z-index: 30;
}
@keyframes comboPop {
  0% { opacity: 0; transform: translateX(-50%) scale(0.7); }
  40% { opacity: 1; transform: translateX(-50%) scale(1.2); }
  100% { opacity: 0; transform: translateX(-50%) scale(1); }
}
.combo-fade-enter-active, .combo-fade-leave-active {
  transition: opacity 0.3s;
}
.combo-fade-enter-from, .combo-fade-leave-to {
  opacity: 0;
}
.cloud-anim, .butterfly-anim {
  will-change: transform;
}
.exit-btn {
  background: linear-gradient(90deg, #f44336, #ff9800);
  color: #fff;
  border: none;
  border-radius: 24px;
  padding: 10px 32px;
  font-size: 1.1rem;
  margin-top: 18px;
  box-shadow: 0 2px 8px rgba(244,67,54,0.10);
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
  font-weight: 600;
  letter-spacing: 1px;
}
.exit-btn:hover {
  background: linear-gradient(90deg, #ff9800, #f44336);
  box-shadow: 0 4px 16px rgba(255,152,0,0.13);
  transform: translateY(-2px) scale(1.04);
}
.gameover-modal {
  position: fixed; left: 0; top: 0; width: 100vw; height: 100vh; z-index: 200;
  background: rgba(0,0,0,0.18); display: flex; align-items: center; justify-content: center;
}
.gameover-content {
  background: #fff; border-radius: 22px; padding: 38px 48px; box-shadow: 0 8px 40px 0 rgba(80,80,160,0.18), 0 1.5px 8px 0 rgba(80,80,160,0.10);
  text-align: center;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.gameover-content h3 {
  font-size: 1.7rem;
  font-weight: bold;
  color: #374151;
  margin-bottom: 22px;
}
.gameover-score {
  font-size: 1.3rem;
  color: #7e57c2;
  font-weight: 600;
  margin-bottom: 28px;
}
.gameover-score span {
  color: #42a5f5;
  font-size: 2.1rem;
  font-weight: bold;
  margin-left: 8px;
}
.gameover-btns {
  display: flex;
  gap: 32px;
  justify-content: center;
  margin-top: 8px;
}
.restart-btn, .exit-btn {
  min-width: 150px;
  font-size: 1.18rem;
  padding: 16px 0;
  border-radius: 28px;
  border: none;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 2px 8px rgba(66,165,245,0.10);
  cursor: pointer;
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
  display: inline-block;
}
.restart-btn {
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
}
.restart-btn:hover {
  background: linear-gradient(90deg, #7e57c2, #42a5f5);
  box-shadow: 0 4px 16px rgba(126,87,194,0.13);
  transform: translateY(-2px) scale(1.04);
}
.exit-btn {
  background: linear-gradient(90deg, #f44336, #ff9800);
  color: #fff;
}
.exit-btn:hover {
  background: linear-gradient(90deg, #ff9800, #f44336);
  box-shadow: 0 4px 16px rgba(255,152,0,0.13);
  transform: translateY(-2px) scale(1.04);
}
.wam-entry-panel.lively {
  background: none !important;
  box-shadow: none !important;
  border-radius: 0 !important;
  padding: 0 !important;
  margin: 0 auto;
  min-width: 0;
  min-height: 0;
  max-width: none;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.wam-btn-circle-row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: flex-end;
  gap: 48px;
  margin: 38px 0 18px 0;
}
.wam-btn-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg,#7b8aff 60%,#a6e3ff 100%);
  color: #fff;
  font-size: 1.1rem;
  font-weight: bold;
  box-shadow: 0 4px 24px #7b8aff22;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: box-shadow .18s, transform .18s;
  margin: 0 8px;
  position: relative;
  outline: none;
}
.wam-btn-circle .icon {
  font-size: 2.2rem;
  margin-bottom: 2px;
}
.wam-btn-circle.start {
  background: linear-gradient(135deg,#4a90e2 60%,#7b8aff 100%);
}
.wam-btn-circle.skin {
  background: linear-gradient(135deg,#6a8dff 60%,#a6e3ff 100%);
}
.wam-btn-circle.exit {
  background: linear-gradient(135deg,#ff7043 60%,#ffb347 100%);
}
.wam-btn-circle.shop {
  background: linear-gradient(135deg,#ffb347 60%,#ffd700 100%);
  color: #fff;
  width: 90px;
  height: 90px;
  font-size: 1.05rem;
  margin-top: 32px;
  box-shadow: 0 2px 12px #ffd70033;
}
.wam-btn-circle:hover {
  box-shadow: 0 8px 32px #7b8aff44;
  transform: scale(1.08) rotate(-6deg);
  z-index: 2;
}
.wam-btn-circle.shop:hover {
  box-shadow: 0 8px 32px #ffd70044;
  transform: scale(1.12) rotate(6deg);
}
.wam-mode-panel {
  background: rgba(255,255,255,0.82);
  border-radius: 28px;
  box-shadow: 0 8px 40px 0 rgba(80,80,160,0.10);
  padding: 38px 32px 28px 32px;
  margin: 48px auto 0 auto;
  min-width: 420px;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.wam-mode-panel h2 {
  font-size: 2.1rem;
  font-weight: bold;
  color: #3a7bd5;
  margin-bottom: 32px;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px #aee1ff44;
}
.mode-list {
  display: flex;
  gap: 36px;
  justify-content: center;
  margin: 0 0 32px 0;
  width: 100%;
}
.mode-item {
  background: linear-gradient(135deg,#e3f0ff 60%,#f8fbff 100%);
  border-radius: 20px;
  box-shadow: 0 4px 24px #7b8aff22;
  padding: 32px 38px;
  cursor: pointer;
  border: 3px solid #e3e7f7;
  transition: border 0.22s, box-shadow 0.22s, transform 0.18s;
  min-width: 220px;
  min-height: 90px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
}
.mode-item.selected {
  border: 3px solid #42a5f5;
  box-shadow: 0 8px 32px #42a5f533;
  transform: scale(1.04) rotate(-2deg);
}
.mode-title {
  font-size: 1.3rem;
  font-weight: bold;
  color: #234d3c;
  margin-bottom: 10px;
  letter-spacing: 1px;
}
.mode-desc {
  font-size: 1.08rem;
  color: #666;
}
.mode-btn-row {
  margin-top: 18px;
  display: flex;
  gap: 32px;
  justify-content: center;
}
.ui-btn {
  font-size: 1.15rem;
  font-weight: 600;
  border: none;
  border-radius: 22px;
  padding: 12px 38px;
  cursor: pointer;
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  box-shadow: 0 2px 8px rgba(66,165,245,0.10);
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.ui-btn.back {
  background: linear-gradient(90deg, #bdbdbd, #90caf9);
  color: #374151;
}
.ui-btn:hover {
  background: linear-gradient(90deg, #7e57c2, #42a5f5);
  transform: scale(1.06);
}
.hall-container, .wam-entry-panel.lively {
  background: rgba(255,255,255,0.68);
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(66,165,245,0.10);
  padding: 28px 18px 18px 18px;
  margin: 48px auto 0 auto;
  min-width: 0;
  min-height: 0;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.wam-entry-panel.lively h2 {
  font-size: 1.5rem;
  font-weight: bold;
  color: #222;
  margin-bottom: 22px;
  letter-spacing: 2px;
}
/* 恢复排行榜原始样式 */
.wam-rank-panel {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px rgba(66,165,245,0.13);
  padding: 28px 32px 18px 32px;
  min-width: 220px;
  max-width: 260px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}
.wam-rank-panel h2 {
  font-size: 1.5rem;
  font-weight: bold;
  color: #222;
  margin-bottom: 10px;
  letter-spacing: 2px;
}
.wam-rank-panel ol {
  text-align: left;
  margin: 24px auto;
  font-size: 1.2rem;
}
.wam-rank-panel li {
  margin-bottom: 8px;
}
.wam-rank-panel .wam-btn {
  font-size: 1.08rem;
  font-weight: 600;
  border: none;
  border-radius: 18px;
  padding: 7px 22px;
  cursor: pointer;
  background: linear-gradient(90deg, #42a5f5, #7e57c2);
  color: #fff;
  box-shadow: 0 2px 8px rgba(66,165,245,0.10);
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
  margin-top: 18px;
}
.wam-rank-panel .wam-btn:hover {
  background: linear-gradient(90deg, #7e57c2, #42a5f5);
  transform: scale(1.06);
}
/* 悬浮排行榜卡片样式 */
.wam-rank-float {
  position: absolute;
  top: 32px;
  right: 48px;
  z-index: 30;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.wam-rank-float .wam-rank-btn {
  position: relative;
  background: linear-gradient(90deg,#7b8aff,#a6e3ff);
  color: #fff;
  border: none;
  border-radius: 999px;
  padding: 10px 32px;
  font-size: 1.18rem;
  font-weight: bold;
  box-shadow: 0 2px 12px #7b8aff22;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: box-shadow .2s, background .2s;
  outline: none;
}
.wam-rank-btn:hover {
  box-shadow: 0 8px 32px #7b8aff44;
  background: linear-gradient(90deg,#a6e3ff,#7b8aff);
}
.wam-rank-btn .icon {
  font-size: 1.3em;
  margin-right: 6px;
}
.wam-rank-card {
  margin-top: 10px;
  background: linear-gradient(135deg,rgba(255,255,255,0.98) 60%,rgba(173,216,255,0.92) 100%);
  border-radius: 28px;
  box-shadow: 0 12px 48px #7b8aff33;
  padding: 32px 32px 18px 32px;
  min-width: 300px;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fadeIn .3s;
}
.rank-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 1.25rem;
  margin-bottom: 18px;
  color: #3a7bd5;
  letter-spacing: 2px;
}
.rank-header span {
  font-size: 1.25em;
  font-weight: bold;
  margin-right: 18px;
}
.rank-select {
  border-radius: 12px;
  border: 1.5px solid #bcd;
  padding: 4px 14px;
  margin: 0 8px;
  font-size: 1.08rem;
  background: #f5faff;
  color: #374151;
  font-weight: 600;
}
.rank-close {
  background: none;
  border: none;
  font-size: 1.3rem;
  color: #888;
  cursor: pointer;
  margin-left: 12px;
}
.rank-list {
  min-height: 120px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.rank-item {
  display: flex;
  align-items: center;
  font-size: 1.08rem;
  margin-bottom: 8px;
  padding: 8px 0;
  color: #234d3c;
  font-weight: 500;
}
.rank-index { color: #7b8aff; font-weight: bold; margin-right: 8px; }
.rank-score { color: #42a5f5; font-weight: bold; margin-right: 8px; }
.rank-time { color: #7e57c2; font-weight: bold; }
.rank-empty {
  color: #aaa;
  text-align: center;
  margin-top: 30px;
  font-size: 1.15rem;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: none; }
}
.stage-select-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg,#e0f7fa 0%,#b2ebf2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.stage-select-card {
  background: rgba(255,255,255,0.96);
  border-radius: 28px;
  box-shadow: 0 8px 40px 0 rgba(80,80,160,0.13);
  padding: 44px 48px 36px 48px;
  min-width: 420px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stage-select-card h2 {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 32px;
  color: #3a7bd5;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px #aee1ff44;
}
.stage-btn-list {
  display: flex;
  flex-wrap: wrap;
  gap: 22px 32px;
  justify-content: center;
  margin: 0 0 28px 0;
  max-width: 420px;
}
.stage-btn {
  min-width: 90px;
  min-height: 48px;
  padding: 0 0;
  border-radius: 18px;
  border: none;
  font-size: 1.18rem;
  font-weight: bold;
  background: linear-gradient(90deg,#7b8aff,#a6e3ff);
  color: #fff;
  box-shadow: 0 2px 10px #0001;
  cursor: pointer;
  transition: filter .2s, box-shadow .2s, transform .2s;
  margin-bottom: 0;
}
.stage-btn.unlocked:hover {
  filter: brightness(1.08);
  box-shadow: 0 4px 18px #7b8aff33;
  transform: translateY(-2px) scale(1.04);
}
.stage-btn.locked {
  background: #eee;
  color: #bbb;
  cursor: not-allowed;
  box-shadow: none;
}
/* 皮肤仓库弹窗美化 */
.wam-skin-modal {
  position: fixed;
  left: 0; top: 0; width: 100vw; height: 100vh; z-index: 300;
  background: rgba(0,0,0,0.18); display: flex; align-items: center; justify-content: center;
}
.wam-skin-content {
  background: linear-gradient(135deg,rgba(255,255,255,0.98) 60%,rgba(173,216,255,0.92) 100%);
  border-radius: 28px;
  box-shadow: 0 12px 48px #7b8aff33;
  padding: 38px 48px 32px 48px;
  min-width: 340px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}
.wam-skin-close {
  position: absolute;
  right: 18px; top: 18px;
  background: none;
  border: none;
  font-size: 2rem;
  color: #888;
  cursor: pointer;
  z-index: 2;
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
.wam-btn:hover {
  background: linear-gradient(90deg, #7e57c2, #42a5f5);
  transform: scale(1.06);
}
/* 皮肤仓库Tab样式 */
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
.wam-fullscreen.bg1 {
  background: linear-gradient(180deg, #b3e0ff 0%, #e0f7fa 60%, #a8e063 100%);
}
.wam-fullscreen.bg2 {
  background: linear-gradient(180deg, #232946 0%, #3e497a 60%, #a8e063 100%);
}
.forest-bg-imgs {
  position: absolute;
  left: 0; right: 0; bottom: 0; top: 0;
  pointer-events: none;
  z-index: 1;
}
.forest-bg-imgs img {
  position: absolute;
  user-select: none;
  pointer-events: none;
}
</style> 