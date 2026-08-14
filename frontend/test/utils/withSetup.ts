import type { App, Plugin } from 'vue';
import { createApp } from 'vue';
import { config } from '@vue/test-utils';

export function withSetup<T>(composable: () => T): { instance: T; app: App } {
    let instance: T | undefined;
    const app = createApp({
        setup() {
            instance = composable();
            return (): void => {};
        },
    });

    config.global.plugins.forEach((plugin) => {
        app.use(plugin as Plugin);
    });

    app.mount(document.createElement('div'));
    if (instance) {
        return { instance, app };
    }
    throw new Error('Failed to init composable with setup');
}
