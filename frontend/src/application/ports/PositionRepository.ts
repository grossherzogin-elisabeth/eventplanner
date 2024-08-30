import type { Position } from '@/domain';

export interface PositionRepository {
    findAll(): Promise<Position[]>;
}
