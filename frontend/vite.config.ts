import vue from '@vitejs/plugin-vue';
import path from 'path';
import { defineConfig } from 'vite';
import { VitePWA } from 'vite-plugin-pwa';
import svgLoader from 'vite-svg-loader';

const pwa = VitePWA({
    registerType: 'autoUpdate',
    workbox: {
        navigateFallbackDenylist: [
            // api routes should always return the current server state and data caching should be done in app
            /^\/api.*/,
            // never cache auth routes in serviceworker, as these need the backend to work
            /^\/auth.*/,
            /^\/login\/oauth2\/code.*/,
        ],
    },
});

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue(), svgLoader(), pwa],
    assetsInclude: ['**/*.csv'],
    server: {
        port: 8090,
        host: true,
        proxy: {
            '/items': 'http://localhost:8083',
            '/api/': 'http://localhost:8091',
            '/auth/': 'http://localhost:8091',
            '/login/oauth2/code/': 'http://localhost:8091',
        },
    },
    resolve: {
        alias: {
            // removes esm warning: https://github.com/intlify/vue-i18n-next/issues/789
            'vue-i18n': 'vue-i18n/dist/vue-i18n.cjs.js',
            // use @ as placeholder for src dir
            '@': path.resolve(__dirname, './src'),
        },
    },
});
