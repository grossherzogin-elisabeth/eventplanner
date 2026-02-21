import { afterAll, afterEach, beforeAll, beforeEach, vi } from 'vitest';
import { RouterLinkStub, config } from '@vue/test-utils';
import { resetAdapters } from '@/adapter';
import { resetApplicationServices, resetUseCases } from '@/application';
import { resetDomainServices } from '@/domain';
import { setupI18n } from '@/ui/plugins/i18n';
import { server } from '~/mocks';

// ---------------------------------------------------------------
// mock http requests
// ---------------------------------------------------------------

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
    resetAdapters();
    resetApplicationServices();
    resetUseCases();
    resetDomainServices();
    vi.clearAllMocks();
    localStorage.clear();
    sessionStorage.clear();
    server.resetHandlers();
});
afterAll(() => server.close());

// ---------------------------------------------------------------
// mock global vue plugins
// ---------------------------------------------------------------

config.global.plugins = [setupI18n({ locale: 'de', fallbackLocale: 'de', availableLocales: ['de'] })];

config.global.stubs = {
    RouterLink: RouterLinkStub,
    teleport: true,
};
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
    console.error('Uncaught promise rejection:', reason);
    throw reason;
});
