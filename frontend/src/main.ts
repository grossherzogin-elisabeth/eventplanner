import {
    AccountRestRepository,
    EventRegistrationRestRepository,
    EventRestRepository,
    PositionRestRepository,
    QualificationRestRepository,
    UserRestRepository,
} from '@/adapter';
import { SettingsRestRepository } from '@/adapter/rest/SettingsRestRepository';
import type {
    AccountRepository,
    Application,
    EventRegistrationsRepository,
    EventRepository,
    PositionRepository,
    QualificationRepository,
    SettingsRepository,
    UserRepository,
} from '@/application';
import { CalendarService } from '@/application';
import { QualificationUseCase } from '@/application';
import { PositionAdministrationUseCase, PositionUseCase, QualificationAdministrationUseCase } from '@/application';
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
import { AppSettingsUseCase } from '@/application/usecases/AppSettingsUseCase';
import { IndexedDBRepository, getConnection } from '@/common';
import type { Domain } from '@/domain';
import { PositionService } from '@/domain';
import { QualificationService } from '@/domain';
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
const indexedDB = getConnection('lissi', Object.values(StoreNames), 3);

// -----------------------------------------------------
// initialize domain services
// -----------------------------------------------------
const domain: Domain = {
    services: {
        users: new UserService(),
        events: new EventService(),
        registrations: new RegistrationService(),
        qualifications: new QualificationService(),
        positions: new PositionService(),
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
const settingsRepository: SettingsRepository = new SettingsRestRepository();

async function init(): Promise<void> {
    const serverConfig = await settingsRepository.readConfig();
    config.menuTitle = serverConfig.menuTitle || 'Reiseplaner';
    config.tabTitle = serverConfig.tabTitle || 'Reiseplaner';
    config.supportEmail = serverConfig.supportEmail || 'support@example.de';
    config.technicalSupportEmail = serverConfig.technicalSupportEmail || 'support@example.de';

    // -----------------------------------------------------
    // initialize use cases and application services
    // -----------------------------------------------------
    const authService = new AuthService();
    const eventCachingService = new EventCachingService({
        cache: new IndexedDBRepository(indexedDB, StoreNames.Events, { invalidateOnReload: true }),
        eventRepository: eventRepository,
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
    const errorHandlingService = new ErrorHandlingService(() => accountRepository.login(location.pathname));
    const calendarService = new CalendarService();

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
                eventCachingService: eventCachingService,
                eventRegistrationsRepository: eventRegistrationsRepository,
                eventRepository: eventRepository,
                userCachingService: userCachingService,
                positionCachingService: positionCachingService,
                registrationService: domain.services.registrations,
                eventService: domain.services.events,
                calendarService: calendarService,
            }),
            users: new UsersUseCase({
                config: config,
                authService: authService,
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
                eventService: domain.services.events,
                authService: authService,
                eventRegistrationsRepository: eventRegistrationsRepository,
            }),
            appSettings: new AppSettingsUseCase({
                notificationService: notificationService,
                errorHandlingService: errorHandlingService,
                settingsRepository: settingsRepository,
            }),
            positions: new PositionUseCase({
                errorHandlingService: errorHandlingService,
                positionCachingService: positionCachingService,
            }),
            positionAdmin: new PositionAdministrationUseCase({
                notificationService: notificationService,
                errorHandlingService: errorHandlingService,
                positionRepository: positionRepository,
                positionCachingService: positionCachingService,
            }),
            qualifications: new QualificationUseCase({
                errorHandlingService: errorHandlingService,
                qualificationCachingService: qualificationCachingService,
            }),
            qualificationAdmin: new QualificationAdministrationUseCase({
                notificationService: notificationService,
                errorHandlingService: errorHandlingService,
                qualificationRepository: qualificationRepository,
                qualificationCachingService: qualificationCachingService,
            }),
        },
    };

    // -----------------------------------------------------
    // initialize ui
    // -----------------------------------------------------
    setupVue({ domain, application });
}
init();
