import {
    AccountRestRepository,
    EventRegistrationRestRepository,
    EventRestRepository,
    PositionRestRepository,
    QualificationRestRepository,
    UserRestRepository,
} from '@/adapter';
import type {
    AccountRepository,
    Application,
    EventRegistrationsRepository,
    EventRepository,
    PositionRepository,
    QualificationRepository,
    UserRepository,
} from '@/application';
import { NotificationService } from '@/application';
import {
    AuthUseCase,
    EventAdministrationUseCase,
    EventCachingService,
    EventUseCase,
    PositionCachingService,
    QualificationCachingService,
    UserAdministrationUseCase,
    UserCachingService,
    UsersUseCase,
} from '@/application';
import { AuthService } from '@/application/services/AuthService';
import { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import { IndexedDB, IndexedDBRepository } from '@/common';
import type { Domain } from '@/domain';
import { EventService, RegistrationService, UserService } from '@/domain';
import { setupVue } from '@/ui';
import { config } from './config';

enum StoreNames {
    Events = 'events',
    Users = 'users',
    Positions = 'positions',
    Qualifications = 'qualifications',
}

// -----------------------------------------------------
// initialize indexed db
// -----------------------------------------------------
const indexedDB = IndexedDB.getConnection('lissi', Object.values(StoreNames), 3);

// -----------------------------------------------------
// initialize domain services
// -----------------------------------------------------
const domain: Domain = {
    services: {
        users: new UserService(),
        events: new EventService(),
        registrations: new RegistrationService(),
    },
};

// -----------------------------------------------------
// initialize adapters
// -----------------------------------------------------
const accountRepository: AccountRepository = new AccountRestRepository();
const positionRepository: PositionRepository = new PositionRestRepository();
const userRepository: UserRepository = new UserRestRepository();
const eventRepository: EventRepository = new EventRestRepository();
const eventRegistrationsRepository: EventRegistrationsRepository = new EventRegistrationRestRepository();
const qualificationRepository: QualificationRepository = new QualificationRestRepository();

// -----------------------------------------------------
// initialize use cases and application services
// -----------------------------------------------------
const authService = new AuthService({
    config: config,
});
const eventCachingService = new EventCachingService({
    cache: new IndexedDBRepository(indexedDB, StoreNames.Events, { invalidateOnReload: true }),
    eventRepository: eventRepository,
    authService: authService,
});
const userCachingService = new UserCachingService({
    cache: new IndexedDBRepository(indexedDB, StoreNames.Users, {
        invalidateOnReload: true,
    }),
    userRepository: userRepository,
});
const positionCachingService = new PositionCachingService({
    cache: new IndexedDBRepository(indexedDB, StoreNames.Positions, {
        invalidateOnReload: true,
    }),
    positionRepository: positionRepository,
});
const qualificationCachingService = new QualificationCachingService({
    cache: new IndexedDBRepository(indexedDB, StoreNames.Qualifications, {
        invalidateOnReload: true,
    }),
    qualificationRepository: qualificationRepository,
});
const notificationService = new NotificationService();
const errorHandlingService = new ErrorHandlingService();

const application: Application = {
    config: config,
    services: {
        eventCache: eventCachingService,
        userCache: userCachingService,
        positionCache: positionCachingService,
        notifications: notificationService,
        errorHandling: errorHandlingService,
    },
    usecases: {
        auth: new AuthUseCase({
            config: config,
            accountRepository: accountRepository,
            authService: authService,
            userRepository: userRepository,
        }),
        events: new EventUseCase({
            authService: authService,
            notificationService: notificationService,
            errorHandlingService: errorHandlingService,
            eventRepository: eventRepository,
            eventCachingService: eventCachingService,
            eventRegistrationsRepository: eventRegistrationsRepository,
        }),
        users: new UsersUseCase({
            config: config,
            notificationService: notificationService,
            errorHandlingService: errorHandlingService,
            registrationService: domain.services.registrations,
            userRepository: userRepository,
            positionCachingService: positionCachingService,
            userCachingService: userCachingService,
            qualificationCachingService: qualificationCachingService,
        }),
        userAdmin: new UserAdministrationUseCase({
            notificationService: notificationService,
            errorHandlingService: errorHandlingService,
            userRepository: userRepository,
            userCachingService: userCachingService,
        }),
        eventAdmin: new EventAdministrationUseCase({
            notificationService: notificationService,
            errorHandlingService: errorHandlingService,
            eventRepository: eventRepository,
            eventCachingService: eventCachingService,
            eventRegistrationsRepository: eventRegistrationsRepository,
        }),
    },
};

// -----------------------------------------------------
// initialize ui
// -----------------------------------------------------
setupVue({ domain, application });
