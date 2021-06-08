import '@babel/polyfill'
import 'mutationobserver-shim'
import Vue from 'vue'
import './plugins/axios'
import './plugins/bootstrap-vue'
import App from './App.vue'
import {IconsPlugin} from "bootstrap-vue";
import router from './router'

import '@/assets/css/main.css'

Vue.config.productionTip = false
Vue.use(IconsPlugin)

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
