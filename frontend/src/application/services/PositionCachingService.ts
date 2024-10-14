import type { PositionRepository } from '@/application';
import { AsyncDebouncer } from '@/application/utils/AsyncDebouncer';
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

    private async fetchPositions(): Promise<Position[]> {
        return AsyncDebouncer.debounce('fetchPositions', async () => {
            const positions = await this.positionRepository.findAll();
            await this.cache.saveAll(positions);
            return positions;
        });
    }
}
