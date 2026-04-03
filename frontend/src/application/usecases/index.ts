import {
    useAccountRepository,
    useEventRegistrationsRepository,
    useEventRepository,
    usePositionRepository,
    useQualificationRepository,
    useSettingsRepository,
    useUserRepository,
} from '@/adapter';
import {
    useAuthService,
    useCalendarService,
    useConfigService,
    useErrorHandlingService,
    useEventCachingService,
    useNotificationService,
    usePositionCachingService,
    useQualificationCachingService,
    useUserCachingService,
} from '@/application/services';
import { useEventService, useQualificationService, useRegistrationService } from '@/domain';
import { AppSettingsUseCase } from './AppSettingsUseCase';
import { AuthUseCase } from './AuthUseCase';
import { EventAdministrationUseCase } from './EventAdministrationUseCase';
import { EventExportUseCase } from './EventExportUseCase';
import { EventUseCase } from './EventUseCase';
import { PositionAdministrationUseCase } from './PositionAdministrationUseCase';
import { PositionUseCase } from './PositionUseCase';
import { QualificationAdministrationUseCase } from './QualificationAdministrationUseCase';
import { QualificationUseCase } from './QualificationUseCase';
import { UserAdministrationUseCase } from './UserAdministrationUseCase';
import { UsersUseCase } from './UsersUseCase';

export {
    EventUseCase,
    UsersUseCase,
    AuthUseCase,
    EventAdministrationUseCase,
    UserAdministrationUseCase,
    QualificationUseCase,
    QualificationAdministrationUseCase,
    PositionUseCase,
    PositionAdministrationUseCase,
    AppSettingsUseCase,
};

let authUseCase: AuthUseCase | undefined;
let eventUseCase: EventUseCase | undefined;
let eventAdministrationUseCase: EventAdministrationUseCase | undefined;
let eventExportUseCase: EventExportUseCase | undefined;
let usersUseCase: UsersUseCase | undefined;
let userAdministrationUseCase: UserAdministrationUseCase | undefined;
let appSettingsUseCase: AppSettingsUseCase | undefined;
let qualificationUseCase: QualificationUseCase | undefined;
let qualificationAdministrationUseCase: QualificationAdministrationUseCase | undefined;
let positionUseCase: PositionUseCase | undefined;
let positionAdministrationUseCase: PositionAdministrationUseCase | undefined;

export function resetUseCases(): void {
    authUseCase = undefined;
    eventUseCase = undefined;
    eventAdministrationUseCase = undefined;
    eventExportUseCase = undefined;
    usersUseCase = undefined;
    userAdministrationUseCase = undefined;
    appSettingsUseCase = undefined;
    qualificationUseCase = undefined;
    qualificationAdministrationUseCase = undefined;
    positionUseCase = undefined;
    positionAdministrationUseCase = undefined;
}

export function useAuthUseCase(): AuthUseCase {
    if (!authUseCase) {
        console.log('🚀 Initializing AuthUseCase');
        authUseCase = new AuthUseCase({
            configService: useConfigService(),
            authService: useAuthService(),
            accountRepository: useAccountRepository(),
            userRepository: useUserRepository(),
            notificationService: useNotificationService(),
        });
    }
    return authUseCase;
}

export function useEventUseCase(): EventUseCase {
    if (!eventUseCase) {
        console.log('🚀 Initializing EventUseCase');
        eventUseCase = new EventUseCase({
            notificationService: useNotificationService(),
            errorHandlingService: useErrorHandlingService(),
            eventCachingService: useEventCachingService(),
            authService: useAuthService(),
            eventService: useEventService(),
            userCachingService: useUserCachingService(),
            positionCachingService: usePositionCachingService(),
            calendarService: useCalendarService(),
            registrationService: useRegistrationService(),
            eventRegistrationsRepository: useEventRegistrationsRepository(),
            eventRepository: useEventRepository(),
        });
    }
    return eventUseCase;
}

