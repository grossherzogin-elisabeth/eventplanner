import { config } from '@/config';
import { initAdapters } from '@/initAdapters.ts';
import { initApplicationServices } from '@/initApplicationServices.ts';
import { initDomainServices } from '@/initDomainServices.ts';
import { initUseCases } from '@/initUseCases.ts';
import { setupI18n } from '@/ui/plugins/i18n.ts';
import { provideApplicationServices } from '@/ui/plugins/provideApplicationServices.ts';
import { provideDomainServices } from '@/ui/plugins/provideDomainServices.ts';
import { provideUseCases } from '@/ui/plugins/provideUseCases.ts';
import { setupRouter } from '@/ui/plugins/router.ts';
import { config as testConfig } from '@vue/test-utils';
import { vi } from 'vitest';

const adapters = initAdapters();
const domainServices = initDomainServices();
const applicationServices = initApplicationServices({ adapters, storage: 'memory' });
const useCases = initUseCases({ config, adapters, domainServices, applicationServices });

// ---------------------------------------------------------------
// mock global vue plugins
// ---------------------------------------------------------------

testConfig.global.plugins = [
    setupI18n({ ...config }),
    setupRouter(useCases.authUseCase),
    provideDomainServices(domainServices),
    provideApplicationServices(applicationServices),
    provideUseCases(useCases),
];

// ---------------------------------------------------------------
// mock global elements not present in happy dom by default
// ---------------------------------------------------------------

class ResizeObserver {
    public observe(): void {
        // do nothing
    }
    public unobserve(): void {
        // do nothing
    }
    public disconnect(): void {
        // do nothing
    }
}
window.ResizeObserver = ResizeObserver;

// EventTarget is the parent of VisualViewport. Mocking visualViewport with
// an EventTarget instance omits some properties defined in the subtype
// (VisualViewport), but these are probably not needed for testing purposes
// https://developer.mozilla.org/en-US/docs/Web/API/VisualViewport
vi.stubGlobal('visualViewport', new EventTarget());
