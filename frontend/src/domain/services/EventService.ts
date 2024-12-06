import { addToDate, cropToPrecision, filterUndefined } from '@/common';
import type { Event, Location, PositionKey, Registration, SignedInUser, Slot, SlotKey, User, UserKey, ValidationHint } from '@/domain';
import { SlotCriticality } from '@/domain';
import { EventState } from '@/domain';
import { v4 as uuid } from 'uuid';

export class EventService {
    public doEventsHaveOverlappingDays(a?: Event, b?: Event): boolean {
        if (a === undefined || b === undefined) {
            return false;
        }

        const aStart = cropToPrecision(a.start, 'days').getTime();
        const aEnd = cropToPrecision(a.end, 'days').getTime();
        const bStart = cropToPrecision(b.start, 'days').getTime();
        const bEnd = cropToPrecision(b.end, 'days').getTime();

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
        event.assignedUserCount = event.slots.filter((it) => it.assignedRegistrationKey).length;
        return this.optimizeSlots(event);
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
        event.assignedUserCount = event.slots.filter((it) => it.assignedRegistrationKey).length;
        return this.optimizeSlots(event);
    }

    public unassignSlot(event: Event, slotKey: SlotKey): Event {
        const slot = event.slots.find((it) => it.key === slotKey);
        if (!slot) {
            throw new Error('Failed to resolve slot');
        }
        slot.assignedRegistrationKey = undefined;
        return this.optimizeSlots(event);
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
        // this.debugSlots(event);
        for (let i = 0; i < event.slots.length; i++) {
            const slot = event.slots[i];
            if (!slot.assignedRegistrationKey) {
                // try to fill slot with a registration from a slot with lesser prio
                for (let j = i + 1; j < event.slots.length; j++) {
                    const lowerPrioSlot = event.slots[j];
                    if (!lowerPrioSlot.assignedRegistrationKey) {
                        continue;
                    }
                    const registration = event.registrations.find((r) => r.key === lowerPrioSlot.assignedRegistrationKey);
                    if (registration && slot.positionKeys.includes(registration.positionKey)) {
                        // the registration of a lower prio slot can also be assigned to this slot, move it up
                        slot.assignedRegistrationKey = lowerPrioSlot.assignedRegistrationKey;
                        lowerPrioSlot.assignedRegistrationKey = undefined;
                        break;
                    }
                }
            }
        }
        // this.debugSlots(event);
        return event;
    }

    // private debugSlots(event: Event): void {
    //     event.slots.forEach((s) => {
    //         console.log(s.order, s.positionKeys, s.assignedRegistrationKey);
    //     });
    // }

    public updateSlot(event: Event, slot: Slot): Event {
        const index = event.slots.findIndex((it) => it.key === slot.key);
        event.slots[index] = slot;
        return event;
    }

    public removeSlot(event: Event, slot: Slot): Event {
        event.slots = event.slots.filter((it) => it.key !== slot.key);
        event.slots.forEach((it, index) => (it.order = index + 1));
        return event;
    }

    public duplicateSlot(event: Event, slot: Slot): Event {
        const index = event.slots.findIndex((it) => it.key === slot.key);
        event.slots.splice(index, 0, {
            key: uuid(),
            order: -1,
            positionName: slot.positionName,
            positionKeys: slot.positionKeys,
            criticality: slot.criticality,
            assignedRegistrationKey: undefined,
        });
        event.slots.forEach((it, index) => (it.order = index + 1));
        return event;
    }

    public moveSlot(event: Event, slot: Slot, offset: number): Event {
        if (offset === 0) {
            return event;
        }
        const orderedSlots = event.slots.sort((a, b) => a.order - b.order);
        const index = orderedSlots.findIndex((it) => it.key === slot.key);
        let otherIndex = -1;
        if (offset < 0) {
            if (index === 0) return event;
            otherIndex = Math.max(index + offset, 0);
        } else {
            if (index === orderedSlots.length - 1) return event;
            otherIndex = Math.min(index + offset, orderedSlots.length - 1);
        }
        const other = orderedSlots[otherIndex];
        // swap orders
        const temp = slot.order;
        slot.order = other.order;
        other.order = temp;
        // swap index
        orderedSlots[otherIndex] = slot;
        orderedSlots[index] = other;
        event.slots = orderedSlots;
        return event;
    }

    public updateLocation(event: Event, location: Location): Event {
        const index = event.locations.findIndex((it) => it.order === location.order);
        event.locations[index] = location;
        return event;
    }

    public removeLocation(event: Event, location: Location): Event {
        event.locations = event.locations.filter((it) => it.order !== location.order);
        return event;
    }

