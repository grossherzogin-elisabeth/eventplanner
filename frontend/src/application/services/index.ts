import {
    useAccountRepository,
    useEventRepository,
    usePositionRepository,
    useQualificationRepository,
    useSettingsRepository,
    useStorage,
    useUserRepository,
} from '@/adapter';
import { AuthService } from './AuthService';
import { CalendarService } from './CalendarService';
import { ConfigService } from './ConfigService';
import { ErrorHandlingService } from './ErrorHandlingService';
import { EventCachingService } from './EventCachingService';
import { NotificationService } from './NotificationService';
import { PositionCachingService } from './PositionCachingService';
import { QualificationCachingService } from './QualificationCachingService';
import { UserCachingService } from './UserCachingService';

export {
    AuthService,
    CalendarService,
    ConfigService,
    ErrorHandlingService,
    EventCachingService,
    NotificationService,
    PositionCachingService,
    QualificationCachingService,
    UserCachingService,
};

enum StoreNames {
    Events = 'events',
    Users = 'users',
    Positions = 'positions',
    Qualifications = 'qualifications',
}

let configService: ConfigService | undefined;
let errorHandlingService: ErrorHandlingService | undefined;
let notificationService: NotificationService | undefined;
let authService: AuthService | undefined;
let calendarService: CalendarService | undefined;
let eventCachingService: EventCachingService | undefined;
let positionCachingService: PositionCachingService | undefined;
let qualificationCachingService: QualificationCachingService | undefined;
let userCachingService: UserCachingService | undefined;

export function resetApplicationServices(): void {
    configService = undefined;
    errorHandlingService = undefined;
    notificationService = undefined;
    authService = undefined;
    calendarService = undefined;
    eventCachingService = undefined;
    positionCachingService = undefined;
    qualificationCachingService = undefined;
    userCachingService = undefined;
}

export function useConfigService(): ConfigService {
    if (!configService) {
        console.log('🚀 Initializing ConfigService');
        configService = new ConfigService({
            settingsRepository: useSettingsRepository(),
        });
    }
    return configService;
}

export function useErrorHandlingService(): ErrorHandlingService {
    if (!errorHandlingService) {
        console.log('🚀 Initializing ErrorHandlingService');
        errorHandlingService = new ErrorHandlingService({
            accountRepository: useAccountRepository(),
        });
    }
    return errorHandlingService;
}

export function useNotificationService(): NotificationService {
    if (!notificationService) {
        console.log('🚀 Initializing NotificationService');
        notificationService = new NotificationService();
    }
    return notificationService;
}

export function useAuthService(): AuthService {
    if (!authService) {
        console.log('🚀 Initializing AuthService');
        authService = new AuthService();
    }
    return authService;
}

export function useCalendarService(): CalendarService {
    if (!calendarService) {
        console.log('🚀 Initializing CalendarService');
        calendarService = new CalendarService();
    }
    return calendarService;
}

export function useEventCachingService(): EventCachingService {
    if (!eventCachingService) {
        console.log('🚀 Initializing EventCachingService');
        eventCachingService = new EventCachingService({
            eventRepository: useEventRepository(),
            cache: useStorage(StoreNames.Events),
        });
    }
    return eventCachingService;
}

export function useUserCachingService(): UserCachingService {
    console.log('🚀 Initializing UserCachingService');
    if (!userCachingService) {
        userCachingService = new UserCachingService({
            userRepository: useUserRepository(),
            cache: useStorage(StoreNames.Users),
        });
    }
    return userCachingService;
}

export function usePositionCachingService(): PositionCachingService {
    if (!positionCachingService) {
        console.log('🚀 Initializing PositionCachingService');
        positionCachingService = new PositionCachingService({
            positionRepository: usePositionRepository(),
            cache: useStorage(StoreNames.Positions),
        });
    }
    return positionCachingService;
}

export function useQualificationCachingService(): QualificationCachingService {
    if (!qualificationCachingService) {
        console.log('🚀 Initializing QualificationCachingService');
        qualificationCachingService = new QualificationCachingService({
            qualificationRepository: useQualificationRepository(),
            cache: useStorage(StoreNames.Qualifications),
        });
    }
    return qualificationCachingService;
}
