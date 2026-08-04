import type { PositionKey, RegistrationKey, SlotCriticality } from '../index';

export type SlotKey = string;

export interface Slot {
    /**
     * Unique key of this slot
     */
    key: SlotKey;
    /**
     * Display order of this slot
     */
    order: number;
    /**
     * How important is it, that the slot is filled?
     */
    criticality: SlotCriticality;
    /**
     * A list of possible positions required for a user to fill this slot
     */
    positionKeys: PositionKey[];
    /**
     * Optional override for this slots position name
     */
    positionName?: string;

    /**
     * If assigned, the key of the registration filling this slot
     */
    assignedRegistrationKey?: RegistrationKey;

    /**
     * This slot has been implicitly created and will be removed when the assigned registration gets deleted or
     * unassigned
     */
    implicit?: boolean;
}
