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
    qualifications?: UserQualificationRepresentation[];
    email?: string;
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
    phoneWork?: string;
    mobile?: string;
    dateOfBirth: string;
    placeOfBirth: string;
    passNr?: string;
    comment?: string;
    address: AddressRepresentation;
    nationality?: string;
    diseases?: string;
    intolerances?: string;
    medication?: string;
    diet?: string;
    emergencyContact: EmergencyContactRepresentation;
}

interface SignedInUserUpdateRequest {
    gender?: string;
    nickName?: string;
    title?: string;
    email?: string;
    phone?: string;
    phoneWork?: string;
    mobile?: string;
    passNr?: string;
    address?: AddressRepresentation;
    nationality?: string;
    diseases?: string;
    intolerances?: string;
    medication?: string;
    diet?: string;
    emergencyContact?: EmergencyContactRepresentation;
    dateOfBirth?: string;
    placeOfBirth?: string;
}

interface UserDetailsUpdateRequest {
    authKey?: string;
    gender?: string;
    title?: string;
    firstName?: string;
    nickName?: string;
    secondName?: string;
    lastName?: string;
    roles?: string[];
    qualifications?: UserQualificationRepresentation[];
    address?: AddressRepresentation;
    email?: string;
    phone?: string;
    phoneWork?: string;
    mobile?: string;
    dateOfBirth?: string;
    placeOfBirth?: string;
    passNr?: string;
    comment?: string;
    nationality?: string;
    emergencyContact?: EmergencyContactRepresentation;
    diseases?: string;
    intolerances?: string;
    medication?: string;
    diet?: string;
}

interface UserCreateRequest {
    firstName: string;
    lastName: string;
    email: string;
}

interface UserQualificationRepresentation {
    qualificationKey: string;
    expiresAt?: string;
    expires?: boolean;
    note?: string;
}

interface AddressRepresentation {
    addressLine1: string;
    addressLine2?: string;
    town: string;
    zipCode: string;
    country: string;
}

interface EmergencyContactRepresentation {
    name: string;
    phone: string;
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
        return representations.map((userRepresentation) => ({
            key: userRepresentation.key,
            firstName: userRepresentation.firstName,
            nickName: userRepresentation.nickName,
            lastName: userRepresentation.lastName,
            positionKeys: userRepresentation.positions as PositionKey[],
            roles: userRepresentation.roles?.map((r) => r as Role),
            qualifications: userRepresentation.qualifications?.map((it) => ({
                qualificationKey: it.qualificationKey,
                expiresAt: UserRestRepository.parseDate(it.expiresAt),
                expires: it.expires === true,
                note: it.note,
            })),
            email: userRepresentation.email,
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
            authKey: user.authKey,
            gender: user.gender,
            title: user.title,
            firstName: user.firstName,
            nickName: user.nickName,
            secondName: user.secondName,
            lastName: user.lastName,
            roles: user.roles,
            qualifications: user.qualifications?.map((it) => ({
                qualificationKey: it.qualificationKey,
                expiresAt: it.expiresAt?.toISOString(),
                note: it.note,
            })),
            address: user.address
                ? {
                      addressLine1: user.address.addressLine1,
                      addressLine2: user.address.addressLine2,
                      town: user.address.town,
                      zipCode: user.address.zipcode,
                      country: user.address.country,
                  }
                : undefined,
            email: user.email,
            phone: user.phone,
            phoneWork: user.phoneWork,
            mobile: user.mobile,
            dateOfBirth: user.dateOfBirth?.toISOString().substring(0, 10),
            placeOfBirth: user.placeOfBirth,
            passNr: user.passNr,
            nationality: user.nationality,
            comment: user.comment,
            diseases: user.diseases,
            intolerances: user.intolerances,
            medication: user.medication,
            diet: user.diet,
            emergencyContact: user.emergencyContact
                ? {
                      name: user.emergencyContact.name,
                      phone: user.emergencyContact.phone,
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

    public async createUser(user: User): Promise<UserDetails> {
        const requestBody: UserCreateRequest = {
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email || '',
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
            phoneWork: user.phoneWork,
            mobile: user.mobile,
            passNr: user.passNr,
            nationality: user.nationality,
            address: user.address
                ? {
                      addressLine1: user.address.addressLine1,
                      addressLine2: user.address.addressLine2,
                      town: user.address.town,
                      zipCode: user.address.zipcode,
                      country: user.address.country,
                  }
                : undefined,
            diet: user.diet,
            diseases: user.diseases,
            medication: user.medication,
            intolerances: user.intolerances,
            emergencyContact: user.emergencyContact
                ? {
                      name: user.emergencyContact?.name,
                      phone: user.emergencyContact?.phone,
                  }
                : undefined,
            dateOfBirth: user.dateOfBirth?.toISOString().substring(0, 10),
            placeOfBirth: user.placeOfBirth,
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
                expires: it.expires === true,
                note: it.note,
            })),
            email: representation.email,
            phone: representation.phone || undefined,
            phoneWork: representation.phoneWork || undefined,
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
                country: representation.address?.country || representation.nationality,
            },
            nationality: representation.nationality,
            diseases: representation.diseases,
            intolerances: representation.intolerances,
            medication: representation.medication,
            diet: representation.diet,
            emergencyContact: representation.emergencyContact
                ? {
                      name: representation.emergencyContact.name,
                      phone: representation.emergencyContact.phone,
                  }
                : {
                      name: '',
                      phone: '',
                  },
        };
    }
}
