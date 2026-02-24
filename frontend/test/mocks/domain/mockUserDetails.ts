import type { User, UserDetails } from '@/domain';

export function mockUserDetails(user: User): UserDetails {
    return {
        gender: 'd',
        key: user.key,
        authKey: user.key, // keep them equal for easier debugging
        title: 'Dr',
        nickName: user.nickName,
        firstName: user.firstName,
        secondName: 'Segundo',
        lastName: user.lastName,
        email: user.email ?? 'mocked@email.com',
        positionKeys: user.positionKeys ?? [],
        roles: user.roles ?? [],
        qualifications: user.qualifications ?? [],

        createdAt: new Date(2026, 1, 14),
        updatedAt: new Date(2024, 11, 1),
        lastLoginAt: new Date(),
        verifiedAt: user.verified ? new Date() : undefined,

        dateOfBirth: new Date(2024, 11, 1),
        placeOfBirth: 'Testtown',
        nationality: 'DE',
        passNr: 'ABC 12234',

        address: {
            addressLine1: 'Mockstreet 2',
            addressLine2: 'Mock',
            town: 'Mocktown',
            zipcode: '12345',
            country: 'DE',
        },
        mobile: '+49 123 12345',
        phone: '+49 123 12346',
        phoneWork: '+49 123 12347',

        emergencyContact: {
            phone: '+49 123 12348',
            name: 'Emilia Emergency',
        },
        diseases: 'mock disease',
        medication: 'mock meds',
        diet: 'omnivore',
        intolerances: 'mock intolerances',

        comment: 'lorem ipsum dolor sit amet',
    };
}
