-- qualifications: expires INTEGER -> BOOLEAN
CREATE TABLE qualifications_new
(
    key             TEXT PRIMARY KEY,
    name            TEXT    NOT NULL,
    icon            TEXT    NOT NULL,
    description     TEXT    NOT NULL,
    expires         BOOLEAN NOT NULL,
    grants_position TEXT    NULL
);

INSERT INTO qualifications_new (key, name, icon, description, expires, grants_position)
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
ALTER TABLE qualifications_new
    RENAME TO qualifications;

-- event_registrations: overnight_stay INTEGER -> BOOLEAN (+ add year for fast year queries)
CREATE TABLE event_registrations_new
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

INSERT INTO event_registrations_new (key, event_key, year, position_key, user_key, name, note, confirmed_at,
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
ALTER TABLE event_registrations_new
    RENAME TO event_registrations;
CREATE INDEX idx_event_registrations_event_key ON event_registrations (event_key);
CREATE INDEX idx_event_registrations_year ON event_registrations (year);

-- events: JSON -> TEXT
CREATE TABLE events_new
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

INSERT INTO events_new (key, year, name, state, note, description, start, "end",
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
ALTER TABLE events_new
    RENAME TO events;
CREATE INDEX idx_events_year ON events (year);