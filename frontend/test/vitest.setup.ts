import { config } from '@/config.ts';
import { setupI18n } from '@/ui/plugins/i18n.ts';
import { config as testConfig } from '@vue/test-utils';
import { vi } from 'vitest';

// ---------------------------------------------------------------
// mock global vue plugins
// ---------------------------------------------------------------

testConfig.global.plugins = [setupI18n({ ...config })];

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
