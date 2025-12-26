<template>
  <div class="sudoku-bg"></div>
  <div class="sudoku-outer">
    <div class="sudoku-container">
      <button class="sudoku-help-btn" @click="showHelp=true">{{ t('sudoku.help') }}</button>
      <!-- 右上角排行榜按钮 -->
      <button class="sudoku-rank-btn" @click="showRankCard=true">{{ t('sudoku.rankBtn') }}</button>
      <!-- 右上角排行榜卡片 -->
      <div class="sudoku-rank-card" v-if="showRankCard">
        <div class="rank-header">
          <span>{{ t('sudoku.rankHeader') }}</span>
          <select v-model="rankMode" class="rank-select" style="margin:0 8px;">
            <option value="classic">{{ t('sudoku.classic') }}</option>
            <option value="diagonal">{{ t('sudoku.diagonal') }}</option>
            <option value="killer">{{ t('sudoku.killer') }}</option>
          </select>
          <button class="rank-close" @click="showRankCard=false">{{ t('sudoku.rankClose') }}</button>
        </div>
        <div class="rank-list">
          <div v-if="rankList.length===0" class="rank-empty">{{ t('sudoku.rankEmpty') }}</div>
          <div v-for="(item,i) in rankList" :key="i" class="rank-item">
            <span class="rank-index">{{i+1}}.</span>
            <span class="rank-time">{{formatTime(item.time)}}</span>
            <span class="rank-date">{{item.date}}</span>
          </div>
        </div>
      </div>
      <template v-if="!started">
        <h2>{{ t('sudoku.title') }}</h2>
        <div class="mode-select">
          <button :class="{active: mode==='classic'}" @click="setMode('classic')">{{ t('sudoku.classic') }}</button>
          <button :class="{active: mode==='diagonal'}" @click="setMode('diagonal')">{{ t('sudoku.diagonal') }}</button>
          <button :class="{active: mode==='killer'}" @click="setMode('killer')">{{ t('sudoku.killer') }}</button>
        </div>
        <div class="entry-btns">
          <button @click="startGame">{{ t('sudoku.startGame') }}</button>
          <button @click="exitGame">{{ t('sudoku.back') }}</button>
        </div>
      </template>
      <template v-else>
        <h2>{{ t('sudoku.title') }}</h2>
        <div style="margin-bottom: 10px; color: #7a4fd3; font-weight: 600; font-size: 1.1rem;">
          关卡 {{ currentLevel + 1 }} / {{ levels.length || 0 }}
          <span style="margin-left: 12px; color: #a084ee; font-size: 1rem;">({{ (levelNames && levelNames[currentLevel]) || '' }})</span>
        </div>
        <div class="board" v-if="puzzle.length > 0 && displayBoard.length > 0">
          <div v-for="(row, i) in 9" :key="i" class="row">
            <div v-for="(col, j) in 9" :key="j" style="position:relative;display:inline-block;">
              <input
                maxlength="1"
                type="text"
                v-model="displayBoard[i][j]"
                :readonly="!!puzzle[i]?.[j]"
                :class="['killer-input', { fixed: !!puzzle[i]?.[j] }]"
                @input="onInput(i, j)"
                aria-label="数独格子"
                placeholder=""
                v-if="mode !== 'killer'"
              />
              <template v-else>
                <input
                  maxlength="1"
                  type="text"
                  v-model="displayBoard[i][j]"
                  :readonly="!!puzzle[i]?.[j]"
                  :class="['killer-input', { fixed: !!puzzle[i]?.[j] },
                    killerCageBorders[`${i},${j}`] ? 'killer-cage' : '',
                    killerCageBorders[`${i},${j}`]?.top ? 'cage-top' : '',
                    killerCageBorders[`${i},${j}`]?.bottom ? 'cage-bottom' : '',
                    killerCageBorders[`${i},${j}`]?.left ? 'cage-left' : '',
                    killerCageBorders[`${i},${j}`]?.right ? 'cage-right' : ''
                  ]"
                  @input="onInput(i, j)"
                  aria-label="数独格子"
                  placeholder=""
                  :style="killerCageBorders[`${i},${j}`] ? { '--killer-cage-color': killerCageBorders[`${i},${j}`].borderColor } : {}"
                />
                <span v-if="killerCageBorders[`${i},${j}`]?.isTopLeft"
                  class="killer-cage-sum"
                  :style="killerCageBorders[`${i},${j}`]?.sumBg || {}"
                >{{ killerCageBorders[`${i},${j}`]?.sum }}</span>
              </template>
            </div>
          </div>
        </div>
        <div v-else style="padding: 20px; text-align: center; color: #7a4fd3;">
          加载中...
        </div>
        <div class="info">
          <div class="btns-flex">
            <button @click="switchLevel(-1)" :disabled="currentLevel === 0">{{ t('sudoku.prevLevel') }}</button>
            <button @click="reset">{{ t('sudoku.reset') }}</button>
            <button @click="check">{{ t('sudoku.check') }}</button>
            <button @click="switchLevel(1)" :disabled="!levels || levels.length === 0 || currentLevel === levels.length - 1">{{ t('sudoku.nextLevel') }}</button>
            <button @click="toggleTimer">{{ timerRunning ? t('sudoku.stopTimer') : t('sudoku.startTimer') }}</button>
            <span v-if="elapsedTime > 0" class="timer">{{ t('sudoku.elapsedTime') }}: {{ formatTime(elapsedTime) }}</span>
            <button @click="showAnswerModal">{{ t('sudoku.showAnswer') }}</button>
            <button @click="exitGame">{{ t('sudoku.back') }}</button>
          </div>
          <div v-if="msg" class="sudoku-msg">{{ msg }}</div>
        </div>
        <div v-if="showAnswer" class="answer-modal">
          <div class="answer-content">
            <h3>{{ t('sudoku.answerTitle') }}</h3>
            <div v-if="notUnique" style="color:#e67e22;font-size:1.1em;margin-bottom:8px;">本题不唯一，仅供体验</div>
            <div class="answer-board">
              <div v-for="(row, i) in 9" :key="i" class="row">
                <span v-for="(col, j) in 9" :key="j">{{ allAnswers[mode]?.[currentLevel]?.[i]?.[j] || '' }}</span>
              </div>
            </div>
            <button @click="showAnswer = false">{{ t('sudoku.close') }}</button>
          </div>
        </div>
      </template>
      <div v-if="showHelp" class="sudoku-help-modal">
        <div class="sudoku-help-content">
          <h3>{{ t('sudoku.helpTitle') }}</h3>
          <ul style="text-align:left;line-height:1.8;font-size:1.08em;">
            <li><b>{{ t('sudoku.classic') }}：</b>{{ t('sudoku.helpClassic') }}</li>
            <li><b>{{ t('sudoku.diagonal') }}：</b>{{ t('sudoku.helpDiagonal') }}</li>
            <li><b>{{ t('sudoku.killer') }}：</b>{{ t('sudoku.helpKiller') }}</li>
          </ul>
          <button class="sudoku-help-close" @click="showHelp=false">{{ t('sudoku.close') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, inject, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { rankAPI } from '@/api'
const lang = inject('lang')
const setLang = inject('setLang')
const t = inject('t')
const router = useRouter()

// 检查是否使用API模式
const useAPI = import.meta.env.VITE_USE_API === 'true' || localStorage.getItem('use_api') === 'true'
// 获取当前用户ID和用户名
const currentUserId = localStorage.getItem('game_user_id') ? Number(localStorage.getItem('game_user_id')) : null
const currentUsername = localStorage.getItem('game_username') || '游客'

// 模式切换
const mode = ref('classic')
function setMode(m) {
  try {
    mode.value = m
    currentLevel.value = 0
    // 使用 nextTick 确保 mode 变化后 computed 属性已更新
    nextTick(() => {
      // 确保 levels.value 已经更新
      if (!levels.value || levels.value.length === 0) {
        console.error('levels.value is empty after mode change:', m, 'levels:', levels.value, 'diagonalLevels:', diagonalLevels, 'killerPuzzles:', killerPuzzles)
        return
      }
      reset()
      // 更新杀手数独的笼子边界缓存
      if (m === 'killer') {
        updateKillerCageBorders()
      } else {
        killerCageBorders.value = {}
      }
    })
  } catch (error) {
    console.error('setMode error:', error)
  }
}

// 关卡数据（包含题目和完整答案）
const classicLevels = [
  // 简单
  {
    puzzle: [
      [5,3,0, 0,7,0, 0,0,0],
      [6,0,0, 1,9,5, 0,0,0],
      [0,9,8, 0,0,0, 0,6,0],
      [8,0,0, 0,6,0, 0,0,3],
      [4,0,0, 8,0,3, 0,0,1],
      [7,0,0, 0,2,0, 0,0,6],
      [0,6,0, 0,0,0, 2,8,0],
      [0,0,0, 4,1,9, 0,0,5],
      [0,0,0, 0,8,0, 0,7,9],
    ],
    answer: [
      [5,3,4, 6,7,8, 9,1,2],
      [6,7,2, 1,9,5, 3,4,8],
      [1,9,8, 3,4,2, 5,6,7],
      [8,5,9, 7,6,1, 4,2,3],
      [4,2,6, 8,5,3, 7,9,1],
      [7,1,3, 9,2,4, 8,5,6],
      [9,6,1, 5,3,7, 2,8,4],
      [2,8,7, 4,1,9, 6,3,5],
      [3,4,5, 2,8,6, 1,7,9],
    ]
  },
  // 普通
  {
    puzzle: [
      [0,0,0, 2,6,0, 7,0,1],
      [6,8,0, 0,7,0, 0,9,0],
      [1,9,0, 0,0,4, 5,0,0],
      [8,2,0, 1,0,0, 0,4,0],
      [0,0,4, 6,0,2, 9,0,0],
      [0,5,0, 0,0,3, 0,2,8],
      [0,0,9, 3,0,0, 0,7,4],
      [0,4,0, 0,5,0, 0,3,6],
      [7,0,3, 0,1,8, 0,0,0],
    ],
    answer: [
      [4,3,5, 2,6,9, 7,8,1],
      [6,8,2, 5,7,1, 4,9,3],
      [1,9,7, 8,3,4, 5,6,2],
      [8,2,6, 1,9,5, 3,4,7],
      [3,7,4, 6,8,2, 9,1,5],
      [9,5,1, 7,4,3, 6,2,8],
      [5,1,9, 3,2,6, 8,7,4],
      [2,4,8, 9,5,7, 1,3,6],
      [7,6,3, 4,1,8, 2,5,9],
    ]
  },
  // 中等
  {
    puzzle: [
      [0,2,0, 6,0,8, 0,0,0],
      [5,8,0, 0,0,9, 7,0,0],
      [0,0,0, 0,4,0, 0,0,0],
      [3,7,0, 0,0,0, 5,0,0],
      [6,0,0, 0,0,0, 0,0,4],
      [0,0,8, 0,0,0, 0,1,3],
      [0,0,0, 0,2,0, 0,0,0],
      [0,0,9, 8,0,0, 0,3,6],
      [0,0,0, 3,0,6, 0,9,0],
    ],
    answer: [
      [1,2,7, 6,3,8, 9,4,5],
      [5,8,4, 2,1,9, 7,6,3],
      [9,6,3, 7,4,5, 8,2,1],
      [3,7,1, 9,8,2, 5,4,6],
      [6,9,2, 5,7,1, 3,8,4],
      [4,5,8, 3,6,7, 2,1,9],
      [7,4,6, 1,2,3, 4,5,8],
      [2,1,9, 8,5,4, 1,3,6],
      [8,3,5, 4,9,6, 6,7,2],
    ]
  },
  // 困难
  {
    puzzle: [
      [0,0,0, 0,0,0, 0,1,2],
      [0,0,0, 0,0,7, 0,0,0],
      [0,0,1, 0,9,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 5,3,8, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,8,0, 4,0,0],
      [0,0,0, 6,0,0, 0,0,0],
      [3,6,0, 0,0,0, 0,0,0],
    ],
    answer: [
      [7,4,5, 3,6,8, 9,1,2],
      [9,8,2, 1,4,7, 5,6,3],
      [6,3,1, 2,9,5, 7,8,4],
      [8,1,9, 4,7,6, 3,2,5],
      [2,7,6, 5,3,8, 1,4,9],
      [4,5,3, 9,1,2, 6,7,8],
      [1,2,7, 9,8,3, 4,5,6],
      [5,9,4, 6,2,1, 8,3,7],
      [3,6,8, 7,5,4, 2,9,1],
    ]
  },
  // 极难
  {
    puzzle: [
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,3, 0,8,5],
      [0,0,1, 0,2,0, 0,0,0],
      [0,0,0, 5,0,7, 0,0,0],
      [0,0,4, 0,0,0, 1,0,0],
      [0,9,0, 0,0,0, 0,0,0],
      [5,0,0, 0,0,0, 0,7,3],
      [0,0,2, 0,1,0, 0,0,0],
      [0,0,0, 0,4,0, 0,0,9],
    ],
    answer: [
      [9,7,8, 4,6,1, 2,3,5],
      [2,4,6, 9,7,3, 1,8,5],
      [3,5,1, 8,2,6, 9,4,7],
      [1,2,3, 5,9,7, 4,6,8],
      [7,6,4, 2,3,8, 1,5,9],
      [8,9,5, 1,4,2, 6,7,3],
      [5,1,9, 6,8,4, 7,2,3],
      [4,3,2, 7,1,9, 5,6,8],
      [6,8,7, 3,5,4, 2,1,9],
    ]
  },
]
const classicLevelNames = ['简单', '普通', '中等', '困难', '极难']

// 对角线数独关卡（包含题目和完整答案）
const diagonalLevels = [
  {
    puzzle: [
      [0,0,0, 0,0,0, 0,1,2],
      [0,0,0, 0,0,7, 0,0,0],
      [0,0,1, 0,9,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 5,3,8, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,8,0, 4,0,0],
      [0,0,0, 6,0,0, 0,0,0],
      [3,6,0, 0,0,0, 0,0,0],
    ],
    answer: [
      [7,4,5, 3,6,8, 9,1,2],
      [9,8,2, 1,4,7, 5,6,3],
      [6,3,1, 2,9,5, 7,8,4],
      [8,1,9, 4,7,6, 3,2,5],
      [2,7,6, 5,3,8, 1,4,9],
      [4,5,3, 9,1,2, 6,7,8],
      [1,2,7, 9,8,3, 4,5,6],
      [5,9,4, 6,2,1, 8,3,7],
      [3,6,8, 7,5,4, 2,9,1],
    ]
  },
  // 对角线2关卡
  {
    puzzle: [
      [0,0,0, 0,0,0, 0,0,1],
      [0,0,0, 0,0,0, 0,2,0],
      [0,0,0, 0,0,3, 0,0,0],
      [0,0,0, 0,4,0, 0,0,0],
      [0,0,0, 5,0,0, 0,0,0],
      [0,0,6, 0,0,0, 0,0,0],
      [0,7,0, 0,0,0, 0,0,0],
      [8,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,0, 9,0,0],
    ],
    answer: [
      [2,3,4, 5,6,7, 8,9,1],
      [1,4,5, 6,7,8, 9,2,3],
      [7,8,9, 1,2,3, 4,5,6],
      [3,1,2, 4,5,6, 7,8,9],
      [4,6,7, 8,9,1, 2,3,5],
      [5,9,1, 2,3,4, 6,7,8],
      [6,2,3, 7,8,9, 1,4,5],
      [9,5,6, 7,1,2, 3,4,8],
      [8,7,2, 3,4,5, 9,6,1],
    ]
  },
  // 对角线3关卡
  {
    puzzle: [
      [4,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,5,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,0],
      [0,0,0, 0,0,0, 0,0,6],
    ],
    answer: [
      [4,1,2, 3,6,7, 8,9,5],
      [3,5,6, 1,8,9, 2,4,7],
      [7,8,9, 2,4,5, 1,3,6],
      [1,2,3, 4,7,8, 5,6,9],
      [6,4,7, 9,5,1, 3,8,2],
      [5,9,8, 6,3,2, 4,7,1],
      [2,3,1, 5,9,4, 6,1,8],
      [8,6,4, 7,1,3, 9,5,2],
      [9,7,5, 8,2,6, 7,4,3],
    ]
  }
];
const diagonalLevelNames = ['对角线1', '对角线2', '对角线3'];

// 杀手数独关卡（简单和中等难度）
const killerLevels = [
  {
    // 杀手1：简单难度 - 使用一个有效的数独解，然后设计匹配的笼子
    puzzle: [
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
    ],
    cages: [
      // 第一行: [1,2,3,4,5,6,7,8,9]
      { sum: 3, cells: [[0,0],[0,1]] },      // 1+2=3 ✓
      { sum: 3, cells: [[0,2]] },          // 3 ✓
      { sum: 9, cells: [[0,3],[0,4]] },   // 4+5=9 ✓
      { sum: 6, cells: [[0,5]] },          // 6 ✓
      { sum: 15, cells: [[0,6],[0,7]] },  // 7+8=15 ✓
      { sum: 9, cells: [[0,8]] },         // 9 ✓
      // 第二行: [4,5,6,7,8,9,1,2,3]
      { sum: 9, cells: [[1,0],[1,1]] },   // 4+5=9 ✓
      { sum: 6, cells: [[1,2]] },         // 6 ✓
      { sum: 15, cells: [[1,3],[1,4]] }, // 7+8=15 ✓
      { sum: 9, cells: [[1,5]] },         // 9 ✓
      { sum: 3, cells: [[1,6],[1,7]] },   // 1+2=3 ✓
      { sum: 3, cells: [[1,8]] },         // 3 ✓
      // 第三行: [7,8,9,1,2,3,4,5,6]
      { sum: 15, cells: [[2,0],[2,1]] },  // 7+8=15 ✓
      { sum: 9, cells: [[2,2]] },         // 9 ✓
      { sum: 3, cells: [[2,3],[2,4]] },   // 1+2=3 ✓
      { sum: 3, cells: [[2,5]] },         // 3 ✓
      { sum: 9, cells: [[2,6],[2,7]] },   // 4+5=9 ✓
      { sum: 6, cells: [[2,8]] },         // 6 ✓
      // 第四行: [2,3,4,5,6,7,8,9,1]
      { sum: 2, cells: [[3,0]] },         // 2 ✓
      { sum: 7, cells: [[3,1],[3,2]] },   // 3+4=7 ✓
      { sum: 11, cells: [[3,3],[3,4]] }, // 5+6=11 ✓
      { sum: 7, cells: [[3,5]] },         // 7 ✓
      { sum: 17, cells: [[3,6],[3,7]] },  // 8+9=17 ✓
      { sum: 1, cells: [[3,8]] },         // 1 ✓
      // 第五行: [5,6,7,8,9,1,2,3,4]
      { sum: 11, cells: [[4,0],[4,1]] },  // 5+6=11 ✓
      { sum: 15, cells: [[4,2],[4,3]] }, // 7+8=15 ✓
      { sum: 9, cells: [[4,4]] },         // 9 ✓
      { sum: 3, cells: [[4,5],[4,6]] },   // 1+2=3 ✓
      { sum: 7, cells: [[4,7],[4,8]] },   // 3+4=7 ✓
      // 第六行: [8,9,1,2,3,4,5,6,7]
      { sum: 17, cells: [[5,0],[5,1]] },  // 8+9=17 ✓
      { sum: 1, cells: [[5,2]] },         // 1 ✓
      { sum: 5, cells: [[5,3],[5,4]] },   // 2+3=5 ✓
      { sum: 9, cells: [[5,5],[5,6]] },   // 4+5=9 ✓
      { sum: 13, cells: [[5,7],[5,8]] },  // 6+7=13 ✓
      // 第七行: [3,4,5,6,7,8,9,1,2]
      { sum: 7, cells: [[6,0],[6,1]] },   // 3+4=7 ✓
      { sum: 11, cells: [[6,2],[6,3]] }, // 5+6=11 ✓
      { sum: 15, cells: [[6,4],[6,5]] },  // 7+8=15 ✓
      { sum: 10, cells: [[6,6],[6,7]] }, // 9+1=10 ✓
      { sum: 2, cells: [[6,8]] },         // 2 ✓
      // 第八行: [6,7,8,9,1,2,3,4,5]
      { sum: 13, cells: [[7,0],[7,1]] },  // 6+7=13 ✓
      { sum: 8, cells: [[7,2]] },         // 8 ✓
      { sum: 10, cells: [[7,3],[7,4]] },  // 9+1=10 ✓
      { sum: 5, cells: [[7,5],[7,6]] },   // 2+3=5 ✓
      { sum: 9, cells: [[7,7],[7,8]] },   // 4+5=9 ✓
      // 第九行: [9,1,2,3,4,5,6,7,8]
      { sum: 10, cells: [[8,0],[8,1]] },  // 9+1=10 ✓
      { sum: 2, cells: [[8,2]] },         // 2 ✓
      { sum: 7, cells: [[8,3],[8,4]] },   // 3+4=7 ✓
      { sum: 11, cells: [[8,5],[8,6]] },  // 5+6=11 ✓
      { sum: 15, cells: [[8,7],[8,8]] }   // 7+8=15 ✓
    ],
    answer: [
      [1,2,3,4,5,6,7,8,9],
      [4,5,6,7,8,9,1,2,3],
      [7,8,9,1,2,3,4,5,6],
      [2,3,4,5,6,7,8,9,1],
      [5,6,7,8,9,1,2,3,4],
      [8,9,1,2,3,4,5,6,7],
      [3,4,5,6,7,8,9,1,2],
      [6,7,8,9,1,2,3,4,5],
      [9,1,2,3,4,5,6,7,8]
    ]
  },
  {
    // 杀手2：中等难度 - 使用另一个有效的数独解
    puzzle: [
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
      [0,0,0,0,0,0,0,0,0],
    ],
    cages: [
      // 第一行: [5,3,4,6,7,8,9,1,2]
      { sum: 8, cells: [[0,0],[0,1]] },    // 5+3=8 ✓
      { sum: 4, cells: [[0,2]] },          // 4 ✓
      { sum: 13, cells: [[0,3],[0,4]] },   // 6+7=13 ✓
      { sum: 8, cells: [[0,5]] },          // 8 ✓
      { sum: 10, cells: [[0,6],[0,7]] },   // 9+1=10 ✓
      { sum: 2, cells: [[0,8]] },          // 2 ✓
      // 第二行: [6,7,2,1,9,5,3,4,8]
      { sum: 13, cells: [[1,0],[1,1]] },   // 6+7=13 ✓
      { sum: 2, cells: [[1,2]] },          // 2 ✓
      { sum: 10, cells: [[1,3],[1,4]] },   // 1+9=10 ✓
      { sum: 5, cells: [[1,5]] },          // 5 ✓
      { sum: 7, cells: [[1,6],[1,7]] },    // 3+4=7 ✓
      { sum: 8, cells: [[1,8]] },          // 8 ✓
      // 第三行: [1,9,8,3,4,2,5,6,7]
      { sum: 10, cells: [[2,0],[2,1]] },   // 1+9=10 ✓
      { sum: 8, cells: [[2,2]] },          // 8 ✓
      { sum: 7, cells: [[2,3],[2,4]] },    // 3+4=7 ✓
      { sum: 2, cells: [[2,5]] },          // 2 ✓
      { sum: 11, cells: [[2,6],[2,7]] },   // 5+6=11 ✓
      { sum: 7, cells: [[2,8]] },          // 7 ✓
      // 第四行: [8,5,9,7,6,1,4,2,3]
      { sum: 13, cells: [[3,0],[3,1]] },   // 8+5=13 ✓
      { sum: 9, cells: [[3,2]] },          // 9 ✓
      { sum: 13, cells: [[3,3],[3,4]] },   // 7+6=13 ✓
      { sum: 1, cells: [[3,5]] },          // 1 ✓
      { sum: 6, cells: [[3,6],[3,7]] },    // 4+2=6 ✓
      { sum: 3, cells: [[3,8]] },          // 3 ✓
      // 第五行: [4,2,6,8,5,3,7,9,1]
      { sum: 6, cells: [[4,0],[4,1]] },    // 4+2=6 ✓
      { sum: 6, cells: [[4,2]] },          // 6 ✓
      { sum: 13, cells: [[4,3],[4,4]] },   // 8+5=13 ✓
      { sum: 3, cells: [[4,5]] },          // 3 ✓
      { sum: 16, cells: [[4,6],[4,7]] },   // 7+9=16 ✓
      { sum: 1, cells: [[4,8]] },          // 1 ✓
      // 第六行: [7,1,3,9,2,4,8,5,6]
      { sum: 8, cells: [[5,0],[5,1]] },    // 7+1=8 ✓
      { sum: 3, cells: [[5,2]] },          // 3 ✓
      { sum: 11, cells: [[5,3],[5,4]] },   // 9+2=11 ✓
      { sum: 4, cells: [[5,5]] },          // 4 ✓
      { sum: 13, cells: [[5,6],[5,7]] },   // 8+5=13 ✓
      { sum: 6, cells: [[5,8]] },          // 6 ✓
      // 第七行: [9,6,1,5,3,7,2,8,4]
      { sum: 15, cells: [[6,0],[6,1]] },   // 9+6=15 ✓
      { sum: 1, cells: [[6,2]] },          // 1 ✓
      { sum: 8, cells: [[6,3],[6,4]] },    // 5+3=8 ✓
      { sum: 7, cells: [[6,5]] },          // 7 ✓
      { sum: 10, cells: [[6,6],[6,7]] },    // 2+8=10 ✓
      { sum: 4, cells: [[6,8]] },          // 4 ✓
      // 第八行: [2,8,7,4,1,9,6,3,5]
      { sum: 10, cells: [[7,0],[7,1]] },    // 2+8=10 ✓
      { sum: 7, cells: [[7,2]] },          // 7 ✓
      { sum: 5, cells: [[7,3],[7,4]] },    // 4+1=5 ✓
      { sum: 9, cells: [[7,5]] },          // 9 ✓
      { sum: 9, cells: [[7,6],[7,7]] },    // 6+3=9 ✓
      { sum: 5, cells: [[7,8]] },          // 5 ✓
      // 第九行: [3,4,5,2,8,6,1,7,9]
      { sum: 7, cells: [[8,0],[8,1]] },    // 3+4=7 ✓
      { sum: 5, cells: [[8,2]] },          // 5 ✓
      { sum: 10, cells: [[8,3],[8,4]] },   // 2+8=10 ✓
      { sum: 6, cells: [[8,5]] },          // 6 ✓
      { sum: 8, cells: [[8,6],[8,7]] },    // 1+7=8 ✓
      { sum: 9, cells: [[8,8]] }           // 9 ✓
    ],
    answer: [
      [5,3,4,6,7,8,9,1,2],
      [6,7,2,1,9,5,3,4,8],
      [1,9,8,3,4,2,5,6,7],
      [8,5,9,7,6,1,4,2,3],
      [4,2,6,8,5,3,7,9,1],
      [7,1,3,9,2,4,8,5,6],
      [9,6,1,5,3,7,2,8,4],
      [2,8,7,4,1,9,6,3,5],
      [3,4,5,2,8,6,1,7,9]
    ]
  }
];
const killerLevelNames = ['杀手1', '杀手2'];

// 关卡和名字根据模式切换（返回puzzle数组）
const levels = computed(() => {
  const currentMode = mode.value
  if (currentMode === 'classic') {
    return classicLevels.map(l => l.puzzle)
  } else if (currentMode === 'diagonal') {
    return diagonalLevels.map(l => l.puzzle)
  } else if (currentMode === 'killer') {
    return killerLevels.map(l => l.puzzle)
  } else {
    return classicLevels.map(l => l.puzzle)
  }
})

// 预存所有答案（直接从关卡数据中获取）
const allAnswers = {
  classic: classicLevels.map(l => l.answer),
  diagonal: diagonalLevels.map(l => l.answer),
  killer: killerLevels.map(l => l.answer) // 杀手数独答案已预存
}
const levelNames = computed(() => {
  const currentMode = mode.value
  if (currentMode === 'classic') return classicLevelNames
  if (currentMode === 'diagonal') return diagonalLevelNames
  if (currentMode === 'killer') return killerLevelNames
  return classicLevelNames
})

const currentLevel = ref(0)
// 延迟初始化puzzle和displayBoard，避免在模块加载时执行computed
const puzzle = ref([])
const displayBoard = ref([])

// 初始化函数
function initPuzzle() {
  try {
    const currentLevels = levels.value
    if (!currentLevels || currentLevels.length === 0) {
      console.warn('levels.value is empty, skipping initPuzzle')
      return
    }
    if (currentLevel.value < 0 || currentLevel.value >= currentLevels.length) {
      console.warn(`currentLevel ${currentLevel.value} is out of range, resetting to 0`)
      currentLevel.value = 0
    }
    puzzle.value = JSON.parse(JSON.stringify(currentLevels[currentLevel.value]))
    displayBoard.value = currentLevels[currentLevel.value].map(row => row.map(cell => cell === 0 ? '' : String(cell)))
  } catch (error) {
    console.error('initPuzzle error:', error)
    // 设置默认值避免页面崩溃
    puzzle.value = Array(9).fill(null).map(() => Array(9).fill(0))
    displayBoard.value = Array(9).fill(null).map(() => Array(9).fill(''))
  }
}
const msg = ref('')
const started = ref(false)
const showRankCard = ref(false)
const rankList = ref([])
const showHelp = ref(false)
const rankMode = ref('classic')

// 杀手数独：笼子颜色数组
const cageColors = [
  '#e67e22', '#3498db', '#27ae60', '#9b59b6', '#e74c3c', '#16a085', '#f39c12', '#2980b9', '#8e44ad', '#2ecc71'
];

// 缓存杀手数独的笼子边界信息（使用ref，只在模式切换时计算，避免computed频繁计算）
const killerCageBorders = ref({})

// 计算并缓存笼子边界信息（包括颜色和和值，避免模板中重复计算）
function updateKillerCageBorders() {
  if (mode.value !== 'killer') {
    killerCageBorders.value = {}
    return
  }
  const borders = {}
  const cages = killerLevels[currentLevel.value].cages
  for (let idx = 0; idx < cages.length; idx++) {
    const cage = cages[idx]
    const color = cageColors[idx % cageColors.length]
    const sum = cage.sum
    for (const [i, j] of cage.cells) {
      const key = `${i},${j}`
      if (!borders[key]) {
        borders[key] = {
          top: !cageHasNeighbor(i, j, cage, -1, 0),
          bottom: !cageHasNeighbor(i, j, cage, 1, 0),
          left: !cageHasNeighbor(i, j, cage, 0, -1),
          right: !cageHasNeighbor(i, j, cage, 0, 1),
          isTopLeft: isCageTopLeft(i, j, cage),
          cageIdx: idx,
          borderColor: color, // 预计算边框颜色
          sumBg: { background: color + '22', color: color }, // 预计算和值背景
          sum: sum // 预计算和值
        }
      }
    }
  }
  killerCageBorders.value = borders
}

// 新增：排行榜数据key
const rankKeys = {
  classic: 'sudoku_rank_classic',
  diagonal: 'sudoku_rank_diagonal',
  killer: 'sudoku_rank_killer',
}

// 新增：排行榜初始数据（8~30分钟）
const defaultRanks = {
  classic: [
    {time: 574, date: '2024-对角线1'}, // 9:34
    {time: 741, date: '2024-普通'},     // 12:21
    {time: 1085, date: '2024-极难'},    // 18:05
  ],
  diagonal: [
    {time: 599, date: '2024-对角线1'},  // 9:59
    {time: 900, date: '2024-对角线2'},  // 15:00
    {time: 1200, date: '2024-对角线2'}, // 20:00
  ],
  killer: [
    {time: 800, date: '2024-杀手1'},    // 13:20
    {time: 1100, date: '2024-杀手2'},   // 18:20
    {time: 1500, date: '2024-杀手2'},   // 25:00
  ]
}

// 关卡切换时重置
function reset() {
  initPuzzle()
  msg.value = ''
  // 不强制清空答案缓存，保留已计算的答案
}
function switchLevel(offset) {
  if (!levels.value || levels.value.length === 0) return
  let next = currentLevel.value + offset
  if (next < 0) next = 0
  const maxLevel = levels.value.length
  if (next >= maxLevel) next = maxLevel - 1
  if (next !== currentLevel.value) {
    currentLevel.value = next
    reset()
    // 如果是杀手数独模式，更新笼子边界缓存
    if (mode.value === 'killer') {
      updateKillerCageBorders()
    }
  }
}

// 校验逻辑增强
function check() {
  // 标准校验
  for (let i = 0; i < 9; i++) {
    const row = new Set(), col = new Set()
    for (let j = 0; j < 9; j++) {
      const rowVal = displayBoard.value[i][j]
      const colVal = displayBoard.value[j][i]
      if (!rowVal || !colVal) {
        msg.value = '请填写完整'; return
      }
      if (row.has(rowVal)) return msg.value = `第${i+1}行有重复`
      row.add(rowVal)
      if (col.has(colVal)) return msg.value = `第${i+1}列有重复`
      col.add(colVal)
    }
  }
  for (let block = 0; block < 9; block++) {
    const set = new Set()
    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 3; j++) {
        const val = displayBoard.value[Math.floor(block/3)*3+i][(block%3)*3+j]
        if (!val) { msg.value = '请填写完整'; return }
        if (set.has(val)) return msg.value = `第${block+1}宫有重复`
        set.add(val)
      }
    }
  }
  // 对角线数独校验
  if (mode.value === 'diagonal') {
    const diag1 = new Set(), diag2 = new Set()
    for (let i = 0; i < 9; i++) {
      const v1 = displayBoard.value[i][i]
      const v2 = displayBoard.value[i][8-i]
      if (diag1.has(v1)) return msg.value = '主对角线有重复'
      if (diag2.has(v2)) return msg.value = '副对角线有重复'
      diag1.add(v1)
      diag2.add(v2)
    }
  }
  // 杀手数独校验
  if (mode.value === 'killer') {
    const cages = killerLevels[currentLevel.value].cages
    for (const cage of cages) {
      let sum = 0
      const seen = new Set()
      for (const [i, j] of cage.cells) {
        const v = Number(displayBoard.value[i][j])
        if (!v) { msg.value = '请填写完整'; return }
        if (seen.has(v)) return msg.value = '笼子内有重复数字'
        seen.add(v)
        sum += v
      }
      if (sum !== cage.sum) return msg.value = `有笼子数字之和不等于${cage.sum}`
    }
  }
  msg.value = '校验通过！'
  if (elapsedTime.value>0) updateRankList(elapsedTime.value)
}

