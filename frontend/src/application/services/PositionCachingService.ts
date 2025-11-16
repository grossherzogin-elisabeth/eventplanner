import type { PositionRepository } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Cache } from '@/common';
import type { Position, PositionKey } from '@/domain';

export class PositionCachingService {
    private readonly positionRepository: PositionRepository;
    private readonly cache: Cache<PositionKey, Position>;

    constructor(params: { positionRepository: PositionRepository; cache: Cache<PositionKey, Position> }) {
        this.positionRepository = params.positionRepository;
        this.cache = params.cache;
    }

    public async getPositions(): Promise<Position[]> {
        const cached = await this.cache.findAll();
        if (cached.length > 0) {
            return cached;
        }
        return this.fetchPositions();
    }

    public async removeFromCache(positionkey: PositionKey): Promise<void> {
        return await this.cache.deleteByKey(positionkey);
    }

    public async updateCache(position: Position): Promise<Position> {
        if ((await this.cache.count()) > 0) {
            return await this.cache.save(position);
        }
        return position;
    }

    private async fetchPositions(): Promise<Position[]> {
        return debounce('fetchPositions', async () => {
            const positions = await this.positionRepository.findAll();
            await this.cache.saveAll(positions);
            return positions;
        });
    }
}
