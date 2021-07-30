import {createLocalVue, mount} from '@vue/test-utils'
import App from '@/App.vue'
import Vuetify from "vuetify";
import VueRouter from "vue-router";

describe('App.vue', () => {

  const localVue = createLocalVue()
  const router = new VueRouter()
  localVue.use(VueRouter)
  let vuetify

  beforeEach(() => {
    vuetify = new Vuetify()
  })

  it('renders app title', () => {
    const wrapper = mount(App, {
      localVue,
      vuetify,
      router
    })
    const appTitle = wrapper.find('[data-cy=app-title]')
    expect(appTitle.text()).toMatch("Cookbook")
  })

  describe('inner describe', () => {
    it('should be something', () => {})
  })
})
