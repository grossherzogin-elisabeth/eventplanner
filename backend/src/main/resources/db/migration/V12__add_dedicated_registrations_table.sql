-- Create registration table
CREATE TABLE event_registrations
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

-- Create index for better query performance on frequently used columns
CREATE INDEX idx_event_registrations_event_key ON event_registrations (event_key);

-- Migrate data into own table
INSERT INTO event_registrations (key,
                                 event_key,
                                 position_key,
                                 user_key,
                                 name,
                                 note,
                                 access_key,
                                 confirmed_at)
SELECT json_extract(json_each.value, '$.key')         AS key,
       e.key                                          as event_key,
       json_extract(json_each.value, '$.positionKey') AS position_key,
       json_extract(json_each.value, '$.userKey')     AS user_key,
       json_extract(json_each.value, '$.name')        AS name,
       json_extract(json_each.value, '$.note')        AS note,
       json_extract(json_each.value, '$.accessKey')   AS access_key,
       json_extract(json_each.value, '$.confirmedAt') AS confirmed_at
FROM events e, json_each(registrations)
WHERE json_valid(registrations);

-- Drop old column on events table
ALTER TABLE events
    DROP COLUMN registrations;