import type { Location } from '@/domain';

export function mockLocation1(overwrite?: Partial<Location>): Location {
    const location: Location = { order: 1, name: 'Elsfleth', country: 'DE', icon: '' };
    return overwrite ? Object.assign(location, overwrite) : location;
}

export function mockLocation2(overwrite?: Partial<Location>): Location {
    const location: Location = { order: 2, name: 'North Sea', country: 'DE', icon: '' };
    return overwrite ? Object.assign(location, overwrite) : location;
}

export function mockLocation3(overwrite?: Partial<Location>): Location {
    const location: Location = { order: 3, name: 'Elsfleth', country: 'DE', icon: '' };
    return overwrite ? Object.assign(location, overwrite) : location;
}

export function mockLocations(): Location[] {
    return [mockLocation1(), mockLocation2(), mockLocation3()];
}
