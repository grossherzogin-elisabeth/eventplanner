CREATE TABLE IF NOT EXISTS event_registrations
(
    key          TEXT PRIMARY KEY,
    event_key    TEXT NOT NULL,
    position_key TEXT NOT NULL,
    user_key     TEXT NULL,
    name         TEXT NULL,
    note         TEXT NULL,
    access_key   TEXT NULL,
    confirmed_at TEXT NULL
);

CREATE INDEX idx_event_registrations_event_key ON event_registrations (event_key);
