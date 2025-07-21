ALTER TABLE events
    ADD column type TEXT NOT NULL DEFAULT '';
ALTER TABLE events
    ADD column signup_type TEXT NOT NULL DEFAULT 'assignment';