import { setupI18n } from '@/ui/plugins/i18n';
import { setupRouter } from '@/ui/plugins/router.ts';
import { config } from '@vue/test-utils';
import { vi } from 'vitest';
import { MOCK_UUID, mockSignedInUser } from '~/mocks';

// ---------------------------------------------------------------
// mock global vue plugins
// ---------------------------------------------------------------

const AuthUseCase = vi.fn();
AuthUseCase.prototype.firstAuthentication = vi.fn(() => Promise.resolve(undefined));
AuthUseCase.prototype.isLoggedIn = vi.fn(() => true);
AuthUseCase.prototype.getSignedInUser = vi.fn(() => mockSignedInUser());

vi.mock('uuid', () => ({
    v4: vi.fn(() => MOCK_UUID),
}));

config.global.plugins = [setupI18n({ locale: 'de', fallbackLocale: 'de', availableLocales: ['de'] }), setupRouter(new AuthUseCase())];

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
