import {createLocalVue, mount} from "@vue/test-utils";
import EditIngredient from "@/components/EditIngredient.vue";
import Vuetify from "vuetify";
import {VMessages, VSelect, VCombobox} from "vuetify/lib/components";
import Vuex from 'vuex'

describe('EditIngredient.vue', () => {
    const localVue = createLocalVue()
    localVue.use(Vuex)
    let vuetify
    let store
    let actions

    beforeEach(async () => {
        vuetify = new Vuetify()
        actions = {}
        store = new Vuex.Store({
            modules: {
                ingredients: {
                    namespaced: true,
                    mutations: {
                        setPagination: jest.fn(),
                        setFilter: jest.fn()
                    },
                    getters: {
                        toc: () => {},
                        total: () => {}
                    },
                    actions: {
                        queryIngredientsdients: () => {}
                    }
                }}})
    })

    it("should validate name", async () => {
        const wrapper = mount(EditIngredient, {
            localVue,
            vuetify,
            store,
            propsData: {
                value: {name: 'hello'}
            }
        });

        await wrapper.setProps({value: {quantity: 1, unit: 'g', name: null}})
        await localVue.nextTick()
        const editName = wrapper.findComponent(VCombobox)
        editName.vm.setValue("")
        const message = editName.findComponent(VMessages)
        expect(message.exists).toBeTruthy()
        expect(message.vm.value.length).toEqual(1)
        expect(message.vm.value[0]).toEqual("Name is required")
    })

    it("should validates quantity", async () => {
        const wrapper = mount(EditIngredient, {
            localVue,
            vuetify,
            store,
            propsData: {
                value: {
                    quantity: 1,
                    unit: 'g',
                    name: 'Mondzucker'
                }
            }
        });

        const editQuantity = wrapper.findAllComponents({ref: 'editQuantity'})
        const messages = editQuantity.at(0).findComponent(VMessages)

        await wrapper.setProps({value: {quantity: null, unit: null}})
        await localVue.nextTick()
        expect(messages.vm.value.length).toEqual(0)

        const unitInput = wrapper.findComponent(VSelect)
        await unitInput.vm.selectItem('g')
        await wrapper.vm.$nextTick()

        expect(messages.exists).toBeTruthy()
        expect(messages.vm.value.length).toEqual(1)
        expect(messages.vm.value[0]).toEqual("Unit requires quantity")
    })
})