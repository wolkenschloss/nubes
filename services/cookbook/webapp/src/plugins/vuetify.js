import Vue from 'vue';
import Vuetify from 'vuetify/lib/framework';
Vue.use(Vuetify);

export default new Vuetify({
    theme: {
        dark: true,
        options: {
            variations: false
        },
        themes: {
            dark: {
                primary: '#839496',
                secondary: '#586e75',
                accent: '#82B1FF',
                error: '#dc322f',
                info: '#268bd2',
                success: '#859900',
                warning: '#b58900',

                yellow : "#b58900",
                orange : "#cb4b16",
                red : "#dc322f",
                magenta : "#d33682",
                violet : "#6c71c4",
                blue : "#268bd2",
                cyan : "#2aa198",
                green : "#859900",
            }
        }
    }
});
