import { nextTick } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { ConfirmationDialog } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';

describe('VConfirmationDialog.vue', () => {
    let testee: VueWrapper;
    let dialog: ConfirmationDialog;

    beforeEach(async () => {
        vi.useFakeTimers();
        testee = mount(VConfirmationDialog, {
            global: { stubs: { teleport: true } },
        });
        dialog = testee.getCurrentComponent().exposed as ConfirmationDialog;
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should display the message', async () => {
        dialog.open({ message: 'Confirmation message' });
        await nextTick();
        expect(testee.find('.dialog-content').text()).toContain('Confirmation message');
    });

    it('should display the title', async () => {
        dialog.open({ title: 'Confirmation title' });
        await nextTick();
        expect(testee.find('.dialog-header').text()).toContain('Confirmation title');
    });

    it('should display custom confirm button text', async () => {
        dialog.open({ submit: 'Custom text' });
        await nextTick();
        expect(testee.find('[data-test-id="button-confirm"]').text()).toContain('Custom text');
    });

    it('should display custom cancel button text', async () => {
        dialog.open({ cancel: 'Custom text' });
        await nextTick();
        expect(testee.find('[data-test-id="button-cancel"]').text()).toContain('Custom text');
    });

    it('should return true on confirm', async () => {
        let confirmed: boolean | undefined = undefined;
        dialog.open().then((r) => (confirmed = r));
        await nextTick();
        await testee.find('[data-test-id="button-confirm"]').trigger('click');
        await timeoutsAndRender();
        expect(confirmed).toBe(true);
    });

    it('should return false on cancel', async () => {
        let confirmed: boolean | undefined = undefined;
        dialog.open().then((r) => (confirmed = r));
        await nextTick();
        await testee.find('[data-test-id="button-cancel"]').trigger('click');
        await timeoutsAndRender();
        expect(confirmed).toBe(false);
    });

    async function timeoutsAndRender(): Promise<void> {
        await nextTick();
        await nextTick();
        vi.runAllTimers();
    }
});
