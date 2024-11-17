import type { NotificationService, UserRepository } from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { UserCachingService } from '@/application/services/UserCachingService';
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

    public validate(event: User): Record<string, ValidationHint[]> {
        const errors: Record<string, ValidationHint[]> = {};
        if (event.firstName.trim().length === 0) {
            errors.firstName = errors.firstName || [];
            errors.firstName.push({
                key: 'Bitte gib einen Vornamen an',
                params: {},
            });
        }
        if (event.lastName.trim().length === 0) {
            errors.lastName = errors.lastName || [];
            errors.lastName.push({
                key: 'Bitte gib einen Nachnamen an',
                params: {},
            });
        }
        if (!event.email || !new RegExp('^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$').test(event.email)) {
            errors.email = errors.email || [];
            errors.email.push({
                key: 'Bitte gib eine gültige Email Adresse an',
                params: {},
            });
        }
        return errors;
    }
}
