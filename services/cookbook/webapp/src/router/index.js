import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import xabout from "@/components/navigation/xabout";
import xhome from "@/components/navigation/xhome";
import xsave from "@/components/navigation/xsave";

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'kochbuch',
    components: {
      default: Home,
      xnav: xhome
    }
  },
  {
    path: '/wochenplan',
    name: 'wochenplan',
    components: {
      default: Home,
      xnav: xhome
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
      xnav: xabout
    }
  },
  {
    path: '/details/:id',
    name: 'details',
    // component: () => import('@/views/Details.vue'),

    components: {
       default: () => import('@/views/Details.vue'),
       xnav: xabout
    },
    // Look at here
    // https://github.com/vuejs/vue-router/issues/1183#issuecomment-281618659
    props: {
      default: true,
      xnav: true
    }
  },
  {
    path: '/edit/:id',
    name: 'edit',
    components: {
      default: () => import('@/views/Edit.vue'),
      xnav: xsave
    },
    props: {
      default: true,
      xnav: true
    }
  },
  {
    path: '/create',
    components: {
      default: () => import('@/views/Create.vue'),
      xnav: xsave
    }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
