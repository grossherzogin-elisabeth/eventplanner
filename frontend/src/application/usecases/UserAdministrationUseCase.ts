import type { UserRepository } from '@/application';
import type { UserCachingService } from '@/application/services/UserCachingService';
import type { UserDetails, UserKey } from '@/domain';

export class UserAdministrationUseCase {
    private readonly userRepository: UserRepository;
    private readonly userCachingService: UserCachingService;

    constructor(params: { userRepository: UserRepository; userCachingService: UserCachingService }) {
        this.userRepository = params.userRepository;
        this.userCachingService = params.userCachingService;
    }

    public async updateUser(user: UserDetails): Promise<UserDetails> {
        const savedUser = await this.userRepository.updateUser(user.key, user);
        await this.userCachingService.updateCache({
            key: savedUser.key,
            firstName: savedUser.firstName,
            lastName: savedUser.lastName,
            positionKeys: savedUser.positionKeys,
            email: savedUser.email,
        });
        return savedUser;
    }

    public async getUserDetailsByKey(key: UserKey): Promise<UserDetails> {
        return await this.userRepository.findByKey(key);
    }

    public async importUsers(file: Blob): Promise<void> {
        return this.userRepository.importUsers(file);
    }
}
