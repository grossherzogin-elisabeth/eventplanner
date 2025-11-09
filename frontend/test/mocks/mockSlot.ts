import type { Slot } from '@/domain';
import { SlotCriticality } from '@/domain';
import { CAPTAIN, DECKHAND, ENGINEER, MATE } from './mockPosition';

export const SLOT_CAPTAIN = 'slot-captain';
export const SLOT_ENGINEER = 'slot-engineer';
export const SLOT_MATE = 'slot-mate';
export const SLOT_DECKHAND = 'slot-deckhand';

export function mockSlotCaptain(overwrite?: Partial<Slot>): Slot {
    const slot = {
        key: SLOT_CAPTAIN,
        order: 1,
        positionKeys: [CAPTAIN],
        criticality: SlotCriticality.Required,
    };
    return overwrite ? Object.assign(slot, overwrite) : slot;
}

export function mockSlotEngineer(overwrite?: Partial<Slot>): Slot {
    const slot = {
        key: SLOT_ENGINEER,
        order: 2,
        positionKeys: [ENGINEER],
        criticality: SlotCriticality.Required,
    };
    return overwrite ? Object.assign(slot, overwrite) : slot;
}

export function mockSlotMate(overwrite?: Partial<Slot>): Slot {
    const slot = {
        key: SLOT_MATE,
        order: 3,
        positionKeys: [MATE],
        criticality: SlotCriticality.Important,
    };
    return overwrite ? Object.assign(slot, overwrite) : slot;
}

export function mockSlotDeckhand(overwrite?: Partial<Slot>): Slot {
    const slot = {
        key: SLOT_DECKHAND,
        order: 4,
        positionKeys: [DECKHAND],
        criticality: SlotCriticality.Optional,
    };
    return overwrite ? Object.assign(slot, overwrite) : slot;
}

export function mockSlots(): Slot[] {
    return [mockSlotCaptain(), mockSlotEngineer(), mockSlotMate(), mockSlotDeckhand()];
}
