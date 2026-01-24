import { nextTick } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { Dialog } from '@/ui/components/common';
import { VDialog } from '@/ui/components/common';

describe('VDialog', () => {
    let testee: VueWrapper;
    let dialog: Dialog;

    beforeEach(async () => {
        vi.useFakeTimers();
        testee = mount(VDialog, {
            global: { stubs: { teleport: true } },
            slots: {
                title: '<div data-test-id="test-title">test dialog title</div>',
                content: '<div data-test-id="test-content">test dialog content</div>',
            },
        });
        dialog = testee.getCurrentComponent().exposed as Dialog;
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should open dialog on method call', async () => {
        dialog.open();
        await nextTick();
        vi.runAllTimers();
        expect(testee.find('[data-test-id="dialog"]').isVisible()).toBe(true);
    });

    it('should render title', async () => {
        dialog.open();
        await nextTick();
        vi.runAllTimers();
        expect(testee.find('[data-test-id="test-title"]').isVisible()).toBe(true);
    });

    it('should render content', async () => {
        dialog.open();
        await nextTick();
        vi.runAllTimers();
        expect(testee.find('[data-test-id="test-content"]').isVisible()).toBe(true);
    });

    it('should resolve promise on submit call', async () => {
        let submitted = false;
        dialog.open().then(() => (submitted = true));
        await nextTick();
        dialog.submit();
        await timeoutsAndRender();
        expect(submitted).toBe(true);
    });

    it('should reject promise on reject call', async () => {
        let rejected = false;
        dialog.open().catch(() => (rejected = true));
        await nextTick();
        dialog.reject();
        await timeoutsAndRender();
        expect(rejected).toBe(true);
    });

    it('should emit events on open', async () => {
        dialog.open();
        await nextTick();
        expect(testee.emitted('opening')).toHaveLength(1);
        await timeoutsAndRender();
        expect(testee.emitted('opened')).toHaveLength(1);
    });

    it('should emit events on close', async () => {
        dialog.open();
        await nextTick();
        dialog.submit();
        expect(testee.emitted('closing')).toHaveLength(1);
        await timeoutsAndRender();
        expect(testee.emitted('closed')).toHaveLength(1);
    });

    it('should render content until fully closed', async () => {
        dialog.open();
        await nextTick();
        dialog.submit();
        expect(testee.find('[data-test-id="test-content"]').isVisible()).toBe(true);
        await timeoutsAndRender();
        expect(testee.find('[data-test-id="test-content"]').exists()).toBe(false);
    });

    async function timeoutsAndRender(): Promise<void> {
        await nextTick();
        await nextTick();
        vi.runAllTimers();
    }
});
