import { getCsrfToken } from '@/adapter/util/Csrf';
import type { PositionRepository } from '@/application';
import type { Position, PositionKey } from '@/domain';

interface PositionRepresentation {
    key: string;
    name: string;
    color: string;
    prio: number;
}

interface CreatePositionRequest {
    name: string;
    color: string;
    prio: number;
}

interface UpdatePositionRequest {
    name: string;
    color: string;
    prio: number;
}

export class PositionRestRepository implements PositionRepository {
    private static mapToDomain(representation: PositionRepresentation): Position {
        return {
            key: representation.key,
            name: representation.name,
            color: representation.color,
            prio: representation.prio,
        };
    }

    public async findAll(): Promise<Position[]> {
        const response = await fetch('/api/v1/positions', { credentials: 'include' });
        if (response.ok) {
            const positions = (await response.clone().json()) as PositionRepresentation[];
            return positions.map(PositionRestRepository.mapToDomain);
        } else {
            throw response;
        }
    }

    public async create(position: Position): Promise<Position> {
        const requestBody: CreatePositionRequest = {
            name: position.name,
            color: position.color,
            prio: position.prio,
        };
        const response = await fetch('/api/v1/position', {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation = (await response.clone().json()) as PositionRepresentation;
        return PositionRestRepository.mapToDomain(representation);
    }

    public async update(positionKey: PositionKey, position: Position): Promise<Position> {
        const requestBody: UpdatePositionRequest = {
            name: position.name,
            color: position.color,
            prio: position.prio,
        };
        const response = await fetch(`/api/v1/Positions/${positionKey}`, {
            method: 'PUT',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation = (await response.clone().json()) as PositionRepresentation;
        return PositionRestRepository.mapToDomain(representation);
    }

    public async deleteByKey(positionKey: PositionKey): Promise<void> {
        const response = await fetch(`/api/v1/Positions/${positionKey}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
    }
}
