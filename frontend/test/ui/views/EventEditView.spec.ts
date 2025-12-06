import type { Router } from 'vue-router';
import { createWebHistory } from 'vue-router';
import { createRouter } from 'vue-router';
import { setupRouter } from '@/ui/plugins/router.ts';
import EventEditView from '@/ui/views/events/edit/EventEditView.vue';
import { mount } from '@vue/test-utils';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { mockEvent, mockSignedInUser, setupDefaultMockServer } from '~/mocks';

describe('EventEditView', () => {
    let router: Router = createRouter({ history: createWebHistory(import.meta.env.BASE_URL), routes: [] });

    beforeEach(() => {
        const AuthUseCase = vi.fn();
        AuthUseCase.prototype.firstAuthentication = vi.fn(() => Promise.resolve(undefined));
        AuthUseCase.prototype.isLoggedIn = vi.fn(() => true);
        AuthUseCase.prototype.getSignedInUser = vi.fn(() => mockSignedInUser());
        router = setupRouter(new AuthUseCase());
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('should fetch event data initially', () => {
        const testee = mount(EventEditView, {
            props: { event: mockEvent() },
            global: {
                plugins: [router],
            },
        });
        expect(testee.exists()).toBe(true);
    });
});
