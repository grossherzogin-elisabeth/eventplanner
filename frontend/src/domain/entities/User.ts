import type { Role } from '@/domain';
import type { QualificationKey } from '@/domain';
import type { Address } from '../values/Address';
import type { PositionKey } from './Position';

export type UserKey = string;
export type AuthKey = string;

export interface User {
    key: UserKey;
    firstName: string;
    nickName?: string;
    lastName: string;
    roles?: Role[];
    positionKeys: PositionKey[];
    email?: string;
    qualifications?: UserQualification[];
}

export interface UserDetails {
    key: UserKey;
    authKey?: AuthKey;
    gender?: string;
    title?: string;
    firstName: string;
    nickName?: string;
    secondName?: string;
    lastName: string;
    roles: Role[];
    positionKeys: PositionKey[];
    qualifications: UserQualification[];
    email: string;
    phone?: string;
    mobile?: string;
    dateOfBirth?: Date;
    placeOfBirth?: string;
    passNr?: string;
    comment?: string;
    address: Address;
}

export interface UserQualification {
    qualificationKey: QualificationKey;
    expiresAt?: Date;
    note?: string;
}
