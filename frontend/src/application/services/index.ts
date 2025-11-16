import {
    useAccountRepository,
    useEventRepository,
    usePositionRepository,
    useQualificationRepository,
    useSettingsRepository,
    useUserRepository,
} from '@/adapter';
import { IndexedDBRepository, getConnection } from '@/common';
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

let indexedDb: Promise<IDBDatabase> | undefined;
let configService: ConfigService | undefined;
let errorHandlingService: ErrorHandlingService | undefined;
let notificationService: NotificationService | undefined;
let authService: AuthService | undefined;
let calendarService: CalendarService | undefined;
let eventCachingService: EventCachingService | undefined;
let positionCachingService: PositionCachingService | undefined;
let qualificationCachingService: QualificationCachingService | undefined;
let userCachingService: UserCachingService | undefined;

export function useConfigService(): ConfigService {
    if (!configService) {
        configService = new ConfigService({
            settingsRepository: useSettingsRepository(),
        });
    }
    return configService;
}

export function useErrorHandlingService(): ErrorHandlingService {
    if (!errorHandlingService) {
        errorHandlingService = new ErrorHandlingService({
            accountRepository: useAccountRepository(),
        });
    }
    return errorHandlingService;
}

export function useNotificationService(): NotificationService {
    if (!notificationService) {
        notificationService = new NotificationService();
    }
    return notificationService;
}

export function useAuthService(): AuthService {
    if (!authService) {
        authService = new AuthService();
    }
    return authService;
}

export function useCalendarService(): CalendarService {
    if (!calendarService) {
        calendarService = new CalendarService();
    }
    return calendarService;
}

export function useIndexedDb(): Promise<IDBDatabase> {
    if (!indexedDb) {
        indexedDb = getConnection('lissi', Object.values(StoreNames), 3);
    }
    return indexedDb;
}

export function useEventCachingService(): EventCachingService {
    if (!eventCachingService) {
        eventCachingService = new EventCachingService({
            eventRepository: useEventRepository(),
            cache: new IndexedDBRepository(useIndexedDb(), StoreNames.Events, { invalidateOnReload: true }),
        });
    }
    return eventCachingService;
}

export function useUserCachingService(): UserCachingService {
    if (!userCachingService) {
        userCachingService = new UserCachingService({
            userRepository: useUserRepository(),
            cache: new IndexedDBRepository(useIndexedDb(), StoreNames.Users, { invalidateOnReload: true }),
        });
    }
    return userCachingService;
}

export function usePositionCachingService(): PositionCachingService {
    if (!positionCachingService) {
        positionCachingService = new PositionCachingService({
            positionRepository: usePositionRepository(),
            cache: new IndexedDBRepository(useIndexedDb(), StoreNames.Positions, { invalidateOnReload: true }),
        });
    }
    return positionCachingService;
}

export function useQualificationCachingService(): QualificationCachingService {
    if (!qualificationCachingService) {
        qualificationCachingService = new QualificationCachingService({
            qualificationRepository: useQualificationRepository(),
            cache: new IndexedDBRepository(useIndexedDb(), StoreNames.Qualifications, { invalidateOnReload: true }),
        });
    }
    return qualificationCachingService;
}
