import { addToDate, cropToPrecision, filterUndefined } from '@/common';
import { Validator, after, maxLength, notEmpty } from '@/common/validation';
import type { Event, Location, PositionKey, Registration, RegistrationKey, SignedInUser, Slot, SlotKey, User, UserKey } from '@/domain';
import { EventSignupType, EventState, Permission, Role, SlotCriticality } from '@/domain';
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
        if (userKey) {
            event.registrations = event.registrations.filter((it) => it.userKey !== userKey);
        }
        return event;
    }

    public cancelGuestRegistration(event: Event, name?: string): Event {
        event.registrations = event.registrations.filter((it) => it.name !== name || it.userKey !== undefined);
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
        return slot.positionKeys.some((positionkey) => user.positionKeys?.includes(positionkey));
    }

    public getOpenSlots(event: Event): Slot[] {
        return event.slots.filter((it) => !it.assignedRegistrationKey);
    }

    public updateSlot(event: Event, slot: Slot): Event {
        const index = event.slots.findIndex((it) => it.key === slot.key);
        if (index >= 0) {
            event.slots[index] = slot;
        }
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
        if (index >= 0) {
            event.locations[index] = location;
        }
        return event;
    }

    public removeLocation(event: Event, location: Location): Event {
        event.locations = event.locations.filter((it) => it.order !== location.order);
        event.locations.forEach((it, index) => (it.order = index + 1));
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
        if (event.signupType === EventSignupType.Open && criticality === 0) {
            // this event has no limited slots
            // only return true for criticality 0 though to prevent hasOpenRequiredSlots returning a false positive
            return true;
        }
        const openSlots = event.slots.filter(
            (it) =>
                it.criticality >= criticality &&
                it.assignedRegistrationKey === undefined &&
                (positions === undefined || positions.length === 0 || positions?.find((p) => it.positionKeys.includes(p)))
        );
        return openSlots.length > 0;
    }

    public findRegistration(event: Event, userKey?: UserKey, name?: string): Registration | undefined {
        return event.registrations.find((r) => (userKey && r.userKey === userKey) || (name && r.name === name));
    }

    public findSignedInUserRegistration(event: Event, signedInUser: SignedInUser): Registration | undefined {
        return this.findRegistration(event, signedInUser.key, undefined);
    }

    public findSlotAssignedToRegistration(event: Event, registrationKey: RegistrationKey): Slot | undefined {
        return event.slots.find((s) => s.assignedRegistrationKey === registrationKey);
    }

    public findSlotAssignedToSignedInUser(event: Event, signedInUser: SignedInUser): Slot | undefined {
        const registration = this.findSignedInUserRegistration(event, signedInUser);
        if (registration) {
            return event.slots.find((s) => s.assignedRegistrationKey === registration.key);
        }
        return undefined;
    }

    public getAssignedRegistrations(event: Event): Registration[] {
        const assignedRegistrationKeys = event.slots.map((it) => it.assignedRegistrationKey).filter(filterUndefined);
        return event.registrations.filter((it) => assignedRegistrationKeys.includes(it.key));
    }

    public getRegistrationsOnWaitingList(event: Event): Registration[] {
        return event.registrations.filter((reg) => !event.slots.some((slt) => slt.assignedRegistrationKey === reg.key));
    }

    public validate(event?: Event): Record<string, string[]> {
        if (!event) {
            return {};
        }
        return Validator.validate('name', event.name, notEmpty(), maxLength(35))
            .validate('start', event.start, notEmpty())
            .validate('end', event.end, notEmpty(), after(event.start, 'Das Enddatum muss nach dem Startdatum liegen'))
            .getErrors();
    }

    public validatePartial(event: Partial<Event>): Record<string, string[]> {
        return Validator.validate('name', event.name, maxLength(35))
            .validate('end', event.end, after(event.start, 'Das Enddatum muss nach dem Startdatum liegen'))
            .getErrors();
    }

    public isInPast(event: Event): boolean {
        return event.end.getTime() < Date.now();
    }

    public isStarted(event: Event): boolean {
        return event.start.getTime() < Date.now();
    }

    public getDurationDays(event: Event): number {
        const dayStart = cropToPrecision(event.start, 'days');
        const dayEnd = cropToPrecision(event.end, 'days');
        return new Date(dayEnd.getTime() - dayStart.getTime()).getDate();
    }

    public canSignedInUserJoin(event: Event, signedInUser: SignedInUser): boolean {
        if (this.findSignedInUserRegistration(event, signedInUser) != undefined) {
            // user already has a registration
            return false;
        }
        if (this.isInPast(event) || [EventState.Canceled].includes(event.state)) {
            // event is not in valid state to create a registration
            return false;
        }
        return signedInUser.positions.length > 0;
    }

    public canSignedInUserLeave(event: Event, signedInUser: SignedInUser): boolean {
        const userRegistration = this.findSignedInUserRegistration(event, signedInUser);
        if (userRegistration === undefined) {
            // user already has no registration
            return false;
        }
        const assignedSlot = this.findSlotAssignedToRegistration(event, userRegistration.key);
        if (assignedSlot) {
            // user is assigned and can cancel the registration until 7 days before event start
            return event.start.getTime() > addToDate(new Date(), { days: 7 }).getTime();
        }
        // user is on the waiting list, or the event is open signup
        // registration can be canceled until the event is finished
        return !this.isInPast(event);
    }

    public canSignedInUserUpdateRegistration(event: Event, signedInUser: SignedInUser): boolean {
        // users can update their registration until the event is finished
        return this.findSignedInUserRegistration(event, signedInUser) != undefined && !this.isInPast(event);
    }

    public canSignedInUserCreateExports(event: Event, signedInUser: SignedInUser): boolean {
        if (!signedInUser?.permissions.includes(Permission.EXPORT_EVENTS)) {
            return false;
        }
        if (signedInUser?.roles.includes(Role.EVENT_LEADER)) {
            return (
                // allow exports in a limited time from 7 days before the event start until the events end and only for
                // events with the signed-in user assigned
                addToDate(new Date(), { days: 7 }).getTime() > event.start.getTime() &&
                new Date().getTime() < event.end.getTime() &&
                this.isSignedInUserAssigned(event, signedInUser)
            );
        }
        return true;
    }

    public isSignedInUserAssigned(event: Event, signedInUser: SignedInUser): boolean {
        // user is assigned or event is open signup (no assignment required)
        return event.signupType !== EventSignupType.Assignment || this.findSlotAssignedToSignedInUser(event, signedInUser) !== undefined;
    }

    public updateComputedValues(event: Event, signedInUser?: SignedInUser): Event {
        // reset all computed values
        event.isInPast = this.isInPast(event);
        event.days = this.getDurationDays(event);
        if (signedInUser) {
            event.signedInUserRegistration = this.findSignedInUserRegistration(event, signedInUser);
            event.signedInUserAssignedSlot = this.findSlotAssignedToSignedInUser(event, signedInUser);
            event.canSignedInUserJoin = this.canSignedInUserJoin(event, signedInUser);
            event.canSignedInUserLeave = this.canSignedInUserLeave(event, signedInUser);
            event.canSignedInUserUpdateRegistration = this.canSignedInUserUpdateRegistration(event, signedInUser);
            event.canSignedInUserCreateExports = this.canSignedInUserCreateExports(event, signedInUser);
            event.isSignedInUserAssigned = this.isSignedInUserAssigned(event, signedInUser);
        } else {
            event.signedInUserRegistration = undefined;
            event.signedInUserAssignedSlot = undefined;
            event.canSignedInUserJoin = false;
            event.canSignedInUserLeave = false;
            event.canSignedInUserUpdateRegistration = false;
            event.canSignedInUserCreateExports = false;
            event.isSignedInUserAssigned = false;
        }
        return event;
    }

    public showWaitingList(event: Event): boolean {
        return (
            event !== undefined &&
            event.signupType !== EventSignupType.Open &&
            ![EventState.Draft, EventState.OpenForSignup].includes(event.state)
        );
    }
}
