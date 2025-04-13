import type { Config } from '@/application';
import {
    AuthUseCase,
    EventAdministrationUseCase,
    EventUseCase,
    PositionAdministrationUseCase,
    PositionUseCase,
    QualificationAdministrationUseCase,
    QualificationUseCase,
    UserAdministrationUseCase,
    UsersUseCase,
} from '@/application';
import { AppSettingsUseCase } from '@/application/usecases/AppSettingsUseCase';
import type { Adapters } from '@/initAdapters';
import type { ApplicationServices } from '@/initApplicationServices';
import type { DomainServices } from '@/initDomainServices';

export interface UseCases {
    authUseCase: AuthUseCase;
    eventUseCase: EventUseCase;
    usersUseCase: UsersUseCase;
    userAdministrationUseCase: UserAdministrationUseCase;
    eventAdministrationUseCase: EventAdministrationUseCase;
    appSettingsUseCase: AppSettingsUseCase;
    positionUseCase: PositionUseCase;
    positionAdministrationUseCase: PositionAdministrationUseCase;
    qualificationUseCase: QualificationUseCase;
    qualificationAdministrationUseCase: QualificationAdministrationUseCase;
}

export function initUseCases(params: {
    config: Config;
    applicationServices: ApplicationServices;
    domainServices: DomainServices;
    adapters: Adapters;
}): UseCases {
    return {
        authUseCase: new AuthUseCase({
            config: params.config,
            authService: params.applicationServices.authService,
            accountRepository: params.adapters.accountRepository,
            userRepository: params.adapters.userRepository,
        }),
        eventUseCase: new EventUseCase({
            authService: params.applicationServices.authService,
            notificationService: params.applicationServices.notificationService,
            errorHandlingService: params.applicationServices.errorHandlingService,
            eventCachingService: params.applicationServices.eventCachingService,
            eventRegistrationsRepository: params.adapters.eventRegistrationsRepository,
            eventRepository: params.adapters.eventRepository,
            userCachingService: params.applicationServices.userCachingService,
            positionCachingService: params.applicationServices.positionCachingService,
            registrationService: params.domainServices.registrations,
            eventService: params.domainServices.events,
            calendarService: params.applicationServices.calendarService,
        }),
        usersUseCase: new UsersUseCase({
            config: params.config,
            authService: params.applicationServices.authService,
            notificationService: params.applicationServices.notificationService,
            errorHandlingService: params.applicationServices.errorHandlingService,
            registrationService: params.domainServices.registrations,
            userRepository: params.adapters.userRepository,
            positionCachingService: params.applicationServices.positionCachingService,
            userCachingService: params.applicationServices.userCachingService,
            qualificationCachingService: params.applicationServices.qualificationCachingService,
        }),
        userAdministrationUseCase: new UserAdministrationUseCase({
            notificationService: params.applicationServices.notificationService,
            errorHandlingService: params.applicationServices.errorHandlingService,
            userRepository: params.adapters.userRepository,
            userCachingService: params.applicationServices.userCachingService,
        }),
        eventAdministrationUseCase: new EventAdministrationUseCase({
            notificationService: params.applicationServices.notificationService,
            errorHandlingService: params.applicationServices.errorHandlingService,
            eventRepository: params.adapters.eventRepository,
            eventCachingService: params.applicationServices.eventCachingService,
            eventService: params.domainServices.events,
            authService: params.applicationServices.authService,
            eventRegistrationsRepository: params.adapters.eventRegistrationsRepository,
        }),
        appSettingsUseCase: new AppSettingsUseCase({
            notificationService: params.applicationServices.notificationService,
            errorHandlingService: params.applicationServices.errorHandlingService,
            settingsRepository: params.adapters.settingsRepository,
        }),
        positionUseCase: new PositionUseCase({
            errorHandlingService: params.applicationServices.errorHandlingService,
            positionCachingService: params.applicationServices.positionCachingService,
        }),
        positionAdministrationUseCase: new PositionAdministrationUseCase({
            notificationService: params.applicationServices.notificationService,
            errorHandlingService: params.applicationServices.errorHandlingService,
            positionRepository: params.adapters.positionRepository,
            positionCachingService: params.applicationServices.positionCachingService,
        }),
        qualificationUseCase: new QualificationUseCase({
            errorHandlingService: params.applicationServices.errorHandlingService,
            qualificationCachingService: params.applicationServices.qualificationCachingService,
        }),
        qualificationAdministrationUseCase: new QualificationAdministrationUseCase({
            notificationService: params.applicationServices.notificationService,
            errorHandlingService: params.applicationServices.errorHandlingService,
            qualificationRepository: params.adapters.qualificationRepository,
            qualificationCachingService: params.applicationServices.qualificationCachingService,
        }),
    };
}
