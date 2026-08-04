import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import { useErrorHandlingService, useEventAdministrationUseCase } from '@/application';
import type { Event, Registration, ResolvedRegistrationSlot, Slot } from '@/domain';
import { RegistrationSlotState, useEventService } from '@/domain';
import TabCrewEditor from '@/ui/views/events/edit/tabs/TabCrewEditor.vue';
import {
    mockEvent,
    mockPositionCaptain,
    mockPositionDeckhand,
    mockRegistrationCaptain,
    mockRegistrationDeckhand,
    mockRegistrationGuest,
    mockSlotCaptain,
    mockSlotDeckhand,
    mockUserCaptain,
    mockUserDeckhand,
} from '~/mocks';
import { copyOf, stubs } from '~/utils';

const RegistrationRowStub = defineComponent({
    name: 'RegistrationRow',
    emits: [
        'add-to-crew',
        'remove-from-crew',
        'cancel-registration',
        'edit-slot',
        'delete-slot',
        'edit-registration',
        'dragstart',
        'dragend',
    ],
    template: `
        <div>
            <button data-test-id="row-add-to-crew" @click="$emit('add-to-crew')" />
            <button data-test-id="row-remove-from-crew" @click="$emit('remove-from-crew')" />
            <button data-test-id="row-cancel-registration" @click="$emit('cancel-registration')" />
            <button data-test-id="row-edit-slot" @click="$emit('edit-slot')" />
            <button data-test-id="row-delete-slot" @click="$emit('delete-slot')" />
            <button data-test-id="row-edit-registration" @click="$emit('edit-registration')" />
        </div>
    `,
});

