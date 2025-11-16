import type { PositionRepresentation } from '@/adapter/rest/PositionRestRepository';
import { CAPTAIN, DECKHAND, ENGINEER, MATE } from '~/mocks/keys';

export function mockPositionRepresentationCaptain(overwrite?: Partial<PositionRepresentation>): PositionRepresentation {
    const position: PositionRepresentation = {
        key: CAPTAIN,
        name: 'Captain',
        color: '#ffffff',
        prio: 0,
        imoListRank: 'Master',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositionRepresentationEngineer(overwrite?: Partial<PositionRepresentation>): PositionRepresentation {
    const position: PositionRepresentation = {
        key: ENGINEER,
        name: 'Engineer',
        color: '#ffffff',
        prio: 1,
        imoListRank: 'Engineer',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositionRepresentationMate(overwrite?: Partial<PositionRepresentation>): PositionRepresentation {
    const position: PositionRepresentation = {
        key: MATE,
        name: 'Mate',
        color: '#ffffff',
        prio: 2,
        imoListRank: 'Mate',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositionRepresentationDeckhand(overwrite?: Partial<PositionRepresentation>): PositionRepresentation {
    const position: PositionRepresentation = {
        key: DECKHAND,
        name: 'Deckhand',
        color: '#ffffff',
        prio: 3,
        imoListRank: 'Deckhand',
    };
    return overwrite ? Object.assign(position, overwrite) : position;
}

export function mockPositionRepresentations(): PositionRepresentation[] {
    return [
        mockPositionRepresentationCaptain(),
        mockPositionRepresentationEngineer(),
        mockPositionRepresentationMate(),
        mockPositionRepresentationDeckhand(),
    ];
}
