CREATE TABLE IF NOT EXISTS users
(
    key               TEXT PRIMARY KEY,
    oidc_id           TEXT,
    created_at        TEXT NOT NULL,
    updated_at        TEXT NOT NULL,
    verified_at       TEXT,
    last_login_at     TEXT,
    gender            TEXT,
    title             TEXT,
    first_name        TEXT NOT NULL,
    nick_name         TEXT,
    second_name       TEXT,
    last_name         TEXT NOT NULL,
    roles             TEXT NOT NULL,
    qualifications    TEXT NOT NULL,
    address           TEXT,
    email             TEXT,
    phone             TEXT,
    phone_work        TEXT,
    mobile            TEXT,
    date_of_birth     TEXT,
    place_of_birth    TEXT,
    pass_nr           TEXT,
    comment           TEXT,
    nationality       TEXT,
    emergency_contact TEXT,
    diseases          TEXT,
    intolerances      TEXT,
    medication        TEXT,
    diet              TEXT
);

CREATE INDEX idx_users_oidc_id ON users (oidc_id);