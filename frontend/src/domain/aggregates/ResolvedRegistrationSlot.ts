import type { Position, QualificationKey, Registration, Slot, User } from '@/domain';

export interface ResolvedRegistrationSlot {
    name: string;
    position: Position;
    registration?: Registration;
    user?: User;
    slot?: Slot;
    expiredQualifications: QualificationKey[];
    hasOverwrittenPosition: boolean;
}
