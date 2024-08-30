import type { Address } from '../values/Address';
import type { PositionKey } from './Position';

export type UserKey = string;
export type AuthKey = string;

export interface User {
    key: UserKey;
    firstName: string;
    lastName: string;
    positionKeys: PositionKey[];
    email?: string;
}

export interface UserDetails {
    key: UserKey;
    authKey: AuthKey;
    firstName: string;
    secondName?: string;
    lastName: string;
    positionKeys: PositionKey[];
    // qualifications: any[];
    email?: string;
    phone?: string;
    mobile?: string;
    dateOfBirth?: Date;
    placeOfBirth?: string;
    passNr?: string;
    comment?: string;
    address: Address;
}
