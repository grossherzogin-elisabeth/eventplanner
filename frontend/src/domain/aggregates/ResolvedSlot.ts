import type { Position, Registration, Slot, UserKey } from '@/domain';

export interface ResolvedSlot extends Slot {
    userName?: string;
    userKey?: UserKey;
    registration?: Registration;
    position: Position;
    confirmed?: boolean;
}
