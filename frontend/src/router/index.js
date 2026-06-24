import { createRouter, createWebHistory } from 'vue-router'
import VoteView from '../views/VoteView.vue'
import ManageView from '../views/ManageView.vue'

const routes = [
  { path: '/', name: 'vote', component: VoteView },
  { path: '/manage', name: 'manage', component: ManageView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
