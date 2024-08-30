import { AuthRestRepository, EventRestRepository, PositionRestRepository, UserRestRepository } from '@/adapter';
import type { Application, AuthRepository, EventRepository, PositionRepository, UserRepository } from '@/application';
import {
    AuthUseCase,
    EventAdministrationUseCase,
    EventCachingService,
    EventUseCase,
    PositionCachingService,
    UserAdministrationUseCase,
    UserCachingService,
    UsersUseCase,
} from '@/application';
import { ErrorHandlingUseCase } from '@/application/usecases/ErrorHandlingUseCase';
import { IndexedDB, IndexedDBRepository } from '@/common';
import type { Domain } from '@/domain';
import { EventService, RegistrationService, UserService } from '@/domain';
import { setupVue } from '@/ui';
import { config } from './config';

enum StoreNames {
    Events = 'events',
    Users = 'users',
    Positions = 'positions',
}

// -----------------------------------------------------
// initialize indexed db
// -----------------------------------------------------
const indexedDB = IndexedDB.getConnection('lissi', Object.values(StoreNames), 2);

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
const authRepository: AuthRepository = new AuthRestRepository(config);
const positionRepository: PositionRepository = new PositionRestRepository();
const userRepository: UserRepository = new UserRestRepository();
const eventRepository: EventRepository = new EventRestRepository();

// -----------------------------------------------------
// initialize use cases and application services
// -----------------------------------------------------
const eventCachingService = new EventCachingService({
    cache: new IndexedDBRepository(indexedDB, StoreNames.Events, { invalidateOnReload: true }),
    eventRepository: eventRepository,
    authRepository: authRepository,
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

const application: Application = {
    config: config,
    services: {
        eventCache: eventCachingService,
        userCache: userCachingService,
        positionCache: positionCachingService,
    },
    usecases: {
        auth: new AuthUseCase({
            authRepository: authRepository,
        }),
        events: new EventUseCase({
            eventRepository: eventRepository,
            authRepository: authRepository,
            eventCachingService: eventCachingService,
        }),
        users: new UsersUseCase({
            registrationService: domain.services.registrations,
            userRepository: userRepository,
            positionCachingService: positionCachingService,
            userCachingService: userCachingService,
        }),
        userAdmin: new UserAdministrationUseCase({
            userRepository: userRepository,
            userCachingService: userCachingService,
        }),
        eventAdmin: new EventAdministrationUseCase({
            eventRepository: eventRepository,
            eventCachingService: eventCachingService,
        }),
        errorHandling: new ErrorHandlingUseCase(),
    },
};

// -----------------------------------------------------
// initialize ui
// -----------------------------------------------------
setupVue({ domain, application });
