import { nextTick } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useEventAdministrationUseCase } from '@/application';
import { wait } from '@/common';
import type { Event } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import EventBatchEditDlg from '@/ui/views/events/list-admin/EventBatchEditDlg.vue';
import { mockEvent } from '~/mocks';
import { find } from '~/utils';

describe('EventBatchEditDlg.vue', () => {
    let testee: VueWrapper;
    let result: boolean | undefined = undefined;

    beforeEach(async () => {
        result = undefined;
        testee = mount(EventBatchEditDlg);
    });

    it('should open dialog', async () => {
        await open([mockEvent()]);
        const dialog = find('[data-test-id="event-batch-edit-dialog"]');
        expect(dialog.exists()).toBe(true);
        expect(dialog.isVisible()).toBe(true);
    });

    it('should close dialog on cancel', async () => {
        const spy = vi.spyOn(useEventAdministrationUseCase(), 'updateEvents');
        await open([mockEvent()]);
        await find('[data-test-id="button-cancel"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        expect(spy).not.toHaveBeenCalled();
        expect(result).toBe(false);
    });

    it('should close dialog on submit without input', async () => {
        const spy = vi.spyOn(useEventAdministrationUseCase(), 'updateEvents');
        await open([mockEvent()]);
        await find('[data-test-id="button-submit"]').trigger('click');
        await nextTick(); // wait for result to be passed to variable
        expect(spy).not.toHaveBeenCalled();
        expect(result).toBe(false);
    });

    it('should patch only event name', async () => {
        const spy = vi.spyOn(useEventAdministrationUseCase(), 'updateEvents');

        await open([mockEvent()]);
        const input = find('[data-test-id="input-event-name"] input');
        await input.setValue('Updated event name');

        await find('[data-test-id="button-submit"]').trigger('click');
        expect(spy).toHaveBeenCalledExactlyOnceWith(['example-event'], { name: 'Updated event name' });
        await nextTick(); // wait for result to be passed to variable
        await wait(1); // wait 1ms to make sure all network requests made after opening
        expect(result).toBe(true);
    });

    async function open(events: Event[]): Promise<void> {
        // const cmp = testee.findComponent('[data-test-id="event-batch-edit-dialog"]');
        const dialog = testee.getCurrentComponent().exposed as Dialog<Event[], boolean>;
        // don't await here, because the promise will only be resolved when the dialog is closed
        dialog.open(events).then((r) => (result = r));
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }
});
