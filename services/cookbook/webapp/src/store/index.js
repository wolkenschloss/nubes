import Vue from 'vue'
import Vuex from 'vuex'
import recipe from './modules/recipe'
import toc from './modules/toc'
import ingredients from './modules/ingredients'
import {createLogger} from 'vuex'
Vue.use(Vuex)

export default new Vuex.Store({
  plugins: [createLogger()],
  state: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
    toc,
    recipe,
    ingredients
  }
})
