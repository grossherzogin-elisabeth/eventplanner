import { EventService, PositionService, QualificationService, RegistrationService, UserService } from '@/domain/index.ts';

let userService: UserService | undefined;
let eventService: EventService | undefined;
let registrationService: RegistrationService | undefined;
let qualificationService: QualificationService | undefined;
let positionService: PositionService | undefined;

export function useUserService(): UserService {
    if (!userService) {
        userService = new UserService();
    }
    return userService;
}

export function useEventService(): EventService {
    if (!eventService) {
        eventService = new EventService();
    }
    return eventService;
}

export function useRegistrationService(): RegistrationService {
    if (!registrationService) {
        registrationService = new RegistrationService();
    }
    return registrationService;
}

export function useQualificationService(): QualificationService {
    if (!qualificationService) {
        qualificationService = new QualificationService();
    }
    return qualificationService;
}

export function usePositionService(): PositionService {
    if (!positionService) {
        positionService = new PositionService();
    }
    return positionService;
}
