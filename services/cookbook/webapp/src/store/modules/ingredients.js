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
    async queryIngredients({commit, state }) {
        console.log("Action query ingredients")
        const {page, itemsPerPage } = state.pagination
        const from = (page - 1) * itemsPerPage;
        const to = from + itemsPerPage - 1;

        console.log(`loading ingredients from ${from} to ${to} search '${state.filter || ''}'`)
        const uri = `/ingredient?from=${from}&to=${to}&q=${state.filter || ''}`
        try {
            const response = await axios.get(uri)
            commit("setToc", {content: response.data.content, total: response.data.count})
        } catch (error) {
            console.log(error)
        }
    }
}

const mutations = {
    setFilter(state, filter) {
        console.log(`mutation ingredients set filter: ${filter}`)
        state.filter = filter
        state.pagination.page = 1
    },
    setPagination(state, payload){
        console.log(`mutation setPagination(${payload}`)
        state.pagination = payload
    },
    setToc(state, {content, total}) {
        console.log(`mutation set ingredients toc(content: ${content.length}, total: ${total}`)
        state.toc = content
        state.total = total
    }
}

export default { namespaced: true, state, getters, actions, mutations }