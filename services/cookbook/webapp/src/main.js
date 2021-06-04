import '@babel/polyfill'
import 'mutationobserver-shim'
import Vue from 'vue'
import './plugins/bootstrap-vue'
import App from './App.vue'
import {IconsPlugin} from "bootstrap-vue";

Vue.config.productionTip = false
Vue.use(IconsPlugin)

new Vue({
  render: h => h(App),
}).$mount('#app')
