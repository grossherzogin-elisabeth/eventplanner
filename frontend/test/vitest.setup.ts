import { setupI18n } from '@/ui/plugins/i18n';
import { config } from '@vue/test-utils';
import { afterAll, afterEach, beforeAll, vi } from 'vitest';
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

vi.stubGlobal('BroadcastChannel', BroadcastChannel);
