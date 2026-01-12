import type { QualificationRepository, Storage } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Qualification, QualificationKey } from '@/domain';

export class QualificationCachingService {
    private readonly qualificationRepository: QualificationRepository;
    private readonly storage: Storage<QualificationKey, Qualification>;
    private readonly initialized: Promise<void>;

    constructor(params: { qualificationRepository: QualificationRepository; cache: Storage<QualificationKey, Qualification> }) {
        this.qualificationRepository = params.qualificationRepository;
        this.storage = params.cache;
        this.initialized = this.initialize();
    }

    private async initialize(): Promise<void> {
        try {
            const qualifications = await this.fetchQualifications();
            await this.storage.deleteAll();
            await this.storage.saveAll(qualifications);
        } catch (e: unknown) {
            const response = e as { status?: number };
            if (response.status === 401 || response.status === 403) {
                // users session is no longer valid, clear all locally stored data
                console.error('Failed to fetch qualifications, clearing local data');
                await this.storage.deleteAll();
            } else {
                console.warn('Failed to fetch qualifications, continuing with local data');
            }
        }
    }

    public async getQualifications(): Promise<Qualification[]> {
        await this.initialized;
        const cached = await this.storage.findAll();
        if (cached.length > 0) {
            return cached;
        }
        return this.fetchQualifications();
    }

    public async removeFromCache(qualificationKey: QualificationKey): Promise<void> {
        await this.initialized;
        return await this.storage.deleteByKey(qualificationKey);
    }

    public async updateCache(qualification: Qualification): Promise<Qualification> {
        await this.initialized;
        if ((await this.storage.count()) > 0) {
            return await this.storage.save(qualification);
        }
        return qualification;
    }

    private async fetchQualifications(): Promise<Qualification[]> {
        console.log('📡 Fetching qualifications');
        return debounce('fetchQualifications', async () => {
            const qualifications = await this.qualificationRepository.findAll();
            await this.storage.saveAll(qualifications);
            return qualifications;
        });
    }
}