function onInput(i, j) {
  if (puzzle.value[i][j]) return // 预设数字不可改
  let v = displayBoard.value[i][j]
  if (!/^[1-9]?$/.test(v)) displayBoard.value[i][j] = ''
}
function startGame() {
  // 确保puzzle和displayBoard已初始化
  if (!puzzle.value || puzzle.value.length === 0) {
    initPuzzle()
  }
  // 如果是杀手模式，确保笼子边界已计算
  if (mode.value === 'killer' && Object.keys(killerCageBorders.value).length === 0) {
    updateKillerCageBorders()
  }
  started.value = true
}
function exitGame() {
  // 如果正在游戏中，返回到开始界面（模式选择界面）
  if (started.value) {
    started.value = false
    // 停止计时器
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
      timerRunning.value = false
    }
    // 重置游戏状态
    elapsedTime.value = 0
    msg.value = ''
    showAnswer.value = false
  } else {
    // 如果在开始界面，返回到游戏大厅
    router.back()
  }
}
// 计时器
const timer = ref(null)
const timerRunning = ref(false)
const elapsedTime = ref(0)
function toggleTimer() {
  if (timerRunning.value) {
    clearInterval(timer.value)
    timerRunning.value = false
  } else {
    timerRunning.value = true
    timer.value = setInterval(() => {
      elapsedTime.value++
    }, 1000)
  }
}
function formatTime(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}
onUnmounted(() => { if (timer.value) clearInterval(timer.value) })
// 答案预计算：存储所有模式的答案
const answers = ref({
  classic: Array(5).fill(null),
  diagonal: Array(3).fill(null),
  killer: Array(2).fill(null)
})
const showAnswer = ref(false)
const notUnique = ref(false)

