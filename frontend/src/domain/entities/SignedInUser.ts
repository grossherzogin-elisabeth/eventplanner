import type { PositionKey } from '@/domain';
import type { UserKey } from '@/domain/entities/User';
import type { Permission } from '@/domain/values/Permission';
import type { Role } from '@/domain/values/Role';

export interface SignedInUser {
    /**
     * The users id
     */
    key: UserKey;
    /**
     * The users gender
     */
    gender?: string;
    /**
     * The users first name
     */
    firstName: string;
    /**
     * The users last name
     */
    lastName: string;
    /**
     * The users email address
     */
    email: string;
    /**
     * The users application roles
     */
    roles: Role[];
    /**
     * The users application permissions
     */
    permissions: Permission[];
    /**
     * The positions this user can be assigned to
     */
    positions: PositionKey[];
    /**
     * The user has been authenticated with an access key and needs to sign in to use all app features
     */
    hasLimitedAccess: boolean;
    /**
     * The user is being impersonated by an admin user
     */
    impersonated: boolean;
}
