import { createRouter, createWebHistory } from 'vue-router'
import PaymentPage from '../view/PaymentPage.vue'
import PaymentResult from '../view/PaymentResult.vue'
import ItemPage from '../view/ItemPage.vue'

const routes = [
  { path: '/payment', component: PaymentPage },
  { path: '/payment/result', component: PaymentResult },
  { path: '/', component: ItemPage },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router;