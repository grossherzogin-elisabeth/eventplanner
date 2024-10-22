import type { Position, PositionKey } from '@/domain';

export interface PositionRepository {
    findAll(): Promise<Position[]>;

    create(position: Position): Promise<Position>;

    update(positionKey: PositionKey, position: Partial<Position>): Promise<Position>;

    deleteByKey(positionKey: PositionKey): Promise<void>;
}
