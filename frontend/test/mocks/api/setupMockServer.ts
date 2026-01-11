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

export function mockGetPositions(response?: PositionRepresentation[], status: number = 200): RequestHandler {
    return http.get('/api/v1/positions', () => HttpResponse.json(response ?? mockPositionRepresentations(), { status }));
}

export function mockDeletePosition(status: number = 200): RequestHandler {
    return http.delete<{ key: string }>('/api/v1/positions/:key', () => HttpResponse.text('', { status }));
}

export function mockPutPosition(status: number = 200): RequestHandler {
    return http.put<{ key: string }>('/api/v1/positions/:key', async ({ request }) => {
        const body = await request.clone().json();
        return HttpResponse.json(body, { status });
    });
}

export function mockPostPosition(status: number = 200): RequestHandler {
    return http.post<{ key: string }>('/api/v1/positions', async ({ request }) => {
        const body = await request.clone().json();
        return HttpResponse.json(body, { status });
    });
}

export function mockGetQualifications(response?: QualificationRepresentation[], status: number = 200): RequestHandler {
    return http.get('/api/v1/qualifications', () => HttpResponse.json(response ?? mockQualificationRepresentations(), { status }));
}

export function mockDeleteQualification(status: number = 200): RequestHandler {
    return http.delete<{ key: string }>('/api/v1/qualifications/:key', () => HttpResponse.text('', { status }));
}

export function mockPutQualification(status: number = 200): RequestHandler {
    return http.put<{ key: string }>('/api/v1/qualifications/:key', async ({ request }) => {
        const body = await request.clone().json();
        return HttpResponse.json(body, { status });
    });
}

export function mockPostQualification(status: number = 200): RequestHandler {
    return http.post<{ key: string }>('/api/v1/qualifications', async ({ request }) => {
        const body = await request.clone().json();
        return HttpResponse.json(body, { status });
    });
}

export function mockUsersRequest(response?: UserRepresentation[], status: number = 200): RequestHandler {
    return http.get('/api/v1/users', () => HttpResponse.json(response ?? mockUserRepresentations(), { status }));
}

export function mockEventListRequest(events?: EventRepresentation[], status: number = 200): RequestHandler {
    const responses: EventRepresentation[] = events ?? [mockEventRepresentation()];
    return http.get('api/v1/events', () => HttpResponse.json(responses, { status }));
}

export function mockEventDetailsRequests(events?: EventRepresentation[], status: number = 200): RequestHandler {
    const responses: EventRepresentation[] = events ?? [mockEventRepresentation()];
    return http.get<{ key: string }>('/api/v1/events/:key', ({ params }) => {
        const response = responses.find((response) => response.key === params.key);
        if (response) {
            return HttpResponse.json(response, { status });
        } else {
            return HttpResponse.json({}, { status: 404 });
        }
    });
}

export function mockEventUpdate(response?: EventRepresentation, status: number = 200): RequestHandler {
    return http.patch<{ key: string }, EventRepresentation>('/api/v1/events/:key', async ({ request, params }) => {
        const patch = await request.clone().json();
        const patched = response ?? mockEventRepresentation(patch);
        patched.key = params.key;
        return HttpResponse.json(patched, { status });
    });
}

export function mockEventCreate(response?: EventRepresentation, status: number = 200): RequestHandler {
    return http.post<object, EventRepresentation>('/api/v1/events', async ({ request }) => {
        const event: EventRepresentation = response ?? (await request.clone().json());
        event.registrations = [];
        return HttpResponse.json(event, { status });
    });
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
        mockGetPositions(),
        mockDeletePosition(),
        mockPutPosition(),
        mockPostPosition(),
        mockGetQualifications(),
        mockPutQualification(),
        mockDeleteQualification(),
        mockPostQualification(),
        mockUsersRequest(),
        mockEventTemplatesRequest(),
        mockEventListRequest(),
        mockEventDetailsRequests(),
        mockEventUpdate(),
        mockEventCreate()
    );
}

export const server = setupDefaultMockServer();
