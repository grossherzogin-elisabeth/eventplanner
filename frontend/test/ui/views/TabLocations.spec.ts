import type { Router } from 'vue-router';
import { createWebHistory } from 'vue-router';
import { createRouter } from 'vue-router';
import { setupRouter } from '@/ui/plugins/router.ts';
import TabLocations from '@/ui/views/events/edit/TabLocations.vue';
import { mount } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import {
    given,
    mockAccountRepresentation,
    mockConfigRepresentation,
    mockEvent,
    mockPositionRepresentations,
    mockQualificationRepresentations,
    mockSignedInUser,
    mockUserRepresentations,
} from '~/mocks';

describe('TabLocations', () => {
    let router: Router = createRouter({ history: createWebHistory(import.meta.env.BASE_URL), routes: [] });

    beforeEach(() => {
        const AuthUseCase = vi.fn();
        AuthUseCase.prototype.firstAuthentication = vi.fn(() => Promise.resolve(undefined));
        AuthUseCase.prototype.isLoggedIn = vi.fn(() => true);
        AuthUseCase.prototype.getSignedInUser = vi.fn(() => undefined);
        AuthUseCase.prototype.getSignedInUser = vi.fn(() => mockSignedInUser());
        router = setupRouter(new AuthUseCase());
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('should fetch event data initially', () => {
        given.get('/api/v1/config').willReturn(200, mockConfigRepresentation());
        given.get('/api/v1/account').willReturn(200, mockAccountRepresentation());
        given.get('/api/v1/positions').willReturn(200, mockPositionRepresentations());
        given.get('/api/v1/qualifications').willReturn(200, mockQualificationRepresentations());
        given.get('/api/v1/users').willReturn(200, mockUserRepresentations());
        given.get('/api/v1/events/export/templates').willReturn(200, []);

        const testee = mount(TabLocations, {
            props: { event: mockEvent() },
            global: {
                plugins: [router],
            },
        });
        expect(testee.exists()).toBe(true);
    });
});
