import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import { VError, VInfo, VSuccess, VWarning } from '@/ui/components/common';

const components = [{ component: VError }, { component: VInfo }, { component: VSuccess }, { component: VWarning }];

describe.each(components)('$component.__name', ({ component }) => {
    it('should display the error message', () => {
        const testee = mount(component, {
            slots: { default: 'message to display' },
        });
        expect(testee.text()).toContain('message to display');
    });

    it('should display dismiss button', () => {
        const testee = mount(component, {
            slots: { default: 'message to display' },
            props: { dismissable: true },
        });
        expect(testee.find('[data-test-id="button-dismiss"]').isVisible()).toBe(true);
    });

    it('should not display dismiss button', () => {
        const testee = mount(component, {
            slots: { default: 'message to display' },
            props: { dismissable: false },
        });
        expect(testee.find('[data-test-id="button-dismiss"]').exists()).toBe(false);
    });

    it('should emit dismiss event', async () => {
        const testee = mount(component, {
            slots: { default: 'message to display' },
            props: { dismissable: true },
        });
        await testee.find('[data-test-id="button-dismiss"]').trigger('click');
        expect(testee.emitted('dismiss')).toHaveLength(1);
    });
});