    public moveLocation(event: Event, location: Location, offset: number): Event {
        if (offset === 0) {
            return event;
        }
        const orderedLocations = event.locations.sort((a, b) => a.order - b.order);
        const index = orderedLocations.findIndex((it) => it.order === location.order);
        let otherIndex = -1;
        if (offset < 0) {
            if (index === 0) return event;
            otherIndex = Math.max(index + offset, 0);
        } else {
            if (index === orderedLocations.length - 1) return event;
            otherIndex = Math.min(index + offset, orderedLocations.length - 1);
        }
        const other = orderedLocations[otherIndex];
        // swap order
        const temp = location.order;
        location.order = other.order;
        other.order = temp;
        // swap index
        orderedLocations[otherIndex] = location;
        orderedLocations[index] = other;
        event.locations = orderedLocations;
        return event;
    }

    public isSlotFilled(event: Event | null | undefined, slotkey: SlotKey): boolean {
        if (!event) {
            return false;
        }
        const slot = event.slots.find((it) => it.key === slotkey);
        return slot !== undefined && slot.assignedRegistrationKey !== undefined;
    }

    public hasOpenRequiredSlots(event: Event, positions?: PositionKey[]): boolean {
        return this.hasOpenSlots(event, positions, SlotCriticality.Required);
    }

    public hasOpenImportantSlots(event: Event, positions?: PositionKey[]): boolean {
        return this.hasOpenSlots(event, positions, SlotCriticality.Important);
    }

    public hasOpenSlots(event: Event, positions?: PositionKey[], criticality: number = 0): boolean {
        const openSlots = event.slots.filter(
            (it) =>
                it.criticality >= criticality &&
                it.assignedRegistrationKey === undefined &&
                (positions === undefined || positions.length === 0 || positions?.find((p) => it.positionKeys.includes(p)))
        );
        return openSlots.length > 0;
    }

    public findRegistration(event: Event, userKey?: string, name?: string): Registration | undefined {
        return event.registrations.find((r) => (userKey && r.userKey === userKey) || r.name === name);
    }

    public getAssignedRegistrations(event: Event): Registration[] {
        const assignedRegistrationKeys = event.slots.map((it) => it.assignedRegistrationKey).filter(filterUndefined);
        return event.registrations.filter((it) => assignedRegistrationKeys.includes(it.key));
    }

    public getRegistrationsOnWaitinglist(event: Event): Registration[] {
        return event.registrations.filter((reg) => !event.slots.find((slt) => slt.assignedRegistrationKey === reg.key));
    }

    public validate(event: Event): Record<string, ValidationHint[]> {
        const errors: Record<string, ValidationHint[]> = {};
        if (event.name.trim().length === 0) {
            errors.name = errors.name || [];
            errors.name.push({
                key: 'Bitte gib einen Anzeigenamen für das Event an',
                params: {},
            });
        }
        if (event.end.getTime() < event.start.getTime()) {
            errors.end = errors.end || [];
            errors.end.push({
                key: 'Das Enddatum muss nach dem Startdatum sein',
                params: {},
            });
        }
        return errors;
    }

    public validatePartial(event: Partial<Event>): Record<string, ValidationHint[]> {
        const errors: Record<string, ValidationHint[]> = {};
        if (event.end && event.start && event.end.getTime() < event.start.getTime()) {
            errors.end = errors.end || [];
            errors.end.push({
                key: 'Das Enddatum muss nach dem Startdatum sein',
                params: {},
            });
        }
        return errors;
    }

    public updateComputedValues(event: Event, signedInUser?: SignedInUser): Event {
        const registration = event.registrations.find((it: Registration) => it.userKey === signedInUser?.key);
        if (registration !== undefined) {
            event.canSignedInUserJoin = false;
            const slot = event.slots.find((it) => it.assignedRegistrationKey === registration.key);
            if (slot) {
                event.signedInUserAssignedPosition = registration.positionKey;
                event.canSignedInUserLeave = event.start.getTime() > addToDate(new Date(), { days: 7 }).getTime();
            } else {
                event.signedInUserWaitingListPosition = registration.positionKey;
                event.canSignedInUserLeave = event.start.getTime() > new Date().getTime();
            }
        } else {
            event.canSignedInUserLeave = false;
            event.canSignedInUserJoin =
                (signedInUser?.positions || []).length > 0 &&
                event.start.getTime() > new Date().getTime() &&
                ![EventState.Canceled].includes(event.state);
        }
        if (event.state === EventState.Canceled) {
            event.canSignedInUserJoin = false;
        }
        const dayStart = cropToPrecision(event.start, 'days');
        const dayEnd = cropToPrecision(event.end, 'days');
        event.days = new Date(dayEnd.getTime() - dayStart.getTime()).getDate();

        return event;
    }
}
