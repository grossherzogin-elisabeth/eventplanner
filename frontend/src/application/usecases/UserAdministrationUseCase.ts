import type { NotificationService, UserRepository } from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { UserCachingService } from '@/application/services/UserCachingService';
import { diff } from '@/common';
import type { UserDetails, UserKey } from '@/domain';

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
}
