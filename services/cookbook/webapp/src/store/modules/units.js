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
            const result = await axios.get('/units/groups')
            console.log(JSON.stringify(result.data))
            const entries = result.data.groups.flatMap(g => [
                {header: g.name},
                ...g.units.map(u => {return {value: u.name, text: u.name} })
                ])

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
