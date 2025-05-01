import type { User, UserDetails, UserKey } from '@/domain';

export interface UserRepository {
    findAll(): Promise<User[]>;

    findByKey(key: UserKey): Promise<UserDetails>;

    findBySignedInUser(): Promise<UserDetails>;

    createUser(user: User): Promise<UserDetails>;

    updateUser(userKey: UserKey, user: Partial<UserDetails>): Promise<UserDetails>;

    deleteUser(userKey: UserKey): Promise<void>;

    updateSignedInUser(user: Partial<UserDetails>): Promise<UserDetails>;
}
