<script setup>
import { RouterLink, RouterView, useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div class="brand">
        <!-- 簽名元素：手繪風印章圖騰，呼應「正式投票/蓋章認證」意象 -->
        <svg class="seal" viewBox="0 0 48 48" aria-hidden="true">
          <circle cx="24" cy="24" r="21" fill="none" stroke="currentColor" stroke-width="2.5" />
          <path d="M14 25 L21 32 L34 17" fill="none" stroke="currentColor" stroke-width="3.5"
                stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        <span class="brand-text">公民投票所</span>
      </div>
      <nav class="nav">
        <RouterLink to="/" class="nav-link" :class="{ active: route.path === '/' }">投票</RouterLink>
        <RouterLink to="/manage" class="nav-link" :class="{ active: route.path === '/manage' }">後台管理</RouterLink>
      </nav>
    </header>

    <main class="content">
      <RouterView />
    </main>

    <footer class="footer">
      <span>線上投票系統 · 範例專案</span>
    </footer>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-5);
  border-bottom: 2px solid var(--color-ink);
  background: var(--color-paper);
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-ink);
}

.seal {
  width: 32px;
  height: 32px;
  color: var(--color-seal);
  flex-shrink: 0;
}

.brand-text {
  font-family: var(--font-display);
  font-size: 1.3rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.nav {
  display: flex;
  gap: var(--space-4);
}

.nav-link {
  font-family: var(--font-body);
  font-size: 0.95rem;
  font-weight: 600;
  text-decoration: none;
  color: var(--color-ink-soft);
  padding: var(--space-1) 0;
  border-bottom: 2px solid transparent;
  transition: color 0.15s ease, border-color 0.15s ease;
}

.nav-link:hover {
  color: var(--color-ink);
}

.nav-link.active {
  color: var(--color-seal);
  border-bottom-color: var(--color-seal);
}

.content {
  flex: 1;
  width: 100%;
  max-width: 880px;
  margin: 0 auto;
  padding: var(--space-5) var(--space-4) var(--space-6);
}

.footer {
  text-align: center;
  padding: var(--space-4);
  color: var(--color-ink-soft);
  font-size: 0.8rem;
  border-top: 1px solid var(--color-line);
}

@media (max-width: 600px) {
  .topbar {
    padding: var(--space-3);
  }
  .brand-text {
    font-size: 1.05rem;
  }
  .content {
    padding: var(--space-4) var(--space-3) var(--space-5);
  }
}
</style>
