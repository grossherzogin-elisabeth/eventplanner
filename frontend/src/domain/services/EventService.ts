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
        return slot.positionKeys.find((positionkey) => user.positionKeys?.includes(positionkey)) !== undefined;
    }

    public getOpenSlots(event: Event): Slot[] {
        return event.slots.filter((it) => !it.assignedRegistrationKey);
    }

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
        return event.registrations.find((r) => (userKey && r.userKey === userKey) || (name && r.name === name));
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
        event.isInPast = event.start.getTime() < new Date().getTime();
        event.signedInUserRegistration = event.registrations.find((it: Registration) => it.userKey === signedInUser?.key);
        if (event.signedInUserRegistration !== undefined) {
            // singed in user has a registration
            event.canSignedInUserJoin = false;
            event.signedInUserAssignedSlot = event.slots.find((it) => it.assignedRegistrationKey === event.signedInUserRegistration?.key);
            const isInPast = event.start.getTime() < new Date().getTime();
            if (event.signedInUserAssignedSlot) {
                event.canSignedInUserUpdateRegistration = !isInPast;
                const isLessThan7daysInFuture = event.start.getTime() < addToDate(new Date(), { days: 7 }).getTime();
                const isLessThan14daysInFuture =
                    isLessThan7daysInFuture || event.start.getTime() < addToDate(new Date(), { days: 14 }).getTime();
                event.canSignedInUserLeave = !isLessThan7daysInFuture;
                if (isLessThan14daysInFuture) {
                    // event.canSignedInUserLeave;
                }
            } else {
                event.canSignedInUserLeave = !isInPast;
                event.canSignedInUserUpdateRegistration = !isInPast;
            }
        } else {
            // singed in user has no registration
            event.canSignedInUserLeave = false;
            event.canSignedInUserJoin =
                (signedInUser?.positions || []).length > 0 &&
                event.start.getTime() > new Date().getTime() &&
                ![EventState.Canceled].includes(event.state);
        }

        // canceled events cannot be joined
        if (event.state === EventState.Canceled) {
            event.canSignedInUserJoin = false;
        }
        const dayStart = cropToPrecision(event.start, 'days');
        const dayEnd = cropToPrecision(event.end, 'days');
        event.days = new Date(dayEnd.getTime() - dayStart.getTime()).getDate();

        return event;
    }
}
