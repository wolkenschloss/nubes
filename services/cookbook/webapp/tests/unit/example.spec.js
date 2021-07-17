import { shallowMount } from '@vue/test-utils'
import About from '@/views/About.vue'

describe('HelloWorld.vue', () => {
  it('renders props.msg when passed', () => {
    const msg = 'new message'
    const wrapper = shallowMount(About, {
      propsData: { msg }
    })
    const header = wrapper.find('h1')
    expect(header.text()).toMatch("This is an about page")
  })
})
