import type { PositionService } from '@/domain/services/PositionService';
import type { EventService } from './services/EventService';
import type { QualificationService } from './services/QualificationService';
import type { RegistrationService } from './services/RegistrationService';
import type { UserService } from './services/UserService';

export interface Domain {
    services: {
        events: EventService;
        users: UserService;
        registrations: RegistrationService;
        qualifications: QualificationService;
        positions: PositionService;
    };
}
