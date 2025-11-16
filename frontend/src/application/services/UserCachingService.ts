import type { UserRepository } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { Cache } from '@/common';
import type { User, UserKey } from '@/domain';

export class UserCachingService {
    private readonly userRepository: UserRepository;
    private readonly cache: Cache<UserKey, User>;

    constructor(params: { userRepository: UserRepository; cache: Cache<UserKey, User> }) {
        this.userRepository = params.userRepository;
        this.cache = params.cache;
    }

    public async getUsers(keys?: UserKey[]): Promise<User[]> {
        let users = await this.cache.findAll();
        if (users.length === 0) {
            users = await this.fetchUsers();
        }
        if (keys) {
            users = users.filter((it) => keys.includes(it.key));
        }
        return users.sort((a, b) => {
            return a.firstName.localeCompare(b.firstName) || a.lastName.localeCompare(b.lastName);
        });
    }

    public async updateCache(user: User): Promise<User> {
        const cached = await this.cache.findByKey(user.key);
        if (cached) {
            // make sure we don't forget any data
            return await this.cache.save(Object.assign(cached, user));
        } else if ((await this.cache.count()) > 0) {
            return await this.cache.save(user);
        }
        return user;
    }

    public async removeFromCache(userkey: UserKey): Promise<void> {
        return await this.cache.deleteByKey(userkey);
    }

    private async fetchUsers(): Promise<User[]> {
        return debounce('fetchUsers', async () => {
            const users = await this.userRepository.findAll();
            await this.cache.saveAll(users);
            return users;
        });
    }
}
