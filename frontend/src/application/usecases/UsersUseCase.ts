import type { AuthService, Config, NotificationService } from '@/application';
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
    ValidationHint,
} from '@/domain';
import { Theme } from '@/domain';
import { Permission } from '@/domain';
import type { RegistrationService } from '@/domain/services/RegistrationService';

export class UsersUseCase {
    private readonly config: Config;
    private readonly userRepository: UserRepository;
    private readonly authService: AuthService;
    private readonly positionCachingService: PositionCachingService;
    private readonly userCachingService: UserCachingService;
    private readonly qualificationCachingService: QualificationCachingService;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        config: Config;
        userRepository: UserRepository;
        authService: AuthService;
        registrationService: RegistrationService;
        positionCachingService: PositionCachingService;
        userCachingService: UserCachingService;
        qualificationCachingService: QualificationCachingService;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.config = params.config;
        this.userRepository = params.userRepository;
        this.authService = params.authService;
        this.positionCachingService = params.positionCachingService;
        this.userCachingService = params.userCachingService;
        this.qualificationCachingService = params.qualificationCachingService;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;

        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => this.applyUserSettings());
    }

    public async getUserDetailsForSignedInUser(): Promise<UserDetails> {
        try {
            let user: UserDetails;
            if (
                this.config.overrideSignedInUserKey &&
                this.authService.getSignedInUser()?.permissions.includes(Permission.READ_USER_DETAILS)
            ) {
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

    public async updateUserDetailsForSignedInUser(originalUser: UserDetails, updatedUser: UserDetails): Promise<UserDetails> {
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
        await this.applyUserSettings(settings);
        return settings;
    }

    public async applyUserSettings(settings?: UserSettings): Promise<void> {
        const loadedSettings = settings ?? (await this.getUserSettings());
        const prefersDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        if (loadedSettings.theme === Theme.Dark || (loadedSettings.theme === Theme.System && prefersDarkMode)) {
            document.querySelector('html')?.classList.add('dark');
        } else {
            document.querySelector('html')?.classList.remove('dark');
        }
    }

    public validate(user: UserDetails | null): Record<string, ValidationHint[]> {
        if (!user) {
            return {};
        }
        const phoneRegex = new RegExp('^[0-9+\\- ]+$');
        const nameRegex = new RegExp(
            "^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžæÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+$"
        );
        const errors: Record<string, ValidationHint[]> = {};
        if (user.nickName && user.nickName.trim().length > 0 && user.nickName.includes(user.lastName)) {
            errors.nickName = errors.nickName || [];
            errors.nickName.push({
                key: 'Bitte gib hier nur deinen Vornamen an',
                params: {},
            });
        }
        if (user.nickName && !nameRegex.test(user.nickName)) {
            errors.nickName = errors.nickName || [];
            errors.nickName.push({
                key: 'Dein Name darf keine Zahlen oder Sonderzeichen enthalten',
                params: {},
            });
        }
        if (user.address.addressLine1.trim().length === 0) {
            errors['address.addressLine1'] = errors['address.addressLine1'] || [];
            errors['address.addressLine1'].push({
                key: 'Diese Eingabe ist erforderlich',
                params: {},
            });
        }
        if (user.address.town.trim().length === 0) {
            errors['address.town'] = errors['address.town'] || [];
            errors['address.town'].push({
                key: 'Diese Eingabe ist erforderlich',
                params: {},
            });
        }
        if (user.address.zipcode.trim().length === 0) {
            errors['address.zipcode'] = errors['address.zipcode'] || [];
            errors['address.zipcode'].push({
                key: 'Diese Eingabe ist erforderlich',
                params: {},
            });
        }
        if (user.address.country.trim().length === 0) {
            errors['address.country'] = errors['address.country'] || [];
            errors['address.country'].push({
                key: 'Diese Eingabe ist erforderlich',
                params: {},
            });
        }
        if (user.phone && !phoneRegex.test(user.phone)) {
            errors['phone'] = errors['phone'] || [];
            errors['phone'].push({
                key: 'Bitte gib eine gültige Telefon Nummer an',
                params: {},
            });
        }
        if (user.mobile && !phoneRegex.test(user.mobile)) {
            errors['mobile'] = errors['mobile'] || [];
            errors['mobile'].push({
                key: 'Bitte gib eine gültige Telefon Nummer an',
                params: {},
            });
        }
        if (user.phoneWork && !phoneRegex.test(user.phoneWork)) {
            errors['phoneWork'] = errors['phoneWork'] || [];
            errors['phoneWork'].push({
                key: 'Bitte gib eine gültige Telefon Nummer an',
                params: {},
            });
        }
        return errors;
    }
}
