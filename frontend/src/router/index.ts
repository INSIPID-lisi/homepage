import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/',
      component: DefaultLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/HomeView.vue')
        },
        {
          path: 'posts',
          name: 'posts',
          component: () => import('@/views/PostsView.vue')
        },
        {
          path: 'posts/:id',
          name: 'post-detail',
          component: () => import('@/views/PostDetailView.vue')
        },
        {
          path: 'anime',
          name: 'anime',
          component: () => import('@/views/AnimeListView.vue')
        },
        {
          path: 'anime/:id',
          name: 'anime-detail',
          component: () => import('@/views/AnimeDetailView.vue')
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/SettingsView.vue')
        }
      ]
    }
  ]
})

export default router