// 预先计算所有答案（只计算标准和对角线，杀手数独按需计算）
function precomputeAllAnswers() {
  // 只预计算标准和对角线数独，它们计算很快
  // 杀手数独计算太慢，改为按需计算（点击显示答案时才计算）
  let index = 0
  const tasks = []
  
  // 答案已预存在 allAnswers 中，不再需要预计算
  // 保留此函数用于兼容性，但不执行任何操作
  
  // 分批执行任务（杀手数独不预计算）
  function runNext() {
    if (index < tasks.length) {
      tasks[index]()
      index++
      // 每计算一个答案后，让出控制权给浏览器渲染
      setTimeout(runNext, 5)
    } else {
      console.log('标准和对角线数独答案已预计算完成（杀手数独按需计算）')
    }
  }
  
  runNext()
}

function showAnswerModal() {
  // 直接使用预存的完整答案，无需计算
  const modeKey = mode.value
  let answer = allAnswers[modeKey]?.[currentLevel.value]
  
  // 如果答案存在，直接显示
  if (answer) {
    // 检查唯一解（对角线数独第二关提示）
    let solutionCount = 0
    function solveAll(board) {
      const b = board.map(row => row.slice())
      function isValid(r, c, val) {
        for (let i = 0; i < 9; i++) {
          if (b[r][i] === val || b[i][c] === val) return false
        }
        const br = Math.floor(r/3)*3, bc = Math.floor(c/3)*3
        for (let i = 0; i < 3; i++) for (let j = 0; j < 3; j++)
          if (b[br+i][bc+j] === val) return false
        if (mode.value === 'diagonal') {
          if (r === c) for (let i = 0; i < 9; i++) if (b[i][i] === val) return false
          if (r + c === 8) for (let i = 0; i < 9; i++) if (b[i][8-i] === val) return false
        }
        return true
      }
      function dfs(pos=0) {
        if (pos === 81) { solutionCount++; return solutionCount > 1 }
        const r = Math.floor(pos/9), c = pos%9
        if (b[r][c]) return dfs(pos+1)
        for (let v = 1; v <= 9; v++) {
          if (isValid(r, c, v)) {
            b[r][c] = v
            if (dfs(pos+1)) return true
            b[r][c] = 0
          }
        }
        return false
      }
      dfs()
    }
    if (mode.value === 'killer' && currentLevel.value === 1) {
      // 杀手2答案不唯一，强制提示
      notUnique.value = true
    } else if (mode.value === 'diagonal' && currentLevel.value === 1) {
      solveAll(levels.value[currentLevel.value])
      notUnique.value = solutionCount !== 1
    } else {
      notUnique.value = false
    }
    showAnswer.value = true
    return
  }
  
  // 如果答案不存在（不应该发生，因为所有答案都已预存），尝试计算
  if (mode.value === 'killer') {
    msg.value = '正在计算答案，请稍候...'
    showAnswer.value = false
    setTimeout(() => {
      try {
        answer = solveKillerSudoku(
          killerLevels[currentLevel.value].puzzle,
          killerLevels[currentLevel.value].cages,
          currentLevel.value
        )
        if (!answer || !Array.isArray(answer) || answer.length === 0 || answer.every(row => row.every(cell => cell === 0))) {
          msg.value = '计算答案失败，请重试'
          console.error('杀手数独答案无效:', answer)
          return
        }
        allAnswers.killer[currentLevel.value] = answer
        msg.value = ''
        showAnswer.value = true
      } catch (error) {
        console.error('计算答案失败:', error)
        msg.value = '计算答案失败，请重试'
      }
    }, 100)
    return
  } else {
    // 标准和对角线数独应该有预存答案，如果不存在则计算（不应该发生）
    answer = solveSudoku(levels.value[currentLevel.value])
    if (!answer || answer.length === 0) {
      msg.value = '计算答案失败，请重试'
      return
    }
    allAnswers[modeKey][currentLevel.value] = answer
    showAnswer.value = true
  }
}
// 简单回溯求解
function solveSudoku(board) {
  const b = board.map(row => row.slice());
  const rows = Array.from({length:9},()=>new Set());
  const cols = Array.from({length:9},()=>new Set());
  const boxes = Array.from({length:9},()=>new Set());
  const diag1 = new Set(), diag2 = new Set();
  for (let i=0;i<9;i++) for (let j=0;j<9;j++) {
    const v = b[i][j];
    if (v) {
      rows[i].add(v);
      cols[j].add(v);
      boxes[Math.floor(i/3)*3+Math.floor(j/3)].add(v);
      if (mode.value==='diagonal') {
        if (i===j) diag1.add(v);
        if (i+j===8) diag2.add(v);
      }
    }
  }
  function dfs(pos=0) {
    if (pos===81) return true;
    const r = Math.floor(pos/9), c = pos%9;
    if (b[r][c]) return dfs(pos+1);
    for (let v=1;v<=9;v++) {
      if (rows[r].has(v) || cols[c].has(v) || boxes[Math.floor(r/3)*3+Math.floor(c/3)].has(v)) continue;
      if (mode.value==='diagonal') {
        if ((r===c && diag1.has(v)) || (r+c===8 && diag2.has(v))) continue;
      }
      b[r][c]=v;
      rows[r].add(v); cols[c].add(v); boxes[Math.floor(r/3)*3+Math.floor(c/3)].add(v);
      if (mode.value==='diagonal') {
        if (r===c) diag1.add(v);
        if (r+c===8) diag2.add(v);
      }
      if (dfs(pos+1)) return true;
      b[r][c]=0;
      rows[r].delete(v); cols[c].delete(v); boxes[Math.floor(r/3)*3+Math.floor(c/3)].delete(v);
      if (mode.value==='diagonal') {
        if (r===c) diag1.delete(v);
        if (r+c===8) diag2.delete(v);
      }
    }
    return false;
  }
  dfs();
  return b;
}
async function loadRankList() {
  if (useAPI) {
    try {
      const response = await rankAPI.getRankList('sudoku', rankMode.value, 10)
      if (response.success) {
        // 转换格式：{time, rankData} -> {time, date}
        rankList.value = response.data.map(item => {
          let date = '未知日期'
          try {
            if (item.rankData) {
              const data = JSON.parse(item.rankData)
              date = data.date || date
            }
          } catch {}
          return {
            time: item.time,
            date: date
          }
        })
        // 如果没有数据，不写入默认数据
        return
      }
    } catch (error) {
      console.error('获取排行榜失败:', error)
      // API失败时回退到localStorage
    }
  }
  
  // localStorage模式（原有逻辑）
  let arr = []
  const key = rankKeys[rankMode.value]
  try {
    arr = JSON.parse(localStorage.getItem(key)||'[]')
    if (!Array.isArray(arr)) arr = []
  } catch { arr = [] }
  if (arr.length === 0) {
    arr = defaultRanks[rankMode.value]
    localStorage.setItem(key, JSON.stringify(arr))
  }
  rankList.value = arr
}

