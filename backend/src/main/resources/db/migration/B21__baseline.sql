-- Schema after V21 for new databases; existing databases retain their migration history.

CREATE TABLE events
(
    key                                       TEXT PRIMARY KEY,
    year                                      INTEGER NOT NULL,
    name                                      TEXT    NOT NULL,
    state                                     TEXT    NOT NULL,
    note                                      TEXT    NOT NULL DEFAULT '',
    description                               TEXT    NOT NULL DEFAULT '',
    start                                     TEXT    NOT NULL,
    "end"                                     TEXT    NOT NULL,
    locations                                 TEXT    NOT NULL DEFAULT '[]',
    slots                                     TEXT    NOT NULL DEFAULT '[]',
    participation_confirmations_requests_sent INTEGER          DEFAULT 0,
    type                                      TEXT    NOT NULL DEFAULT '',
    signup_type                               TEXT    NOT NULL DEFAULT 'assignment'
);

CREATE INDEX idx_events_year ON events (year);

CREATE TABLE positions
(
    key           TEXT PRIMARY KEY,
    name          TEXT    NOT NULL,
    color         TEXT    NOT NULL,
    prio          INTEGER NOT NULL,
    imo_list_name TEXT    NOT NULL DEFAULT ''
);

CREATE TABLE qualifications
(
    key             TEXT PRIMARY KEY,
    name            TEXT    NOT NULL,
    icon            TEXT    NOT NULL,
    description     TEXT    NOT NULL,
    expires         BOOLEAN NOT NULL,
    grants_position TEXT    NULL
);

CREATE TABLE users
(
    key               TEXT PRIMARY KEY,
    gender            TEXT,
    title             TEXT,
    first_name        TEXT NOT NULL,
    nick_name         TEXT,
    second_name       TEXT,
    last_name         TEXT NOT NULL,
    roles             TEXT NOT NULL,
    qualifications    TEXT NOT NULL,
    address           TEXT,
    email             TEXT,
    phone             TEXT,
    mobile            TEXT,
    date_of_birth     TEXT,
    place_of_birth    TEXT,
    pass_nr           TEXT,
    comment           TEXT,
    nationality       TEXT,
    emergency_contact TEXT NULL,
    diseases          TEXT NULL,
    intolerances      TEXT NULL,
    medication        TEXT NULL,
    diet              TEXT NULL,
    phone_work        TEXT NULL,
    oidc_id           TEXT NULL,
    created_at        TEXT NULL,
    updated_at        TEXT NULL,
    verified_at       TEXT NULL,
    last_login_at     TEXT NULL
);

CREATE UNIQUE INDEX idx_user_oidc_id ON users (oidc_id);

CREATE TABLE settings
(
    key   TEXT PRIMARY KEY,
    value TEXT
);

CREATE TABLE queued_emails
(
    key        TEXT PRIMARY KEY,
    email      TEXT    NOT NULL,
    subject    TEXT    NOT NULL,
    body       TEXT    NOT NULL,
    retries    INTEGER NOT NULL DEFAULT 0,
    created_at TEXT    NOT NULL,
    type       TEXT             DEFAULT '',
    user_key   TEXT             DEFAULT ''
);

CREATE INDEX idx_email_queue_created_at ON queued_emails (created_at);

CREATE TABLE access_keys
(
    access_key TEXT PRIMARY KEY DEFAULT '',
    user_key   TEXT             DEFAULT '',
    created_at TEXT NOT NULL
);

CREATE INDEX idx_access_keys_user_key ON access_keys (user_key);
CREATE INDEX idx_access_keys_created_at ON access_keys (created_at);

CREATE TABLE event_registrations
(
    key            TEXT PRIMARY KEY,
    event_key      TEXT    NOT NULL,
    year           INTEGER NOT NULL,
    position_key   TEXT    NOT NULL,
    user_key       TEXT    NULL,
    name           TEXT    NULL,
    note           TEXT    NULL,
    confirmed_at   TEXT    NULL,
    overnight_stay BOOLEAN NULL,
    arrival        TEXT    NULL
);

CREATE INDEX idx_event_registrations_event_key ON event_registrations (event_key);
CREATE INDEX idx_event_registrations_year ON event_registrations (year);
CREATE INDEX idx_event_registrations_year_event_key ON event_registrations (year, event_key);