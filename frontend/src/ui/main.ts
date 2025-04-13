import { createApp } from 'vue';
import type { Config } from '@/application';
import type { ApplicationServices } from '@/initApplicationServices';
import type { DomainServices } from '@/initDomainServices';
import type { UseCases } from '@/initUseCases';
import { CONFIG } from '@/ui/composables/Application';
import { provideApplicationServices } from '@/ui/plugins/provideApplicationServices';
import { provideDomainServices } from '@/ui/plugins/provideDomainServices';
import { provideUseCases } from '@/ui/plugins/provideUseCases';
import { setupTooltips } from '@/ui/plugins/tooltip';
import App from './App.vue';
import './assets/css/main.css';
import './plugins/countries';
import './plugins/fontawesome';
import { setupI18n } from './plugins/i18n';
import { setupRouter } from './plugins/router';
import './plugins/shortcuts';

export function setupVue(params: {
    config: Config;
    domainServices: DomainServices;
    useCases: UseCases;
    applicationServices: ApplicationServices;
}): void {
    const app = createApp(App);
    app.use(setupI18n(params.config));
    app.use(setupRouter(params.useCases.authUseCase));
    app.use(setupTooltips());
    app.use(provideDomainServices(params.domainServices));
    app.use(provideApplicationServices(params.applicationServices));
    app.use(provideUseCases(params.useCases));
    app.provide(CONFIG, params.config);
    app.mount('#app');
}
