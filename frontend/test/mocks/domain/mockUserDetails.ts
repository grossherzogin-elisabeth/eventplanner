import type { User, UserDetails } from '@/domain';

export function mockUserDetails(user: User): UserDetails {
    return {
        gender: undefined,
        key: user.key,
        authKey: user.key, // keep them equal for easier debugging
        title: undefined,
        nickName: user.nickName,
        firstName: user.firstName,
        secondName: undefined,
        lastName: user.lastName,
        email: user.email ?? 'mocked@email.com',
        positionKeys: user.positionKeys ?? [],
        roles: user.roles ?? [],
        qualifications: [],

        createdAt: new Date(),
        updatedAt: new Date(),
        lastLoginAt: new Date(),
        verifiedAt: user.verified ? new Date() : undefined,

        dateOfBirth: undefined,
        placeOfBirth: undefined,
        nationality: undefined,
        passNr: undefined,

        address: {
            addressLine1: '',
            addressLine2: undefined,
            town: '',
            zipcode: '',
            country: 'DE',
        },
        mobile: undefined,
        phone: undefined,
        phoneWork: undefined,

        emergencyContact: {
            phone: '',
            name: '',
        },
        diseases: undefined,
        medication: undefined,
        diet: undefined,
        intolerances: undefined,

        comment: undefined,
    };
}
