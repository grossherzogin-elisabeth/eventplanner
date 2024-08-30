import type { PositionRepository } from '@/application';
import type { Position } from '@/domain';

interface PositionRepresentation {
    key: string;
    name: string;
    color: string;
    prio: number;
}

export class PositionRestRepository implements PositionRepository {
    public async findAll(): Promise<Position[]> {
        const response = await fetch('/api/v1/positions', { credentials: 'include' });
        if (response.ok) {
            const positions = (await response.clone().json()) as PositionRepresentation[];
            return positions.map((it) => ({
                key: it.key,
                name: it.name,
                color: it.color,
                prio: it.prio,
            }));
        } else {
            throw response;
        }
    }
}
