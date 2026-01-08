import { getConnection } from '@/adapter/indexeddb/IndexedDB.ts';
import { IndexedDBRepository } from '@/adapter/indexeddb/IndexedDBRepository.ts';
import { InMemoryCache } from '@/adapter/memory/InMemoryCache.ts';
import type { Cache, CacheableEntity } from '@/application';
import type {
    AccountRepository,
    EventRegistrationsRepository,
    EventRepository,
    PositionRepository,
    QualificationRepository,
    SettingsRepository,
    UserRepository,
} from '@/application/ports';
import { AccountRestRepository } from './rest/AccountRestRepository';
import { EventRegistrationRestRepository } from './rest/EventRegistrationRestRepository';
import { EventRestRepository } from './rest/EventRestRepository';
import { PositionRestRepository } from './rest/PositionRestRepository';
import { QualificationRestRepository } from './rest/QualificationRestRepository';
import { SettingsRestRepository } from './rest/SettingsRestRepository';
import { UserRestRepository } from './rest/UserRestRepository';

let accountRepository: AccountRepository | undefined;
let eventRegistrationsRepository: EventRegistrationsRepository | undefined;
let eventRepository: EventRepository | undefined;
let positionRepository: PositionRepository | undefined;
let qualificationRepository: QualificationRepository | undefined;
let settingsRepository: SettingsRepository | undefined;
let userRepository: UserRepository | undefined;
let indexedDb: Promise<IDBDatabase> | undefined;
const caches = new Map<string, Cache<string | number, CacheableEntity<string | number>>>();

enum StoreNames {
    Events = 'events',
    Users = 'users',
    Positions = 'positions',
    Qualifications = 'qualifications',
}

export function useAccountRepository(): AccountRepository {
    if (!accountRepository) {
        console.log('🚀 Initializing AccountRestRepository');
        accountRepository = new AccountRestRepository();
    }
    return accountRepository;
}

export function useEventRegistrationsRepository(): EventRegistrationsRepository {
    if (!eventRegistrationsRepository) {
        console.log('🚀 Initializing EventRegistrationRestRepository');
        eventRegistrationsRepository = new EventRegistrationRestRepository();
    }
    return eventRegistrationsRepository;
}

export function useEventRepository(): EventRepository {
    if (!eventRepository) {
        console.log('🚀 Initializing EventRestRepository');
        eventRepository = new EventRestRepository();
    }
    return eventRepository;
}

export function usePositionRepository(): PositionRepository {
    if (!positionRepository) {
        console.log('🚀 Initializing PositionRestRepository');
        positionRepository = new PositionRestRepository();
    }
    return positionRepository;
}

export function useQualificationRepository(): QualificationRepository {
    if (!qualificationRepository) {
        console.log('🚀 Initializing QualificationRestRepository');
        qualificationRepository = new QualificationRestRepository();
    }
    return qualificationRepository;
}

export function useSettingsRepository(): SettingsRepository {
    if (!settingsRepository) {
        console.log('🚀 Initializing SettingsRestRepository');
        settingsRepository = new SettingsRestRepository();
    }
    return settingsRepository;
}

export function useUserRepository(): UserRepository {
    if (!userRepository) {
        console.log('🚀 Initializing UserRestRepository');
        userRepository = new UserRestRepository();
    }
    return userRepository;
}

function useIndexedDb(): Promise<IDBDatabase> {
    if (!indexedDb) {
        console.log('🚀 Connecting to IndexedDb');
        indexedDb = getConnection('lissi', Object.values(StoreNames), 3);
    }
    return indexedDb;
}

export function useCache<K extends string | number, T extends CacheableEntity<K>>(name: string): Cache<K, T> {
    let cache = caches.get(name);
    if (!cache) {
        console.log(`🚀 Initialising ${name} cache`);
        if (import.meta.env.VITE_USE_INDEXED_DB === 'true') {
            cache = new IndexedDBRepository(useIndexedDb(), name, { invalidateOnReload: true });
        } else {
            cache = new InMemoryCache();
        }
        caches.set(name, cache);
    }
    return cache as Cache<K, T>;
}
