import type { App, Plugin } from 'vue';

export function setupTooltips(): Plugin {
    return {
        install(app: App): void {
            app.directive('tooltip', {
                mounted: (el) => console.log(el),
            });
        },
    };
}
