import type { PositionKey, SlotKey, UserKey } from '@/domain';

export interface Registration {
    positionKey: PositionKey;
    userKey?: UserKey;
    name?: string;
    slotKey?: SlotKey;
}
