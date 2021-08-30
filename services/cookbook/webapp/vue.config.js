// noinspection JSUnusedGlobalSymbols

module.exports = {
  transpileDependencies: [
    'vuetify'
  ],
  devServer: {
    port: 8181,
    proxy: {
      "^/": {
        target: 'http://localhost:8180/',
        changeOrigin: true,
        onProxyRes: (proxyRes) => {
          if (proxyRes.headers.location) {
            let loc = proxyRes.headers.location;
            loc = loc.replace('http://localhost:8180', '');
            proxyRes.headers.location = loc;
          }
        }
      }
    },
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, OPTIONS",
      "Access-Control-Allow-Headers": "X-Requested-With, content-type, Authorization"
    }
  }
}