export function useEventAdministrationUseCase(): EventAdministrationUseCase {
    if (!eventAdministrationUseCase) {
        console.log('🚀 Initializing EventAdministrationUseCase');
        eventAdministrationUseCase = new EventAdministrationUseCase({
            authService: useAuthService(),
            errorHandlingService: useErrorHandlingService(),
            eventCachingService: useEventCachingService(),
            eventRegistrationsRepository: useEventRegistrationsRepository(),
            eventRepository: useEventRepository(),
            eventService: useEventService(),
            notificationService: useNotificationService(),
        });
    }
    return eventAdministrationUseCase;
}

export function useEventExportUseCase(): EventExportUseCase {
    if (!eventExportUseCase) {
        console.log('🚀 Initializing EventExportUseCase');
        eventExportUseCase = new EventExportUseCase({
            errorHandlingService: useErrorHandlingService(),
            authService: useAuthService(),
            eventRepository: useEventRepository(),
        });
    }
    return eventExportUseCase;
}

export function useUsersUseCase(): UsersUseCase {
    if (!usersUseCase) {
        console.log('🚀 Initializing UsersUseCase');
        usersUseCase = new UsersUseCase({
            authService: useAuthService(),
            configService: useConfigService(),
            errorHandlingService: useErrorHandlingService(),
            notificationService: useNotificationService(),
            positionCachingService: usePositionCachingService(),
            registrationService: useRegistrationService(),
            userCachingService: useUserCachingService(),
            userRepository: useUserRepository(),
        });
    }
    return usersUseCase;
}

export function useUserAdministrationUseCase(): UserAdministrationUseCase {
    if (!userAdministrationUseCase) {
        console.log('🚀 Initializing UserAdministrationUseCase');
        userAdministrationUseCase = new UserAdministrationUseCase({
            errorHandlingService: useErrorHandlingService(),
            notificationService: useNotificationService(),
            userCachingService: useUserCachingService(),
            userRepository: useUserRepository(),
        });
    }
    return userAdministrationUseCase;
}

export function useAppSettingsUseCase(): AppSettingsUseCase {
    if (!appSettingsUseCase) {
        console.log('🚀 Initializing AppSettingsUseCase');
        appSettingsUseCase = new AppSettingsUseCase({
            errorHandlingService: useErrorHandlingService(),
            notificationService: useNotificationService(),
            settingsRepository: useSettingsRepository(),
        });
    }
    return appSettingsUseCase;
}

export function useQualificationsUseCase(): QualificationUseCase {
    if (!qualificationUseCase) {
        console.log('🚀 Initializing QualificationUseCase');
        qualificationUseCase = new QualificationUseCase({
            errorHandlingService: useErrorHandlingService(),
            qualificationCachingService: useQualificationCachingService(),
        });
    }
    return qualificationUseCase;
}

export function useQualificationsAdministrationUseCase(): QualificationAdministrationUseCase {
    if (!qualificationAdministrationUseCase) {
        console.log('🚀 Initializing QualificationAdministrationUseCase');
        qualificationAdministrationUseCase = new QualificationAdministrationUseCase({
            errorHandlingService: useErrorHandlingService(),
            qualificationService: useQualificationService(),
            notificationService: useNotificationService(),
            qualificationCachingService: useQualificationCachingService(),
            qualificationRepository: useQualificationRepository(),
        });
    }
    return qualificationAdministrationUseCase;
}

export function usePositionUseCase(): PositionUseCase {
    if (!positionUseCase) {
        console.log('🚀 Initializing PositionUseCase');
        positionUseCase = new PositionUseCase({
            errorHandlingService: useErrorHandlingService(),
            positionCachingService: usePositionCachingService(),
        });
    }
    return positionUseCase;
}

export function usePositionAdministrationUseCase(): PositionAdministrationUseCase {
    if (!positionAdministrationUseCase) {
        console.log('🚀 Initializing PositionAdministrationUseCase');
        positionAdministrationUseCase = new PositionAdministrationUseCase({
            errorHandlingService: useErrorHandlingService(),
            notificationService: useNotificationService(),
            positionCachingService: usePositionCachingService(),
            positionRepository: usePositionRepository(),
        });
    }
    return positionAdministrationUseCase;
}
