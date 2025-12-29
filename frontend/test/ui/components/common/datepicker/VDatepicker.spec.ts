import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import VDatepicker from '@/ui/components/common/datepicker/VDatepicker.vue';

describe('VDatepicker.vue', () => {
    let testee: VueWrapper;

    beforeEach(() => {
        testee = mount(VDatepicker, {
            props: {
                modelValue: new Date(2025, 9, 28),
            },
        });
    });

    it('Should render selected year', () => {
        const monthButton = testee.find('[data-test-id="datepicker-year"]');
        expect(monthButton.text()).toContain(2025);
    });

    it('Should render selected month', () => {
        const currentMonthButton = testee.find('[data-test-id="datepicker-month"]');
        const expectedMonthName = testee.vm.$t('generic.month.9');
        expect(currentMonthButton.text()).toContain(expectedMonthName);
    });

    it('Should highlight selected day', () => {
        expect(testee.find('.day.selected').text()).toContain(28);
    });

    it('Should render correct days for month', () => {
        const days = testee.findAll('.day');
        expect(days).toHaveLength(5 * 7);
        expect(days[0].text()).toBe('29');
        expect(days[7].text()).toBe('6');
        expect(days[14].text()).toBe('13');
        expect(days[21].text()).toBe('20');
        expect(days[34].text()).toBe('2');
    });

    it('Should switch to previous month', async () => {
        const previousMonthButton = testee.find('[data-test-id="datepicker-previous-month"]');
        await previousMonthButton.trigger('click');
        const expectedMonthName = testee.vm.$t('generic.month.8');
        const currentMonthButton = testee.find('[data-test-id="datepicker-month"]');
        expect(currentMonthButton.text()).toContain(expectedMonthName);
        expect(testee.findAll('.day')[0].text()).toBe('1');
    });

    it('Should switch to next month', async () => {
        const nextMonthButton = testee.find('[data-test-id="datepicker-next-month"]');
        await nextMonthButton.trigger('click');
        const expectedMonthName = testee.vm.$t('generic.month.10');
        const currentMonthButton = testee.find('[data-test-id="datepicker-month"]');
        expect(currentMonthButton.text()).toContain(expectedMonthName);
        expect(testee.findAll('.day')[0].text()).toBe('27');
    });

    it('Should not update model value when switching month', async () => {
        const nextMonthButton = testee.find('[data-test-id="datepicker-next-month"]');
        await nextMonthButton.trigger('click');
        expect(testee.emitted('update:modelValue')).toBeUndefined();
    });

    it('Should switch to previous year', async () => {
        const previousYearButton = testee.find('[data-test-id="datepicker-previous-year"]');
        await previousYearButton.trigger('click');
        const currentYearButton = testee.find('[data-test-id="datepicker-year"]');
        expect(currentYearButton.text()).toContain(2024);
        expect(testee.findAll('.day')[0].text()).toBe('30');
    });

    it('Should switch to next year', async () => {
        const nextYearButton = testee.find('[data-test-id="datepicker-next-year"]');
        await nextYearButton.trigger('click');
        const currentYearButton = testee.find('[data-test-id="datepicker-year"]');
        expect(currentYearButton.text()).toContain(2026);
        expect(testee.findAll('.day')[0].text()).toBe('28');
    });

    it('Should not update model value when switching year', async () => {
        const nextYearButton = testee.find('[data-test-id="datepicker-next-year"]');
        await nextYearButton.trigger('click');
        expect(testee.emitted('update:modelValue')).toBeUndefined();
    });

    it('Should switch to month selection', async () => {
        const currentMonthButton = testee.find('[data-test-id="datepicker-month"]');
        await currentMonthButton.trigger('click');
        expect(testee.find('.month-selection').exists()).toBe(true);
    });

    it('Should switch to year selection', async () => {
        const currentYearButton = testee.find('[data-test-id="datepicker-year"]');
        await currentYearButton.trigger('click');
        expect(testee.find('.year-selection').exists()).toBe(true);
    });

    it('Should emit update event on day click', async () => {
        const days = testee.findAll('.day button');
        await days[7].trigger('click');
        expect(testee.emitted('update:modelValue')).toEqual([[new Date(2025, 9, 6)]]);
    });
});
