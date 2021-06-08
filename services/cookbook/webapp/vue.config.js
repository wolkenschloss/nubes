// vue.config.js
module.exports = {
    devServer: {
        port: 8181,
        proxy: 'http://localhost:8180/'
    },
    chainWebpack: config => {
        config.module
            .rule('vue')
            .use('vue-loader')
            .tap(options => {
                options.compilerOptions.whitespace = 'preserve'
                return options;
            })
    }
};