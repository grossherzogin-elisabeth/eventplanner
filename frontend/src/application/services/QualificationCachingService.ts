import type { Cache, QualificationRepository } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Qualification, QualificationKey } from '@/domain';

export class QualificationCachingService {
    private readonly qualificationRepository: QualificationRepository;
    private readonly cache: Cache<QualificationKey, Qualification>;

    constructor(params: { qualificationRepository: QualificationRepository; cache: Cache<QualificationKey, Qualification> }) {
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

    public async removeFromCache(qualificationKey: QualificationKey): Promise<void> {
        return await this.cache.deleteByKey(qualificationKey);
    }

    public async updateCache(qualification: Qualification): Promise<Qualification> {
        if ((await this.cache.count()) > 0) {
            return await this.cache.save(qualification);
        }
        return qualification;
    }

    private async fetchQualifications(): Promise<Qualification[]> {
        return debounce('fetchQualifications', async () => {
            const qualifications = await this.qualificationRepository.findAll();
            await this.cache.saveAll(qualifications);
            return qualifications;
        });
    }
}
