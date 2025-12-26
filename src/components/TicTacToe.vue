<template>
  <div class="tictactoe-container">
    <h2>井字棋</h2>
    <div class="board">
      <div v-for="(cell, idx) in board" :key="idx" class="cell" @click="makeMove(idx)">
        {{ cell }}
      </div>
    </div>
    <div class="info">
      <div v-if="winner">{{ winnerMsg }}</div>
      <div v-else>当前回合: {{ currentPlayer }}</div>
      <button @click="reset">重置</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
const board = ref(Array(9).fill(''))
const currentPlayer = ref('X')
const winner = ref('')

function makeMove(idx) {
  if (board.value[idx] || winner.value) return
  board.value[idx] = currentPlayer.value
  if (checkWinner()) {
    winner.value = currentPlayer.value
  } else if (board.value.every(cell => cell)) {
    winner.value = '平局'
  } else {
    currentPlayer.value = currentPlayer.value === 'X' ? 'O' : 'X'
  }
}
function checkWinner() {
  const lines = [
    [0,1,2],[3,4,5],[6,7,8],
    [0,3,6],[1,4,7],[2,5,8],
    [0,4,8],[2,4,6]
  ]
  return lines.some(([a,b,c]) => board.value[a] && board.value[a] === board.value[b] && board.value[a] === board.value[c])
}
function reset() {
  board.value = Array(9).fill('')
  currentPlayer.value = 'X'
  winner.value = ''
}
const winnerMsg = computed(() => winner.value === '平局' ? '平局！' : winner.value ? `胜者: ${winner.value}` : '')
</script>

<style scoped>
.tictactoe-container { text-align: center; margin-top: 40px; }
.board {
  display: grid;
  grid-template-columns: repeat(3, 60px);
  grid-gap: 5px;
  justify-content: center;
  margin: 20px 0;
}
.cell {
  width: 60px; height: 60px;
  background: #f0f0f0;
  font-size: 2em;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  border: 1px solid #ccc;
}
.info { margin-top: 20px; }
button { margin-top: 10px; }
</style> 