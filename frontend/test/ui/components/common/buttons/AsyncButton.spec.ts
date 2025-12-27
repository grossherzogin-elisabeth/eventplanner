import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { wait } from '@/common';
import { AsyncButton, VLoadingSpinner } from '@/ui/components/common';

describe('AsyncButton.vue', () => {
    let testee: VueWrapper;
    let clicked = false;
    let resolve: () => void = () => {};
    let reject: () => void = () => {};

    beforeEach(() => {
        testee = mount(AsyncButton, {
            props: {
                action: (): Promise<void> =>
                    new Promise<void>((resolveCallback, rejectCallback) => {
                        clicked = true;
                        resolve = resolveCallback;
                        reject = rejectCallback;
                    }),
            },
            slots: { label: 'label text' },
        });
    });

    it('should show label text', () => {
        expect(testee.text()).toContain('label text');
    });

    it('should execute action on click', async () => {
        await testee.find('button').trigger('click');
        resolve();
        expect(clicked).toBe(true);
    });

    it('should show check icon on success', async () => {
        await testee.find('button').trigger('click');
        resolve();
        await wait(1);
        expect(testee.find('.fa-check').exists()).toBe(true);
    });

    it('should show warn icon on error', async () => {
        await testee.find('button').trigger('click');
        reject();
        await wait(1);
        expect(testee.find('.fa-warning').exists()).toBe(true);
    });

    it('should show loading spinner during execution', async () => {
        await testee.find('button').trigger('click');
        expect(testee.findComponent(VLoadingSpinner).exists()).toBe(true);
        resolve();
        await wait(1);
        expect(testee.findComponent(VLoadingSpinner).exists()).toBe(false);
    });

    it('should be disabled during execution', async () => {
        const button = testee.find('button');
        await button.trigger('click');
        expect(button.element.disabled).toBe(true);
        resolve();
        await wait(1);
        expect(button.element.disabled).toBe(false);
    });

    it('should be disabled', async () => {
        await testee.setProps({ disabled: true });
        expect(testee.find('button').element.disabled).toBe(true);
    });
});
