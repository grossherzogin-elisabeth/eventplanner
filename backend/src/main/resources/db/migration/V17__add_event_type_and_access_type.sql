ALTER TABLE events
    ADD column type TEXT NOT NULL DEFAULT '';
ALTER TABLE events
    ADD column access_type TEXT NOT NULL DEFAULT 'assignment';