import type { User, UserDetails, UserKey } from '@/domain';

export interface UserRepository {
    findAll(): Promise<User[]>;

    findByKey(key: UserKey): Promise<UserDetails>;

    findBySignedInUser(): Promise<UserDetails>;

    createUser(user: UserDetails): Promise<UserDetails>;

    updateUser(userKey: UserKey, user: Partial<UserDetails>): Promise<UserDetails>;

    updateSignedInUser(user: Partial<UserDetails>): Promise<UserDetails>;

    importUsers(file: Blob): Promise<void>;
}
