import type { Slot } from '@/domain';
import { SlotCriticality } from '@/domain';
import { CAPTAIN, DECKHAND, ENGINEER, MATE, SLOT_CAPTAIN, SLOT_DECKHAND, SLOT_ENGINEER, SLOT_MATE } from '~/mocks/keys';

export function mockSlotCaptain(overwrite?: Partial<Slot>): Slot {
    const slot: Slot = {
        key: SLOT_CAPTAIN,
        order: 1,
        positionKeys: [CAPTAIN],
        criticality: SlotCriticality.Required,
    };
    return overwrite ? Object.assign(slot, overwrite) : slot;
}

export function mockSlotEngineer(overwrite?: Partial<Slot>): Slot {
    const slot: Slot = {
        key: SLOT_ENGINEER,
        order: 2,
        positionKeys: [ENGINEER],
        criticality: SlotCriticality.Required,
    };
    return overwrite ? Object.assign(slot, overwrite) : slot;
}

export function mockSlotMate(overwrite?: Partial<Slot>): Slot {
    const slot: Slot = {
        key: SLOT_MATE,
        order: 3,
        positionKeys: [MATE],
        criticality: SlotCriticality.Important,
    };
    return overwrite ? Object.assign(slot, overwrite) : slot;
}

export function mockSlotDeckhand(overwrite?: Partial<Slot>): Slot {
    const slot: Slot = {
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
