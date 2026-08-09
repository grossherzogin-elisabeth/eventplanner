import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { PositionUseCase } from '@/application';
import { usePositionUseCase } from '@/application';
import { EventState, SlotCriticality } from '@/domain';
import EventStateBadge from '@/ui/views/events/list-admin/EventStateBadge.vue';
import { mockEvent, mockPositions, mockSlotCaptain, mockSlotDeckhand, mockSlotEngineer, mockSlotMate } from '~/mocks';

describe('EventStateBadge.vue', () => {
    let testee: VueWrapper;
    let positionUseCase: PositionUseCase;

    beforeEach(() => {
        positionUseCase = usePositionUseCase();
        vi.spyOn(positionUseCase, 'getPositions').mockResolvedValue(mockPositions());
        testee = mount(EventStateBadge, {
            props: { event: mockEvent() },
            global: {
                stubs: {
                    VTooltip: {
                        template: '<div><slot /><slot name="tooltip" /></div>',
                    },
                },
            },
        });
    });

    afterEach(() => testee?.unmount());

    it('should render draft state badge', async () => {
        await testee.setProps({ event: mockEvent({ state: EventState.Draft }) });

        await expect.poll(() => testee.text()).toContain(testee.vm.$t('domain.event-state.draft'));
        expect(testee.find('.status-badge').classes()).toContain('neutral');
        expect(testee.find('.fa-compass-drafting').exists()).toBe(true);
    });

    it('should render open for signup state badge', async () => {
        await testee.setProps({ event: mockEvent({ state: EventState.OpenForSignup }) });

        await expect.poll(() => testee.text()).toContain(testee.vm.$t('domain.event-state.open-for-signup'));
        expect(testee.find('.status-badge').classes()).toContain('neutral');
        expect(testee.find('.fa-people-group').exists()).toBe(true);
    });

    it('should render canceled state badge', async () => {
        await testee.setProps({ event: mockEvent({ state: EventState.Canceled }) });

        await expect.poll(() => testee.text()).toContain(testee.vm.$t('domain.event-state.canceled'));
        expect(testee.find('.status-badge').classes()).toContain('error');
        expect(testee.find('.fa-ban').exists()).toBe(true);
    });

    it('should render missing crew state when required slots are open', async () => {
        await testee.setProps({
            event: mockEvent({
                state: EventState.Planned,
                slots: [mockSlotCaptain(), mockSlotEngineer(), mockSlotMate()],
            }),
        });

        await expect.poll(() => testee.text()).toContain(testee.vm.$t('views.event-admin-list.state.missing-crew'));
        expect(testee.find('.status-badge').classes()).toContain('warning');
        expect(testee.find('.fa-warning').exists()).toBe(true);
    });

    it('should render open slots state when only optional slots are open', async () => {
        await testee.setProps({
            event: mockEvent({
                state: EventState.Planned,
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: 'reg-1' }),
                    mockSlotEngineer({ assignedRegistrationKey: 'reg-2' }),
                    mockSlotMate({ assignedRegistrationKey: 'reg-3' }),
                    mockSlotDeckhand({ criticality: SlotCriticality.Optional }),
                ],
            }),
        });

        await expect.poll(() => testee.text()).toContain(testee.vm.$t('domain.event-state.open-slots'));
        expect(testee.find('.status-badge').classes()).toContain('info');
        expect(testee.find('.fa-info-circle').exists()).toBe(true);
    });

    it('should render full state when no slots are open', async () => {
        await testee.setProps({
            event: mockEvent({
                state: EventState.Planned,
                slots: [
                    mockSlotCaptain({ assignedRegistrationKey: 'reg-1' }),
                    mockSlotEngineer({ assignedRegistrationKey: 'reg-2' }),
                    mockSlotMate({ assignedRegistrationKey: 'reg-3' }),
                    mockSlotDeckhand({ assignedRegistrationKey: 'reg-4' }),
                ],
            }),
        });

        await expect.poll(() => testee.text()).toContain(testee.vm.$t('domain.event-state.full'));
        expect(testee.find('.status-badge').classes()).toContain('success');
        expect(testee.find('.fa-check-circle').exists()).toBe(true);
    });

    it('should render tooltip details for required and optional open positions', async () => {
        await testee.setProps({
            event: mockEvent({
                state: EventState.Planned,
                slots: [mockSlotCaptain(), mockSlotCaptain({ key: 'slot-captain-2' }), mockSlotDeckhand()],
            }),
        });

        await expect.poll(() => testee.text()).toContain(testee.vm.$t('views.event-admin-list.state.missing-crew'));
        expect(testee.text()).toContain(testee.vm.$t('views.event-admin-list.state.free-slots-for'));
        expect(testee.text()).toContain('Captain');
        expect(testee.text()).toContain('Deckhand');
        expect(testee.text()).toContain('2');
        expect(testee.text()).toContain('1');
    });
});
