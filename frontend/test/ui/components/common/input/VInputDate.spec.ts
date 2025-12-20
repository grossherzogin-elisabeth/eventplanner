import { nextTick } from 'vue';
import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { wait } from '@/common';
import { VDialog, VDropdownWrapper, VInputDate } from '@/ui/components/common';
import { happyDOM } from '~/utils/happyDOM.ts';

describe('VInputDate', () => {
    it('should render initial value', async () => {
        const props = { modelValue: new Date(2025, 10, 15) };
        const testee = mount(VInputDate, { props });
        const input = testee.find('input');
        expect(input.element.value).toEqual('15.11.2025');
    });

    it('should render when value changes', async () => {
        const testee = mount(VInputDate);
        await testee.setProps({ modelValue: new Date(2025, 10, 16) });
        const input = testee.find('input');
        expect(input.element.value).toEqual('16.11.2025');
    });

    it.each(['15.04.2025', '15.4.25', '15/04/2025', '15/4/2025', '4/15/2025', '15042025', '150425'])(
        'should parse user input $0 correctly',
        async (value: string) => {
            const testee = mount(VInputDate);
            const input = testee.find('input');
            await input.setValue(value);
            await input.trigger('blur');
            expect(input.element.value).toEqual('15.04.2025');
            expect(testee.emitted('update:modelValue')?.[0]).toEqual([new Date(2025, 3, 15)]);
        }
    );

    it('should open datepicker dropdown on large screens', async () => {
        happyDOM().setViewport({ width: 1200 });
        const testee = mount(VInputDate);
        const button = testee.find('button');
        await button.trigger('click');
        await nextTick();
        // dialog wrapper is rendered always, but should not emit opening event
        expect(testee.findComponent(VDialog).emitted('opening')).toBeUndefined();
        // dropdown should be rendered
        expect(testee.findComponent(VDropdownWrapper).exists()).toBeTruthy();
    });

    it('should open datepicker dialog on small screens', async () => {
        happyDOM().setViewport({ width: 500 });
        const testee = mount(VInputDate);
        const button = testee.find('button');
        await button.trigger('click');
        await nextTick();
        await wait(100);
        // dialog wrapper should have emitted opening event
        expect(testee.findComponent(VDialog).emitted('opening')).not.toBeUndefined();
        // dropdown should not be rendered
        expect(testee.findComponent(VDropdownWrapper).exists()).toBeFalsy();
    });
});
