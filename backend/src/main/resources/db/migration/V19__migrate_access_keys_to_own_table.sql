CREATE TABLE access_keys
(
    access_key TEXT PRIMARY KEY   DEFAULT '',
    user_key   TEXT               DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_access_keys_user_key ON access_keys (user_key);

INSERT INTO access_keys (access_key, user_key, created_at)
SELECT r.access_key                              AS access_key,
       COALESCE(MAX(NULLIF(r.user_key, '')), '') AS user_key,
       MIN(e.start)                              AS created_at
FROM event_registrations r
         JOIN events e ON e.key = r.event_key
WHERE r.access_key IS NOT NULL
  AND r.access_key <> ''
  AND r.user_key IS NOT NULL
  AND r.user_key <> ''
  AND datetime(e.start) > datetime('now')
GROUP BY r.access_key;

ALTER TABLE event_registrations
    DROP COLUMN access_key;
