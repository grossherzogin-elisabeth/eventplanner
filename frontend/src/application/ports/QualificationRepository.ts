import type { Qualification, QualificationKey } from '@/domain';

export interface QualificationRepository {
    findAll(): Promise<Qualification[]>;

    create(qualification: Qualification): Promise<Qualification>;

    update(qualificationKey: QualificationKey, qualification: Qualification): Promise<Qualification>;

    deleteByKey(qualificationKey: QualificationKey): Promise<void>;
}
