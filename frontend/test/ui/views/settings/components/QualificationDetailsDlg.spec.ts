import { nextTick } from 'vue';
import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { wait } from '@/common';
import type { Qualification } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import QualificationDetailsDlg from '@/ui/views/settings/components/QualificationDetailsDlg.vue';
import { mockPositions, mockQualificationCaptain } from '~/mocks';

describe('QualificationDetailsDlg.vue', () => {
    let testee: VueWrapper;
    let result: Qualification | undefined = undefined;
    let closed: boolean = false;

    beforeEach(async () => {
        result = undefined;
        closed = false;
        testee = mount(QualificationDetailsDlg, { global: { stubs: { teleport: true } } });
    });

    describe('create mode', () => {
        beforeEach(async () => {
            await open(undefined);
        });

        it('should open dialog and show correct title', async () => {
            const dialog = testee.find('[data-test-id="qualification-details-dialog"]');
            expect(dialog.isVisible()).toBe(true);
            expect(testee.find('h1').text()).toEqual(testee.vm.$t('views.settings.qualifications.add-new'));
        });

        it('should close dialog on cancel and return undefined', async () => {
            await cancel();
            expect(result).toBe(undefined);
        });

        it('should enable key input', async () => {
            const input = testee.find('[data-test-id="input-key"] input');
            expect((input.element as HTMLInputElement).disabled).toBe(false);
        });

        it('should not show validation errors directly when creating', async () => {
            const input = testee.find('[data-test-id="input-name"] input');
            expect(input.classes()).not.toContain('invalid');
        });

        it('should render all positions', async () => {
            const positions = mockPositions();
            const panel = testee.find('[data-test-id="input-positions"]');
            positions.forEach((position) => expect(panel.text()).toContain(position.name));
            expect(panel.findAll('.check-box-input')).toHaveLength(positions.length);
        });
    });

    describe('edit mode', () => {
        let qualification = mockQualificationCaptain();

        beforeEach(async () => {
            qualification = mockQualificationCaptain();
            await open(qualification);
        });

        it('should open dialog and show correct title', async () => {
            const dialog = testee.find('[data-test-id="qualification-details-dialog"]');
            expect(dialog.isVisible()).toBe(true);
            expect(testee.find('h1').text()).toEqual(testee.vm.$t('views.settings.qualifications.edit'));
        });

        it('should close dialog on cancel and return undefined', async () => {
            await cancel();
            expect(result).toBe(undefined);
        });

        it('should return edited position on submit', async () => {
            const input = testee.find('[data-test-id="input-name"] input');
            await input.setValue('updated');
            await submit();
            expect(result).toEqual(mockQualificationCaptain({ name: 'updated' }));
        });

        it('should disable key input', async () => {
            const input = getInputElement('[data-test-id="input-key"] input');
            expect(input.disabled).toBe(true);
        });

        it('should render key', async () => {
            const input = getInputElement('[data-test-id="input-key"] input');
            expect(input.value).toEqual(qualification.key);
        });

        it('should render name', async () => {
            const input = getInputElement('[data-test-id="input-name"] input');
            expect(input.value).toEqual(qualification.name);
        });

        it('should render description', async () => {
            const input = getInputElement('[data-test-id="input-description"] textarea');
            expect(input.value).toEqual(qualification.description);
        });

        it('should render icon', async () => {
            const input = getInputElement('[data-test-id="input-icon"] input');
            expect(input.value).toEqual(qualification.icon);
        });

        it('should show validation errors directly', async () => {
            let input = testee.find('[data-test-id="input-name"] input');
            await input.setValue('');
            input = testee.find('[data-test-id="input-name"] input');
            expect(input.classes()).toContain('invalid');
            expect(closed).toBe(false);
        });
    });

    function getInputElement(selector: string): HTMLInputElement {
        const input = testee.find(selector);
        return input.element as HTMLInputElement;
    }

    async function cancel(): Promise<void> {
        await testee.find('[data-test-id="button-cancel"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }

    async function submit(): Promise<void> {
        await testee.find('[data-test-id="button-submit"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }

    async function open(param: Qualification | undefined): Promise<void> {
        const dialog = testee.getCurrentComponent().exposed as Dialog<Qualification | undefined, Qualification | undefined>;
        // don't await here, because the promise will only be resolved when the dialog is closed
        dialog.open(param).then((r) => {
            closed = true;
            result = r;
        });
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }
});