async function updateRankList(time) {
  if (useAPI && currentUserId) {
    try {
      const now = new Date()
      const dateStr = `${now.getFullYear()}-${now.getMonth()+1}-${now.getDate()}`
      const rankData = JSON.stringify({date: dateStr})
      
      // 提交到API（数独总是创建新记录）
      const response = await rankAPI.submitRank('sudoku', {
        userId: currentUserId,
        username: currentUsername,
        mode: mode.value,
        time: time,
        rankData: rankData
      })
      if (response.success) {
        console.log('排行榜已更新:', response.data)
        // 重新加载排行榜
        if (rankMode.value === mode.value) {
          await loadRankList()
        }
        return
      }
    } catch (error) {
      console.error('提交排行榜失败:', error)
      // API失败时回退到localStorage
    }
  }
  
  // localStorage模式（原有逻辑）
  const key = rankKeys[mode.value]
  let arr = []
  try {
    arr = JSON.parse(localStorage.getItem(key)||'[]')
    if (!Array.isArray(arr)) arr = []
  } catch { arr = [] }
  const now = new Date()
  const dateStr = `${now.getFullYear()}-${now.getMonth()+1}-${now.getDate()}`
  arr.push({time, date: dateStr})
  arr.sort((a,b)=>a.time-b.time)
  arr = arr.slice(0,10)
  localStorage.setItem(key, JSON.stringify(arr))
  // 如果当前排行榜正好在本模式，刷新
  if (rankMode.value === mode.value) rankList.value = arr
}
watch([showRankCard, rankMode], ([v])=>{ 
  if(v) {
    // 使用 setTimeout 避免阻塞UI
    setTimeout(() => {
      loadRankList().catch(err => {
        console.error('loadRankList error:', err)
      })
    }, 0)
  }
})

