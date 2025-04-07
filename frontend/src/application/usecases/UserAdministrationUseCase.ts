import type { NotificationService, UserRepository } from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { UserCachingService } from '@/application/services/UserCachingService';
import { EMAIL_REGEX, NAME_REGEX, PHONE_REGEX } from '@/application/utils/RegExpresions.ts';
import { diff, filterUndefined, wait } from '@/common';
import type { User, UserDetails, UserKey, ValidationHint } from '@/domain';

export class UserAdministrationUseCase {
    private readonly userRepository: UserRepository;
    private readonly userCachingService: UserCachingService;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        userRepository: UserRepository;
        userCachingService: UserCachingService;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.userRepository = params.userRepository;
        this.userCachingService = params.userCachingService;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async createUser(user: User): Promise<UserDetails> {
        try {
            const savedUser = await this.userRepository.createUser(user);
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
            this.notificationService.success('Nutzer erstellt');
            return savedUser;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async updateUser(original: UserDetails, updated: UserDetails): Promise<UserDetails> {
        try {
            const changes = diff(original, updated);
            if (Object.keys(changes).length > 0) {
                const savedUser = await this.userRepository.updateUser(updated.key, changes);
                await this.userCachingService.updateCache({
                    key: savedUser.key,
                    firstName: savedUser.firstName,
                    nickName: savedUser.nickName,
                    lastName: savedUser.lastName,
                    positionKeys: savedUser.positionKeys,
                    roles: savedUser.roles,
                    email: savedUser.email,
                    qualifications: savedUser.qualifications,
                    verified: savedUser.verifiedAt !== undefined,
                });
                this.notificationService.success('Änderungen gespeichert');
                return savedUser;
            }
            return updated;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async deleteUserByKey(key: UserKey): Promise<void> {
        try {
            await this.userRepository.deleteUser(key);
            await this.userCachingService.removeFromCache(key);
            this.notificationService.success('Nutzer gelöscht');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async getUserDetailsByKey(key: UserKey): Promise<UserDetails> {
        try {
            return await this.userRepository.findByKey(key);
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async importUsers(file: Blob): Promise<void> {
        try {
            return this.userRepository.importUsers(file);
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async contactUsers(users: User[], ignoreMissingEmails: boolean = false): Promise<void> {
        let mailtoLink: string;
        if (users.length === 1) {
            const user = users[0];
            if (!user.email) {
                this.errorHandlingService.handleError({
                    title: 'Keine Email Adresse hinterlegt',
                    message: `Für ${user.nickName || user.firstName} ${user.lastName} haben wir keine
                        Email Adresse hinterlegt. Du kannst ${user.nickName || user.firstName} deshalb
                        nicht per Email kontaktieren.`,
                });
                return;
            }
            mailtoLink = `mailto:${user.email}`;
        } else {
            if (!ignoreMissingEmails) {
                const usersWithoutEmail = users.filter((it) => !it.email).map((it) => `${it.firstName} ${it.lastName}`);
                if (usersWithoutEmail.length > 0) {
                    this.errorHandlingService.handleError({
                        title: `${usersWithoutEmail.length} Nutzer ohne Email`,
                        message: `Für ${usersWithoutEmail.length} der ${users.length} ausgewählten Nutzer haben wir keine
                        Email Adresse hinterlegt: ${usersWithoutEmail.join(', ')}`,
                        retryText: 'Andere kontaktieren',
                        cancelText: 'Abbrechen',
                        retry: () => this.contactUsers(users, true),
                    });
                    return;
                }
            }
            const emails = users.map((it) => it.email).filter(filterUndefined);
            mailtoLink = `mailto:?bcc=${emails?.join(',%20')}`;
        }

        const mailToElement = document.createElement('a');
        mailToElement.setAttribute('href', mailtoLink);
        mailToElement.style.display = 'none';
        document.body.appendChild(mailToElement);
        mailToElement.click();
        await wait(1000);
        document.body.removeChild(mailToElement);
    }

    public validateForCreate(user: User): Record<string, ValidationHint[]> {
        const errors: Record<string, ValidationHint[]> = {};
        if (user.firstName.trim().length === 0) {
            errors.firstName = errors.firstName || [];
            errors.firstName.push({
                key: 'Bitte gib einen Vornamen an',
                params: {},
            });
        }
        if (user.firstName && !NAME_REGEX.test(user.firstName)) {
            errors.firstName = errors.firstName || [];
            errors.firstName.push({
                key: 'Namen dürfen keine Zahlen oder Sonderzeichen enthalten',
                params: {},
            });
        }
        if (user.lastName.trim().length === 0) {
            errors.lastName = errors.lastName || [];
            errors.lastName.push({
                key: 'Bitte gib einen Nachnamen an',
                params: {},
            });
        }
        if (user.lastName && !NAME_REGEX.test(user.lastName)) {
            errors.lastName = errors.lastName || [];
            errors.lastName.push({
                key: 'Namen dürfen keine Zahlen oder Sonderzeichen enthalten',
                params: {},
            });
        }
        if (!user.email || !EMAIL_REGEX.test(user.email)) {
            errors.email = errors.email || [];
            errors.email.push({
                key: 'Bitte gib eine gültige Email Adresse an',
                params: {},
            });
        }
        return errors;
    }

    public validate(user: UserDetails | null): Record<string, ValidationHint[]> {
        if (!user) {
            return {};
        }
        const errors: Record<string, ValidationHint[]> = {};
        if (user.firstName.trim().length === 0) {
            errors.firstName = errors.firstName || [];
            errors.firstName.push({
                key: 'Bitte gib einen Vornamen an',
                params: {},
            });
        }
        if (user.firstName && !NAME_REGEX.test(user.firstName)) {
            errors.firstName = errors.firstName || [];
            errors.firstName.push({
                key: 'Namen dürfen keine Zahlen oder Sonderzeichen enthalten',
                params: {},
            });
        }
        if (user.lastName.trim().length === 0) {
            errors.lastName = errors.lastName || [];
            errors.lastName.push({
                key: 'Bitte gib einen Nachnamen an',
                params: {},
            });
        }
        if (user.lastName && !NAME_REGEX.test(user.lastName)) {
            errors.lastName = errors.lastName || [];
            errors.lastName.push({
                key: 'Namen dürfen keine Zahlen oder Sonderzeichen enthalten',
                params: {},
            });
        }
        if (!user.email || !EMAIL_REGEX.test(user.email)) {
            errors.email = errors.email || [];
            errors.email.push({
                key: 'Bitte gib eine gültige Email Adresse an',
                params: {},
            });
        }
        if (user.nickName && user.nickName.trim().length > 0 && user.nickName.includes(user.lastName)) {
            errors.nickName = errors.nickName || [];
            errors.nickName.push({
                key: 'Bitte gib hier nur einen Vornamen an',
                params: {},
            });
        }
        if (user.nickName && !NAME_REGEX.test(user.nickName)) {
            errors.nickName = errors.nickName || [];
            errors.nickName.push({
                key: 'Namen dürfen keine Zahlen oder Sonderzeichen enthalten',
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
        if (user.phone && !PHONE_REGEX.test(user.phone)) {
            errors['phone'] = errors['phone'] || [];
            errors['phone'].push({
                key: 'Bitte gib eine gültige Telefon Nummer an',
                params: {},
            });
        }
        if (user.mobile && !PHONE_REGEX.test(user.mobile)) {
            errors['mobile'] = errors['mobile'] || [];
            errors['mobile'].push({
                key: 'Bitte gib eine gültige Telefon Nummer an',
                params: {},
            });
        }
        if (user.phoneWork && !PHONE_REGEX.test(user.phoneWork)) {
            errors['phoneWork'] = errors['phoneWork'] || [];
            errors['phoneWork'].push({
                key: 'Bitte gib eine gültige Telefon Nummer an',
                params: {},
            });
        }
        return errors;
    }
}
