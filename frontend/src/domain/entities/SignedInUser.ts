import type { UserKey } from '@/domain/entities/User';
import type { Permission } from '../values/Permission';
import type { Role } from '../values/Role';

export interface SignedInUser {
    key: UserKey;
    firstname: string;
    lastname: string;
    email: string;
    roles: Role[];
    permissions: Permission[];
    impersonated: boolean;
}
