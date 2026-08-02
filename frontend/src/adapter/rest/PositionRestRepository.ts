import { getAccessKeyHeader } from '@/adapter/rest/util/getAccessKeyHeader';
import { getCsrfTokenHeader } from '@/adapter/rest/util/getCsrfTokenHeader';
import type { PositionRepository } from '@/application';
import type { Position, PositionKey } from '@/domain';

export interface PositionRepresentation {
    key: string;
    name: string;
    imoListRank: string;
    color: string;
    prio: number;
}

interface CreatePositionRequest {
    key: string;
    name: string;
    imoListRank: string;
    color: string;
    prio: number;
}

interface UpdatePositionRequest {
    name: string;
    imoListRank: string;
    color: string;
    prio: number;
}

export class PositionRestRepository implements PositionRepository {
    private static mapToDomain(representation: PositionRepresentation): Position {
        return {
            key: representation.key,
            name: representation.name,
            imoListRank: representation.imoListRank || '',
            color: representation.color,
            prio: representation.prio,
        };
    }

    public async findAll(): Promise<Position[]> {
        const response = await fetch('/api/v1/positions', {
            credentials: 'include',
            headers: getAccessKeyHeader(),
        });
        if (response.ok) {
            const positions = (await response.clone().json()) as PositionRepresentation[];
            return positions.map(PositionRestRepository.mapToDomain);
        } else {
            throw response;
        }
    }

    public async create(position: Position): Promise<Position> {
        const requestBody: CreatePositionRequest = {
            key: position.key,
            name: position.name,
            imoListRank: position.imoListRank,
            color: position.color,
            prio: position.prio,
        };
        const response = await fetch('/api/v1/positions', {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                ...getAccessKeyHeader(),
                ...getCsrfTokenHeader(),
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
            imoListRank: position.imoListRank,
            color: position.color,
            prio: position.prio,
        };
        const response = await fetch(`/api/v1/positions/${positionKey}`, {
            method: 'PUT',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                ...getAccessKeyHeader(),
                ...getCsrfTokenHeader(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation = (await response.clone().json()) as PositionRepresentation;
        return PositionRestRepository.mapToDomain(representation);
    }

    public async deleteByKey(positionKey: PositionKey): Promise<void> {
        const response = await fetch(`/api/v1/positions/${positionKey}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                ...getAccessKeyHeader(),
                ...getCsrfTokenHeader(),
            },
        });
        if (!response.ok) {
            throw response;
        }
    }
}
