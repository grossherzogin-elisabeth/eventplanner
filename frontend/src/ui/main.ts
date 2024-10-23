import { createApp } from 'vue';
import type { Application } from '@/application';
import type { Domain } from '@/domain';
import {
    APP_SETTINGS_USE_CASE,
    AUTH_USE_CASE,
    CONFIG,
    ERROR_HANDLING_SERVICE,
    EVENT_ADMIN_USE_CASE,
    EVENT_USE_CASE,
    NOTIFICATION_SERVICE,
    POSITION_ADMIN_USE_CASE,
    POSITION_USE_CASE,
    QUALIFICATION_ADMIN_USE_CASE,
    QUALIFICATION_USE_CASE,
    USER_ADMIN_USE_CASE,
    USER_USE_CASE,
} from '@/ui/composables/Application';
import {
    EVENT_SERVICE,
    POSITION_SERVICE,
    QUALIFICATION_SERVICE,
    REGISTRATION_SERVICE,
    USER_SERVICE,
} from '@/ui/composables/Domain';
import App from './App.vue';
import './assets/css/main.css';
import './plugins/fontawesome';
import { setupI18n } from './plugins/i18n';
import { setupRouter } from './plugins/router';

export function setupVue(context: { domain: Domain; application: Application }) {
    const app = createApp(App);
    app.use(setupI18n(context.application.config));
    app.use(setupRouter(context.application.usecases.auth));

    app.provide(USER_SERVICE, context.domain.services.users);
    app.provide(REGISTRATION_SERVICE, context.domain.services.registrations);
    app.provide(EVENT_SERVICE, context.domain.services.events);
    app.provide(QUALIFICATION_SERVICE, context.domain.services.qualifications);
    app.provide(POSITION_SERVICE, context.domain.services.positions);

    app.provide(CONFIG, context.application.config);
    app.provide(AUTH_USE_CASE, context.application.usecases.auth);
    app.provide(EVENT_USE_CASE, context.application.usecases.events);
    app.provide(EVENT_ADMIN_USE_CASE, context.application.usecases.eventAdmin);
    app.provide(USER_USE_CASE, context.application.usecases.users);
    app.provide(USER_ADMIN_USE_CASE, context.application.usecases.userAdmin);
    app.provide(APP_SETTINGS_USE_CASE, context.application.usecases.appSettings);
    app.provide(QUALIFICATION_USE_CASE, context.application.usecases.qualifications);
    app.provide(QUALIFICATION_ADMIN_USE_CASE, context.application.usecases.qualificationAdmin);
    app.provide(POSITION_USE_CASE, context.application.usecases.positions);
    app.provide(POSITION_ADMIN_USE_CASE, context.application.usecases.positionAdmin);
    app.provide(NOTIFICATION_SERVICE, context.application.services.notifications);
    app.provide(ERROR_HANDLING_SERVICE, context.application.services.errorHandling);
    app.mount('#app');
}
