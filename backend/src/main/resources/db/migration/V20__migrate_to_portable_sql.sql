-- qualifications: expires INTEGER -> BOOLEAN
CREATE TABLE qualifications_temp
(
    key             TEXT PRIMARY KEY,
    name            TEXT    NOT NULL,
    icon            TEXT    NOT NULL,
    description     TEXT    NOT NULL,
    expires         BOOLEAN NOT NULL,
    grants_position TEXT    NULL
);

INSERT INTO qualifications_temp (key, name, icon, description, expires, grants_position)
SELECT key,
       name,
       icon,
       description,
       CASE
           WHEN expires IN (1, '1', true, 'true') THEN 1
           ELSE 0
           END,
       grants_position
FROM qualifications;

DROP TABLE qualifications;
ALTER TABLE qualifications_temp
    RENAME TO qualifications;

-- queued_emails: created_at TIMESTAMP -> TEXT
CREATE TABLE queued_emails_temp
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

INSERT INTO queued_emails_temp (key, email, subject, body, retries, created_at, type, user_key)
SELECT key,
       email,
       subject,
       body,
       retries,
       CAST(created_at AS TEXT),
       type,
       user_key
FROM queued_emails;

DROP TABLE queued_emails;
ALTER TABLE queued_emails_temp
    RENAME TO queued_emails;
CREATE INDEX idx_email_queue_created_at ON queued_emails (created_at);

-- access_keys: created_at TIMESTAMP -> TEXT
CREATE TABLE access_keys_temp
(
    access_key TEXT PRIMARY KEY DEFAULT '',
    user_key   TEXT             DEFAULT '',
    created_at TEXT NOT NULL
);

INSERT INTO access_keys_temp (access_key, user_key, created_at)
SELECT access_key,
       user_key,
       CAST(created_at AS TEXT)
FROM access_keys;

DROP TABLE access_keys;
ALTER TABLE access_keys_temp
    RENAME TO access_keys;
CREATE INDEX idx_access_keys_user_key ON access_keys (user_key);

-- event_registrations: overnight_stay INTEGER -> BOOLEAN (+ add year for fast year queries)
CREATE TABLE event_registrations_temp
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

INSERT INTO event_registrations_temp (key, event_key, year, position_key, user_key, name, note, confirmed_at,
                                      overnight_stay, arrival)
SELECT r.key,
       r.event_key,
       e.year,
       r.position_key,
       r.user_key,
       r.name,
       r.note,
       r.confirmed_at,
       CASE
           WHEN r.overnight_stay IS NULL THEN NULL
           WHEN r.overnight_stay IN (1, '1', true, 'true') THEN 1
           ELSE 0
           END,
       r.arrival
FROM event_registrations r
         JOIN events e ON e.key = r.event_key;

DROP TABLE event_registrations;
ALTER TABLE event_registrations_temp
    RENAME TO event_registrations;
CREATE INDEX idx_event_registrations_event_key ON event_registrations (event_key);
CREATE INDEX idx_event_registrations_year ON event_registrations (year);

-- events: JSON -> TEXT
CREATE TABLE events_temp
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

INSERT INTO events_temp (key, year, name, state, note, description, start, "end",
                         locations, slots, participation_confirmations_requests_sent, type, signup_type)
SELECT key,
       year,
       name,
       state,
       note,
       description,
       start,
       "end",
       locations,
       slots,
       participation_confirmations_requests_sent,
       type,
       signup_type
FROM events;

DROP TABLE events;
ALTER TABLE events_temp
    RENAME TO events;
CREATE INDEX idx_events_year ON events (year);