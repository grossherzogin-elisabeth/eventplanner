export enum Permission {
    READ_EVENTS = 'events:read',
    WRITE_EVENTS = 'events:write',
    READ_USERS = 'users:read',
    READ_OWN_USER = 'user-details:read-self',
    WRITE_OWN_USER = 'user-details:write-self',
    READ_USER_DETAILS = 'user-details:read',
    WRITE_USERS = 'user-details:write',
    READ_POSITIONS = 'positions:read',
    WRITE_POSITIONS = 'positions:write',
    EVENT_TEAM_WRITE_SELF = 'event-team:write-self',
    EVENT_TEAM_WRITE = 'event-team:write',
    BETA_FEATURES = 'beta-features',
}
