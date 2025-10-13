import type { Position, QualificationKey, Registration, Slot, User } from '@/domain';

export interface ResolvedRegistrationSlot {
    name: string;
    state: RegistrationSlotState;
    position: Position;
    registration?: Registration;
    user?: User;
    slot?: Slot;
    expiredQualifications: QualificationKey[];
    hasOverwrittenPosition: boolean;
}

export enum RegistrationSlotState {
    OPEN = 'open',
    WAITING_LIST = 'waiting-list',
    ASSIGNED = 'assigned',
    CONFIRMED = 'confirmed',
}
