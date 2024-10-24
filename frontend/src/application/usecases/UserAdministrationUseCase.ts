import type { NotificationService, UserRepository } from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { UserCachingService } from '@/application/services/UserCachingService';
import { ObjectUtils } from '@/common';
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
        const diff = ObjectUtils.diff(original, updated);
        if (Object.keys(diff).length > 0) {
            const savedUser = await this.userRepository.updateUser(updated.key, diff);
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
    }

    public async deleteUserByKey(key: UserKey): Promise<void> {
        await this.userRepository.deleteUser(key);
        await this.userCachingService.removeFromCache(key);
    }

    public async getUserDetailsByKey(key: UserKey): Promise<UserDetails> {
        return await this.userRepository.findByKey(key);
    }

    public async importUsers(file: Blob): Promise<void> {
        return this.userRepository.importUsers(file);
    }
}
