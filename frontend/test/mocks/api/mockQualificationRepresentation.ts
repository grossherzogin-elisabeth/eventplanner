import type { QualificationRepresentation } from '@/adapter/rest/QualificationRestRepository';
import {
    CAPTAIN,
    DECKHAND,
    ENGINEER,
    MATE,
    QUALIFICATION_CAPTAIN,
    QUALIFICATION_DECKHAND,
    QUALIFICATION_ENGINEER,
    QUALIFICATION_EXPIRES,
    QUALIFICATION_GENERIC,
    QUALIFICATION_MATE,
} from '~/mocks/keys';

export function mockQualificationRepresentationCaptain(overwrite?: Partial<QualificationRepresentation>): QualificationRepresentation {
    const qualification: QualificationRepresentation = {
        key: QUALIFICATION_CAPTAIN,
        name: 'Captain',
        description: 'Grants the captain position',
        expires: true,
        icon: 'fa-anchor',
        grantsPositions: [CAPTAIN],
    };
    return overwrite ? Object.assign(qualification, overwrite) : qualification;
}

export function mockQualificationRepresentationEngineer(overwrite?: Partial<QualificationRepresentation>): QualificationRepresentation {
    const qualification: QualificationRepresentation = {
        key: QUALIFICATION_ENGINEER,
        name: 'Engineer',
        description: 'Grants the engineer position',
        expires: false,
        icon: 'fa-anchor',
        grantsPositions: [ENGINEER],
    };
    return overwrite ? Object.assign(qualification, overwrite) : qualification;
}

export function mockQualificationRepresentationMate(overwrite?: Partial<QualificationRepresentation>): QualificationRepresentation {
    const qualification: QualificationRepresentation = {
        key: QUALIFICATION_MATE,
        name: 'Mate',
        description: 'Grants the mate position',
        expires: false,
        icon: 'fa-anchor',
        grantsPositions: [MATE],
    };
    return overwrite ? Object.assign(qualification, overwrite) : qualification;
}

export function mockQualificationRepresentationDeckhand(overwrite?: Partial<QualificationRepresentation>): QualificationRepresentation {
    const qualification: QualificationRepresentation = {
        key: QUALIFICATION_DECKHAND,
        name: 'Deckhand',
        description: 'Grants the deckhand position',
        expires: false,
        icon: 'fa-anchor',
        grantsPositions: [DECKHAND],
    };
    return overwrite ? Object.assign(qualification, overwrite) : qualification;
}

export function mockQualificationRepresentationExpires(overwrite?: Partial<QualificationRepresentation>): QualificationRepresentation {
    const qualification: QualificationRepresentation = {
        key: QUALIFICATION_EXPIRES,
        name: 'Expires',
        description: 'Expires and grants no position',
        expires: true,
        icon: 'fa-anchor',
        grantsPositions: [],
    };
    return overwrite ? Object.assign(qualification, overwrite) : qualification;
}

export function mockQualificationRepresentationGeneric(overwrite?: Partial<QualificationRepresentation>): QualificationRepresentation {
    const qualification: QualificationRepresentation = {
        key: QUALIFICATION_GENERIC,
        name: 'Generic',
        description: 'Grants no position',
        expires: false,
        icon: 'fa-anchor',
        grantsPositions: [],
    };
    return overwrite ? Object.assign(qualification, overwrite) : qualification;
}

export function mockQualificationRepresentations(): QualificationRepresentation[] {
    return [
        mockQualificationRepresentationCaptain(),
        mockQualificationRepresentationEngineer(),
        mockQualificationRepresentationMate(),
        mockQualificationRepresentationDeckhand(),
        mockQualificationRepresentationExpires(),
        mockQualificationRepresentationGeneric(),
    ];
}
