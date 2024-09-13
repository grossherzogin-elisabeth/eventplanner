import type { Qualification } from '@/domain';

export interface QualificationRepository {
    findAll(): Promise<Qualification[]>;
}
