import axios from 'axios'

const state = {
    toc: [],
    filter: "",
    pagination: {page: 1},
    total: 0,
    loading: false
}

const getters = {
    toc: (state) => state.toc,
    filter: (state) => state.filter,
    loading: (state) => state.loading,
    pagination: (state) => state.pagination,
    total: (state) => state.total
}

const actions = {
    async queryRecipes({ commit, state }) {
        console.log("Action queryRecipes")
        const {page, itemsPerPage} = state.pagination
        const from = (page - 1) * itemsPerPage;
        const to = from + itemsPerPage - 1;

        console.log(`loading recipes from ${from} to ${to} search '${state.filter || ''}'`)
        const uri = `/recipe?from=${from}&to=${to}&q=${state.filter || ''}`

        try {
            const response = await axios.get(uri)
            commit('setToc', {content: response.data.content, total: response.data.total})
        } catch (error) {
            console.log(error)
        }
    },
    async updateQuery({commit, filter}) {
        console.log(`Action updateQuery(${filter})`)
        commit('setFilter', filter)
    }
}

const mutations = {
    setFilter(state, filter) {
        console.log(`Mutation setFilter(${filter})`)
        state.filter = filter
        state.pagination.page = 1
    },
    setPagination(state, payload){
        console.log(`Mutation setPagination(${payload}`)
        state.pagination = payload
    },
    setToc(state, {content, total }) {
        console.log(`Mutation setToc({content: ${content.length}, total: ${total}}`)
        state.toc = content
        state.total = total
    }
}

export default {
    state,
    getters,
    actions,
    mutations,
}
