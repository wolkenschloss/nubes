// vue.config.js
module.exports = {
    devServer: {
        port: 8181,
        proxy: 'http://localhost:8180/'
    }
};