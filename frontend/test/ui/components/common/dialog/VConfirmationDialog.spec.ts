import { nextTick } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { ConfirmationDialog } from '@/ui/components/common';
import { VConfirmationDialog } from '@/ui/components/common';

describe('VConfirmationDialog.vue', () => {
    let testee: VueWrapper;
    let dialog: ConfirmationDialog;

    beforeEach(async () => {
        vi.useFakeTimers();
        closed = false;
        testee = mount(VConfirmationDialog, {
            global: { stubs: { teleport: true } },
        });
        dialog = testee.getCurrentComponent().exposed as ConfirmationDialog;
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
});
