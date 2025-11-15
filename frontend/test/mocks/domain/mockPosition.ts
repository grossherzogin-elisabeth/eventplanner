import type { Position } from '@/domain';
import { CAPTAIN, DECKHAND, ENGINEER, MATE } from '~/mocks/keys';

export function mockPositionCaptain(overwrite?: Partial<Position>): Position {
    const position: Position = {
        key: CAPTAIN,
        name: 'Captain',
        color: '#ffffff',
        prio: 0,
        imoListRank: 'Master',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositionEngineer(overwrite?: Partial<Position>): Position {
    const position: Position = {
        key: ENGINEER,
        name: 'Engineer',
        color: '#ffffff',
        prio: 1,
        imoListRank: 'Engineer',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositionMate(overwrite?: Partial<Position>): Position {
    const position: Position = {
        key: MATE,
        name: 'Mate',
        color: '#ffffff',
        prio: 2,
        imoListRank: 'Mate',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositionDeckhand(overwrite?: Partial<Position>): Position {
    const position: Position = {
        key: DECKHAND,
        name: 'Deckhand',
        color: '#ffffff',
        prio: 3,
        imoListRank: 'Deckhand',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositions(): Position[] {
    return [mockPositionCaptain(), mockPositionEngineer(), mockPositionMate(), mockPositionDeckhand()];
}
