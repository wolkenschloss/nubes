module.exports = {
  transpileDependencies: [
    'vuetify'
  ],
  devServer: {
    port: 8181,
    proxy: 'http://localhost:8180/'
  },
}
