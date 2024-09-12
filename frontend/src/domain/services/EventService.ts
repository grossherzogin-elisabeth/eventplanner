import { ArrayUtils, DateUtils } from '@/common';
import type { Event, Registration, Slot, SlotKey, User, UserKey } from '@/domain';

export class EventService {
    public doEventsHaveOverlappingDays(a?: Event, b?: Event): boolean {
        if (a === undefined || b === undefined) {
            return false;
        }

        const aStart = DateUtils.cropToPrecision(a.start, 'days').getTime();
        const aEnd = DateUtils.cropToPrecision(a.end, 'days').getTime();
        const bStart = DateUtils.cropToPrecision(b.start, 'days').getTime();
        const bEnd = DateUtils.cropToPrecision(b.end, 'days').getTime();

        return (aEnd >= bStart && aEnd <= bEnd) || (bEnd >= aStart && bEnd <= aEnd);
    }

    public doesEventMatchFilter(event: Event, filter: string): boolean {
        const filterLc = filter.toLowerCase();
        if (event.name.toLowerCase().includes(filterLc)) {
            return true;
        }
        if (event.description.toLowerCase().includes(filterLc)) {
            return true;
        }
        for (const location of event.locations) {
            if (location.name.toLowerCase().includes(filterLc)) {
                return true;
            }
            if (location.country?.toLowerCase().includes(filterLc)) {
                return true;
            }
        }
        return false;
    }

    public assignUserToSlot(event: Event, user: User, slotKey: SlotKey): Event {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        if (!slot.positionKeys.find((positionkey) => user.positionKeys.includes(positionkey))) {
            console.warn(`Assigning ${user.firstName} ${user.lastName} to slot with mismatching positions!`);
            // throw new Error('User does not have the required position');
        }
        const registration = event.registrations.find((it) => it.userKey === user.key);
        if (!registration) {
            throw new Error('Failed to resolve user registration');
        }
        slot.assignedRegistrationKey = registration.key;
        event.assignedUserCount++;
        return event;
    }

    public assignGuestToSlot(event: Event, name: string, slotKey: SlotKey): Event {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        const registration = event.registrations.find((it) => it.name === name);
        if (!registration) {
            throw new Error('Failed to resolve guest registration');
        }
        slot.assignedRegistrationKey = registration.key;
        event.assignedUserCount++;
        return event;
    }

    public unassignSlot(event: Event, slotKey: SlotKey): Event {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        slot.assignedRegistrationKey = undefined;
        event = this.optimizeSlots(event);
        return event;
    }

    public cancelUserRegistration(event: Event, userKey?: UserKey): Event {
        event.registrations = event.registrations.filter((it) => it.userKey !== userKey);
        return event;
    }

    public cancelGuestRegistration(event: Event, name?: string): Event {
        event.registrations = event.registrations.filter((it) => it.name !== name);
        return event;
    }

    public canUserBeAssignedToSlot(event: Event, user: User, slotKey: SlotKey): boolean {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            return false;
        }
        const registration = event.registrations.find((it) => it.userKey === user.key);
        if (!registration) {
            return false;
        }
        return slot.positionKeys.find((positionkey) => user.positionKeys.includes(positionkey)) !== undefined;
    }

    public getOpenSlots(event: Event): Slot[] {
        return event.slots.filter((it) => !it.assignedRegistrationKey);
    }

    /**
     * Reorders slots to make sure the higher ranked slots are filled first, making space for lower qualified
     * team members
     * @param event
     */
    private optimizeSlots(event: Event): Event {
        for (let i = 0; i < event.slots.length; i++) {
            const slot = event.slots[i];
            if (!slot.assignedRegistrationKey) {
                // try to fill slot with a registration from a slot with lesser prio
                for (let j = i + 1; j < event.slots.length; j++) {
                    const lowerPrioSlot = event.slots[j];
                    if (!lowerPrioSlot.assignedRegistrationKey) {
                        continue;
                    }
                    const registration = event.registrations.find(
                        (r) => r.key === lowerPrioSlot.assignedRegistrationKey
                    );
                    if (registration && slot.positionKeys.includes(registration.positionKey)) {
                        // the registration of a lower prio slot can also be assigned to this slot, move it up
                        slot.assignedRegistrationKey = lowerPrioSlot.assignedRegistrationKey;
                        lowerPrioSlot.assignedRegistrationKey = undefined;
                        break;
                    }
                }
            }
        }
        return event;
    }

    public updateSlot(event: Event, slot: Slot): Event {
        const index = event.slots.findIndex((it) => it.key === slot.key);
        event.slots[index] = slot;
        return event;
    }

    public isSlotFilled(event: Event | null | undefined, slotkey: SlotKey): boolean {
        if (!event) {
            return false;
        }
        const slot = event.slots.find((it) => it.key === slotkey);
        return slot !== undefined && slot.assignedRegistrationKey !== undefined;
    }

    public hasOpenRequiredSlots(event: Event): boolean {
        const openRequiredSlots = event.slots.filter(
            (it) => it.criticality >= 1 && it.assignedRegistrationKey === undefined
        );
        return openRequiredSlots.length > 0;
    }

    public findRegistration(event: Event, userKey?: string, name?: string): Registration | undefined {
        return event.registrations.find((r) => (userKey && r.userKey === userKey) || r.name === name);
    }

    public getAssignedRegistrations(event: Event): Registration[] {
        return event.slots
            .map((slt) => event.registrations.find((reg) => reg.key === slt.assignedRegistrationKey))
            .filter(ArrayUtils.filterUndefined);
    }

    public getRegistrationsOnWaitinglist(event: Event): Registration[] {
        return event.registrations.filter((reg) => !event.slots.find((slt) => slt.assignedRegistrationKey === reg.key));
    }
}
