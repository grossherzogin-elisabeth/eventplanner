import type { PositionKey, SlotCriticality } from '../index';

export type SlotKey = string;

export interface Slot {
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
}
