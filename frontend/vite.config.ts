import vue from '@vitejs/plugin-vue';
import path from 'path';
import { defineConfig } from 'vite';
import { VitePWA } from 'vite-plugin-pwa';
import svgLoader from 'vite-svg-loader';

const pwa = VitePWA({
    registerType: 'autoUpdate',
    workbox: {
       navigateFallbackDenylist: [
           /^\/auth.*/, // never cache auth routes in serviceworker, as these need the backend to work
       ],
   },
});

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue(), svgLoader(), pwa],
    assetsInclude: ['**/*.csv'],
    server: {
        port: 8080,
        host: true,
        proxy: {
            '/auth/': 'http://localhost:8081',
            '/api/': 'http://localhost:8081',
            '/login/oauth2/code/': 'http://localhost:8081',
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
