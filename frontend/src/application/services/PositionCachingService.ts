import type { PositionRepository, Storage } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Position, PositionKey } from '@/domain';

export class PositionCachingService {
    private readonly positionRepository: PositionRepository;
    private readonly cache: Storage<PositionKey, Position>;
    private readonly initialized: Promise<void>;

    constructor(params: { positionRepository: PositionRepository; cache: Storage<PositionKey, Position> }) {
        this.positionRepository = params.positionRepository;
        this.cache = params.cache;
        this.initialized = this.initialize();
    }

    private async initialize(): Promise<void> {
        await this.fetchPositions();
    }

    public async getPositions(): Promise<Position[]> {
        await this.initialized;
        const cached = await this.cache.findAll();
        if (cached.length > 0) {
            return cached;
        }
        return this.fetchPositions();
    }

    public async removeFromCache(positionkey: PositionKey): Promise<void> {
        await this.initialized;
        return await this.cache.deleteByKey(positionkey);
    }

    public async updateCache(position: Position): Promise<Position> {
        await this.initialized;
        if ((await this.cache.count()) > 0) {
            return await this.cache.save(position);
        }
        return position;
    }

    private async fetchPositions(): Promise<Position[]> {
        console.log('📡 Fetching positions');
        return debounce('fetchPositions', async () => {
            const positions = await this.positionRepository.findAll();
            await this.cache.saveAll(positions);
            return positions;
        });
    }
}
