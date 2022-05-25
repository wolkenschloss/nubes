const path = require("path")
const ca_dir = path.resolve(process.env.HOME, ".local/share/wolkenschloss/ca")

module.exports = {
    publicPath: '/cookbook/',
    productionSourceMap: false,
    transpileDependencies: ['vuetify'],
    devServer: {
        hot: process.env.NODE_ENV === 'development',
        historyApiFallback: false,
        port: 8181,
        server: {
            type: 'https', options: {
                key: process.env.WOLKENSCHLOSS_HTTPS_KEY || path.resolve(ca_dir, "localhost+1/key.pem"),
                cert: process.env.WOLKENSCHLOSS_HTTPS_CRT || path.resolve(ca_dir, "localhost+1/crt.pem")
            }
        },
        client: {
            logging: 'verbose',
            overlay: true,
            progress: true,
            webSocketURL: {protocol: 'wss'},
        },
        proxy: {
            '^/cookbook': {
                ws: false,
                logLevel: 'debug',
                target: 'http://localhost:8180',
                changeOrigin: true,
                onProxyRes: (proxyRes) => {
                    if (proxyRes.headers.location) {
                        let loc = proxyRes.headers.location;
                        loc = loc.replace('http://localhost:8180/cookbook', '/cookbook');
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
        // watch: process.env.NODE_ENV === 'development',
        watchOptions: {
            ignored: /node_modules/,
        },
        performance: {
            // maxEntrypointSize: 700000,
            maxEntrypointSize: 819200,
            maxAssetSize: 512000
        },
        optimization: {
            // minimize: false,

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
            favicon16: null,
            appleTouchIcon: null,
            maskIcon: null,
            msTileImage: null,
        },
        // configure the workbox plugin
        // workboxPluginMode: 'InjectManifest',
        workboxPluginMode: 'GenerateSW',
        workboxOptions: {
            navigateFallback: '/',
            mode: 'development',
            // runtimeCaching: [{
            //     handler: async ({event, params, request, url}) => {
            //         console.log("handler for route called!", params)
            //         return Response.redirect("https://google.de", 301)
            //     },
            //     method: 'GET',
            //     urlPattern: '/share-target'
            // }]
        }
    }
}
