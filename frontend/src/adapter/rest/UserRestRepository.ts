import { getCsrfToken } from '@/adapter/util/Csrf';
import type { UserRepository } from '@/application';
import type { PositionKey, Role, User, UserDetails, UserKey } from '@/domain';

interface UserRepresentation {
    key: string;
    firstName: string;
    nickName?: string;
    lastName: string;
    positions: string[];
    roles?: string[];
    expiredQualificationCount?: number;
    soonExpiringQualificationCount?: number;
}

interface UserDetailsRepresentation {
    readonly key: string;
    readonly authKey: string;
    gender?: string;
    title?: string;
    firstName: string;
    nickName?: string;
    secondName?: string;
    lastName: string;
    positions: string[];
    roles: string[];
    email: string;
    qualifications: UserQualificationRepresentation[];
    phone?: string;
    mobile?: string;
    dateOfBirth: string;
    placeOfBirth: string;
    passNr?: string;
    comment?: string;
    address: AddressRepresentation;
}

interface SignedInUserUpdateRequest {
    gender?: string;
    nickName?: string;
    title?: string;
    email?: string;
    phone?: string;
    mobile?: string;
    passNr?: string;
    address?: AddressRepresentation;
}

interface UserDetailsUpdateRequest {
    gender?: string;
    title?: string;
    firstName?: string;
    nickName?: string;
    secondName?: string;
    lastName?: string;
    positions?: string[];
    roles?: string[];
    email?: string;
    qualifications?: UserQualificationRepresentation[];
    phone?: string;
    mobile?: string;
    passNr?: string;
    comment?: string;
    address?: AddressRepresentation;
}

interface UserDetailsCreateRequest {
    gender?: string;
    title?: string;
    firstName: string;
    nickName?: string;
    secondName?: string;
    lastName: string;
    positions: string[];
    email: string;
    qualifications: UserQualificationRepresentation[];
    phone?: string;
    mobile?: string;
    dateOfBirth?: string;
    placeOfBirth?: string;
    passNr?: string;
    comment?: string;
    address: AddressRepresentation;
}

interface UserQualificationRepresentation {
    qualificationKey: string;
    expiresAt?: string;
}

interface AddressRepresentation {
    addressLine1: string;
    addressLine2?: string;
    town: string;
    zipCode: string;
}

export class UserRestRepository implements UserRepository {
    private static parseDate(value?: string): Date | undefined {
        if (!value) {
            return undefined;
        }
        if (value.includes('+')) {
            return new Date(value.split('+')[0]);
        }
        return new Date(value);
    }

