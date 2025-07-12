import type { PositionKey, UserKey } from '@/domain';

export type RegistrationKey = string;

export interface Registration {
    key: RegistrationKey;
    positionKey: PositionKey;
    userKey?: UserKey;
    name?: string;
    note?: string;
    confirmed?: boolean;
    overnightStay?: boolean;
    arrival?: Date;
}
