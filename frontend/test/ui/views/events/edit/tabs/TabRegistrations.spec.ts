import { defineComponent } from 'vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { VueWrapper } from '@vue/test-utils';
import { mount } from '@vue/test-utils';
import type { Router } from 'vue-router';
import { useErrorHandlingService } from '@/application';
import type { Event, Registration, ResolvedRegistrationSlot, Slot } from '@/domain';
import { RegistrationSlotState } from '@/domain';
import TabRegistrations from '@/ui/views/events/edit/tabs/TabRegistrations.vue';
import {
    mockEvent,
    mockPositionCaptain,
    mockPositionDeckhand,
    mockRegistrationCaptain,
    mockRegistrationDeckhand,
    mockRegistrationGuest,
    mockSlotCaptain,
    mockSlotDeckhand,
    mockRouter,
    mockUserCaptain,
    mockUserDeckhand,
} from '~/mocks';
import { copyOf, stubs } from '~/utils';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

const RegistrationsTableStub = defineComponent({
    name: 'RegistrationsTable',
    props: {
        registrations: {
            type: Array<ResolvedRegistrationSlot>,
            required: true,
        },
    },
    emits: ['delete-registration', 'edit-registration', 'edit-slot', 'delete-slot', 'add-to-crew', 'remove-from-crew'],
    template: `
        <div>
            <button data-test-id="table-add-to-crew" @click="$emit('add-to-crew', registrations[0])" />
            <button data-test-id="table-remove-from-crew" @click="$emit('remove-from-crew', registrations[0])" />
            <button data-test-id="table-delete-registration" @click="$emit('delete-registration', registrations[0])" />
            <button data-test-id="table-edit-slot" @click="$emit('edit-slot', registrations[0])" />
            <button data-test-id="table-delete-slot" @click="$emit('delete-slot', registrations[0])" />
            <button data-test-id="table-edit-registration" @click="$emit('edit-registration', registrations[0])" />
        </div>
    `,
});

