import type { QualificationKey } from '@/domain';

export interface UserQualification {
    qualificationKey: QualificationKey;
    expiresAt?: Date;
    note?: string;
}
