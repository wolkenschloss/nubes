import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: {name: 'contents'}
  },
  {
    path: '/contents',
    name: 'contents',
    components: {
      default: () => import(/* webpackChunkName: "contents" */ '../views/Contents')
    },
    props: {
      default: route => ({first: route.query.first || 0, last: route.query.last || 4})
    }
  },
  {
    path: "/recipe/:id",
    name: "details",
    props: {default: true},
    components: {
      default: () =>
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
