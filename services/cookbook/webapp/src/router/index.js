import Vue from 'vue'
import VueRouter from 'vue-router'
import Share from "@/views/Share";

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    components: {
      default: () => import(/* webpackChunkName: "contents" */ '@/views/Contents')
    },
    props: {
      default: route => ({first: route.query.first || 0, last: route.query.last || 4})
    },
    name: "contents",
    meta: { title: "Cookbook" }
  },
  {
    name: 'share-target',
    path: '/share-target',
    components: {default: Share},
    // components: {default: ()  => import(/* webpackChunkName "share-target" */ '@/views/Share')},
    props: {
      default: route => ({title: route.query.title, text: route.query.text, url: route.query.url})
    },
    meta: {title: 'Share'}
  },
  {
    name: "details",
    path: "/recipe/:id",
    props: {default: true},
    components: {
      default: () =>
          import(/* webpackChunkName: "details" */ '@/views/Details.vue')
    },
    meta: {title: "Recipe" }
  },
  {
    name: 'ingredients',
    path: '/ingredients',
    components: {
      default: () => import(/* webpackChunkName: "ingredients" */ '@/views/Ingredients')
    },
    meta: {title: "Ingredients"}
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta["title"]
  next()
})

export default router
