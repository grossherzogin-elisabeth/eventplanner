import type { App, Plugin } from 'vue';
import type { UseCases } from '@/initUseCases';
import {
    APP_SETTINGS_USE_CASE,
    AUTH_USE_CASE,
    EVENT_ADMIN_USE_CASE,
    EVENT_USE_CASE,
    POSITION_ADMIN_USE_CASE,
    POSITION_USE_CASE,
    QUALIFICATION_ADMIN_USE_CASE,
    QUALIFICATION_USE_CASE,
    USER_ADMIN_USE_CASE,
    USER_USE_CASE,
} from '@/ui/composables/Application';

export function provideUseCases(useCases: UseCases): Plugin {
    return {
        install(app: App): void {
            app.provide(AUTH_USE_CASE, useCases.authUseCase);
            app.provide(EVENT_USE_CASE, useCases.eventUseCase);
            app.provide(EVENT_ADMIN_USE_CASE, useCases.eventAdministrationUseCase);
            app.provide(USER_USE_CASE, useCases.usersUseCase);
            app.provide(USER_ADMIN_USE_CASE, useCases.userAdministrationUseCase);
            app.provide(APP_SETTINGS_USE_CASE, useCases.appSettingsUseCase);
            app.provide(QUALIFICATION_USE_CASE, useCases.qualificationUseCase);
            app.provide(QUALIFICATION_ADMIN_USE_CASE, useCases.qualificationAdministrationUseCase);
            app.provide(POSITION_USE_CASE, useCases.positionUseCase);
            app.provide(POSITION_ADMIN_USE_CASE, useCases.positionAdministrationUseCase);
        },
    };
}
