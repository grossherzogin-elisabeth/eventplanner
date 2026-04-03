import ViteYaml from '@modyfi/vite-plugin-yaml';
import vue from '@vitejs/plugin-vue';
import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import { VitePWA } from 'vite-plugin-pwa';
import svgLoader from 'vite-svg-loader';

const pwa = VitePWA({
    includeAssets: ['favicon.svg'],
    manifest: {
        name: 'Lissi Reiseplaner',
        short_name: 'Lissi',
        description: 'Lissi Reiseplaner',
        theme_color: '#082f49',
        background_color: '#082f49',
        display: 'standalone',
        start_url: '.',
        icons: [
            {
                src: '/favicon.svg',
                sizes: 'any',
            },
        ],
    },
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

// Be really careful adjusting manual chunking, as this can break a production build while still working fine in the
// dev preview. Always build a local docker image and run the container locally when doing changes here.
const chunks = {
    'src/': 'app', // put all app code in a single chunk, so the full app is loaded initially
    '@fortawesome/fontawesome-free/js/brands': 'fontawesome-brands',
    '@fortawesome/fontawesome-free/js/regular': 'fontawesome-regular',
    '@fortawesome/fontawesome-free/js/solid': 'fontawesome-solid',
    'node_modules': 'vendor',
};

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    process.env = { ...process.env, ...loadEnv(mode, process.cwd()) };
    const host = process.env.VITE_HOST || 'localhost';

    return {
        plugins: [vue(), svgLoader(), pwa, ViteYaml()],
        build: {
            rollupOptions: {
                output: {
                    manualChunks: (id): string | null => {
                        for (const [key, value] of Object.entries(chunks)) {
                            if (id.includes(key)) {
                                return value;
                            }
                        }
                        return null;
                    },
                },
            },
        },
        assetsInclude: ['**/*.csv'],
        server: {
            port: 8090,
            host: true,
            proxy: {
                '/api/': `http://${host}:8091`,
                '/auth/': `http://${host}:8091`,
                '/login/oauth2/code/': `http://${host}:8091`,
            },
            allowedHosts: [host],
        },
        resolve: {
            alias: {
                // removes esm warning: https://github.com/intlify/vue-i18n-next/issues/789
                'vue-i18n': 'vue-i18n/dist/vue-i18n.cjs.js',
                // use @ as placeholder for src dir
                '@': path.resolve(__dirname, './src'),
            },
        },
    };
});
