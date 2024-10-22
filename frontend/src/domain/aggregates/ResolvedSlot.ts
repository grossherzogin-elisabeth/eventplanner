import type { Position, QualificationKey, Registration, Slot, UserKey } from '@/domain';

export interface ResolvedSlot extends Slot {
    userName?: string;
    userKey?: UserKey;
    registration?: Registration;
    position: Position;
    confirmed?: boolean;
    expiredQualifications: QualificationKey[];
    hasOverwrittenPosition: boolean;
    hasFitnessForSeaService: boolean;
}
