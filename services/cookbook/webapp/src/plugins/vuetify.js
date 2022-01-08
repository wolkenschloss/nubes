import Vue from 'vue';
import Vuetify from 'vuetify/lib';
import colors from "vuetify/lib/util/colors";
Vue.use(Vuetify);

export default new Vuetify({
    theme: {
        themes: {
            dark: {
                primary: colors.deepPurple.lighten1,
                secondary: colors.teal.base
            },
            light: {
                primary: colors.deepPurple.base,
                secondary: colors.teal.base
            }
        }
    }
});
