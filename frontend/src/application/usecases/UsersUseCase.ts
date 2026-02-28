import type { UserRepository } from '@/application/ports';
import type {
    AuthService,
    ConfigService,
    ErrorHandlingService,
    NotificationService,
    PositionCachingService,
    UserCachingService,
} from '@/application/services';
import { diff } from '@/common';
import { generateColorSchemeCssVariables } from '@/common/theme/ThemeGenerator.ts';
import type { Position, PositionKey, User, UserDetails, UserKey, UserSettings } from '@/domain';
import { Permission, Theme } from '@/domain';
import type { RegistrationService } from '@/domain/services/RegistrationService';

export class UsersUseCase {
    private readonly configService: ConfigService;
    private readonly userRepository: UserRepository;
    private readonly authService: AuthService;
    private readonly positionCachingService: PositionCachingService;
    private readonly userCachingService: UserCachingService;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        configService: ConfigService;
        userRepository: UserRepository;
        authService: AuthService;
        registrationService: RegistrationService;
        positionCachingService: PositionCachingService;
        userCachingService: UserCachingService;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.configService = params.configService;
        this.userRepository = params.userRepository;
        this.authService = params.authService;
        this.positionCachingService = params.positionCachingService;
        this.userCachingService = params.userCachingService;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;

        globalThis.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => this.applyUserSettings());
    }

    public async getUserDetailsForSignedInUser(): Promise<UserDetails> {
        const overrideSignedInUserKey = this.configService.getConfig().overrideSignedInUserKey;
        try {
            let user: UserDetails;
            if (overrideSignedInUserKey && this.authService.getSignedInUser()?.permissions.includes(Permission.READ_USER_DETAILS)) {
                user = await this.userRepository.findByKey(overrideSignedInUserKey);
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
        if (!settings.theme) {
            settings.theme = Theme.System;
        }
        if (!settings.contrast) {
            settings.contrast = 0;
        }
        if (!settings.color) {
            settings.color = '#188edc';
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
        const prefersDarkMode = globalThis.matchMedia && globalThis.matchMedia('(prefers-color-scheme: dark)').matches;
        if (loadedSettings.theme === Theme.Dark || (loadedSettings.theme === Theme.System && prefersDarkMode)) {
            document.querySelector('html')?.classList.add('dark');
        } else if (loadedSettings.theme === Theme.Light || (loadedSettings.theme === Theme.System && !prefersDarkMode)) {
            document.querySelector('html')?.classList.remove('dark');
        }
        console.log(`🎨 Applying custom theme color ${loadedSettings.color}`);
        const contrast = loadedSettings.contrast ?? 0;
        const color = loadedSettings.color ?? '#188edc';
        const light = `html.theme {
            ${generateColorSchemeCssVariables(color, false, contrast).join('\n    ')}
        }`;
        const dark = `html.theme.dark {
            ${generateColorSchemeCssVariables(color, true, contrast).join('\n    ')}
        }`;
        this.appendCustomStyles('theme-light', light);
        this.appendCustomStyles('theme-dark', dark);
        document.querySelector('html')?.classList.add('theme');
    }

    private appendCustomStyles(name: string, style: string): void {
        const css = document.createElement('style');
        css.id = name;
        css.innerHTML = style;
        document.querySelector('head')?.appendChild(css);
    }
}
