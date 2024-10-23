import { inject } from 'vue';
import type { EventService, PositionService, QualificationService, RegistrationService, UserService } from '@/domain';

export const USER_SERVICE = 'domain.service.user';
export const EVENT_SERVICE = 'domain.service.event';
export const REGISTRATION_SERVICE = 'domain.service.registration';
export const QUALIFICATION_SERVICE = 'domain.service.qualifications';
export const POSITION_SERVICE = 'domain.service.positions';

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

export function useQualificationService(): QualificationService {
    const service = inject<QualificationService>(QUALIFICATION_SERVICE);
    if (!service) {
        throw new Error('Qualification domain service not found!');
    }
    return service;
}

export function usePositionService(): PositionService {
    const service = inject<PositionService>(POSITION_SERVICE);
    if (!service) {
        throw new Error('Position domain service not found!');
    }
    return service;
}
