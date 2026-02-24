import { nextTick } from 'vue';
import type { RouteLocationNormalizedLoadedGeneric } from 'vue-router';
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { Event } from '@/domain';
import type { Sheet } from '@/ui/components/common';
import EventDetailsCard from '@/ui/components/events/EventDetailsCard.vue';
import EventLocationsCard from '@/ui/components/events/EventLocationsCard.vue';
import EventStateBanner from '@/ui/components/events/EventStateBanner.vue';
import EventDetailsSheet from '@/ui/components/sheets/EventDetailsSheet.vue';
import { usePositions } from '@/ui/composables/Positions.ts';
import { mockEvent, mockRouter } from '~/mocks';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRoute: (): RouteLocationNormalizedLoadedGeneric => router.currentRoute.value,
}));

describe('EventDetailsSheet.vue', () => {
    let testee: VueWrapper;
    let event = mockEvent();

    beforeAll(() => {
        vi.useFakeTimers();
    });

    afterAll(() => {
        vi.useRealTimers();
    });

    beforeEach(async () => {
        event = mockEvent();
        testee = mount(EventDetailsSheet, { props: {} });
        await usePositions().loading;
    });

    it('should render event details', async () => {
        await open(event);
        expect(testee.findComponent(EventDetailsCard).exists()).toBe(true);
    });

    it('should render event locations', async () => {
        await open(event);
        expect(testee.findComponent(EventLocationsCard).exists()).toBe(true);
    });

    it('should render event state banner', async () => {
        await open(event);
        expect(testee.findComponent(EventStateBanner).exists()).toBe(true);
    });

    async function open(event: Event): Promise<void> {
        const sheet = testee.getCurrentComponent().exposed as Sheet<Event>;
        // don't await here, because the promise will only be resolved when the dialog is closed
        sheet.open(event).then(() => {
            console.log('Sheet closed');
        });
        vi.runAllTimers();
        await nextTick();
    }
});
