import type { Address, EmergencyContact, PositionKey, QualificationKey, Role } from '@/domain';

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
    verified?: boolean;
}

export interface UserDetails {
    key: UserKey;
    authKey?: AuthKey;
    createdAt: Date;
    updatedAt: Date;
    verifiedAt?: Date;
    lastLoginAt?: Date;
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
    phoneWork?: string;
    mobile?: string;
    dateOfBirth?: Date;
    placeOfBirth?: string;
    passNr?: string;
    comment?: string;
    address: Address;
    nationality?: string;
    diseases?: string;
    intolerances?: string;
    medication?: string;
    diet?: string;
    emergencyContact: EmergencyContact;
}

export interface UserQualification {
    qualificationKey: QualificationKey;
    expires: boolean;
    expiresAt?: Date;
    note?: string;
}
