import path from 'path';
import { defineConfig, mergeConfig } from 'vitest/config';
import viteConfig from './vite.config';

export default mergeConfig(
    viteConfig({ mode: 'test', command: 'serve' }),
    defineConfig({
        resolve: {
            alias: {
                // use ~ as placeholder for test dir
                '~': path.resolve(__dirname, 'test'),
            },
        },
        test: {
            pool: 'vmThreads',
            globals: true,
            environment: './test/environment.ts',
            setupFiles: ['./test/vitest.setup.ts'],
            coverage: {
                provider: 'v8',
                reporter: ['lcov'],
                include: ['src/**/*.{ts,vue}'],
            },
        },
    })
);
