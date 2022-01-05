import axios from 'axios'
import Vue from "vue";

const state = () => ({
    entries: [],
})

const getters = {
    entries: (state) => state.entries,
}

const actions = {
    async load({ commit, state }) {

        try {
            const result = await axios.get('/units')
            const entries = result.data.flatMap(u => u.values.map(v => {
                return {value: u.name, text: v}
            }))

            commit('setUnits', entries)
        } catch (error) {
            console.log(error)
        }
    },
}

const mutations = {
    setUnits(state, entries) {
        Vue.set(state, 'entries', entries)
    }
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations,
}
