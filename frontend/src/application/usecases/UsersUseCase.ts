import type { Config, NotificationService } from '@/application';
import type { UserRepository } from '@/application/ports/UserRepository';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { PositionCachingService } from '@/application/services/PositionCachingService';
import type { QualificationCachingService } from '@/application/services/QualificationCachingService';
import type { UserCachingService } from '@/application/services/UserCachingService';
import { diff } from '@/common';
import type {
    Position,
    PositionKey,
    Qualification,
    QualificationKey,
    User,
    UserDetails,
    UserKey,
    UserSettings,
} from '@/domain';
import type { RegistrationService } from '@/domain/services/RegistrationService';

export class UsersUseCase {
    private readonly config: Config;
    private readonly userRepository: UserRepository;
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
        this.positionCachingService = params.positionCachingService;
        this.userCachingService = params.userCachingService;
        this.qualificationCachingService = params.qualificationCachingService;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async getUserDetailsForSignedInUser(): Promise<UserDetails> {
        try {
            let user: UserDetails;
            if (this.config.overrideSignedInUserKey) {
                user = await this.userRepository.findByKey(this.config.overrideSignedInUserKey);
            } else {
                user = await this.userRepository.findBySignedInUser();
            }
            const userSettings = await this.getUserSettings();
            user.positionKeys = user.positionKeys.sort((a, b) => {
                if (userSettings.preferredPosition === a) return -1;
                if (userSettings.preferredPosition === b) return 1;
                return 0;
            });
            return user;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async updateUserDetailsForSignedInUser(
        originalUser: UserDetails,
        updatedUser: UserDetails
    ): Promise<UserDetails> {
        try {
            const changes = diff(originalUser, updatedUser);
            if (Object.keys(changes).length > 0) {
                const savedUser = await this.userRepository.updateSignedInUser({
                    gender: changes.gender,
                    title: changes.title,
                    nickName: changes.nickName,
                    phone: changes.phone,
                    phoneWork: changes.phoneWork,
                    mobile: changes.mobile,
                    address: changes.address,
                    passNr: changes.passNr,
                    email: changes.email,
                    diseases: changes.diseases,
                    medication: changes.medication,
                    emergencyContact: changes.emergencyContact,
                    diet: changes.diet,
                    intolerances: changes.intolerances,
                    nationality: changes.nationality,
                    dateOfBirth: changes.dateOfBirth,
                    placeOfBirth: changes.placeOfBirth,
                });
                await this.userCachingService.updateCache({
                    key: savedUser.key,
                    firstName: savedUser.firstName,
                    nickName: savedUser.nickName,
                    lastName: savedUser.lastName,
                    positionKeys: savedUser.positionKeys,
                    roles: savedUser.roles,
                    email: savedUser.email,
                    qualifications: savedUser.qualifications,
                });
                this.notificationService.success('Deine Angaben wurden gespeichert');
                return savedUser;
            }
            return updatedUser;
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

    public async getUserSettings(): Promise<UserSettings> {
        let settings: UserSettings = {};
        const settingsJson = localStorage.getItem('settings') || '{}';
        if (settingsJson) {
            settings = Object.assign(settings, JSON.parse(settingsJson));
        }
        return settings;
    }

    public async saveUserSettings(patch: Partial<UserSettings>): Promise<UserSettings> {
        let settings = await this.getUserSettings();
        settings = Object.assign(settings, patch);
        localStorage.setItem('settings', JSON.stringify(settings));
        return settings;
    }
}
