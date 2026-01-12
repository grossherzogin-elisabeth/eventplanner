import type { QualificationRepository, Storage } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Qualification, QualificationKey } from '@/domain';

export class QualificationCachingService {
    private readonly qualificationRepository: QualificationRepository;
    private readonly cache: Storage<QualificationKey, Qualification>;
    private readonly initialized: Promise<void>;

    constructor(params: { qualificationRepository: QualificationRepository; cache: Storage<QualificationKey, Qualification> }) {
        this.qualificationRepository = params.qualificationRepository;
        this.cache = params.cache;
        this.initialized = this.initialize();
    }

    private async initialize(): Promise<void> {
        await this.fetchQualifications();
    }

    public async getQualifications(): Promise<Qualification[]> {
        await this.initialized;
        const cached = await this.cache.findAll();
        if (cached.length > 0) {
            return cached;
        }
        return this.fetchQualifications();
    }

    public async removeFromCache(qualificationKey: QualificationKey): Promise<void> {
        await this.initialized;
        return await this.cache.deleteByKey(qualificationKey);
    }

    public async updateCache(qualification: Qualification): Promise<Qualification> {
        await this.initialized;
        if ((await this.cache.count()) > 0) {
            return await this.cache.save(qualification);
        }
        return qualification;
    }

    private async fetchQualifications(): Promise<Qualification[]> {
        console.log('📡 Fetching qualifications');
        return debounce('fetchQualifications', async () => {
            const qualifications = await this.qualificationRepository.findAll();
            await this.cache.saveAll(qualifications);
            return qualifications;
        });
    }
}
