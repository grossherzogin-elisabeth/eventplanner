CREATE TABLE users
(
    key            TEXT PRIMARY KEY,
    auth_key       TEXT,
    gender         TEXT,
    title          TEXT,
    first_name     TEXT NOT NULL,
    nick_name      TEXT,
    second_name    TEXT,
    last_name      TEXT NOT NULL,
    positions      TEXT NOT NULL,
    roles          TEXT NOT NULL,
    qualifications TEXT NOT NULL,
    address        TEXT,
    email          TEXT,
    phone          TEXT,
    mobile         TEXT,
    date_of_birth  TEXT,
    place_of_birth TEXT,
    pass_nr        TEXT,
    comment        TEXT,
    nationality    TEXT
);