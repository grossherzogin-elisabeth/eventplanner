import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { wait } from '@/common';
import type { Dialog } from '@/ui/components/common';
import { VDialog } from '@/ui/components/common';

describe('VDialog', () => {
    let testee: VueWrapper;
    let dialog: Dialog;

    beforeEach(async () => {
        closed = false;
        testee = mount(VDialog, {
            props: { animationDuration: 0 },
            global: { stubs: { teleport: true } },
            slots: {
                title: '<div data-test-id="test-title">test dialog title</div>',
                content: '<div data-test-id="test-content">test dialog content</div>',
            },
        });
        dialog = testee.getCurrentComponent().exposed as Dialog;
    });

    it('should open dialog on method call', async () => {
        dialog.open();
        await wait(1);
        expect(testee.find('[data-test-id="dialog"]').isVisible()).toBe(true);
    });

    it('should render title', async () => {
        dialog.open();
        await wait(1);
        expect(testee.find('[data-test-id="test-title"]').isVisible()).toBe(true);
    });

    it('should render content', async () => {
        dialog.open();
        await wait(1);
        expect(testee.find('[data-test-id="test-content"]').isVisible()).toBe(true);
    });

    it('should resolve promise dialog on submit call', async () => {
        let submitted = false;
        dialog.open().then(() => (submitted = true));
        await wait(1);
        dialog.submit();
        await wait(1);
        await wait(10);
        expect(submitted).toBe(true);
    });

    it('should reject promise dialog on reject call', async () => {
        let rejected = false;
        dialog.open().catch(() => (rejected = true));
        await wait(1);
        dialog.reject();
        await wait(1);
        expect(rejected).toBe(true);
    });

    it('should emit events on open', async () => {
        dialog.open();
        await wait(1);
        expect(testee.emitted('opening')).toHaveLength(1);
        await wait(1);
        expect(testee.emitted('opened')).toHaveLength(1);
    });

    it('should emit events on close', async () => {
        dialog.open();
        await wait(1);
        dialog.submit();
        await wait(1);
        expect(testee.emitted('closing')).toHaveLength(1);
        await wait(11); // the close animation takes 100ms longer than the open animation
        expect(testee.emitted('closed')).toHaveLength(1);
    });

    it('should render content until fully closed', async () => {
        dialog.open();
        await wait(1);
        dialog.submit();
        await wait(5); // 100ms closing duration - 5ms grace period
        expect(testee.find('[data-test-id="test-content"]').isVisible()).toBe(true);
        await wait(10);
        expect(testee.find('[data-test-id="test-content"]').exists()).toBe(false);
    });
});
