import { nextTick } from 'vue';
import { type MockInstance, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useEventAdministrationUseCase } from '@/application';
import { wait } from '@/common';
import type { Event } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import EventCreateDlg from '@/ui/components/events/EventCreateDlg.vue';
import { mockEvent } from '~/mocks';

describe('EventCreateDlg.vue', () => {
    let testee: VueWrapper;
    let result: Event | undefined = undefined;
    let closed: boolean = false;
    let createFunc: MockInstance;

    beforeEach(async () => {
        result = undefined;
        closed = false;
        testee = mount(EventCreateDlg, { global: { stubs: { teleport: true } } });
        createFunc = vi.spyOn(useEventAdministrationUseCase(), 'createEvent');
        await open();
    });

    it('should open dialog', async () => {
        const dialog = testee.find('[data-test-id="event-create-dialog"]');
        expect(dialog.isVisible()).toBe(true);
    });

    it('should close dialog on cancel', async () => {
        await cancel();
        expect(createFunc).not.toHaveBeenCalled();
        expect(result).toBe(undefined);
        expect(closed).toBe(true);
    });

    it('should not show validation errors initially', async () => {
        const inputName = await testee.find('[data-test-id="input-event-name"] input');
        expect(inputName.classes()).not.toContain('invalid');
    });

    it('should show validation error on submit without any input', async () => {
        await submit();
        const inputName = await testee.find('[data-test-id="input-event-name"] input');
        expect(inputName.classes()).toContain('invalid');
        expect(closed).toBe(false);
    });

    it('should not show validation errors initially on reopening', async () => {
        await submit(); // show the validation errors
        await cancel(); // close the dialog
        await open(); // open it again, which should have a clean state
        const inputName = await testee.find('[data-test-id="input-event-name"] input');
        expect(inputName.classes()).not.toContain('invalid');
    });

    it('should create event when validations pass', async () => {
        const inputName = await testee.find('[data-test-id="input-event-name"] input');
        await inputName.setValue('Valid event name');
        await submit();
        expect(closed).toBe(true);
        expect(createFunc).toHaveBeenCalledOnce();
        expect(result?.name).toBe('Valid event name');
    });

    it('should copy slots and locations from template', async () => {
        const inputName = await testee.find('[data-test-id="input-event-name"] input');
        await inputName.setValue('Valid event name');

        const inputTemplate = testee.find('[data-test-id="input-event-template"] input');
        await inputTemplate.trigger('click');
        await selectOption(1);

        await submit();
        expect(createFunc).toHaveBeenCalledOnce();
        expect(result?.slots).toEqual(mockEvent().slots);
        expect(result?.locations).toEqual(mockEvent().locations);
    });

    async function open(partial?: Partial<Event>): Promise<void> {
        const dialog = testee.getCurrentComponent().exposed as Dialog<Partial<Event>, Event | undefined>;
        // don't await here, because the promise will only be resolved when the dialog is closed
        dialog.open(partial).then((r) => {
            result = r;
            closed = true;
        });
        await wait(1); // wait 1ms to make sure all network requests made after opening
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

    async function selectOption(option: string | number): Promise<void> {
        const options = testee.findAll('.input-dropdown li');
        if (typeof option === 'string') {
            await options.find((it) => it.text() === option)?.trigger('click');
        } else {
            await options[option].trigger('click');
        }
    }
});
