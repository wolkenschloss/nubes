import axios from 'axios'
import Vue from 'vue'
import router from '../../router'
import {cloneDeep} from "lodash";
import {Resource} from "@/store/modules/resource";

export const resource = new Resource("{+baseUrl}/recipe/{id}{?servings}")

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
    async scale({commit, state}, servings) {
        let url = resource.url({id: state.recipe._id, servings})
        const response = await axios.get(url)
        commit('setServings', servings)
        commit('setIngredients', response.data.ingredients)
    },
    async read({commit, state}, id) {
        let url = resource.url({id})
        const response = await axios.get(url)
        commit('setRecipe', response.data)
        commit('setServings', response.data.servings)
        commit('setIngredients', response.data.ingredients)
    },
    async destroy({commit, state}, id) {
        let url = resource.url({id})
        await axios.delete(url)
        await router.push("/")
    },
    async update({commit, state}, recipe) {
        let url = resource.url({id: recipe._id})
        await axios.put(url, recipe)
        commit('setRecipe', recipe)
        commit('setServings', recipe.servings)
        commit('setIngredients', recipe.ingredients)
        commit('setCopy', null)
    },
    async cancel({commit, state}) {
        commit('setCopy', null)
    },
    reset({commit}) {
        commit('setRecipe', null)
        commit('setCopy', null)
    },
    async edit({commit, state}, recipe) {
        commit('setCopy', recipe)
    },
    async create({commit, state}, recipe) {
        console.log(`create recipe ${recipe.title}`)
        let url = resource.url()

        if (recipe.upload != null) {
            console.log(`upload recipe with preview image ${recipe.upload.name} (${recipe.upload.size} Bytes)`)
            console.log(`content type: ${recipe.upload.type}`)
            const formData = new FormData();
            formData.append('preview', recipe.upload, recipe.upload.name)
            formData.append('recipe{}', JSON.stringify(recipe))

            if (recipe.upload == null) {
                console.warn("upload ist null")
            }

            await fetch(url, {method: 'POST', body: formData})

            // await axios.post(url, {'recipe{}': recipe, preview: recipe.upload}, {
            //     headers: {
            //         'Content-Type': 'multipart/form-data'
            //     }
            // })
        }
        else {
            console.log("upload recipe")
            await axios.post(url, recipe)
        }

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