describe('TabRegistrations.vue', () => {
    let testee: VueWrapper;
    let event: Event;
    let crew: ResolvedRegistrationSlot[];
    let waitinglist: ResolvedRegistrationSlot[];
    let handleErrorSpy: ReturnType<typeof vi.spyOn>;
    const slotDialogOpenSpy = vi.fn(async () => undefined as Slot | undefined);
    const registrationDialogOpenSpy = vi.fn(async () => undefined as Registration | undefined);

    function mountTestee(): void {
        testee = mount(TabRegistrations, {
            props: {
                event,
                crew,
                waitinglist,
                'onUpdate:event': (updatedEvent: Event) => testee.setProps({ event: updatedEvent }),
            },
            global: {
                stubs: {
                    RegistrationsTable: RegistrationsTableStub,
                    SlotEditDlg: stubs('SlotEditDlgStub', slotDialogOpenSpy),
                    RegistrationEditDlg: stubs('RegistrationEditDlgStub', registrationDialogOpenSpy),
                },
            },
        });
    }

    function emittedEvent(index: number = 0): Event | undefined {
        return testee.emitted('update:event')?.[index]?.[0] as Event | undefined;
    }

    beforeEach(() => {
        const errorHandlingService = useErrorHandlingService();
        handleErrorSpy = vi.spyOn(errorHandlingService, 'handleError');
        slotDialogOpenSpy.mockReset();
        slotDialogOpenSpy.mockResolvedValue(undefined);
        registrationDialogOpenSpy.mockReset();
        registrationDialogOpenSpy.mockResolvedValue(undefined);

        event = mockEvent({
            slots: [mockSlotDeckhand({ key: 'slot-deckhand', assignedRegistrationKey: undefined })],
            registrations: [mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' })],
        });
        crew = [];
        waitinglist = [];
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

        await testee.findAll('[data-test-id="table-add-to-crew"]')[1].trigger('click');

        await expect.poll(() => testee.emitted('update:event')?.length).toBe(1);
        const updatedEvent = emittedEvent();
        expect(updatedEvent?.slots.find((it) => it.key === 'slot-deckhand')?.assignedRegistrationKey).toBe('reg-deckhand');
        expect(updatedEvent?.assignedUserCount).toBe(1);
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
        event = mockEvent({
            slots: [mockSlotDeckhand({ key: 'slot-deckhand', assignedRegistrationKey: undefined })],
            registrations: [mockRegistrationGuest({ key: 'reg-guest', name: 'Guest Name' })],
        });
        waitinglist = [aggregate];
        mountTestee();

        await testee.findAll('[data-test-id="table-add-to-crew"]')[1].trigger('click');

        await expect.poll(() => testee.emitted('update:event')?.length).toBe(1);
        const updatedEvent = emittedEvent();
        expect(updatedEvent?.slots.find((it) => it.key === 'slot-deckhand')?.assignedRegistrationKey).toBe('reg-guest');
        expect(updatedEvent?.assignedUserCount).toBe(1);
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
        event = mockEvent({
            slots: [mockSlotDeckhand({ key: 'slot-deckhand', assignedRegistrationKey: undefined })],
            registrations: [mockRegistrationCaptain({ key: 'reg-captain', userKey: 'user-captain' })],
        });
        waitinglist = [aggregate];
        mountTestee();

        await testee.findAll('[data-test-id="table-add-to-crew"]')[1].trigger('click');

        expect(handleErrorSpy).toHaveBeenCalledTimes(1);
        expect(testee.emitted('update:event')).toBeUndefined();

        const retry = handleErrorSpy.mock.calls[0][0].retry;
        await retry();

        await expect.poll(() => testee.emitted('update:event')?.length).toBe(1);
        const updatedEvent = emittedEvent();
        const implicitSlot = updatedEvent?.slots.find((it) => it.implicit === true);
        expect(implicitSlot?.assignedRegistrationKey).toBe('reg-captain');
        expect(implicitSlot?.positionKeys).toEqual([aggregate.registration!.positionKey]);
        expect(updatedEvent?.assignedUserCount).toBe(1);
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
        event = mockEvent({
            slots: [mockSlotDeckhand({ key: 'slot-assigned', assignedRegistrationKey: 'reg-deckhand' })],
            registrations: [mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' })],
        });
        crew = [aggregate];
        mountTestee();

        await testee.findAll('[data-test-id="table-remove-from-crew"]')[0].trigger('click');

        await expect.poll(() => testee.emitted('update:event')?.length).toBe(1);
        const updatedEvent = emittedEvent();
        expect(updatedEvent?.slots.find((it) => it.key === 'slot-assigned')?.assignedRegistrationKey).toBeUndefined();
    });

    it('should delete user registration and unassign slot first', async () => {
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
        event = mockEvent({
            slots: [mockSlotDeckhand({ key: 'slot-assigned', assignedRegistrationKey: 'reg-deckhand' })],
            registrations: [mockRegistrationDeckhand({ key: 'reg-deckhand', userKey: 'user-deckhand' })],
        });
        crew = [aggregate];
        mountTestee();

        await testee.findAll('[data-test-id="table-delete-registration"]')[0].trigger('click');

        await expect.poll(() => testee.emitted('update:event')?.length).toBe(2);
        const unassignedEvent = emittedEvent(0);
        const deletedEvent = emittedEvent(1);
        expect(unassignedEvent?.slots.find((it) => it.key === 'slot-assigned')?.assignedRegistrationKey).toBeUndefined();
        expect(deletedEvent?.registrations).toEqual([]);
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
        event = mockEvent({ slots: [mockSlotCaptain({ key: 'slot-captain' })] });
        crew = [aggregate];
        slotDialogOpenSpy.mockResolvedValue(editedSlot);
        mountTestee();

        await testee.findAll('[data-test-id="table-edit-slot"]')[0].trigger('click');

        const updatedEvent = emittedEvent();
        expect(updatedEvent?.slots.find((it) => it.key === 'slot-captain')?.positionName).toBe('Edited Captain');
    });

    it('should delete unassigned slot', async () => {
        const aggregate: ResolvedRegistrationSlot = {
            name: 'Open Captain',
            state: RegistrationSlotState.OPEN,
            position: mockPositionCaptain(),
            slot: mockSlotCaptain({ key: 'slot-open', assignedRegistrationKey: undefined }),
            expiredQualifications: [],
            hasOverwrittenPosition: false,
        };
        event = mockEvent({ slots: [mockSlotCaptain({ key: 'slot-open', assignedRegistrationKey: undefined })] });
        crew = [aggregate];
        mountTestee();

        await testee.findAll('[data-test-id="table-delete-slot"]')[0].trigger('click');

        const updatedEvent = emittedEvent();
        expect(updatedEvent?.slots).toEqual([]);
    });

    it('should update registration fields and emit updated event when editing registration', async () => {
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
        registrationDialogOpenSpy.mockResolvedValue({
            ...registration,
            name: 'New Name',
            note: 'new note',
            positionKey: mockPositionCaptain().key,
        });
        mountTestee();

        await testee.findAll('[data-test-id="table-edit-registration"]')[1].trigger('click');

        const updatedEvent = emittedEvent();
        expect(updatedEvent?.registrations[0].name).toBe('New Name');
        expect(updatedEvent?.registrations[0].note).toBe('new note');
        expect(updatedEvent?.registrations[0].positionKey).toBe(mockPositionCaptain().key);
    });

    it('should open slot dialog with a copy of slot', async () => {
        const slot = mockSlotCaptain({ key: 'slot-captain' });
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
        crew = [aggregate];
        mountTestee();

        await testee.findAll('[data-test-id="table-edit-slot"]')[0].trigger('click');

        expect(slotDialogOpenSpy).toHaveBeenCalledWith(copyOf(slot));
    });

    it('should open registration dialog with a copy of registration', async () => {
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
        mountTestee();

        await testee.findAll('[data-test-id="table-edit-registration"]')[1].trigger('click');

        expect(registrationDialogOpenSpy).toHaveBeenCalledWith(copyOf(registration));
    });
});
