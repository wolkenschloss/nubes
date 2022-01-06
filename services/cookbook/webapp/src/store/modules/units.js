import axios from 'axios'
import Vue from "vue";

const state = () => ({
    groups: []
})

const getters = {
    // groups -> items for v-select
    entries: (state) => {
        return state.groups.flatMap(g => [
            {header: g.name},
            {divider: true},
            ...g.units.flatMap(u => unitEntries(u))
        ])
    }
}

function unitEntries(unit) {
    return unit.values.map(u => {return {text: u, value: u}})
}

const actions = {
    async load({ commit }) {

        try {
            const result = await axios.get('/units/groups')
            console.log(JSON.stringify(result.data))
            commit('setGroups', result.data.groups)
        } catch (error) {
            console.log(error)
        }
    },
}

const mutations = {
    setGroups(state, groups) {
        Vue.set(state, 'groups', groups)
    }
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations,
}
