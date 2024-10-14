import type { QualificationRepository } from '@/application';
import { AsyncDebouncer } from '@/application/utils/AsyncDebouncer';
import type { Cache } from '@/common';
import type { Qualification, QualificationKey } from '@/domain';

export class QualificationCachingService {
    private readonly qualificationRepository: QualificationRepository;
    private readonly cache: Cache<QualificationKey, Qualification>;

    constructor(params: {
        qualificationRepository: QualificationRepository;
        cache: Cache<QualificationKey, Qualification>;
    }) {
        this.qualificationRepository = params.qualificationRepository;
        this.cache = params.cache;
    }

    public async getQualifications(): Promise<Qualification[]> {
        const cached = await this.cache.findAll();
        if (cached.length > 0) {
            return cached;
        }
        return this.fetchQualifications();
    }

    private async fetchQualifications(): Promise<Qualification[]> {
        return AsyncDebouncer.debounce('fetchQualifications', async () => {
            const qualifications = await this.qualificationRepository.findAll();
            await this.cache.saveAll(qualifications);
            return qualifications;
        });
    }
}
