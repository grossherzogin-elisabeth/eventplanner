import type { UserKey } from '@/domain/entities/User';
import type { Permission } from '../values/Permission';
import type { Role } from '../values/Role';

export interface SignedInUser {
    key: UserKey;
    gender: 'm' | 'w' | 'd';
    username: string;
    firstname: string;
    lastname: string;
    email: string;
    phone: string;
    roles: Role[];
    permissions: Permission[];
}
