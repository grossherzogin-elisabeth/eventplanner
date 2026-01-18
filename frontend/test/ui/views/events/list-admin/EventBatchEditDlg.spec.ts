import { nextTick } from 'vue';
import type { MockInstance } from 'vitest';
import { afterAll } from 'vitest';
import { beforeAll } from 'vitest';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { HttpResponse, http } from 'msw';
import { useEventAdministrationUseCase } from '@/application';
import type { Event } from '@/domain';
import { EventSignupType } from '@/domain';
import { EventType } from '@/domain';
import { EventState } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import EventBatchEditDlg from '@/ui/views/events/list-admin/EventBatchEditDlg.vue';
import { mockEvent, server } from '~/mocks';
import { selectDropdownOption } from '~/utils';

describe('EventBatchEditDlg.vue', () => {
    let testee: VueWrapper;
    let result: boolean | undefined = undefined;
    let updateFunc: MockInstance;
    let eventA = mockEvent({ key: 'a' });
    let eventB = mockEvent({ key: 'b' });

    beforeAll(() => {
        vi.useFakeTimers();
    });

    beforeEach(() => {
        server.use(http.get('/api/v1/events', () => HttpResponse.json([eventA, eventB], { status: 200 })));
        server.use(http.get('/api/v1/events/a', () => HttpResponse.json(eventA, { status: 200 })));
        server.use(http.get('/api/v1/events/b', () => HttpResponse.json(eventB, { status: 200 })));
    });

    afterAll(() => {
        vi.useRealTimers();
    });

    beforeEach(async () => {
        result = undefined;
        updateFunc = vi.spyOn(useEventAdministrationUseCase(), 'updateEvents');
        eventA = mockEvent({ key: 'a' });
        eventB = mockEvent({ key: 'b' });
        testee = mount(EventBatchEditDlg, { global: { stubs: { teleport: true } } });
        await open([eventA, eventB]);
    });

    it('should open dialog', async () => {
        const dialog = testee.find('[data-test-id="event-batch-edit-dialog"]');
        expect(dialog.isVisible()).toBe(true);
    });

    it('should close dialog on cancel', async () => {
        await cancel();
        expect(updateFunc).not.toHaveBeenCalled();
        await expect.poll(() => result).toBe(false);
    });

    it('should close dialog on submit without input', async () => {
        await submit();
        expect(updateFunc).not.toHaveBeenCalled();
        await expect.poll(() => result).toBe(false);
    });

    it.each([EventState.Draft, EventState.OpenForSignup, EventState.Planned, EventState.Canceled])(
        'should patch only event status to $0',
        async (state) => {
            await testee.find('[data-test-id="input-event-status"] input').trigger('click');
            await selectDropdownOption(testee, testee.vm.$t(`generic.event-state.${state}`));

            await submit();
            expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { state });
            await expect.poll(() => result).toBe(true);
        }
    );

    it('should patch only event name', async () => {
        await testee.find('[data-test-id="input-event-name"] input').setValue('updated');
        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { name: 'updated' });
        await expect.poll(() => result).toBe(true);
    });

    it.each([
        EventType.SingleDayEvent,
        EventType.WeekendEvent,
        EventType.MultiDayEvent,
        EventType.WorkEvent,
        EventType.TrainingEvent,
        EventType.Other,
    ])('should patch only event type to $0', async (type) => {
        await testee.find('[data-test-id="input-event-type"] input').trigger('click');
        await selectDropdownOption(testee, testee.vm.$t(`generic.event-type.${type}`));

        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { type });
        await expect.poll(() => result).toBe(true);
    });

    it.each([EventSignupType.Assignment, EventSignupType.Open])('should patch only event signup type to $0', async (signupType) => {
        await testee.find('[data-test-id="input-event-signup-type"] input').trigger('click');
        await selectDropdownOption(testee, testee.vm.$t(`generic.event-signup-type.${signupType}`));

        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { signupType });
        await expect.poll(() => result).toBe(true);
    });

    it('should patch only event slots', async () => {
        await expect.poll(() => testee.find('[data-test-id="input-event-slots"]').element.classList).not.toContain('loading');
        await testee.find('[data-test-id="input-event-slots"] input').trigger('click');
        await selectDropdownOption(testee, 1);

        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { slots: mockEvent().slots });
        await expect.poll(() => result).toBe(true);
    });

    it('should show warning when changing slots', async () => {
        await expect.poll(() => testee.find('[data-test-id="input-event-slots"]').element.classList).not.toContain('loading');
        await testee.find('[data-test-id="input-event-slots"] input').trigger('click');
        await selectDropdownOption(testee, 1);
        const warning = testee.find('[data-test-id="warning-slots-overwrite"]');
        expect(warning.isVisible()).toBe(true);
    });

    it('should patch only event description', async () => {
        await testee.find('[data-test-id="input-event-description"] textarea').setValue('updated');
        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { description: 'updated' });
        await expect.poll(() => result).toBe(true);
    });

    it('should patch event name and description', async () => {
        await testee.find('[data-test-id="input-event-name"] input').setValue('updated');
        await testee.find('[data-test-id="input-event-description"] textarea').setValue('updated');
        await submit();
        expect(updateFunc).toHaveBeenCalledExactlyOnceWith(['a', 'b'], { name: 'updated', description: 'updated' });
        await expect.poll(() => result).toBe(true);
    });

    async function cancel(): Promise<void> {
        await testee.find('[data-test-id="button-cancel"]').trigger('click');
        vi.runAllTimers();
        await nextTick();
    }

    async function submit(): Promise<void> {
        await testee.find('[data-test-id="button-submit"]').trigger('click');
        vi.runAllTimers();
        await nextTick();
    }

    async function open(events: Event[]): Promise<void> {
        const dialog = testee.getCurrentComponent().exposed as Dialog<Event[], boolean>;
        // don't await here, because the promise will only be resolved when the dialog is closed
        dialog.open(events).then((r) => (result = r));
        vi.runAllTimers();
        await nextTick();
    }
});
