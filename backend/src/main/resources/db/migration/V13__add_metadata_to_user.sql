ALTER TABLE users
    ADD column oidc_id TEXT NULL;
ALTER TABLE users
    ADD column created_at TEXT NULL;
ALTER TABLE users
    ADD column updated_at TEXT NULL;
ALTER TABLE users
    ADD column verified_at TEXT NULL;
ALTER TABLE users
    ADD column last_login_at TEXT NULL;
