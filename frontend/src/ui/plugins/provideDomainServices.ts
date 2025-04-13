import type { App, Plugin } from 'vue';
import type { DomainServices } from '@/initDomainServices';
import { EVENT_SERVICE, POSITION_SERVICE, QUALIFICATION_SERVICE, REGISTRATION_SERVICE, USER_SERVICE } from '@/ui/composables/Domain';

export function provideDomainServices(domainServices: DomainServices): Plugin {
    return {
        install(app: App): void {
            app.provide(USER_SERVICE, domainServices.users);
            app.provide(REGISTRATION_SERVICE, domainServices.registrations);
            app.provide(EVENT_SERVICE, domainServices.events);
            app.provide(QUALIFICATION_SERVICE, domainServices.qualifications);
            app.provide(POSITION_SERVICE, domainServices.positions);
        },
    };
}
