import type { Storage, UserRepository } from '@/application/ports';
import { debounce } from '@/application/utils/AsyncDebouncer';
import type { User, UserKey } from '@/domain';






export class UserCachingService {
    private readonly userRepository: UserRepository;
    private readonly storage: Storage<UserKey, User>;
    private readonly initialized: Promise<void>;

    constructor(params: { userRepository: UserRepository; cache: Storage<UserKey, User> }) {
        console.log('🚀 Initializing UserCachingService');
        this.userRepository = params.userRepository;
        this.storage = params.cache;
        this.initialized = this.initialize();
    }

    private async initialize(): Promise<void> {
        try {
            const users = await this.fetchUsers();
            await this.storage.deleteAll();
            await this.storage.saveAll(users);
        } catch (e: unknown) {
            const response = e as { status?: number };
            if (response.status === 401 || response.status === 403) {
                // users session is no longer valid, clear all locally stored data
                console.error('Failed to fetch users, clearing local data');
                await this.storage.deleteAll();
            } else {
                console.warn('Failed to fetch users, continuing with local data');
            }
        }
    }

    public async getUsers(keys?: UserKey[]): Promise<User[]> {
        await this.initialized;
        let users = await this.storage.findAll();
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
        await this.initialized;
        const cached = await this.storage.findByKey(user.key);
        if (cached) {
            // make sure we don't forget any data
            return await this.storage.save(Object.assign(cached, user));
        } else if ((await this.storage.count()) > 0) {
            return await this.storage.save(user);
        }
        return user;
    }

    public async removeFromCache(userkey: UserKey): Promise<void> {
        await this.initialized;
        await this.initialized;
        return await this.storage.deleteByKey(userkey);
    }

    private async fetchUsers(): Promise<User[]> {
        console.log('📡 Fetching users');
        return debounce('fetchUsers', async () => {
            const users = await this.userRepository.findAll();
            await this.storage.saveAll(users);
            return users;
        });
    }
}
