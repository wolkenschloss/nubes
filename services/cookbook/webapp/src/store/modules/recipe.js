import axios from 'axios'
import Vue from 'vue'
import router from '../../router'
import {cloneDeep} from "lodash";

const state = () => ({
    recipe: {},
    copy: null,
    servings: null,
    ingredients: null
})

const getters = {
    recipe: (state) => state.recipe,
    copy: (state) => state.copy,
    servings: (state) => state.servings || (state.recipe && state.recipe.servings) || null,
    ingredients: (state) => state.ingredients
}

const actions = {
    async scaleIngredients({commit, state}, servings) {
        let url = `/recipe/${state.recipe._id}?servings=${servings}`
        const response = await axios.get(url)
        commit('setServings', servings)
        commit('setIngredients', response.data.ingredients)
    },
    async loadRecipe({commit, state}, id) {
        let url = "/recipe/" + id
        const response = await axios.get(url)
        commit('setRecipe', response.data)
        commit('setServings', response.data.servings)
        commit('setIngredients', response.data.ingredients)
    },
    async deleteRecipe({commit, state}, id) {
        let url = "/recipe/" + id
        await axios.delete(url)
        router.push("/")
    },
    async saveRecipe({commit, state}, recipe) {
        let url = "/recipe/" + recipe._id;
        await axios.put(url, recipe)
        commit('setRecipe', recipe)
        commit('setServings', recipe.servings)
        commit('setIngredients', recipe.ingredients)
        commit('setCopy', null)
    },
    async createRecipe({commit, state}, recipe) {
        let url = "/recipe"
        const result = await axios.post(url, recipe)
        commit('setRecipe', result.data)
    },
    async cancelEdit({commit, state}) {
        commit('setCopy', null)
    },
    newRecipe({commit}) {
        // commit('setRecipe', {ingredients: [], servings: 1})
        commit('setRecipe', null)
        commit('setCopy', null)
    },
    async edit({commit, state}, recipe) {
        commit('setCopy', recipe)
        // commit('setRecipe', recipe)
    },
    async create({commit, state}, recipe) {
        let url = "/recipe"
        await axios.post(url, recipe)
        commit('setCopy', null) // Close Dialog
        this.dispatch("toc/queryRecipes", {}, {root: true})
    }
}

const mutations = {
    setRecipe(state, recipe) {
        Vue.set(state, 'recipe', cloneDeep(recipe))
    },
    setCopy(state, copy) {
        Vue.set(state, 'copy', cloneDeep(copy))
    },
    setServings(state, servings) {
        state.servings = servings
    },
    setIngredients(state, ingredients) {
        state.ingredients = ingredients
    }
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}