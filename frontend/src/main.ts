import { createApp } from 'vue';
import { useAuthUseCase, useConfigService, useUsersUseCase } from '@/application';
import App from '@/ui/App.vue';
import '@/ui/plugins/countries';
import '@/ui/plugins/fontawesome';
import { setupI18n } from '@/ui/plugins/i18n';
import { setupRouter } from '@/ui/plugins/router';
import '@/ui/plugins/shortcuts';
import { setupTooltips } from '@/ui/plugins/tooltip';
import '@/ui/assets/css/main.css';

const config = useConfigService().getConfig();

const app = createApp(App);
app.use(
    setupI18n({
        locale: config.i18nLocale,
        fallbackLocale: config.i18nFallbackLocale,
        availableLocales: config.i18nAvailableLocales,
    })
);
app.use(setupRouter(useAuthUseCase()));
app.use(setupTooltips());
app.mount('#app');

useUsersUseCase().applyUserSettings();
