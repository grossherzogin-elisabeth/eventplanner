import { nextTick } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { Sheet } from '@/ui/components/common';
import { VSheet } from '@/ui/components/common';

describe('VSheet', () => {
    let testee: VueWrapper;
    let sheet: Sheet;

    beforeEach(async () => {
        vi.useFakeTimers();
        testee = mount(VSheet, {
            props: { animationDuration: 0 },
            global: { stubs: { teleport: true } },
            slots: {
                title: '<div data-test-id="test-title">test sheet title</div>',
                content: '<div data-test-id="test-content">test sheet content</div>',
            },
        });
        sheet = testee.getCurrentComponent().exposed as Sheet;
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should open sheet on method call', async () => {
        sheet.open();
        await nextTick();
        expect(testee.find('[data-test-id="sheet"]').isVisible()).toBe(true);
    });

    it('should render title', async () => {
        sheet.open();
        await nextTick();
        expect(testee.find('[data-test-id="test-title"]').isVisible()).toBe(true);
    });

    it('should render content', async () => {
        sheet.open();
        await nextTick();
        expect(testee.find('[data-test-id="test-content"]').isVisible()).toBe(true);
    });

    it('should resolve promise on submit call', async () => {
        let submitted = false;
        sheet.open().then(() => (submitted = true));
        await timeoutsAndRender();
        sheet.submit();
        await timeoutsAndRender();
        expect(submitted).toBe(true);
    });

    it('should reject promise on reject call', async () => {
        let rejected = false;
        sheet.open().catch(() => (rejected = true));
        await timeoutsAndRender();
        sheet.reject();
        await timeoutsAndRender();
        expect(rejected).toBe(true);
    });

    it('should emit events on open', async () => {
        sheet.open();
        await nextTick();
        expect(testee.emitted('opening')).toHaveLength(1);
        await timeoutsAndRender();
        expect(testee.emitted('opened')).toHaveLength(1);
    });

    it('should emit events on close', async () => {
        sheet.open();
        await timeoutsAndRender();
        sheet.submit();
        expect(testee.emitted('closing')).toHaveLength(1);
        await timeoutsAndRender();
        expect(testee.emitted('closed')).toHaveLength(1);
    });

    it('should render content until fully closed', async () => {
        sheet.open();
        await timeoutsAndRender();
        sheet.submit();
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
