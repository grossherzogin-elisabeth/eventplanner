CREATE TABLE IF NOT EXISTS qualifications
(
    key             TEXT PRIMARY KEY,
    name            TEXT    NOT NULL,
    icon            TEXT    NOT NULL,
    description     TEXT    NOT NULL,
    expires         INTEGER NOT NULL,
    grants_position TEXT    NULL
);