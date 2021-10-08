import axios from 'axios'

const state = {
 itoc: [],
 ifilter: "",
 ipagination: {page: 1},
 itotal: 0,
 iloading: false
}

const getters = {
 itoc: (state) => state.itoc,
 ifilter: (state) => state.ifilter,
 iloading: (state) => state.iloading,
 ipagination: (state) => state.ipagination,
 itotal: (state) => state.itotal
}

const actions = {
    async queryIngredients({commit, state }) {
        console.log("Action query ingredients")
        const {page, itemsPerPage } = state.ipagination
        const from = (page - 1) * itemsPerPage;
        const to = from + itemsPerPage - 1;

        console.log(`loading ingredients from ${from} to ${to} search '${state.filter || ''}'`)
        const uri = `/ingredient?from=${from}&to=${to}&q=${state.filter || ''}`
        try {
            const response = await axios.get(uri)
            commit("setItoc", {content: response.data.content, total: response.data.total})
        } catch (error) {
            console.log(error)
        }
    }
}

const mutations = {
    setIpagination(state, payload){
        console.log(`mutation setIpagination(${payload}`)
        state.ipagination = payload
    },
    setItoc(state, {content, total}) {
        console.log(`mutation set itoc(content: ${content.length}, total: ${total}`)
        state.itoc = content
        state.itotal = total
    }
}

export default { state, getters, actions, mutations }