CREATE TABLE ics_calendar
(
    key      TEXT PRIMARY KEY,
    token    TEXT        NOT NULL,
    user_key TEXT UNIQUE NOT NULL
);

CREATE INDEX idx_ics_calendar_key_and_token ON ics_calendar (key, token);
CREATE INDEX idx_ics_calendar_user_key ON ics_calendar (user_key);