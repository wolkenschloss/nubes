import Vue from 'vue'
import Vuex from 'vuex'
import recipe from './modules/recipe'
import toc from './modules/toc'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
    toc,
    recipe
  }
})
