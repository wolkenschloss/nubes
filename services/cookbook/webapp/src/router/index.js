import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    components: {
      default: () => import(/* webpackChunkName: "contents" */ '../views/Contents')
    },
    props: {
      default: route => ({first: route.query.first || 0, last: route.query.last || 4})
    },
    name: "contents"
  },
  {
    name: "details",
    path: "/recipe/:id",
    props: {default: true},
    components: {
      default: () =>
          import(/* webpackChunkName: "details" */ '../views/Details.vue')
    }
  },
  {
    name: 'ingredients',
    path: '/ingredients',
    components: {
      default: () => import(/* webpackChunkName: "ingredients" */ '../views/Ingredients')
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
