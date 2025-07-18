import type { PositionKey } from '@/domain';
import type { UserKey } from '@/domain/entities/User';
import type { Permission } from '@/domain/values/Permission';
import type { Role } from '@/domain/values/Role';

export interface SignedInUser {
    key: UserKey;
    gender?: string;
    firstName: string;
    lastName: string;
    email: string;
    roles: Role[];
    permissions: Permission[];
    positions: PositionKey[];
    impersonated: boolean;
}
