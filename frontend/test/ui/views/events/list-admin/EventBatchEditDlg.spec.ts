import { beforeEach, describe, expect, it } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { wait } from '@/common';
import type { Event } from '@/domain';
import type { Dialog } from '@/ui/components/common';
import EventBatchEditDlg from '@/ui/views/events/list-admin/EventBatchEditDlg.vue';
import { mockEvent } from '~/mocks';
import { find } from '~/utils';

describe('EventBatchEditDlg.vue', () => {
    let testee: VueWrapper;

    beforeEach(async () => {
        testee = mount(EventBatchEditDlg);
    });

    it('should open dialog', async () => {
        await open([mockEvent()]);
        const dialog = find('[data-test-id="event-batch-edit-dialog"]');
        expect(dialog.exists()).toBe(true);
    });

    async function open(events: Event[]): Promise<void> {
        // const cmp = testee.findComponent('[data-test-id="event-batch-edit-dialog"]');
        const dialog = testee.getCurrentComponent().exposed as Dialog<Event[], boolean>;
        dialog.open(events); // don't await here, because the promise will only be resolved when the dialog is closed
        await wait(1); // wait 1ms to make sure all network requests made after opening
    }
});
