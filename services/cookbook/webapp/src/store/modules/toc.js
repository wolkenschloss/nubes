import axios from 'axios'

const state = () => ({
    toc: [],
    filter: "",
    pagination: {page: 1},
    total: 0,
    loading: false
})

const getters = {
    toc: (state) => state.toc,
    filter: (state) => state.filter,
    loading: (state) => state.loading,
    pagination: (state) => state.pagination,
    total: (state) => state.total
}

const actions = {
    async queryRecipes({ commit, state }) {
        const {page, itemsPerPage} = state.pagination
        const from = (page - 1) * itemsPerPage;
        const to = from + itemsPerPage - 1;
        const uri = `/recipe?from=${from}&to=${to}&q=${state.filter || ''}`

        try {
            const response = await axios.get(uri)
            commit('setToc', {content: response.data.content, total: response.data.total})
        } catch (error) {
            console.log(error)
        }
    },
    async updateQuery({commit, filter}) {
        commit('setFilter', filter)
    }
}

const mutations = {
    setFilter(state, filter) {
        state.filter = filter
        state.pagination.page = 1
    },
    setPagination(state, payload){
        state.pagination = payload
    },
    setToc(state, {content, total }) {
        state.toc = content
        state.total = total
    }
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations,
}
