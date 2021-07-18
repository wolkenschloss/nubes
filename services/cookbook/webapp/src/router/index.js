import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/about',
    name: 'About',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: function () {
      return import(/* webpackChunkName: "about" */ '../views/About.vue')
    }
  },
  {
    path: '/create',
    name: 'Create',
    component: function () {
      return import(/* webpackChunkName: "create" */ '../views/Create.vue')
    }
  },
  {
    path: "/recipe/:id",
    name: "details",
    props: {default: true },
    components: {default: () =>
       import(/* webpackChunkName: "recipe" */ '../views/Recipe.vue')
    }
  },
  {
    path: "/edit/:id",
    name: "edit",
    props: {default: true},
    components: { default: () => import(/*webpackChungName: 'edit' */ "../views/Edit.vue")}
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
