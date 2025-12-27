import { nextTick } from 'vue';
import type { MockInstance } from 'vitest';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { useEventAdministrationUseCase } from '@/application';
import { wait } from '@/common';
import type { Event } from '@/domain';
import { EventSignupType } from '@/domain';
import { EventType } from '@/domain';
import { EventState } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import EventBatchEditDlg from '@/ui/views/events/list-admin/EventBatchEditDlg.vue';
import { mockEvent, server } from '~/mocks';
import { find, findAll } from '~/utils';

describe('EventBatchEditDlg.vue', () => {
    let testee: VueWrapper;
    let result: boolean | undefined = undefined;
    let updateFunc: MockInstance;

    beforeEach(async () => {
        result = undefined;
        testee = mount(EventBatchEditDlg);
        updateFunc = vi.spyOn(useEventAdministrationUseCase(), 'updateEvents');
        const eventA = mockEvent({ key: 'a' });
        const eventB = mockEvent({ key: 'b' });
        server.use(http.get('api/v1/events/a', () => HttpResponse.json(eventA, { status: 200 })));
        server.use(http.get('api/v1/events/b', () => HttpResponse.json(eventB, { status: 200 })));
        await open([eventA, eventB]);
    });

    it('should open dialog', async () => {
        const dialog = find('[data-test-id="event-batch-edit-dialog"]');
        expect(dialog.isVisible()).toBe(true);
    });

    it('should close dialog on cancel', async () => {
        await cancel();
        expect(updateFunc).not.toHaveBeenCalled();
        expect(result).toBe(false);
    });

    it('should close dialog on submit without input', async () => {
        await submit();
        expect(updateFunc).not.toHaveBeenCalled();
        expect(result).toBe(false);
    });

    it.each([EventState.Draft, EventState.OpenForSignup, EventState.Planned, EventState.Canceled])(
        'should patch only event status to $0',
        async (state) => {
            const input = find('[data-test-id="input-event-status"] input');
            await input.trigger('click');
            await selectOption(testee.vm.$t(`generic.event-state.${state}`));

            await submit();
            expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { state });
            expect(result).toBe(true);
        }
    );

    it('should patch only event name', async () => {
        const input = find('[data-test-id="input-event-name"] input');
        await input.setValue('updated');
        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { name: 'updated' });
        expect(result).toBe(true);
    });

    it.each([
        EventType.SingleDayEvent,
        EventType.WeekendEvent,
        EventType.MultiDayEvent,
        EventType.WorkEvent,
        EventType.TrainingEvent,
        EventType.Other,
    ])('should patch only event type to $0', async (type) => {
        const input = find('[data-test-id="input-event-type"] input');
        await input.trigger('click');
        await selectOption(testee.vm.$t(`generic.event-type.${type}`));

        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { type });
        expect(result).toBe(true);
    });

    it.each([EventSignupType.Assignment, EventSignupType.Open])('should patch only event signup type to $0', async (signupType) => {
        const input = find('[data-test-id="input-event-signup-type"] input');
        await input.trigger('click');
        await selectOption(testee.vm.$t(`generic.event-signup-type.${signupType}`));

        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { signupType });
        expect(result).toBe(true);
    });

    it('should patch only event slots', async () => {
        const input = find('[data-test-id="input-event-slots"] input');
        await input.trigger('click');
        await selectOption(1);

        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { slots: mockEvent().slots });
        expect(result).toBe(true);
    });

    it('should show warning when changing slots', async () => {
        const input = find('[data-test-id="input-event-slots"] input');
        await input.trigger('click');
        await selectOption(1);
        const warning = find('[data-test-id="warning-slots-overwrite"]');
        expect(warning.isVisible()).toBe(true);
    });

    it('should patch only event description', async () => {
        const input = find('[data-test-id="input-event-description"] textarea');
        await input.setValue('updated');
        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { description: 'updated' });
        expect(result).toBe(true);
    });

    it('should patch event name and description', async () => {
        const name = find('[data-test-id="input-event-name"] input');
        await name.setValue('updated');
        const description = find('[data-test-id="input-event-description"] textarea');
        await description.setValue('updated');
        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { name: 'updated', description: 'updated' });
        expect(result).toBe(true);
    });

    async function selectOption(option: string | number): Promise<void> {
        const options = findAll('.input-dropdown li');
        if (typeof option === 'string') {
            await options.find((it) => it.text() === option)?.trigger('click');
        } else {
            await options[option].trigger('click');
        }
    }

    async function cancel(): Promise<void> {
        await find('[data-test-id="button-cancel"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }

    async function submit(): Promise<void> {
        await find('[data-test-id="button-submit"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }

    async function open(events: Event[]): Promise<void> {
        // const cmp = testee.findComponent('[data-test-id="event-batch-edit-dialog"]');
        const dialog = testee.getCurrentComponent().exposed as Dialog<Event[], boolean>;
        // don't await here, because the promise will only be resolved when the dialog is closed
        dialog.open(events).then((r) => (result = r));
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }
});
