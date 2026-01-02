import { nextTick } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useErrorHandlingService } from '@/application';
import { type ErrorDialogMessage, VErrorDialog } from '@/ui/components/common';
import type { Dialog } from '@/ui/components/common';

describe('VErrorDialog.vue', () => {
    let testee: VueWrapper;
    let dialog: Dialog<ErrorDialogMessage>;

    beforeEach(async () => {
        vi.useFakeTimers();
        closed = false;
        testee = mount(VErrorDialog, {
            global: { stubs: { teleport: true } },
        });
        dialog = testee.getCurrentComponent().exposed as Dialog<ErrorDialogMessage>;
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should register self at error handling service', async () => {
        const registerErrorHandlerFunction = vi.spyOn(useErrorHandlingService(), 'registerErrorHandler');
        mount(VErrorDialog);
        expect(registerErrorHandlerFunction).toHaveBeenCalledOnce();
    });

    it('should display the error message', async () => {
        dialog.open({ message: 'Error message' });
        await nextTick();
        expect(testee.find('.dialog-content').text()).toContain('Error message');
    });

    it('should display the error title', async () => {
        dialog.open({ title: 'Error title' });
        await nextTick();
        expect(testee.find('.dialog-header').text()).toContain('Error title');
    });

    it('should display a default error message', async () => {
        dialog.open({});
        await nextTick();
        const defaultMessage = testee.vm.$t('components.error-dialog.default-text');
        expect(testee.find('.dialog-content').text()).toContain(defaultMessage);
    });

    it('should display a default error title', async () => {
        dialog.open({});
        await nextTick();
        const defaultMessage = testee.vm.$t('components.error-dialog.default-title');
        expect(testee.find('.dialog-header').text()).toContain(defaultMessage);
    });

    it('should not show only close button when no retry function is specified', async () => {
        dialog.open({});
        await nextTick();
        expect(testee.find('[data-test-id="button-retry"]').exists()).toBe(false);
        expect(testee.find('[data-test-id="button-close"]').exists()).toBe(true);
    });

    it('should not show close and retry button when retry function is specified', async () => {
        dialog.open({ retry: () => console.log('retry') });
        await nextTick();
        expect(testee.find('[data-test-id="button-retry"]').exists()).toBe(true);
        expect(testee.find('[data-test-id="button-close"]').exists()).toBe(true);
    });

    it('should display custom close button text', async () => {
        dialog.open({ cancelText: 'Custom button text' });
        await nextTick();
        expect(testee.find('[data-test-id="button-close"]').text()).toBe('Custom button text');
    });

    it('should display custom retry button text', async () => {
        dialog.open({
            retry: () => console.log('retry'),
            retryText: 'Custom button text',
        });
        await nextTick();
        expect(testee.find('[data-test-id="button-retry"]').text()).toBe('Custom button text');
    });
});
