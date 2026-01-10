import type { App } from 'vue';
import { createApp } from 'vue';

export function withSetup<T>(composable: () => T): { instance: T; app: App } {
    let instance: T | undefined;
    const app = createApp({
        setup() {
            instance = composable();
            return (): void => {};
        },
    });
    app.mount(document.createElement('div'));
    if (instance) {
        return { instance, app };
    }
    throw new Error('Failed to init composable with setup');
}
