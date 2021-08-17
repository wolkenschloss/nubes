import {createLocalVue, mount} from "@vue/test-utils"
import Vuetify from "vuetify"
import Preparation from "@/components/Preparation.vue"

describe('preparation component', () => {

    const localVue = createLocalVue();
    let vuetify;

    beforeEach(() => {
        vuetify = new Vuetify()
    })

    it("should render no paragraphs for empty preparation", async () => {
        const wrapper = mount(Preparation, {
            localVue,
            vuetify,
            propsData: {
                    text: ""
            }
        })

        const paragraphs = wrapper.findAll('p')
        expect(paragraphs.length).toBe(0)
    })

    it("should render one paragraph for text without double newline", async () => {
        const wrapper = mount(Preparation, {
            localVue,
            vuetify,
            propsData: {
                text: "A second paragraph"
            }
        })

        const paragraphs = wrapper.findAll('p')
        expect(paragraphs.length).toBe(1)
        expect(paragraphs.at(0).text()).toEqual("A second paragraph")
    })

    it("should render one paragraph for each double newline block", async () => {
        const wrapper = mount(Preparation, {
            localVue,
            vuetify,
            propsData: {
                text: "first paragraph\n\nsecond paragraph\n\nthird paragraph"
            }
        })

        const paragraphs = wrapper.findAll('p')
        expect(paragraphs.length).toBe(3)
        expect(paragraphs.at(0).text()).toEqual("first paragraph")
        expect(paragraphs.at(1).text()).toEqual("second paragraph")
        expect(paragraphs.at(2).text()).toEqual("third paragraph")
    })
})