    public async findAll(): Promise<User[]> {
        const response = await fetch('/api/v1/users', {
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        const representations: UserRepresentation[] = await response.clone().json();
        return representations.map((it) => ({
            key: it.key,
            firstName: it.firstName,
            nickName: it.nickName,
            lastName: it.lastName,
            positionKeys: it.positions as PositionKey[],
            roles: it.roles?.map((r) => r as Role),
            expiredQualificationCount: it.expiredQualificationCount,
            soonExpiringQualificationCount: it.soonExpiringQualificationCount,
        }));
    }

    public async findByKey(key: UserKey): Promise<UserDetails> {
        const response = await fetch(`/api/v1/users/${key}`, {
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        const representation: UserDetailsRepresentation = await response.clone().json();
        return UserRestRepository.mapUserDetailsToDomain(representation);
    }

    public async findBySignedInUser(): Promise<UserDetails> {
        const response = await fetch('/api/v1/users/self', {
            credentials: 'include',
        });
        if (!response.ok) {
            throw response;
        }
        const representation: UserDetailsRepresentation = await response.clone().json();
        return UserRestRepository.mapUserDetailsToDomain(representation);
    }

    public async updateUser(userKey: UserKey, user: Partial<UserDetails>): Promise<UserDetails> {
        const requestBody: UserDetailsUpdateRequest = {
            gender: user.gender,
            title: user.title,
            firstName: user.firstName,
            nickName: user.nickName,
            secondName: user.secondName,
            lastName: user.lastName,
            positions: user.positionKeys,
            roles: user.roles,
            email: user.email,
            qualifications: user.qualifications?.map((it) => ({
                qualificationKey: it.qualificationKey,
                expiresAt: it.expiresAt?.toISOString(),
            })),
            phone: user.phone,
            mobile: user.mobile,
            passNr: user.passNr,
            comment: user.comment,
            address: user.address
                ? {
                      addressLine1: user.address.addressLine1,
                      addressLine2: user.address.addressLine2,
                      town: user.address.town,
                      zipCode: user.address.zipcode,
                  }
                : undefined,
        };
        const response = await fetch(`/api/v1/users/${userKey}`, {
            method: 'PATCH',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation: UserDetailsRepresentation = await response.clone().json();
        return UserRestRepository.mapUserDetailsToDomain(representation);
    }

    public async createUser(user: UserDetails): Promise<UserDetails> {
        const requestBody: UserDetailsCreateRequest = {
            gender: user.gender,
            title: user.title,
            firstName: user.firstName,
            nickName: user.nickName,
            secondName: user.secondName,
            lastName: user.lastName,
            dateOfBirth: user.dateOfBirth?.toISOString(),
            placeOfBirth: user.placeOfBirth,
            positions: user.positionKeys,
            email: user.email,
            qualifications: user.qualifications?.map((it) => ({
                qualificationKey: it.qualificationKey,
                expiresAt: it.expiresAt?.toISOString(),
            })),
            phone: user.phone,
            mobile: user.mobile,
            passNr: user.passNr,
            comment: user.comment,
            address: {
                addressLine1: user.address.addressLine1,
                addressLine2: user.address.addressLine2,
                town: user.address.town,
                zipCode: user.address.zipcode,
            },
        };
        const response = await fetch('/api/v1/users', {
            method: 'POST',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation: UserDetailsRepresentation = await response.clone().json();
        return UserRestRepository.mapUserDetailsToDomain(representation);
    }

    public async deleteUser(userKey: UserKey): Promise<void> {
        const response = await fetch(`/api/v1/users/${userKey}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
    }

    public async updateSignedInUser(user: Partial<UserDetails>): Promise<UserDetails> {
        const requestBody: SignedInUserUpdateRequest = {
            gender: user.gender,
            title: user.title,
            nickName: user.nickName,
            email: user.email,
            phone: user.phone,
            mobile: user.mobile,
            passNr: user.passNr,
            address: user.address
                ? {
                      addressLine1: user.address.addressLine1,
                      addressLine2: user.address.addressLine2,
                      town: user.address.town,
                      zipCode: user.address.zipcode,
                  }
                : undefined,
        };
        const response = await fetch(`/api/v1/users/self`, {
            method: 'PATCH',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (!response.ok) {
            throw response;
        }
        const representation: UserDetailsRepresentation = await response.clone().json();
        return UserRestRepository.mapUserDetailsToDomain(representation);
    }

    public async importUsers(file: Blob): Promise<void> {
        const formParams = new FormData();
        formParams.append('file', file);
        // don't add 'Content-Type': 'multipart/form-data' header, as this will break the upload!
        const response = await fetch('/api/v1/import/users', {
            method: 'POST',
            credentials: 'include',
            body: formParams,
        });
        if (!response.ok) {
            throw response;
        }
    }

    private static mapUserDetailsToDomain(representation: UserDetailsRepresentation): UserDetails {
        return {
            gender: representation.gender || undefined,
            title: representation.title || undefined,
            key: representation.key,
            authKey: representation.authKey || undefined,
            firstName: representation.firstName,
            nickName: representation.nickName || undefined,
            secondName: representation.secondName || undefined,
            lastName: representation.lastName,
            positionKeys: representation.positions as PositionKey[],
            roles: representation.roles?.map((r) => r as Role),
            qualifications: representation.qualifications.map((it) => ({
                qualificationKey: it.qualificationKey,
                expiresAt: UserRestRepository.parseDate(it.expiresAt),
            })),
            email: representation.email,
            phone: representation.phone || undefined,
            mobile: representation.mobile || undefined,
            dateOfBirth: UserRestRepository.parseDate(representation.dateOfBirth),
            placeOfBirth: representation.placeOfBirth || undefined,
            passNr: representation.passNr || undefined,
            comment: representation.comment || undefined,
            address: {
                addressLine1: representation.address?.addressLine1 || '',
                addressLine2: representation.address?.addressLine2 || undefined,
                town: representation.address?.town || '',
                zipcode: representation.address?.zipCode || '',
            },
        };
    }
}
