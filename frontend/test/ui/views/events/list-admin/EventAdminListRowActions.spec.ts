import { ref } from 'vue';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { DOMWrapper, VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { Event } from '@/domain';
import { EventState } from '@/domain';
import type { UseEventExports } from '@/ui/composables/EventExports';
import EventAdminListRowActions from '@/ui/views/events/list-admin/EventAdminListRowActions.vue';
import { mockEvent } from '~/mocks';

const exportTemplates = ref<string[]>([]);

vi.mock('@/ui/composables/EventExports', () => ({
    useEventExports: (): UseEventExports => ({
        templates: exportTemplates,
        loading: Promise.resolve(),
        exportEvents: vi.fn(),
        exportEvent: vi.fn(),
    }),
}));

describe('EventAdminListRowActions.vue', () => {
    let testee: VueWrapper;

    beforeEach(() => {
        exportTemplates.value = ['template a', 'template b'];
    });

    afterEach(() => testee?.unmount());

    it('should show all non-conditional actions when events are provided', () => {
        testee = mountTestee([mockEvent({ state: EventState.Planned })]);

        expect(action('action-edit').exists()).toBe(true);
        expect(action('action-open-for-signup').exists()).toBe(true);
        expect(action('action-publish-crew').exists()).toBe(true);
        expect(action('action-cancel').exists()).toBe(true);
        expect(action('action-delete').exists()).toBe(true);
        expect(action('action-contact-crew').exists()).toBe(true);
        expect(action('action-create-registration').exists()).toBe(true);
    });

    it('should show view action only for a single event', () => {
        testee = mountTestee([mockEvent()]);
        expect(action('action-view').exists()).toBe(true);

        testee.unmount();
        testee = mountTestee([mockEvent({ key: 'event-a' }), mockEvent({ key: 'event-b' })]);
        expect(action('action-view').exists()).toBe(false);
    });

    it('should hide all actions when no events are provided', () => {
        testee = mount(EventAdminListRowActions);

        expect(action('action-view').exists()).toBe(false);
        expect(action('action-edit').exists()).toBe(false);
        expect(action('action-open-for-signup').exists()).toBe(false);
        expect(action('action-publish-crew').exists()).toBe(false);
        expect(action('action-cancel').exists()).toBe(false);
        expect(action('action-delete').exists()).toBe(false);
        expect(action('action-contact-crew').exists()).toBe(false);
        expect(action('action-create-registration').exists()).toBe(false);
        expect(testee.findAll('[data-test-id="action-export"]')).toHaveLength(0);
    });

    it('should show export actions when templates are available', () => {
        testee = mountTestee([mockEvent()]);

        expect(testee.findAll('[data-test-id="action-export"]')).toHaveLength(2);
    });

    it('should hide export actions when no templates are available', () => {
        exportTemplates.value = [];
        testee = mountTestee([mockEvent()]);

        expect(testee.findAll('[data-test-id="action-export"]')).toHaveLength(0);
    });

    it('should disable open-for-signup action when no draft event is present', () => {
        testee = mountTestee([mockEvent({ state: EventState.Planned })]);

        expect(action('action-open-for-signup').classes()).toContain('disabled');
    });

    it('should enable open-for-signup action when at least one draft event is present', () => {
        testee = mountTestee([mockEvent({ state: EventState.Planned }), mockEvent({ state: EventState.Draft })]);

        expect(action('action-open-for-signup').classes()).not.toContain('disabled');
    });

    it('should disable publish action when no open-for-signup event is present', () => {
        testee = mountTestee([mockEvent({ state: EventState.Planned })]);

        expect(action('action-publish-crew').classes()).toContain('disabled');
    });

    it('should enable publish action when at least one open-for-signup event is present', () => {
        testee = mountTestee([mockEvent({ state: EventState.Planned }), mockEvent({ state: EventState.OpenForSignup })]);

        expect(action('action-publish-crew').classes()).not.toContain('disabled');
    });

    function mountTestee(events: Event[]): VueWrapper {
        return mount(EventAdminListRowActions, { props: { events } });
    }

    function action(testId: string): DOMWrapper<HTMLElement> {
        return testee.find(`[data-test-id="${testId}"]`);
    }
});
