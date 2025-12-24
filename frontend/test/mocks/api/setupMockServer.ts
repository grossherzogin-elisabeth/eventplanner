import type { RequestHandler } from 'msw';
import { HttpResponse, http } from 'msw';
import type { SetupServerApi } from 'msw/node';
import { setupServer } from 'msw/node';
import type { AccountRepresentation } from '@/adapter/rest/AccountRestRepository';
import type { EventRepresentation } from '@/adapter/rest/EventRestRepository.ts';
import type { PositionRepresentation } from '@/adapter/rest/PositionRestRepository.ts';
import type { QualificationRepresentation } from '@/adapter/rest/QualificationRestRepository.ts';
import type { UiSettingsRepresentation } from '@/adapter/rest/SettingsRestRepository';
import type { UserRepresentation } from '@/adapter/rest/UserRestRepository.ts';
import {
    mockAccountRepresentation,
    mockConfigRepresentation,
    mockEventRepresentation,
    mockPositionRepresentations,
    mockQualificationRepresentations,
    mockUserRepresentations,
} from '~/mocks';

export function mockAccountRequest(response?: AccountRepresentation, status: number = 200): RequestHandler {
    return http.get('/api/v1/account', () => HttpResponse.json(response ?? mockAccountRepresentation(), { status }));
}

export function mockConfigRequest(response?: UiSettingsRepresentation, status: number = 200): RequestHandler {
    return http.get('/api/v1/config', () => HttpResponse.json(response ?? mockConfigRepresentation(), { status }));
}

export function mockPositionsRequest(response?: PositionRepresentation[], status: number = 200): RequestHandler {
    return http.get('/api/v1/positions', () => HttpResponse.json(response ?? mockPositionRepresentations(), { status }));
}

export function mockQualificationsRequest(response?: QualificationRepresentation[], status: number = 200): RequestHandler {
    return http.get('/api/v1/qualifications', () => HttpResponse.json(response ?? mockQualificationRepresentations(), { status }));
}

export function mockUsersRequest(response?: UserRepresentation[], status: number = 200): RequestHandler {
    return http.get('/api/v1/users', () => HttpResponse.json(response ?? mockUserRepresentations(), { status }));
}

export function mockEventRequests(events?: EventRepresentation[], status: number = 200): RequestHandler[] {
    const responses: EventRepresentation[] = events ?? [mockEventRepresentation()];
    const eventDetailRequests: RequestHandler[] = responses.map((eventResponse) =>
        http.get('/api/v1/events/' + eventResponse.key, () => HttpResponse.json(eventResponse, { status }))
    );

    const eventListRequest: RequestHandler = http.get('api/v1/events', () => HttpResponse.json(responses, { status }));
    return [eventListRequest, ...eventDetailRequests];
}

export function mockEventTemplatesRequest(response?: string[], status: number = 200): RequestHandler {
    return http.get('/api/v1/events/export/templates', () =>
        HttpResponse.json(response ?? ['some template', 'some other template'], { status })
    );
}

export function setupDefaultMockServer(): SetupServerApi {
    return setupServer(
        mockAccountRequest(),
        mockConfigRequest(),
        mockPositionsRequest(),
        mockQualificationsRequest(),
        mockUsersRequest(),
        mockEventTemplatesRequest(),
        ...mockEventRequests()
    );
}

export const server = setupDefaultMockServer();
