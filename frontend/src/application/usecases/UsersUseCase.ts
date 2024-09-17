import type { Config, NotificationService } from '@/application';
import type { UserRepository } from '@/application/ports/UserRepository';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { PositionCachingService } from '@/application/services/PositionCachingService';
import type { QualificationCachingService } from '@/application/services/QualificationCachingService';
import type { UserCachingService } from '@/application/services/UserCachingService';
import type {
    Event,
    Position,
    PositionKey,
    Qualification,
    QualificationKey,
    Registration,
    ResolvedRegistration,
    ResolvedSlot,
    User,
    UserDetails,
    UserKey,
} from '@/domain';
import type { RegistrationService } from '@/domain/services/RegistrationService';

export class UsersUseCase {
    private readonly config: Config;
    private readonly userRepository: UserRepository;
    private readonly registrationService: RegistrationService;
    private readonly positionCachingService: PositionCachingService;
    private readonly userCachingService: UserCachingService;
    private readonly qualificationCachingService: QualificationCachingService;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        config: Config;
        userRepository: UserRepository;
        registrationService: RegistrationService;
        positionCachingService: PositionCachingService;
        userCachingService: UserCachingService;
        qualificationCachingService: QualificationCachingService;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.config = params.config;
        this.userRepository = params.userRepository;
        this.registrationService = params.registrationService;
        this.positionCachingService = params.positionCachingService;
        this.userCachingService = params.userCachingService;
        this.qualificationCachingService = params.qualificationCachingService;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async getUserDetailsForSignedInUser(): Promise<UserDetails> {
        if (this.config.overrideSignedInUserKey) {
            return await this.userRepository.findByKey(this.config.overrideSignedInUserKey);
        }
        return await this.userRepository.findBySignedInUser();
    }

    public async updateUserDetailsForSignedInUser(details: UserDetails): Promise<UserDetails> {
        try {
            const savedUser = await this.userRepository.updateSignedInUser({
                gender: details.gender,
                title: details.title,
                phone: details.phone,
                mobile: details.mobile,
                address: details.address,
                passNr: details.passNr,
                email: details.email,
            });
            this.notificationService.success('Deine Angaben wurden gespeichert');
            return savedUser;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getUsers(keys?: UserKey[]): Promise<User[]> {
        let users = await this.userCachingService.getUsers();
        if (keys) {
            users = users.filter((it) => keys.includes(it.key));
        }
        return users.sort((a, b) => {
            return a.firstName.localeCompare(b.firstName) || a.lastName.localeCompare(b.lastName);
        });
    }

    public async resolvePositionNames(): Promise<Map<PositionKey, Position>> {
        const map = new Map<PositionKey, Position>();
        const positions = await this.positionCachingService.getPositions();
        positions.forEach((it) => map.set(it.key, it));
        return map;
    }

    public async resolveQualifications(): Promise<Map<QualificationKey, Qualification>> {
        const map = new Map<QualificationKey, Qualification>();
        const qualifications = await this.qualificationCachingService.getQualifications();
        qualifications.forEach((it) => map.set(it.key, it));
        return map;
    }

    public async resolveUserNames(keys: UserKey[]): Promise<Map<UserKey, string>> {
        const map = new Map<UserKey, string>();
        const users = await this.getUsers(keys);
        users.forEach((it) => map.set(it.key, `${it.firstName} ${it.lastName}`));
        return map;
    }

    public async resolveEventSlots(event: Event): Promise<ResolvedSlot[]> {
        const users = await this.getUsers();
        const positions = await this.resolvePositionNames();
        return this.registrationService.resolveRegistrationsWithAssignedSlots(event, users, positions);
    }

    public async resolveWaitingList(event: Event): Promise<ResolvedRegistration[]> {
        const users = await this.getUsers();
        const positions = await this.resolvePositionNames();
        return this.registrationService.resolveRegistrationsOnWaitingList(event, users, positions);
    }

    public async resolveRegistrationUser(registration: Registration): Promise<User | undefined> {
        if (!registration.userKey) {
            return undefined;
        }
        const users = await this.userCachingService.getUsers();
        return users.find((it) => it.key === registration.userKey);
    }
}
