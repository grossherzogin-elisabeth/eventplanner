export enum Permission {
    READ_ACCOUNT = 'account:read',

    LIST_EVENTS = 'events:list',
    READ_EVENTS = 'events:read',
    EXPORT_EVENTS = 'events:export',
    CREATE_EVENTS = 'events:create',
    UPDATE_EVENTS = 'events:update',
    UPDATE_EVENT_DETAILS = 'events:update-details',
    UPDATE_EVENT_SLOTS = 'events:update-slots',
    DELETE_EVENTS = 'events:delete',

    UPDATE_REGISTRATIONS = 'registrations:update',
    UPDATE_OWN_REGISTRATIONS = 'registrations:update-own',
    CONFIRM_OWN_REGISTRATIONS = 'registrations:confirm-own',
    DECLINE_OWN_REGISTRATIONS = 'registrations:decline-own',

    READ_OWN_USER = 'users:read-own-details',
    UPDATE_OWN_USER = 'users:update-own-details',

    LIST_USERS = 'users:list',
    LIST_DETAILED_USERS = 'users:list-details',
    READ_DETAILED_USERS = 'users:read-details',
    CREATE_USERS = 'users:create',
    UPDATE_USERS = 'users:update-details',
    DELETE_USERS = 'users:delete',

    LIST_POSITIONS = 'positions:list',
    CREATE_POSITIONS = 'positions:create',
    UPDATE_POSITIONS = 'positions:update',
    DELETE_POSITIONS = 'positions:delete',

    LIST_QUALIFICATIONS = 'qualifications:list',
    CREATE_QUALIFICATIONS = 'qualifications:create',
    UPDATE_QUALIFICATIONS = 'qualifications:update',
    DELETE_QUALIFICATIONS = 'qualifications:delete',

    UPDATE_SETTINGS = 'application-settings:update',
}
