import Vue from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import store from './store'

Vue.config.productionTip = true
Vue.use(require('vue-shortkey'))
Vue.directive('shortcut', {
  bind(el, binding) {
    el.accessKey = binding.value
    el.title = el.accessKeyLabel
    console.log(`access key: ${el.accessKeyLabel}`)
  }
})
new Vue({
  router,
  vuetify,
  store,
  render: function (h) { return h(App) }
}).$mount('#app')
