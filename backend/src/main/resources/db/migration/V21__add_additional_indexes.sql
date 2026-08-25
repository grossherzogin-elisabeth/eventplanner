CREATE INDEX idx_event_registrations_year_event_key ON event_registrations (year, event_key);
CREATE INDEX idx_access_keys_created_at ON access_keys (created_at);
