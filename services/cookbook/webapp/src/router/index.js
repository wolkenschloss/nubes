import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'kochbuch',
    components: {
      default: Home,
    }
  },
  {
    path: '/wochenplan',
    name: 'wochenplan',
    components: {
      default: Home,
    }
  },
  {
    path: '/about',
    name: 'About',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    components: {
      default: () => import('@/views/About.vue'),
    }
  },
  {
    path: '/details/:id',
    name: 'details',
    components: {
       default: () => import('@/views/Details.vue'),
    },
    // Look at here
    // https://github.com/vuejs/vue-router/issues/1183#issuecomment-281618659
    props: {
      default: true,
    }
  },
  {
    path: '/edit/:id',
    name: 'edit',
    components: {
      default: () => import('@/views/Edit.vue'),
    },
    props: {
      default: true,
    }
  },
  {
    path: '/create',
    components: {
      default: () => import('@/views/Create.vue'),
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
