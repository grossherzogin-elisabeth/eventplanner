import type { PositionRepository, Storage } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Position, PositionKey } from '@/domain';

export class PositionCachingService {
    private readonly positionRepository: PositionRepository;
    private readonly storage: Storage<PositionKey, Position>;
    private readonly initialized: Promise<void>;

    constructor(params: { positionRepository: PositionRepository; cache: Storage<PositionKey, Position> }) {
        this.positionRepository = params.positionRepository;
        this.storage = params.cache;
        console.log('🚀 Initializing PositionCachingService');
        this.initialized = this.initialize();
    }

    private async initialize(): Promise<void> {
        try {
            const positions = await this.fetchPositions();
            await this.storage.deleteAll();
            await this.storage.saveAll(positions);
        } catch (e: unknown) {
            const response = e as { status?: number };
            if (response.status === 401 || response.status === 403) {
                // users session is no longer valid, clear all locally stored data
                console.error('Failed to fetch positions, clearing local data');
                await this.storage.deleteAll();
            } else {
                console.warn('Failed to fetch positions, continuing with local data');
            }
        }
    }

    public async getPositions(): Promise<Position[]> {
        await this.initialized;
        const cached = await this.storage.findAll();
        if (cached.length > 0) {
            return cached;
        }
        return this.fetchPositions();
    }

    public async removeFromCache(positionkey: PositionKey): Promise<void> {
        await this.initialized;
        return await this.storage.deleteByKey(positionkey);
    }

    public async updateCache(position: Position): Promise<Position> {
        await this.initialized;
        if ((await this.storage.count()) > 0) {
            return await this.storage.save(position);
        }
        return position;
    }

    private async fetchPositions(): Promise<Position[]> {
        console.log('📡 Fetching positions');
        return debounce('fetchPositions', async () => {
            const positions = await this.positionRepository.findAll();
            await this.storage.saveAll(positions);
            return positions;
        });
    }
}
