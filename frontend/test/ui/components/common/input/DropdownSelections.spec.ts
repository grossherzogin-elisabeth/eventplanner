import { nextTick } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { VInputCombobox, VInputSelect } from '@/ui/components/common';

const components = [{ component: VInputCombobox }, { component: VInputSelect }];

describe.each(components)('$component.__name', ({ component }) => {
    let testee: VueWrapper;

    beforeEach(() => {
        vi.useFakeTimers();
        testee = mount(component, {
            props: {
                label: 'label text',
                options: [
                    { value: 'hidden', label: 'should not be visible', hidden: true },
                    { value: 'a', label: 'A' },
                    { value: 'b', label: 'B' },
                    { value: 'c', label: 'C' },
                    { value: 'd', label: 'D' },
                ],
            },
        });
    });

    afterEach(() => {
        vi.useRealTimers();
    });

    it('should open dropdown on click', async () => {
        await testee.find('input').trigger('click');
        const dropdown = testee.find('.input-dropdown');
        expect(dropdown.exists()).toBe(true);
        expect(dropdown.isVisible()).toBe(true);
    });

    it('should render all visible options', async () => {
        await testee.find('input').trigger('click');
        const options = testee.findAll('.input-dropdown li');
        expect(options).toHaveLength(4);
    });

    it('should render display value for option', async () => {
        await testee.find('input').trigger('click');
        const option = testee.find('.input-dropdown li');
        expect(option.text()).toBe('A');
    });

    it('should emit event with value on option click', async () => {
        await testee.find('input').trigger('click');
        const option = testee.find('.input-dropdown li');
        await option.trigger('click');
        expect(testee.emitted('update:modelValue')).toHaveLength(1);
        expect(testee.emitted('update:modelValue')?.[0]).toEqual(['a']);
    });

    it('should show hidden option when selected', async () => {
        await testee.setProps({ modelValue: 'hidden' });
        await testee.find('input').trigger('click');
        const option = testee.find('.input-dropdown li');
        expect(option.text()).toBe('should not be visible');
    });

    it('should highlight selected option', async () => {
        await testee.setProps({ modelValue: 'b' });
        await testee.find('input').trigger('click');
        const option = testee.find('.input-dropdown-option-focus');
        expect(option.text()).toBe('B');
    });

    it('should open dropdown on keydown:down', async () => {
        await testee.find('input').trigger('keydown.down');
        const dropdown = testee.find('.input-dropdown');
        expect(dropdown.exists()).toBe(true);
        expect(dropdown.isVisible()).toBe(true);
    });

    it('should close dropdown on keydown:esc', async () => {
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.esc');
        vi.runAllTimers();
        await nextTick();
        const dropdown = testee.find('.input-dropdown');
        expect(dropdown.exists()).toBe(false);
    });

    it('should highlight first option on keydown:down', async () => {
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.down');
        const option = testee.find('.input-dropdown-option-focus');
        expect(option.text()).toBe('A');
    });

    it('should highlight next option on keydown:down', async () => {
        await testee.setProps({ modelValue: 'b' });
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.down');
        const option = testee.find('.input-dropdown-option-focus');
        expect(option.text()).toBe('C');
    });

    it('should cycle highlighted options on bottom reached', async () => {
        await testee.setProps({ modelValue: 'd' });
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.down');
        const option = testee.find('.input-dropdown-option-focus');
        expect(option.text()).toBe('A');
    });

    it('should highlight previous option on keydown:up', async () => {
        await testee.setProps({ modelValue: 'b' });
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.up');
        const option = testee.find('.input-dropdown-option-focus');
        expect(option.text()).toBe('A');
    });

    it('should cycle highlighted options on top reached', async () => {
        await testee.setProps({ modelValue: 'a' });
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.up');
        const option = testee.find('.input-dropdown-option-focus');
        expect(option.text()).toBe('D');
    });

    it('should not emit eny event on focus change', async () => {
        await testee.setProps({ modelValue: 'a' });
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.down');
        expect(testee.emitted('update:modelValue')).toBeUndefined();
    });

    it('should emit update event on keydown:enter', async () => {
        await testee.setProps({ modelValue: 'a' });
        await testee.find('input').trigger('click');
        await testee.find('input').trigger('keydown.down');
        await testee.find('input').trigger('keydown.enter');
        expect(testee.emitted('update:modelValue')).toHaveLength(1);
        expect(testee.emitted('update:modelValue')?.[0]).toEqual(['b']);
    });
});
