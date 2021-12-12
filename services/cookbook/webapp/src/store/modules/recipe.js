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
    servings: (state) => state.servings || state.recipe.servings,
    ingredients: (state) => state.ingredients
}

const actions = {
    async scaleIngredients({commit, state}, servings) {
        console.log(`Action scaleIngredients(${servings}`)
        let url = `/recipe/${state.recipe._id}?servings=${servings}`
        const response = await axios.get(url)
        commit('setServings', servings)
        commit('setIngredients', response.data.ingredients)
    },
    async loadRecipe({commit, state}, id) {
        console.log(`Action loadRecipe(${id})`)
        let url = "/recipe/" + id
        const response = await axios.get(url)
        commit('setRecipe', response.data)
        commit('setServings', response.data.servings)
        commit('setIngredients', response.data.ingredients)
    },
    async deleteRecipe({commit, state}, id) {
        console.log(`Action deleteRecipe(${id})`)
        let url = "/recipe/" + id
        await axios.delete(url)
        router.push("/")
    },
    async saveRecipe({commit, state}, recipe) {
        console.log(`action recipe save: ${JSON.stringify(recipe)}`)
        let url = "/recipe/" + recipe._id;
        await axios.put(url, recipe)
        commit('setRecipe', recipe)
        commit('setServings', recipe.servings)
        commit('setIngredients', recipe.ingredients)
        commit('setCopy', null)
    },
    async createRecipe({commit, state}, recipe) {
        console.log(`action recipe create: ${JSON.stringify(recipe)}`)
        let url = "/recipe"
        const result = await axios.post(url, recipe)
        commit('setRecipe', result)
    },
    async cancelEdit({commit, state}) {
        console.log((`Action cancelEdit()`))
        commit('setCopy', null)
    },
    newRecipe({commit}) {
        console.log("Action newRecipe()")
        commit('setRecipe', {ingredients: [], servings: 1})
    },
    async edit({commit, state}, recipe) {
        console.log(`action recipe edit: ${JSON.stringify(recipe)}`)
        commit('setCopy', recipe)
    }
}

const mutations = {
    setRecipe(state, recipe) {
        console.log(`Mutation setRecipe(${JSON.stringify(recipe)})`)
        Vue.set(state, 'recipe', cloneDeep(recipe))
    },
    setCopy(state, copy) {
        console.log(`Mutation setCopy(${JSON.stringify(copy)})`)
        Vue.set(state, 'copy', cloneDeep(copy))
    },
    setServings(state, servings) {
        console.log(`Mutation setServings(${servings})`)
        state.servings = servings
    },
    setIngredients(state, ingredients) {
        console.log(`mutation recipe setIngredients(${JSON.stringify(ingredients)}`)
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