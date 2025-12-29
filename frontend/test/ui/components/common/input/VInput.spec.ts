import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import {
    VInputCheckBox,
    VInputCombobox,
    VInputDate,
    VInputFile,
    VInputNumber,
    VInputSelect,
    VInputSlider,
    VInputText,
    VInputTextArea,
    VInputTime,
} from '@/ui/components/common';

const components = [
    { component: VInputCombobox, props: { options: [] } },
    { component: VInputCheckBox },
    { component: VInputDate },
    { component: VInputFile },
    { component: VInputNumber },
    { component: VInputSelect, props: { options: [] } },
    { component: VInputSlider },
    { component: VInputText },
    { component: VInputTextArea },
    { component: VInputTime },
];

describe.each(components)('$component.__name', ({ component, props }) => {
    it('should render label', async () => {
        const testee = mount(component, {
            props: { ...props, label: 'label text' },
        });
        const label = testee.find('label');
        expect(label.text()).toContain('label text');
    });

    it('should generate id for input field', async () => {
        const testee = mount(component, { props: { ...props } });
        const label = testee.find('label');
        const input = testee.find('input,textarea');

        expect(input.attributes().id).not.toBeUndefined();
        expect(label.attributes().for).toEqual(input.attributes().id);
    });

    it('should render hint text', async () => {
        const testee = mount(VInputDate, {
            props: { ...props, hint: 'the hint' },
        });
        const hint = testee.find('.input-hint');

        expect(hint.text()).toContain('the hint');
    });

    it('should render all errors', async () => {
        const testee = mount(VInputDate, {
            props: { ...props, errors: ['some error', 'some other error'], errorsVisible: true },
        });
        const errors = testee.find('.input-errors');

        expect(errors.text()).toContain('some error');
        expect(errors.text()).toContain('some other error');
    });

    it('should not render any error', async () => {
        const testee = mount(VInputDate, {
            props: { ...props, errors: ['some error', 'some other error'], errorsVisible: false },
        });
        const errors = testee.find('.input-errors');
        expect(errors.exists()).toBeFalsy();
    });

    it('should not render hint if errors are present', async () => {
        const testee = mount(VInputDate, {
            props: { ...props, errors: ['some error'], errorsVisible: true, hint: 'should not be rendered' },
        });
        const hint = testee.find('.input-hint');
        expect(hint.exists()).toBeFalsy();
    });

    it('should render placeholder', async () => {
        const testee = mount(VInputDate, {
            props: { ...props, placeholder: 'the placeholder' },
        });
        const input = testee.find('input,textarea');
        expect(input.attributes().placeholder).toEqual('the placeholder');
    });

    it('should disable user input', async () => {
        const testee = mount(VInputDate, {
            props: { ...props, disabled: true },
        });
        const input = testee.find('input,textarea');
        expect(input.attributes().disabled).not.toBeUndefined();

        await input.setValue('some input');
        expect(testee.emitted()).toEqual({});
    });
});
