import { defineConfig } from 'vitest/config';

export default defineConfig({
    test: {
        pool: 'vmThreads',
        globals: true,
        environment: './happy-dom-env.ts',
        setupFiles: ['test/vitest.setup.ts'],
        coverage: {
            reporter: ['text', 'lcov'],
            exclude: ['postcss.config.cjs', 'babel.config.js', 'eslint.config.mjs', 'vite.config.ts', 'dist', 'test', '*.yml'],
        },
    },
});