// 组件挂载时预计算所有答案
onMounted(() => {
  // 使用 setTimeout 延迟初始化，确保所有 computed 属性都已准备好
  setTimeout(() => {
    try {
      // 初始化puzzle和displayBoard（延迟到mounted，避免在模块加载时执行computed）
      initPuzzle()
      
      // 初始化杀手数独的笼子边界（如果当前是杀手模式）
      if (mode.value === 'killer') {
        updateKillerCageBorders()
      }
    } catch (error) {
      console.error('onMounted initialization error:', error)
    }
  }, 0)
  
  // 完全禁用预计算，改为按需计算，避免阻塞页面
})
// 杀手数独：辅助函数，判断格子属于哪个笼子
function getCageIndex(i, j) {
  if (mode.value !== 'killer') return -1;
  const cages = killerLevels[currentLevel.value].cages;
  for (let idx = 0; idx < cages.length; idx++) {
    if (cages[idx].cells.some(([ci, cj]) => ci === i && cj === j)) return idx;
  }
  return -1;
}
// 杀手数独：判断格子是否是笼子的左上角
function isCageTopLeft(i, j, cage) {
  return cage.cells.every(([ci, cj]) => (ci > i || (ci === i && cj >= j)));
}
// 杀手数独：判断边界（上、下、左、右）
function cageHasNeighbor(i, j, cage, di, dj) {
  return cage.cells.some(([ci, cj]) => ci === i + di && cj === j + dj);
}
// 杀手数独：获取笼子边框颜色
function getCageBorderColor(idx) {
  const color = cageColors[idx % cageColors.length];
  return {
    '--killer-cage-color': color
  };
}
// 杀手数独：获取和数字背景色
function getCageSumBg(idx) {
  const color = cageColors[idx % cageColors.length];
  return {
    background: color + '22', // 加透明度
    color: color
  };
}
function solveKillerSudoku(puzzle, cages, level) {
  // 使用回溯算法求解杀手数独
  const b = puzzle.map(row => row.slice())
  
  // 获取格子所在的笼子索引
  function getCageIndexForLevel(i, j, lvl) {
    const levelCages = killerLevels[lvl].cages
    for (let idx = 0; idx < levelCages.length; idx++) {
      if (levelCages[idx].cells.some(([ci, cj]) => ci === i && cj === j)) return idx
    }
    return -1
  }
  
  // 验证标准数独规则和笼子约束
  function isValid(r, c, val) {
    // 行、列、宫
    for (let i = 0; i < 9; i++) {
      if (b[r][i] === val || b[i][c] === val) return false
    }
    const br = Math.floor(r/3)*3, bc = Math.floor(c/3)*3
    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 3; j++) {
        if (b[br+i][bc+j] === val) return false
      }
    }
    
    // 验证笼子约束：当前格子的值加上已填写的值不能超过笼子的和
    const cageIdx = getCageIndexForLevel(r, c, level)
    if (cageIdx !== -1) {
      const cage = cages[cageIdx]
      let currentSum = val
      let filledCount = 1
      for (const [ci, cj] of cage.cells) {
        if (ci === r && cj === c) continue
        if (b[ci][cj] !== 0) {
          currentSum += b[ci][cj]
          filledCount++
        }
      }
      // 如果所有格子都填满了，和必须等于cage.sum
      if (filledCount === cage.cells.length) {
        if (currentSum !== cage.sum) return false
      } else {
        // 如果还没填满，和不能超过cage.sum
        if (currentSum > cage.sum) return false
        // 最小可能和也不能太小（简化处理，这里只检查上限）
      }
    }
    return true
  }
  
  function dfs(pos = 0) {
    if (pos === 81) {
      // 验证所有笼子的和
      for (const cage of cages) {
        let sum = 0
        for (const [i, j] of cage.cells) {
          sum += b[i][j]
        }
        if (sum !== cage.sum) return false
      }
      return true
    }
    const r = Math.floor(pos/9), c = pos%9
    if (b[r][c] !== 0) return dfs(pos+1)
    for (let v = 1; v <= 9; v++) {
      if (isValid(r, c, v)) {
        b[r][c] = v
        if (dfs(pos+1)) return true
        b[r][c] = 0
      }
    }
    return false
  }
  
  if (dfs()) {
    return b
  }
  
  // 如果求解失败，返回null而不是空数组，这样更容易检测
  console.error('杀手数独求解失败')
  return null
}
</script>

