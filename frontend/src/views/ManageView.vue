<script setup>
import { ref, onMounted } from 'vue'
import { itemApi } from '../api'
import { validateItemName } from '../utils/validators'

const items = ref([])
const loading = ref(true)
const errorMsg = ref('')
const successMsg = ref('')

const newItemName = ref('')
const creating = ref(false)

const editingId = ref(null)
const editingName = ref('')
const savingEdit = ref(false)

const deletingId = ref(null)

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

function flashSuccess(msg) {
  successMsg.value = msg
  setTimeout(() => {
    if (successMsg.value === msg) successMsg.value = ''
  }, 3000)
}

async function createItem() {
  errorMsg.value = ''
  const err = validateItemName(newItemName.value)
  if (err) {
    errorMsg.value = err
    return
  }
  creating.value = true
  try {
    await itemApi.create(newItemName.value.trim())
    newItemName.value = ''
    flashSuccess('新增成功')
    await loadItems()
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    creating.value = false
  }
}

function startEdit(item) {
  editingId.value = item.itemId
  editingName.value = item.itemName
  errorMsg.value = ''
}

function cancelEdit() {
  editingId.value = null
  editingName.value = ''
}

async function saveEdit() {
  errorMsg.value = ''
  const err = validateItemName(editingName.value)
  if (err) {
    errorMsg.value = err
    return
  }
  savingEdit.value = true
  try {
    await itemApi.update(editingId.value, editingName.value.trim())
    flashSuccess('更新成功')
    cancelEdit()
    await loadItems()
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    savingEdit.value = false
  }
}

async function deleteItem(item) {
  if (!window.confirm(`確定要刪除「${item.itemName}」嗎？此操作無法復原。`)) return
  deletingId.value = item.itemId
  errorMsg.value = ''
  try {
    await itemApi.remove(item.itemId)
    flashSuccess('刪除成功')
    await loadItems()
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    deletingId.value = null
  }
}

onMounted(loadItems)
</script>

<template>
  <section class="manage-view">
    <header class="page-head">
      <p class="eyebrow">後台管理</p>
      <h1 class="title">投票項目維護</h1>
      <p class="subtitle">新增、編輯或刪除投票項目。</p>
    </header>

    <form class="create-form" @submit.prevent="createItem">
      <input
        v-model="newItemName"
        type="text"
        maxlength="100"
        placeholder="輸入新投票項目名稱"
        autocomplete="off"
      />
      <button type="submit" class="btn btn-primary" :disabled="creating">
        {{ creating ? '新增中…' : '新增項目' }}
      </button>
    </form>

    <p v-if="errorMsg" class="msg error">{{ errorMsg }}</p>
    <p v-if="successMsg" class="msg success">{{ successMsg }}</p>

    <div v-if="loading" class="state-msg">載入中…</div>
    <div v-else-if="items.length === 0" class="state-msg">尚無投票項目，請於上方新增。</div>

    <table v-else class="item-table">
      <thead>
        <tr>
          <th class="col-id">編號</th>
          <th>項目名稱</th>
          <th class="col-count">票數</th>
          <th class="col-actions">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in items" :key="item.itemId">
          <td class="col-id">{{ item.itemId }}</td>
          <td>
            <input
              v-if="editingId === item.itemId"
              v-model="editingName"
              type="text"
              maxlength="100"
              class="edit-input"
              @keyup.enter="saveEdit"
              @keyup.escape="cancelEdit"
            />
            <span v-else>{{ item.itemName }}</span>
          </td>
          <td class="col-count">{{ item.voteCount }}</td>
          <td class="col-actions">
            <template v-if="editingId === item.itemId">
              <button class="btn btn-small btn-primary" :disabled="savingEdit" @click="saveEdit">
                {{ savingEdit ? '儲存中…' : '儲存' }}
              </button>
              <button class="btn btn-small btn-ghost" @click="cancelEdit">取消</button>
            </template>
            <template v-else>
              <button class="btn btn-small btn-ghost" @click="startEdit(item)">編輯</button>
              <button
                class="btn btn-small btn-danger"
                :disabled="deletingId === item.itemId"
                @click="deleteItem(item)"
              >
                {{ deletingId === item.itemId ? '刪除中…' : '刪除' }}
              </button>
            </template>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.page-head {
  margin-bottom: var(--space-4);
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
}

.subtitle {
  margin: 0;
  color: var(--color-ink-soft);
}

.create-form {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.create-form input {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  border: 1.5px solid var(--color-line);
  border-radius: var(--radius);
  font-size: 1rem;
  background: #fff;
  color: var(--color-ink);
}

.create-form input:focus {
  border-color: var(--color-seal);
}

.btn {
  border: none;
  border-radius: var(--radius);
  font-weight: 600;
  padding: var(--space-2) var(--space-4);
  font-size: 0.95rem;
  transition: opacity 0.15s ease, background 0.15s ease;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: var(--color-seal);
  color: var(--color-paper);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-seal-deep);
}

.btn-ghost {
  background: transparent;
  border: 1.5px solid var(--color-line);
  color: var(--color-ink-soft);
}

.btn-ghost:hover {
  border-color: var(--color-ink-soft);
  color: var(--color-ink);
}

.btn-danger {
  background: transparent;
  border: 1.5px solid var(--color-seal);
  color: var(--color-seal-deep);
}

.btn-danger:hover:not(:disabled) {
  background: var(--color-seal);
  color: var(--color-paper);
}

.btn-small {
  padding: var(--space-1) var(--space-2);
  font-size: 0.82rem;
  margin-right: var(--space-1);
}

.msg {
  margin: 0 0 var(--space-3);
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

.state-msg {
  padding: var(--space-5);
  text-align: center;
  color: var(--color-ink-soft);
}

.item-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-paper-deep);
  border-radius: var(--radius);
  overflow: hidden;
}

.item-table th,
.item-table td {
  padding: var(--space-2) var(--space-3);
  text-align: left;
  border-bottom: 1px solid var(--color-line);
}

.item-table th {
  font-family: var(--font-mono);
  font-size: 0.78rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--color-ink-soft);
  border-bottom: 2px solid var(--color-ink);
}

.col-id {
  width: 64px;
  font-family: var(--font-mono);
}

.col-count {
  width: 80px;
  font-family: var(--font-mono);
  font-weight: 700;
}

.col-actions {
  width: 180px;
  white-space: nowrap;
}

.edit-input {
  width: 100%;
  padding: var(--space-1) var(--space-2);
  border: 1.5px solid var(--color-seal);
  border-radius: var(--radius);
  font-size: 0.95rem;
}

@media (max-width: 600px) {
  .create-form {
    flex-direction: column;
  }
  .item-table {
    font-size: 0.85rem;
  }
  .col-actions {
    width: auto;
  }
}
</style>
