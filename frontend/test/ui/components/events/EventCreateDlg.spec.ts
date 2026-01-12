import { nextTick } from 'vue';
import { type MockInstance, afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useEventAdministrationUseCase, useEventCachingService } from '@/application';
import type { Event } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import EventCreateDlg from '@/ui/components/events/EventCreateDlg.vue';
import { mockEvent } from '~/mocks';
import { selectDropdownOption } from '~/utils';

describe('EventCreateDlg.vue', () => {
    let testee: VueWrapper;
    let result: Event | undefined = undefined;
    let closed: boolean = false;
    let createFunc: MockInstance;

    beforeAll(() => {
        vi.useFakeTimers();
    });

    afterAll(() => {
        vi.useRealTimers();
    });

    beforeEach(async () => {
        result = undefined;
        closed = false;
        createFunc = vi.spyOn(useEventAdministrationUseCase(), 'createEvent');
        useEventCachingService().clear();
        testee = mount(EventCreateDlg, { global: { stubs: { teleport: true } } });
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
        const inputName = testee.find('[data-test-id="input-event-name"] input');
        expect(inputName.classes()).not.toContain('invalid');
    });

    it('should show validation error on submit without any input', async () => {
        await submit();
        const inputName = testee.find('[data-test-id="input-event-name"] input');
        expect(inputName.classes()).toContain('invalid');
        expect(closed).toBe(false);
    });

    it('should not show validation errors initially on reopening', async () => {
        await submit(); // show the validation errors
        await cancel(); // close the dialog
        await open(); // open it again, which should have a clean state
        const inputName = testee.find('[data-test-id="input-event-name"] input');
        expect(inputName.classes()).not.toContain('invalid');
    });

    it('should create event when validations pass', async () => {
        await testee.find('[data-test-id="input-event-name"] input').setValue('Valid event name');
        await submit(true);
        expect(createFunc).toHaveBeenCalledOnce();
        expect(result?.name).toBe('Valid event name');
    });

    it('should copy slots and locations from template', async () => {
        await testee.find('[data-test-id="input-event-name"] input').setValue('Valid event name');
        await expect.poll(() => testee.find('[data-test-id="input-event-template"]').element.classList).not.toContain('loading');
        await testee.find('[data-test-id="input-event-template"] input').trigger('click');
        await selectDropdownOption(testee, /Example Event.*/);
        await submit(true);
        expect(createFunc).toHaveBeenCalledOnce();
        expect(result).toBeDefined();
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
        vi.runAllTimers();
        await nextTick();
    }

    async function cancel(): Promise<void> {
        await testee.find('[data-test-id="button-cancel"]').trigger('click');
        vi.runAllTimers();
        await expect.poll(() => closed).toBe(true);
    }

    async function submit(awaitClosed: boolean = false): Promise<void> {
        await testee.find('[data-test-id="button-submit"]').trigger('click');
        vi.runAllTimers();
        if (awaitClosed) {
            await expect.poll(() => closed).toBe(true);
        }
    }
});
