-- Create Event table with JSON columns for nested entities
CREATE TABLE events
(
    key           TEXT PRIMARY KEY,
    name          TEXT NOT NULL,
    state         TEXT,
    note          TEXT,
    description   TEXT,
    start         TEXT NOT NULL,
    end           TEXT NOT NULL,
    locations     JSON,
    slots         JSON,
    registrations JSON
);

-- Create index for better query performance on frequently used columns
CREATE INDEX idx_events_start ON events (start);
CREATE INDEX idx_events_end ON events (end);
CREATE INDEX idx_events_state ON events (state);