import path from 'path';
import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from '../vite.config';

export default mergeConfig(
    viteConfig({ mode: 'test', command: 'serve' }),
    defineConfig({
        resolve: {
            alias: {
                // use ~ as placeholder for test dir
                '~': path.resolve(__dirname, '.'),
            },
        },
        test: {
            pool: 'vmThreads',
            globals: true,
            environment: 'happy-dom',
            setupFiles: ['vitest.setup.ts'],
            coverage: {
                reporter: ['text', 'lcov'],
                exclude: ['postcss.config.cjs', 'babel.config.js', 'eslint.config.mjs', 'vite.config.ts', 'dist', 'test', '*.yml'],
            },
        },
    })
);
