CREATE TABLE IF NOT EXISTS positions
(
    key           TEXT PRIMARY KEY,
    name          TEXT    NOT NULL,
    color         TEXT    NOT NULL,
    prio          INTEGER NOT NULL,
    imo_list_name TEXT    NOT NULL
);