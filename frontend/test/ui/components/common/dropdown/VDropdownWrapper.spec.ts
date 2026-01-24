import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { VDropdownWrapper } from '@/ui/components/common';

describe('VDropdownWrapper.vue', () => {
    let testee: VueWrapper;

    beforeEach(async () => {
        vi.useFakeTimers();
        testee = mount(VDropdownWrapper, {
            global: { stubs: { teleport: true } },
            slots: {
                default: '<div data-test-id="test-content">test dropdown content</div>',
            },
        });
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should render slot content', () => {
        expect(testee.find('.dropdown-wrapper').text()).toEqual('test dropdown content');
    });

    it('should emit close event on background click', async () => {
        await testee.find('.dropdown-wrapper-background').trigger('click');
        vi.runAllTimers();
        expect(testee.emitted('close')).toHaveLength(1);
    });

    it('should emit close event on esc keypress', async () => {
        await testee.find('.dropdown-wrapper-background').trigger('keydown.esc');
        vi.runAllTimers();
        expect(testee.emitted('close')).toHaveLength(1);
    });
});
