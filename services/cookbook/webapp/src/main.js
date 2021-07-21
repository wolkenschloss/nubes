import Vue from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'

Vue.config.productionTip = true

new Vue({
  router,
  vuetify,
  render: function (h) { return h(App) }
}).$mount('#app')
