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
import { InMemoryCache } from '@/common/cache/InMemoryCache.ts';
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

export function initApplicationServices(params: { adapters: Adapters; storage?: 'indexeddb' | 'memory' }): ApplicationServices {
    let indexedDB: Promise<IDBDatabase> = Promise.reject();
    if (params.storage === 'indexeddb') {
        indexedDB = getConnection('lissi', Object.values(StoreNames), 3);
    }

    return {
        authService: new AuthService(),
        eventCachingService: new EventCachingService({
            cache:
                params.storage === 'indexeddb'
                    ? new IndexedDBRepository(indexedDB, StoreNames.Events, { invalidateOnReload: true })
                    : new InMemoryCache(),
            eventRepository: params.adapters.eventRepository,
        }),
        userCachingService: new UserCachingService({
            cache:
                params.storage === 'indexeddb'
                    ? new IndexedDBRepository(indexedDB, StoreNames.Users, { invalidateOnReload: true })
                    : new InMemoryCache(),
            userRepository: params.adapters.userRepository,
        }),
        positionCachingService: new PositionCachingService({
            cache:
                params.storage === 'indexeddb'
                    ? new IndexedDBRepository(indexedDB, StoreNames.Positions, { invalidateOnReload: true })
                    : new InMemoryCache(),
            positionRepository: params.adapters.positionRepository,
        }),
        qualificationCachingService: new QualificationCachingService({
            cache:
                params.storage === 'indexeddb'
                    ? new IndexedDBRepository(indexedDB, StoreNames.Qualifications, { invalidateOnReload: true })
                    : new InMemoryCache(),
            qualificationRepository: params.adapters.qualificationRepository,
        }),
        notificationService: new NotificationService(),
        errorHandlingService: new ErrorHandlingService({
            accountRepository: params.adapters.accountRepository,
        }),
        calendarService: new CalendarService(),
    };
}
