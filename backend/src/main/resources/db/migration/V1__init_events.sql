-- Create Event table with JSON columns for nested entities
CREATE TABLE events
(
    key           TEXT PRIMARY KEY,
    year          INTEGER NOT NULL,
    name          TEXT    NOT NULL,
    state         TEXT    NOT NULL,
    note          TEXT    NOT NULL default '',
    description   TEXT    NOT NULL default '',
    start         TEXT    NOT NULL,
    end           TEXT    NOT NULL,
    locations     JSON    NOT NULL DEFAULT '[]',
    slots         JSON    NOT NULL DEFAULT '[]',
    registrations JSON    NOT NULL DEFAULT '[]'
);

-- Create index for better query performance on frequently used columns
CREATE INDEX idx_events_year ON events (year);