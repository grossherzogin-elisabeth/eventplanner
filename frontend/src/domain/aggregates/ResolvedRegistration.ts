import type { Position, QualificationKey, Registration, User } from '@/domain';

export interface ResolvedRegistration extends Registration {
    name: string;
    position: Position;
    user?: User;
    expiredQualifications: QualificationKey[];
    hasOverwrittenPosition: boolean;
    hasFitnessForSeaService: boolean;
}
