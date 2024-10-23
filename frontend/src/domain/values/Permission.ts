export enum Permission {
    READ_EVENTS = 'events:read',
    WRITE_EVENTS = 'events:write',

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

    EVENT_TEAM_WRITE_SELF = 'event-team:write-self',
    EVENT_TEAM_WRITE = 'event-team:write',
    BETA_FEATURES = 'beta-features',
    WRITE_SETTINGS = 'application-settings:write',
}
