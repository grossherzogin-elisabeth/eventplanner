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
        this.clearSlot(event, slotKey);
        registration.slotKey = slotKey;
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
        this.clearSlot(event, slotKey);
        registration.slotKey = slotKey;
        event.assignedUserCount++;
        return event;
    }

    public unassignSlot(event: Event, slotKey: SlotKey): Event {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        event = this.clearSlot(event, slotKey);
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

    public clearSlot(event: Event, slotKey: SlotKey): Event {
        event.registrations.filter((it) => it.slotKey === slotKey).forEach((it) => (it.slotKey = undefined));
        event.assignedUserCount = event.registrations.filter((it) => it.slotKey).length;
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
        const usedSlotKeys = event.registrations.map((it) => it.slotKey).filter(ArrayUtils.filterUndefined);
        return event.slots.filter((it) => !usedSlotKeys.includes(it.key));
    }

    /**
     * Reorders slots to make sure the higher ranked slots are filled first, making space for lower qualified
     * team members
     * @param event
     */
    private optimizeSlots(event: Event): Event {
        const slotMap = new Map<SlotKey | undefined, Registration | undefined>();
        event.registrations.forEach((it) => slotMap.set(it.slotKey, it));

        for (let i = 0; i < event.slots.length; i++) {
            const slot = event.slots[i];
            if (!slotMap.has(slot.key)) {
                for (let j = i + 1; j < event.slots.length; j++) {
                    const nextSlot = event.slots[j];
                    const registration = slotMap.get(nextSlot?.key);
                    if (registration && slot.positionKeys.includes(registration.positionKey)) {
                        // move registration to higher prio slot
                        registration.slotKey = slot.key;
                        slotMap.set(slot.key, registration);
                        slotMap.delete(nextSlot.key);
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
        return event.registrations.filter((it) => it.slotKey === slotkey).length > 0;
    }

    public hasOpenRequiredSlots(event: Event): boolean {
        const filledSlotKeys = event.registrations.map((it) => it.slotKey).filter(ArrayUtils.filterUndefined);
        const openRequiredSlots = event.slots
            .filter((it) => it.criticality >= 1)
            .filter((it) => !filledSlotKeys.includes(it.key));
        return openRequiredSlots.length > 0;
    }

    public findRegistration(event: Event, userKey?: string, name?: string): Registration | undefined {
        return event.registrations.find((r) => (userKey && r.userKey === userKey) || r.name === name);
    }
}
