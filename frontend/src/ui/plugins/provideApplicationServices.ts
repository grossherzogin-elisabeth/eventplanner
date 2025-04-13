import type { App, Plugin } from 'vue';
import type { ApplicationServices } from '@/initApplicationServices';
import { ERROR_HANDLING_SERVICE, NOTIFICATION_SERVICE } from '@/ui/composables/Application';

export function provideApplicationServices(applicationServices: ApplicationServices): Plugin {
    return {
        install(app: App): void {
            app.provide(NOTIFICATION_SERVICE, applicationServices.notificationService);
            app.provide(ERROR_HANDLING_SERVICE, applicationServices.errorHandlingService);
        },
    };
}