describe('TabCrewEditor.vue', () => {
    let testee: VueWrapper;
    let event: Event;
    let crew: ResolvedRegistrationSlot[];
    let waitinglist: ResolvedRegistrationSlot[];
    let assignUserToSlotSpy: ReturnType<typeof vi.spyOn>;
    let assignGuestToSlotSpy: ReturnType<typeof vi.spyOn>;
    let assignRegistrationToImplicitSlotSpy: ReturnType<typeof vi.spyOn>;
    let unassignSlotSpy: ReturnType<typeof vi.spyOn>;
    let getOpenSlotsSpy: ReturnType<typeof vi.spyOn>;
    let cancelUserRegistrationSpy: ReturnType<typeof vi.spyOn>;
    let cancelGuestRegistrationSpy: ReturnType<typeof vi.spyOn>;
    let updateSlotSpy: ReturnType<typeof vi.spyOn>;
    let removeSlotSpy: ReturnType<typeof vi.spyOn>;
    let findRegistrationSpy: ReturnType<typeof vi.spyOn>;
    let handleErrorSpy: ReturnType<typeof vi.spyOn>;
    const slotDialogOpenSpy = vi.fn(async () => undefined as Slot | undefined);
    const registrationDialogOpenSpy = vi.fn(async () => undefined as Registration | undefined);

    function mountTestee(): void {
        testee = mount(TabCrewEditor, {
            props: {
                event,
                crew,
                waitinglist,
                'onUpdate:event': (updatedEvent: Event) => testee.setProps({ event: updatedEvent }),
            },
            global: {
                stubs: {
                    RegistrationRow: RegistrationRowStub,
                    SlotEditDlg: stubs('SlotEditDlgStub', slotDialogOpenSpy),
                    RegistrationEditDlg: stubs('RegistrationEditDlgStub', registrationDialogOpenSpy),
                },
            },
        });
    }

    beforeEach(() => {
        const eventAdministrationUseCase = useEventAdministrationUseCase();
        const eventService = useEventService();
        const errorHandlingService = useErrorHandlingService();

        assignUserToSlotSpy = vi.spyOn(eventAdministrationUseCase, 'assignUserToSlot');
        assignGuestToSlotSpy = vi.spyOn(eventAdministrationUseCase, 'assignGuestToSlot');
        assignRegistrationToImplicitSlotSpy = vi.spyOn(eventAdministrationUseCase, 'assignRegistrationToImplicitSlot');
        unassignSlotSpy = vi.spyOn(eventAdministrationUseCase, 'unassignSlot');
        getOpenSlotsSpy = vi.spyOn(eventService, 'getOpenSlots');
        cancelUserRegistrationSpy = vi.spyOn(eventService, 'cancelUserRegistration');
        cancelGuestRegistrationSpy = vi.spyOn(eventService, 'cancelGuestRegistration');
        updateSlotSpy = vi.spyOn(eventService, 'updateSlot');
        removeSlotSpy = vi.spyOn(eventService, 'removeSlot');
        findRegistrationSpy = vi.spyOn(eventService, 'findRegistration');
        handleErrorSpy = vi.spyOn(errorHandlingService, 'handleError');

        event = mockEvent({
            slots: [mockSlotDeckhand({ key: 'slot-deckhand', assignedRegistrationKey: undefined })],
            registrations: [mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' })],
        });
        crew = [];
        waitinglist = [];

        assignUserToSlotSpy.mockImplementation(async (updatedEvent: Event) => updatedEvent);
        assignGuestToSlotSpy.mockImplementation(async (updatedEvent: Event) => updatedEvent);
        assignRegistrationToImplicitSlotSpy.mockImplementation(async (updatedEvent: Event) => updatedEvent);
        unassignSlotSpy.mockImplementation(async (updatedEvent: Event) => updatedEvent);
        cancelUserRegistrationSpy.mockImplementation((updatedEvent: Event) => updatedEvent);
        cancelGuestRegistrationSpy.mockImplementation((updatedEvent: Event) => updatedEvent);
        updateSlotSpy.mockImplementation((updatedEvent: Event) => updatedEvent);
        removeSlotSpy.mockImplementation((updatedEvent: Event) => updatedEvent);
        getOpenSlotsSpy.mockReturnValue(event.slots);
    });

    it('should assign user registration to matching open slot', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Deck Hand',
            state: RegistrationSlotState.WAITING_LIST,
            position: mockPositionDeckhand(),
            registration: mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' }),
            user: mockUserDeckhand({ key: 'user-deckhand' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        waitinglist = [aggregate];
        mountTestee();

        await testee.find('[data-test-id="row-add-to-crew"]').trigger('click');

        expect(assignUserToSlotSpy).toHaveBeenCalledTimes(1);
        expect(assignUserToSlotSpy).toHaveBeenCalledWith(copyOf(event), aggregate.user, 'slot-deckhand');
        expect(testee.emitted('update:event')?.length).toBe(1);
    });

    it('should assign guest registration to matching open slot', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Guest Name',
            state: RegistrationSlotState.WAITING_LIST,
            position: mockPositionDeckhand(),
            registration: mockRegistrationGuest({ key: 'reg-guest', name: 'Guest Name' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        waitinglist = [aggregate];
        mountTestee();

        await testee.find('[data-test-id="row-add-to-crew"]').trigger('click');

        expect(assignGuestToSlotSpy).toHaveBeenCalledTimes(1);
        expect(assignGuestToSlotSpy).toHaveBeenCalledWith(copyOf(event), 'Guest Name', 'slot-deckhand');
        expect(testee.emitted('update:event')?.length).toBe(1);
    });

    it('should offer retry and assign implicit slot when no open slot exists', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Captain',
            state: RegistrationSlotState.WAITING_LIST,
            position: mockPositionCaptain(),
            registration: mockRegistrationCaptain({ key: 'reg-captain', userKey: 'user-captain' }),
            user: mockUserCaptain({ key: 'user-captain' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        waitinglist = [aggregate];
        getOpenSlotsSpy.mockReturnValue([]);
        mountTestee();

        await testee.find('[data-test-id="row-add-to-crew"]').trigger('click');

        expect(handleErrorSpy).toHaveBeenCalledTimes(1);
        expect(assignRegistrationToImplicitSlotSpy).not.toHaveBeenCalled();
        expect(testee.emitted('update:event')).toBeUndefined();

        const retry = handleErrorSpy.mock.calls[0][0].retry;
        await retry();

        expect(assignRegistrationToImplicitSlotSpy).toHaveBeenCalledWith(copyOf(event), aggregate.registration);
        expect(testee.emitted('update:event')?.length).toBe(1);
    });

    it('should do nothing when add-to-crew aggregate has no registration', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Open Slot',
            state: RegistrationSlotState.OPEN,
            position: mockPositionDeckhand(),
            slot: mockSlotDeckhand({ key: 'slot-open' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        waitinglist = [aggregate];
        mountTestee();

        await testee.find('[data-test-id="row-add-to-crew"]').trigger('click');

        expect(assignUserToSlotSpy).not.toHaveBeenCalled();
        expect(assignGuestToSlotSpy).not.toHaveBeenCalled();
        expect(assignRegistrationToImplicitSlotSpy).not.toHaveBeenCalled();
        expect(testee.emitted('update:event')).toBeUndefined();
    });

    it('should unassign slot when removing from crew', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Deck Hand',
            state: RegistrationSlotState.ASSIGNED,
            position: mockPositionDeckhand(),
            registration: mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' }),
            user: mockUserDeckhand({ key: 'user-deckhand' }),
            slot: mockSlotDeckhand({ key: 'slot-assigned', assignedRegistrationKey: 'reg-deckhand' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        crew = [aggregate];
        mountTestee();

        await testee.find('[data-test-id="row-remove-from-crew"]').trigger('click');

        expect(unassignSlotSpy).toHaveBeenCalledTimes(1);
        expect(unassignSlotSpy).toHaveBeenCalledWith(copyOf(event), 'slot-assigned');
        expect(testee.emitted('update:event')?.length).toBe(1);
    });

    it('should cancel user registration from crew', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Deck Hand',
            state: RegistrationSlotState.ASSIGNED,
            position: mockPositionDeckhand(),
            registration: mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' }),
            user: mockUserDeckhand({ key: 'user-deckhand' }),
            slot: mockSlotDeckhand({ key: 'slot-assigned', assignedRegistrationKey: 'reg-deckhand' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        crew = [aggregate];
        mountTestee();

        await testee.find('[data-test-id="row-cancel-registration"]').trigger('click');

        expect(cancelUserRegistrationSpy).toHaveBeenCalledTimes(1);
        expect(cancelUserRegistrationSpy).toHaveBeenCalledWith(copyOf(event), 'user-deckhand');
        expect(testee.emitted('update:event')?.length).toBe(1);
    });

    it('should cancel guest registration from waiting list', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Guest Name',
            state: RegistrationSlotState.WAITING_LIST,
            position: mockPositionDeckhand(),
            registration: mockRegistrationGuest({ key: 'reg-guest', name: 'Guest Name' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        waitinglist = [aggregate];
        mountTestee();

        await testee.find('[data-test-id="row-cancel-registration"]').trigger('click');

        expect(cancelGuestRegistrationSpy).toHaveBeenCalledTimes(1);
        expect(cancelGuestRegistrationSpy).toHaveBeenCalledWith(copyOf(event), 'Guest Name');
        expect(testee.emitted('update:event')?.length).toBe(1);
    });

    it('should update event after editing slot', async () => {
        const slot = mockSlotCaptain({ key: 'slot-captain' });
        const editedSlot = mockSlotCaptain({ key: 'slot-captain', positionName: 'Edited Captain' });
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Captain',
            state: RegistrationSlotState.ASSIGNED,
            position: mockPositionCaptain(),
            registration: mockRegistrationCaptain({ key: 'reg-captain', userKey: 'user-captain' }),
            user: mockUserCaptain({ key: 'user-captain' }),
            slot,
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        const updatedEvent = mockEvent({ key: 'event-updated' });
        crew = [aggregate];
        slotDialogOpenSpy.mockReturnValue(Promise.resolve(editedSlot));
        updateSlotSpy.mockReturnValue(updatedEvent);
        mountTestee();

        await testee.find('[data-test-id="row-edit-slot"]').trigger('click');

        expect(slotDialogOpenSpy).toHaveBeenCalledWith(slot);
        expect(updateSlotSpy).toHaveBeenCalledWith(copyOf(event), editedSlot);
        expect(testee.emitted('update:event')?.[0]?.[0]).toBe(updatedEvent);
    });

    it('should delete unassigned slot', async () => {
        const removable: ResolvedRegistrationSlot = {
            name: 'Open Captain',
            state: RegistrationSlotState.OPEN,
            position: mockPositionCaptain(),
            slot: mockSlotCaptain({ key: 'slot-open', assignedRegistrationKey: undefined }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        crew = [removable];
        mountTestee();

        const deleteButtons = testee.findAll('[data-test-id="row-delete-slot"]');
        await deleteButtons[0].trigger('click');

        expect(removeSlotSpy).toHaveBeenCalledTimes(1);
        expect(removeSlotSpy).toHaveBeenCalledWith(copyOf(event), removable.slot);
        expect(testee.emitted('update:event')?.length).toBe(1);
    });

    it('should not delete slot with assigned user', async () => {
        const protectedSlot: ResolvedRegistrationSlot = {
            name: 'Assigned Deck Hand',
            state: RegistrationSlotState.ASSIGNED,
            position: mockPositionDeckhand(),
            registration: mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' }),
            user: mockUserDeckhand({ key: 'user-deckhand' }),
            slot: mockSlotDeckhand({ key: 'slot-assigned', assignedRegistrationKey: 'reg-deckhand' }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        crew = [protectedSlot];
        mountTestee();

        const deleteButtons = testee.findAll('[data-test-id="row-delete-slot"]');
        await deleteButtons[0].trigger('click');

        expect(removeSlotSpy).not.toHaveBeenCalled();
        expect(testee.emitted('update:event')).toBeUndefined();
    });

    it('should update registration fields when editing registration', async () => {
        const registration = mockRegistrationGuest({
            key: 'reg-guest',
            name: 'Old Name',
            note: 'old note',
            positionKey: mockPositionDeckhand().key,
        });
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Old Name',
            state: RegistrationSlotState.WAITING_LIST,
            position: mockPositionDeckhand(),
            registration,
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        event = mockEvent({ registrations: [registration] });
        waitinglist = [aggregate];
        registrationDialogOpenSpy.mockReturnValue(
            Promise.resolve({
                ...registration,
                name: 'New Name',
                note: 'new note',
                positionKey: mockPositionCaptain().key,
            })
        );
        findRegistrationSpy.mockReturnValue(registration);
        mountTestee();

        await testee.find('[data-test-id="row-edit-registration"]').trigger('click');

        expect(registrationDialogOpenSpy).toHaveBeenCalledWith(registration);
        expect(registration.name).toBe('New Name');
        expect(registration.note).toBe('new note');
        expect(registration.positionKey).toBe(mockPositionCaptain().key);
        expect(testee.emitted('update:event')).toBeUndefined();
    });
});
