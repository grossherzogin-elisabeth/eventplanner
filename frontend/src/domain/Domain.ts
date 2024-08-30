import type { RegistrationService } from '@/domain/services/RegistrationService';
import type { EventService } from './services/EventService';
import type { UserService } from './services/UserService';

export interface Domain {
    services: {
        events: EventService;
        users: UserService;
        registrations: RegistrationService;
    };
}
