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

export function useAccountRepository(): AccountRepository {
    if (!accountRepository) {
        accountRepository = new AccountRestRepository();
    }
    return accountRepository;
}

export function useEventRegistrationsRepository(): EventRegistrationsRepository {
    if (!eventRegistrationsRepository) {
        eventRegistrationsRepository = new EventRegistrationRestRepository();
    }
    return eventRegistrationsRepository;
}

export function useEventRepository(): EventRepository {
    if (!eventRepository) {
        eventRepository = new EventRestRepository();
    }
    return eventRepository;
}

export function usePositionRepository(): PositionRepository {
    if (!positionRepository) {
        positionRepository = new PositionRestRepository();
    }
    return positionRepository;
}

export function useQualificationRepository(): QualificationRepository {
    if (!qualificationRepository) {
        qualificationRepository = new QualificationRestRepository();
    }
    return qualificationRepository;
}

export function useSettingsRepository(): SettingsRepository {
    if (!settingsRepository) {
        settingsRepository = new SettingsRestRepository();
    }
    return settingsRepository;
}

export function useUserRepository(): UserRepository {
    if (!userRepository) {
        userRepository = new UserRestRepository();
    }
    return userRepository;
}
