<script setup>
import { ref, computed, onMounted } from 'vue'
import VoteCard from '../components/VoteCard.vue'
import { itemApi, voteApi } from '../api'
import { validateVoterName } from '../utils/validators'

const items = ref([])
const selectedIds = ref(new Set())
const voterName = ref('')
const loading = ref(true)
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const maxVotes = computed(() =>
  items.value.length ? Math.max(...items.value.map((i) => i.voteCount), 1) : 1
)

async function loadItems() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await itemApi.list()
    items.value = res.data || []
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    loading.value = false
  }
}

function toggleSelect(itemId) {
  if (selectedIds.value.has(itemId)) {
    selectedIds.value.delete(itemId)
  } else {
    selectedIds.value.add(itemId)
  }
  // 觸發響應式更新 (Set 內部變動需要重新賦值才能讓 Vue 偵測到)
  selectedIds.value = new Set(selectedIds.value)
}

async function submitVote() {
  errorMsg.value = ''
  successMsg.value = ''

  const nameError = validateVoterName(voterName.value)
  if (nameError) {
    errorMsg.value = nameError
    return
  }
  if (selectedIds.value.size === 0) {
    errorMsg.value = '請至少選擇一個投票項目'
    return
  }

  submitting.value = true
  try {
    await voteApi.cast(voterName.value.trim(), Array.from(selectedIds.value))
    successMsg.value = '投票成功，感謝您的參與！'
    selectedIds.value = new Set()
    voterName.value = ''
    await loadItems()
  } catch (err) {
    errorMsg.value = err.message
  } finally {
    submitting.value = false
  }
}

onMounted(loadItems)
</script>

<template>
  <section class="vote-view">
    <header class="page-head">
      <p class="eyebrow">第 01 期 · 辦公室用品票選</p>
      <h1 class="title">您支持哪個選項？</h1>
      <p class="subtitle">可多選，票數即時累積顯示。</p>
    </header>

    <div v-if="loading" class="state-msg">載入投票項目中…</div>

    <template v-else>
      <div v-if="items.length === 0" class="state-msg">目前尚無投票項目，請洽後台管理新增。</div>

      <div v-else class="card-list">
        <VoteCard
          v-for="item in items"
          :key="item.itemId"
          :item="item"
          :selected="selectedIds.has(item.itemId)"
          :max-votes="maxVotes"
          @toggle="toggleSelect"
        />
      </div>

      <form class="vote-form" @submit.prevent="submitVote" v-if="items.length">
        <label class="field">
          <span class="field-label">您的姓名</span>
          <input
            v-model="voterName"
            type="text"
            maxlength="50"
            placeholder="請輸入姓名"
            autocomplete="off"
          />
        </label>

        <p v-if="errorMsg" class="msg error">{{ errorMsg }}</p>
        <p v-if="successMsg" class="msg success">{{ successMsg }}</p>

        <button type="submit" class="submit-btn" :disabled="submitting">
          {{ submitting ? '送出中…' : '送出投票' }}
        </button>
      </form>
    </template>
  </section>
</template>

<style scoped>
.page-head {
  margin-bottom: var(--space-5);
  border-bottom: 1px solid var(--color-line);
  padding-bottom: var(--space-4);
}

.eyebrow {
  font-family: var(--font-mono);
  font-size: 0.78rem;
  letter-spacing: 0.08em;
  color: var(--color-seal);
  margin: 0 0 var(--space-2);
  text-transform: uppercase;
}

.title {
  font-family: var(--font-display);
  font-size: 2rem;
  margin: 0 0 var(--space-2);
  color: var(--color-ink);
}

.subtitle {
  margin: 0;
  color: var(--color-ink-soft);
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.state-msg {
  padding: var(--space-5);
  text-align: center;
  color: var(--color-ink-soft);
  font-family: var(--font-body);
}

.vote-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 2px solid var(--color-ink);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  max-width: 320px;
}

.field-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--color-ink-soft);
}

.field input {
  padding: var(--space-2) var(--space-3);
  border: 1.5px solid var(--color-line);
  border-radius: var(--radius);
  background: #fff;
  font-size: 1rem;
  color: var(--color-ink);
}

.field input:focus {
  border-color: var(--color-seal);
}

.submit-btn {
  align-self: flex-start;
  padding: var(--space-2) var(--space-5);
  background: var(--color-seal);
  color: var(--color-paper);
  border: none;
  border-radius: var(--radius);
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: 0.02em;
  transition: background 0.15s ease;
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-seal-deep);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.msg {
  margin: 0;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius);
  font-size: 0.9rem;
}

.msg.error {
  background: #F4DEDB;
  color: var(--color-seal-deep);
}

.msg.success {
  background: #DCEADF;
  color: var(--color-success);
}
</style>
