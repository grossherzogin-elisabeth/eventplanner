CREATE TABLE IF NOT EXISTS events
(
    key                                       TEXT PRIMARY KEY,
    year                                      INTEGER NOT NULL,
    name                                      TEXT    NOT NULL,
    state                                     TEXT    NOT NULL,
    note                                      TEXT    NOT NULL default '',
    description                               TEXT    NOT NULL default '',
    start                                     TEXT    NOT NULL,
    end                                       TEXT    NOT NULL,
    locations                                 JSON    NOT NULL DEFAULT '[]',
    slots                                     JSON    NOT NULL DEFAULT '[]',
    participation_confirmations_requests_sent INT              DEFAULT 0
);

CREATE INDEX idx_events_year ON events (year);