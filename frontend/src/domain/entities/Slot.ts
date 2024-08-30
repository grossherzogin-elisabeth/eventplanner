import type { PositionKey } from '../index';

export type SlotKey = string;

export interface Slot {
    key: SlotKey;
    /**
     * Display order of this slot
     */
    order: number;
    /**
     * Is this slot required?
     */
    required: boolean;
    /**
     * A list of possible positions required for a user to fill this slot
     */
    positionKeys: PositionKey[];
    /**
     * Optional override for this slots position name
     */
    positionName?: string;
}
