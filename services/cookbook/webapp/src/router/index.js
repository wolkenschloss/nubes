import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    components: {
      default: () => import(/* webpackChunkName: "home" */ '../views/Home')
    }
  },
  {
    path: "/recipe/:id",
    name: "details",
    props: {default: true },
    components: {default: () =>
       import(/* webpackChunkName: "details" */ '../views/Details.vue')
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
