<script setup>
defineProps({
  item: { type: Object, required: true },
  selected: { type: Boolean, default: false },
  maxVotes: { type: Number, default: 1 }
})
defineEmits(['toggle'])
</script>

<template>
  <label
    class="vote-card"
    :class="{ selected }"
  >
    <input
      type="checkbox"
      class="checkbox"
      :checked="selected"
      @change="$emit('toggle', item.itemId)"
    />
    <div class="card-body">
      <!-- Vue 插值預設自動 HTML-escape，可防止 XSS -->
      <span class="item-name">{{ item.itemName }}</span>
      <div class="bar-track">
        <div
          class="bar-fill"
          :style="{ width: maxVotes > 0 ? `${(item.voteCount / maxVotes) * 100}%` : '0%' }"
        ></div>
      </div>
    </div>
    <span class="vote-count">{{ item.voteCount }}</span>
  </label>
</template>

<style scoped>
.vote-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--color-paper-deep);
  border: 1.5px solid var(--color-line);
  border-radius: var(--radius);
  cursor: pointer;
  transition: border-color 0.15s ease, transform 0.1s ease;
}

.vote-card:hover {
  border-color: var(--color-ink-soft);
}

.vote-card.selected {
  border-color: var(--color-seal);
  background: #F1E6DC;
}

.checkbox {
  width: 20px;
  height: 20px;
  accent-color: var(--color-seal);
  flex-shrink: 0;
  cursor: pointer;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.item-name {
  display: block;
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: var(--space-1);
  word-break: break-word;
}

.bar-track {
  height: 6px;
  background: var(--color-line);
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: var(--color-seal);
  border-radius: 3px;
  transition: width 0.4s ease;
}

.vote-count {
  font-family: var(--font-mono);
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--color-ink);
  min-width: 48px;
  text-align: right;
  flex-shrink: 0;
}
</style>
