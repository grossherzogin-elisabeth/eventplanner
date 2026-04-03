import type { UserDetailsRepresentation } from '@/adapter/rest/UserRestRepository';
import { Role } from '@/domain';
import { CAPTAIN, MATE, QUALIFICATION_CAPTAIN, QUALIFICATION_MATE, USER_CAPTAIN } from '~/mocks/keys';

export function mockUserDetailsRepresentation(overwrite?: Partial<UserDetailsRepresentation>): UserDetailsRepresentation {
    const user: UserDetailsRepresentation = {
        // gender: undefined,
        // key: user.key,
        // authKey: user.key, // keep them equal for easier debugging
        // title: 'Dr',
        // nickName: user.nickName,
        // firstName: user.firstName,
        // secondName: 'Segundo',
        // lastName: user.lastName,
        // email: user.email ?? 'mocked@email.com',
        // positions: user.positionKeys ?? [],
        // roles: user.roles ?? [],
        // qualifications: user.qualifications ?? [],
        gender: undefined,
        key: USER_CAPTAIN,
        authKey: USER_CAPTAIN,
        title: 'Dr',
        firstName: 'Charlie',
        secondName: 'Segundo',
        lastName: 'Captain',
        email: 'charlie.captain@example.com',
        positions: [CAPTAIN, MATE],
        roles: [Role.TEAM_MEMBER],
        qualifications: [
            { qualificationKey: QUALIFICATION_CAPTAIN, expires: true, expiresAt: '2024-07-10T09:00:00Z' },
            { qualificationKey: QUALIFICATION_MATE, expires: false },
        ],
        createdAt: '2026-02-14',
        updatedAt: '2024-12-1',
        lastLoginAt: '2026-02-21T08:41:59.497Z',
        verifiedAt: '2026-02-21T08:41:59.497Z',

        dateOfBirth: '2024-11-01',
        placeOfBirth: 'Testtown',
        nationality: 'DE',
        passNr: 'ABC 12234',

        address: {
            addressLine1: 'Mockstreet 2',
            addressLine2: 'Mock',
            town: 'Mocktown',
            zipCode: '12345',
            country: 'DE',
        },
        mobile: '+49 123 12345',
        phone: '+49 123 12345',
        phoneWork: '+49 123 12345',

        emergencyContact: {
            phone: '',
            name: '',
        },
        diseases: 'mock disease',
        medication: 'mock meds',
        diet: 'omnivore',
        intolerances: 'mock intolerances',

        comment: 'lorem ipsum dolor sit amet',
    };
    return overwrite ? Object.assign(user, overwrite) : user;
}
