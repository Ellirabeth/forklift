import { createRouter, createWebHistory } from 'vue-router'
import ForkliftDirectory from '../views/ForkliftDirectory.vue'

const routes = [
  {
    path: '/',
    name: 'ForkliftDirectory',
    component: ForkliftDirectory
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
