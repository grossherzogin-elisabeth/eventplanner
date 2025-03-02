CREATE TABLE IF NOT EXISTS queued_emails
(
    key        TEXT PRIMARY KEY,
    email      TEXT      NOT NULL,
    subject    TEXT      NOT NULL,
    body       TEXT      NOT NULL,
    retries    INTEGER   NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    type       TEXT               DEFAULT '',
    user_key   TEXT               DEFAULT ''
);

CREATE INDEX idx_email_queue_created_at ON queued_emails (created_at);