<style scoped>
.sudoku-bg {
  position: fixed;
  left: 0; top: 0; width: 100vw; height: 100vh;
  background: url('@/assets/shudu2.jpg') center center/cover no-repeat;
  z-index: 0;
}
.sudoku-outer {
  min-height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  left: 0; top: 0;
  z-index: 10;
  pointer-events: none;
}
.sudoku-container {
  pointer-events: auto;
  text-align: center;
  background: rgba(255,255,255,0.75);
  border-radius: 28px;
  box-shadow: 0 8px 40px 0 rgba(80,80,160,0.18), 0 1.5px 8px 0 rgba(80,80,160,0.10);
  padding: 44px 48px 32px 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  -webkit-backdrop-filter: blur(12px) saturate(1.2);
  backdrop-filter: blur(12px) saturate(1.2);
  border: 1.5px solid rgba(120,120,180,0.13);
  min-width: 420px;
}
.mode-select {
  display: flex;
  gap: 18px;
  justify-content: center;
  margin-bottom: 18px;
}
.mode-select button {
  padding: 8px 22px;
  border-radius: 18px;
  border: none;
  background: linear-gradient(90deg, #e0e7ff 0%, #a6e3ff 100%);
  color: #7a4fd3;
  font-weight: 700;
  font-size: 1.08rem;
  cursor: pointer;
  transition: background 0.18s, color 0.18s, box-shadow 0.18s;
  box-shadow: 0 2px 8px rgba(106,141,255,0.08);
}
.mode-select button.active, .mode-select button:hover {
  background: linear-gradient(90deg, #6a8dff 0%, #a084ee 100%);
  color: #fff;
}
.killer-cages {
  position: absolute;
  left: 0; top: 0;
  width: 100%; height: 100%;
  pointer-events: none;
}
.killer-cage-label {
  pointer-events: none;
  box-shadow: 0 1.5px 6px rgba(120,120,180,0.07);
}
h2 {
  font-size: 2.1rem;
  font-weight: 800;
  letter-spacing: 2px;
  color: #3a3a5a;
  margin-bottom: 28px;
  text-shadow: 0 2px 8px rgba(120,120,180,0.08);
}
.board {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 18px 0 10px 0;
}
.row {
  display: flex;
}
input {
  width: 36px; height: 36px;
  text-align: center;
  font-size: 22px;
  margin: 1.5px;
  border: 1.5px solid #bfc6e0;
  background: #f7f8fc;
  border-radius: 8px;
  box-shadow: 0 1.5px 6px rgba(120,120,180,0.07);
  transition: background 0.2s, border 0.2s, box-shadow 0.2s;
  outline: none;
}
input:focus {
  background: #e6eaff;
  border: 1.5px solid #7a8cff;
  box-shadow: 0 2px 8px rgba(120,120,180,0.13);
}
input.fixed {
  background: #e3e6f3;
  color: #333;
  font-weight: bold;
  cursor: not-allowed;
  border: 1.5px solid #bfc6e0;
}
input:nth-child(3n) { border-right: 2.5px solid #6a7bb8; }
.row:nth-child(3n) input { border-bottom: 2.5px solid #6a7bb8; }
.info {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.btns-flex {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 18px 24px;
  margin: 24px 0 10px 0;
  row-gap: 18px;
}
.btns-flex button {
  min-width: 110px;
  margin: 0;
}
.btns-flex .timer {
  min-width: 80px;
  text-align: center;
  margin: 0 8px;
}
button {
  padding: 10px 32px;
  font-size: 1.1rem;
  border-radius: 22px;
  border: none;
  background: linear-gradient(90deg, #6a8dff 0%, #a084ee 100%);
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 2px 8px rgba(106,141,255,0.10);
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
  margin: 0 6px;
}
button:hover {
  background: linear-gradient(90deg, #4e6edb 0%, #7c5fd3 100%);
  box-shadow: 0 4px 16px rgba(106,141,255,0.13);
  transform: translateY(-2px) scale(1.04);
}
.sudoku-msg {
  margin-top: 10px;
  min-height: 28px;
  padding: 6px 18px;
  border-radius: 16px;
  background: linear-gradient(90deg, #f7e8ff 0%, #e6f0ff 100%);
  color: #7a4fd3;
  font-size: 1rem;
  font-weight: 500;
  box-shadow: 0 1.5px 8px rgba(160,120,255,0.07);
  display: inline-block;
  letter-spacing: 1px;
}
.entry-btns { display: flex; gap: 32px; justify-content: center; margin-top: 32px; }
.answer-modal {
  position: fixed; left: 0; top: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.18); z-index: 100;
  display: flex; align-items: center; justify-content: center;
}
.answer-content {
  background: #fff; border-radius: 18px; padding: 32px 38px; box-shadow: 0 4px 24px rgba(80,80,160,0.13);
  text-align: center;
}
.answer-board { margin: 18px 0; }
.answer-board .row { display: flex; justify-content: center; }
.answer-board span {
  display: inline-block; width: 28px; height: 28px; line-height: 28px;
  background: #f7f8fc; border-radius: 6px; margin: 1.5px; font-size: 18px; color: #7a4fd3;
  font-weight: 600;
}
.sudoku-rank-btn {
  position: absolute;
  top: 32px;
  right: 48px;
  z-index: 20;
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
.sudoku-rank-btn:hover { box-shadow: 0 4px 16px #0003; }
.sudoku-rank-card {
  position: absolute;
  top: 70px;
  right: 48px;
  z-index: 30;
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
.rank-time {
  color: #333;
  font-weight: bold;
  margin-right: 12px;
}
.rank-date {
  color: #aaa;
  font-size: 0.95em;
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
.killer-input.killer-cage.cage-top {
  border-top: 2px dashed var(--killer-cage-color, #e67e22) !important;
}
.killer-input.killer-cage.cage-bottom {
  border-bottom: 2px dashed var(--killer-cage-color, #e67e22) !important;
}
.killer-input.killer-cage.cage-left {
  border-left: 2px dashed var(--killer-cage-color, #e67e22) !important;
}
.killer-input.killer-cage.cage-right {
  border-right: 2px dashed var(--killer-cage-color, #e67e22) !important;
}
.killer-cage-sum {
  position: absolute;
  left: 4px;
  top: 2px;
  font-size: 12px;
  font-weight: bold;
  border-radius: 4px;
  padding: 0 4px;
  pointer-events: none;
  z-index: 2;
  background: #fff7;
  color: #e67e22;
}
.sudoku-help-btn {
  position: absolute;
  top: 24px;
  left: 32px;
  z-index: 30;
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
.sudoku-help-btn:hover { box-shadow: 0 4px 16px #0003; }
.sudoku-help-modal {
  position: fixed; left: 0; top: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.18); z-index: 100;
  display: flex; align-items: flex-start; justify-content: flex-start;
}
.sudoku-help-content {
  margin: 60px 0 0 60px;
  background: #fff; border-radius: 18px; padding: 32px 38px; box-shadow: 0 4px 24px rgba(80,80,160,0.13);
  min-width: 340px;
  max-width: 420px;
}
.sudoku-help-content h3 { text-align:center; margin-bottom: 18px; }
.sudoku-help-close {
  margin-top: 18px;
  background: linear-gradient(90deg, #6a8dff 0%, #a084ee 100%);
  color: #fff;
  border: none;
  border-radius: 18px;
  padding: 8px 32px;
  font-size: 1.08rem;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(106,141,255,0.10);
  transition: background 0.18s, box-shadow 0.18s, transform 0.13s;
}
.sudoku-help-close:hover {
  background: linear-gradient(90deg, #4e6edb 0%, #7c5fd3 100%);
  box-shadow: 0 4px 16px rgba(106,141,255,0.13);
  transform: translateY(-2px) scale(1.04);
}
</style> 