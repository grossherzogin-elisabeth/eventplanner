import { setupI18n } from '@/ui/plugins/i18n';
import { config } from '@vue/test-utils';
import { afterAll, afterEach, beforeAll, beforeEach } from 'vitest';
import { server } from '~/mocks';

// ---------------------------------------------------------------
// mock http requests
// ---------------------------------------------------------------

beforeAll(() => server.listen());
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
