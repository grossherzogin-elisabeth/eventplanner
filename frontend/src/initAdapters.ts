import {
    AccountRestRepository,
    EventRegistrationRestRepository,
    EventRestRepository,
    PositionRestRepository,
    QualificationRestRepository,
    SettingsRestRepository,
    UserRestRepository,
} from '@/adapter';

export interface Adapters {
    accountRepository: AccountRestRepository;
    positionRepository: PositionRestRepository;
    userRepository: UserRestRepository;
    eventRepository: EventRestRepository;
    eventRegistrationsRepository: EventRegistrationRestRepository;
    qualificationRepository: QualificationRestRepository;
    settingsRepository: SettingsRestRepository;
}

export function initAdapters(): Adapters {
    return {
        accountRepository: new AccountRestRepository(),
        positionRepository: new PositionRestRepository(),
        userRepository: new UserRestRepository(),
        eventRepository: new EventRestRepository(),
        eventRegistrationsRepository: new EventRegistrationRestRepository(),
        qualificationRepository: new QualificationRestRepository(),
        settingsRepository: new SettingsRestRepository(),
    };
}
