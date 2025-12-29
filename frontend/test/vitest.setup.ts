import { afterAll, afterEach, beforeAll, beforeEach } from 'vitest';
import { config } from '@vue/test-utils';
import { setupI18n } from '@/ui/plugins/i18n';
import { server } from '~/mocks';

// ---------------------------------------------------------------
// mock http requests
// ---------------------------------------------------------------

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// ---------------------------------------------------------------
// mock global vue plugins
// ---------------------------------------------------------------

config.global.plugins = [setupI18n({ locale: 'de', fallbackLocale: 'de', availableLocales: ['de'] })];

// ---------------------------------------------------------------
// mock teleport targets
// ---------------------------------------------------------------

const teleportTargetIds = ['nav-right'];

beforeEach(() => {
    teleportTargetIds.forEach((id) => {
        const el = document.createElement('div');
        el.id = id;
        document.body.appendChild(el);
    });
});

afterEach(() => (document.body.innerHTML = ''));

process.on('unhandledRejection', (reason) => {
    console.error('Got an unhandled promise rejection');
    console.error(reason);
    throw reason;
});
