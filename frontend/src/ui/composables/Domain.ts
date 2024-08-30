import { inject } from 'vue';
import type { EventService, RegistrationService, UserService } from '@/domain';

export const USER_SERVICE = 'domain.service.user';
export const EVENT_SERVICE = 'domain.service.event';
export const REGISTRATION_SERVICE = 'domain.service.registration';

export function useUserService(): UserService {
    const service = inject<UserService>(USER_SERVICE);
    if (!service) {
        throw new Error('Event domain service not found!');
    }
    return service;
}

export function useEventService(): EventService {
    const service = inject<EventService>(EVENT_SERVICE);
    if (!service) {
        throw new Error('User domain service not found!');
    }
    return service;
}

export function useRegistrationService(): RegistrationService {
    const service = inject<RegistrationService>(REGISTRATION_SERVICE);
    if (!service) {
        throw new Error('Registration domain service not found!');
    }
    return service;
}
