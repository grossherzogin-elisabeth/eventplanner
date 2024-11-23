export enum Permission {
    READ_EVENTS = 'events:read',
    WRITE_EVENTS = 'events:write',
    CREATE_EVENTS = 'events:create',
    DELETE_EVENTS = 'events:delete',
    WRITE_EVENT_DETAILS = 'events:write-details',
    WRITE_EVENT_SLOTS = 'events:write-slots',

    READ_USERS = 'users:read',
    READ_OWN_USER = 'users:read-details-self',
    WRITE_OWN_USER = 'users:write-self',
    READ_USER_DETAILS = 'users:read-details',
    WRITE_USERS = 'users:write',
    DELETE_USERS = 'users:delete',

    READ_POSITIONS = 'positions:read',
    WRITE_POSITIONS = 'positions:write',

    READ_QUALIFICATIONS = 'qualifications:read',
    WRITE_QUALIFICATIONS = 'qualifications:write',

    WRITE_REGISTRATIONS = 'registrations:write',
    WRITE_OWN_REGISTRATIONS = 'registrations:write-self',
    BETA_FEATURES = 'beta-features',
    WRITE_SETTINGS = 'application-settings:write',
}
