import axios from 'axios'
import Vue from 'vue'
import router from '../../router'
import {cloneDeep} from "lodash";

const state = {
    recipe: {},
    copy: {},
    servings: null
}

const getters = {
    recipe: (state) => state.recipe,
    copy: (state) => state.copy,
    servings: (state) => state.servings || state.recipe.servings
}

const actions = {
    async loadRecipe({commit, state}, id) {
        console.log(`Action loadRecipe(${id})`)
        let url = "/recipe/" + id
        const response = await axios.get(url)
        commit('setRecipe', response.data)
        commit('setCopy', response.data)
        commit('setServings', response.data.servings)
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
        commit('setServings', state.copy.servings)
    },
    async createRecipe({commit, state}) {
        console.log('Action createRecipe()')
        let url = "/recipe"
        const result = await axios.post(url, state.copy)
        commit('setRecipe', result)
        commit('setServings', state.copy.servings)
    },
    async cancelEdit({commit, state}) {
        console.log((`Action cancelEdit()`))
        commit('setCopy', state.recipe)
    },
    newRecipe({commit, state}) {
        console.log("Action newRecipe()")
        commit('setRecipe', {ingredients: [], servings: 1})
        commit('setCopy', {ingredients: [], servings: 1})
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
    },
    setServings(state, servings) {
        console.log(`Mutation setServings(${servings})`)
        state.servings = servings
    }
}

export default {
    state,
    getters,
    actions,
    mutations
}