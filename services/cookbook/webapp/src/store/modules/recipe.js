import axios from 'axios'
import Vue from 'vue'
import router from '../../router'
import {cloneDeep} from "lodash";

const state = {
    recipe: {},
    copy: {}
}

const getters = {
    recipe: (state) => state.recipe,
    copy: (state) => state.copy
}

const actions = {
    async loadRecipe({commit, state}, id) {
        console.log(`Action loadRecipe(${id})`)
        let url = "/recipe/" + id
        const response = await axios.get(url)
        commit('setRecipe', response.data)
        commit('setCopy', response.data)
    },
    async deleteRecipe({commit, state}, id) {
        console.log(`Action deleteRecipe(${id})`)
        let url = "/recipe/" + id
        await axios.delete(url)
        router.push("/")
    },
    async saveRecipe({commit, state}) {
        console.log('Action saveRecipe()')
        let url = "/recipe/" + state.recipe.recipeId;
        await axios.put(url, state.copy)
        commit('setRecipe', state.copy)
    },
    async createRecipe({commit, state}) {
        console.log('Action createRecipe()')
        let url = "/recipe"
        const result = await axios.post(url, state.copy)
        commit('setRecipe', result)
    },
    async cancelEdit({commit, state}) {
        console.log((`Action cancelEdit()`))
        commit('setCopy', state.recipe)
    },
    newRecipe({commit, state}) {
        console.log("Action newRecipe()")
        commit('setRecipe', {ingredients: []})
        commit('setCopy', {ingredients: []})
    }
}

const mutations = {
    setRecipe(state, recipe) {
        console.log(`Mutation setRecipe(${JSON.stringify(recipe)})`)
        // state.title = recipe.title
        // state.preparation = recipe.preparation
        // state.ingredients = recipe.ingredients
        Vue.set(state, 'recipe', cloneDeep(recipe))
    },
    setCopy(state, copy) {
        console.log(`Mutation setCopy(${JSON.stringify(copy)})`)
        Vue.set(state, 'copy', cloneDeep(copy))
    }
}

export default {
    state,
    getters,
    actions,
    mutations
}