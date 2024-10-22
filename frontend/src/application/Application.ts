import type { ErrorHandlingService } from './services/ErrorHandlingService';
import type { EventCachingService } from './services/EventCachingService';
import type { NotificationService } from './services/NotificationService';
import type { PositionCachingService } from './services/PositionCachingService';
import type { UserCachingService } from './services/UserCachingService';
import type { AppSettingsUseCase } from './usecases/AppSettingsUseCase';
import type { AuthUseCase } from './usecases/AuthUseCase';
import type { EventAdministrationUseCase } from './usecases/EventAdministrationUseCase';
import type { EventUseCase } from './usecases/EventUseCase';
import type { PositionAdministrationUseCase } from './usecases/PositionAdministrationUseCase';
import type { PositionUseCase } from './usecases/PositionUseCase';
import type { QualificationAdministrationUseCase } from './usecases/QualificationAdministrationUseCase';
import type { QualificationUseCase } from './usecases/QualificationUseCase';
import type { UserAdministrationUseCase } from './usecases/UserAdministrationUseCase';
import type { UsersUseCase } from './usecases/UsersUseCase';
import type { Config } from './values/Config';

export interface Application {
    config: Config;
    services: {
        eventCache: EventCachingService;
        positionCache: PositionCachingService;
        userCache: UserCachingService;
        notifications: NotificationService;
        errorHandling: ErrorHandlingService;
    };
    usecases: {
        auth: AuthUseCase;
        events: EventUseCase;
        eventAdmin: EventAdministrationUseCase;
        appSettings: AppSettingsUseCase;
        users: UsersUseCase;
        userAdmin: UserAdministrationUseCase;
        positions: PositionUseCase;
        positionAdmin: PositionAdministrationUseCase;
        qualifications: QualificationUseCase;
        qualificationAdmin: QualificationAdministrationUseCase;
    };
}
