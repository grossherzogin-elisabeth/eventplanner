import type { PositionKey } from '@/domain';
import type { QualificationKey } from '../entities/Qualification';

export interface ResolvedUserQualification {
    key: QualificationKey;
    name: string;
    icon: string;
    description: string;
    expires: boolean;
    expiresAt?: Date;
    note?: string;
    isExpired: boolean;
    willExpireSoon: boolean;
    grantsPositions: PositionKey[];
}
