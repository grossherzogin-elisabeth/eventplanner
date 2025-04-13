import { EventService, PositionService, QualificationService, RegistrationService, UserService } from '@/domain';

export interface DomainServices {
    users: UserService;
    events: EventService;
    registrations: RegistrationService;
    qualifications: QualificationService;
    positions: PositionService;
}

export function initDomainServices(): DomainServices {
    return {
        users: new UserService(),
        events: new EventService(),
        registrations: new RegistrationService(),
        qualifications: new QualificationService(),
        positions: new PositionService(),
    };
}
