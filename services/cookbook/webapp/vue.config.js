const options = {
    key: "localhost-key.pem",
    cert: "localhost.pem"
}
const server = process.env.HTTPS === 'true' ? {type: 'https', options} : {type: 'http'}
const webSocketURL = process.env.HTTPS === 'true' ? {protocol: 'wss'} : {protocol: 'ws'}

module.exports = {
    publicPath: process.env.NODE_ENV === 'production' ? '/cookbook/' : '',
    transpileDependencies: ['vuetify'],
    devServer: {
        historyApiFallback: false,
        port: 8181,
        server,
        client: {
            logging: 'verbose',
            overlay: true,
            progress: true,
            webSocketURL,
        },
        proxy: {
            '^/': {
                ws: false,
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
    },
    css: {
        extract: {ignoreOrder: true}
    },
    configureWebpack: {
        performance: {
            maxEntrypointSize: 700000,
            // maxEntrypointSize: 819200,
            maxAssetSize: 512000
        },
        optimization: {
            splitChunks: {
                minSize: 10000,
                maxSize: 250000
            }
        }
    },

    pwa: {
        name: 'Cookbook',
        themeColor: '#009688',
        msTileColor: '#000000',
        appleMobileWebAppCapable: 'yes',
        appleMobileWebAppStatusBarStyle: 'black',
        manifestOptions: {
            id: "/cookbook/",
            start_url: "/cookbook/",
            icons: [
                {
                    src: "/cookbook/images/icons/manifest-icon-512.png",
                    sizes: "512x512",
                    type: "image/png",
                },
                {
                    src: "/cookbook/images/icons/manifest-icon-192.png",
                    sizes: "192x192",
                    type: "image/png",
                },
                {
                    src: "/cookbook/images/icons/manifest-icon-512.maskable.png",
                    sizes: "512x512",
                    type: "image/png",
                    purpose: "maskable"
                },
                {
                    src: "/cookbook/images/icons/manifest-icon-192.maskable.png",
                    sizes: "192x192",
                    type: "image/png",
                    purpose: "maskable"
                },
            ],
            share_target: {
                action: "/cookbook/share-target",
                method: "GET",
                params: {
                    title: "title",
                    text: "text",
                    url: "url"
                }
            }
        },
        iconPaths: {
            faviconSVG: 'favicon.svg',
            favicon32: null,
            favicon16:  null,
            appleTouchIcon:  null,
            maskIcon:  null,
            msTileImage:  null,
        },
        // configure the workbox plugin
        // workboxPluginMode: 'InjectManifest',
        workboxPluginMode: 'GenerateSW'
    }
}
