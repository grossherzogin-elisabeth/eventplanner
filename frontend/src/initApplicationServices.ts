import {
    AuthService,
    CalendarService,
    ErrorHandlingService,
    EventCachingService,
    NotificationService,
    PositionCachingService,
    QualificationCachingService,
    UserCachingService,
} from '@/application';
import { IndexedDBRepository, getConnection } from '@/common';
import type { Adapters } from '@/initAdapters';

enum StoreNames {
    Events = 'events',
    Users = 'users',
    Positions = 'positions',
    Qualifications = 'qualifications',
}

export interface ApplicationServices {
    authService: AuthService;
    eventCachingService: EventCachingService;
    userCachingService: UserCachingService;
    positionCachingService: PositionCachingService;
    qualificationCachingService: QualificationCachingService;
    notificationService: NotificationService;
    errorHandlingService: ErrorHandlingService;
    calendarService: CalendarService;
}

export function initApplicationServices(params: { adapters: Adapters }): ApplicationServices {
    const indexedDB = getConnection('lissi', Object.values(StoreNames), 3);

    return {
        authService: new AuthService(),
        eventCachingService: new EventCachingService({
            cache: new IndexedDBRepository(indexedDB, StoreNames.Events, { invalidateOnReload: true }),
            eventRepository: params.adapters.eventRepository,
        }),
        userCachingService: new UserCachingService({
            cache: new IndexedDBRepository(indexedDB, StoreNames.Users, {
                invalidateOnReload: true,
            }),
            userRepository: params.adapters.userRepository,
        }),
        positionCachingService: new PositionCachingService({
            cache: new IndexedDBRepository(indexedDB, StoreNames.Positions, {
                invalidateOnReload: true,
            }),
            positionRepository: params.adapters.positionRepository,
        }),
        qualificationCachingService: new QualificationCachingService({
            cache: new IndexedDBRepository(indexedDB, StoreNames.Qualifications, {
                invalidateOnReload: true,
            }),
            qualificationRepository: params.adapters.qualificationRepository,
        }),
        notificationService: new NotificationService(),
        errorHandlingService: new ErrorHandlingService({
            accountRepository: params.adapters.accountRepository,
        }),
        calendarService: new CalendarService(),
    };
}
