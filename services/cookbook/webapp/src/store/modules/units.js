import axios from 'axios'
import Vue from "vue";

const state = () => ({
    entries: [],
})

const getters = {
    entries: (state) => state.entries,
}

function unitEntries(unit) {
    return unit.values.map(u => {return {text: u, value: u}})
}

const actions = {
    async load({ commit, state }) {

        try {
            const result = await axios.get('/units/groups')
            console.log(JSON.stringify(result.data))
            const entries = result.data.groups.flatMap(g => [
                {header: g.name},
                ...g.units.flatMap(u => unitEntries(u))
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
