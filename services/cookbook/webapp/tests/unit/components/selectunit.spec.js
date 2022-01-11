import {createLocalVue, mount} from "@vue/test-utils";
import Vuex from 'vuex'
import Vuetify from "vuetify";
import {VSelect} from "vuetify/lib/components";
import SelectUnit from "@/components/SelectUnit";

describe('SelectUnit.vue', () => {
    const localVue = createLocalVue()
    localVue.use(Vuex)

    let vuetify
    let store

    const items = [
        {text: 'item1', value: 'item1'},
        {text: 'item2', value: 'item2'}
    ];

    function createWrapper() {
        return mount(SelectUnit, {
            localVue,
            vuetify,
            store,
            propsData: {
                value: 'item1'
            }
        });
    }

    beforeEach(async () => {
        vuetify = new Vuetify()
        store = new Vuex.Store({
            modules: {
                units: {
                    namespaced: true,
                    getters: {
                        entries: () => items
                    }
                }
            }
        })
    })

    it('should render value', async () => {
        const wrapper = createWrapper();

        const input = wrapper.findComponent(VSelect)
        const value = input.vm.value
        expect(value).toBeTruthy()
        expect(value).toEqual('item1')
    })

    it('should render unit entries', async () => {
        const wrapper = createWrapper();

        const input = wrapper.findComponent(VSelect)
        expect(input.props('items')).toEqual(items)
    })

    it('should emit input event', async () => {
        const wrapper = createWrapper();

        const input = wrapper.findComponent(VSelect)
        input.vm.$emit('input', 'item2')

        await wrapper.vm.$nextTick()
        expect(wrapper.emitted().input[0]).toEqual(['item2'])
    })

    it('should emit change event', async () => {
        const wrapper = createWrapper();

        const input = wrapper.findComponent(VSelect)
        input.vm.$emit('change', 'item2')
        await wrapper.vm.$nextTick()
        expect(wrapper.emitted().change[0]).toEqual(['item2'])
    })

    it('should clear selection', async () => {
        const wrapper = createWrapper()

        wrapper.findComponent(VSelect).vm.$emit('click:clear')
        await wrapper.vm.$nextTick()

        const value = wrapper.findComponent(VSelect).vm.value
        expect(value).toEqual('')
    })

    it('should set item', async() => {
        const wrapper = createWrapper();

        wrapper.vm.selectItem('item2')

        expect(wrapper.emitted().change[0]).toEqual(['item2'])
        expect(wrapper.emitted().input[0]).toEqual(['item2'])
    })
})