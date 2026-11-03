ALTER TABLE events
    ADD column flags TEXT NOT NULL DEFAULT '{}';

UPDATE events
SET flags = '{
    "enableDirectSignup": false,
    "enableRegistrationConfirmation": true,
    "enableOvernightStaySelection": true,
    "enableArrivalDaySelection": true
}'
WHERE signup_type = 'assignment';

UPDATE events
SET flags = '{
    "enableDirectSignup": true,
    "enableRegistrationConfirmation": true,
    "enableOvernightStaySelection": true,
    "enableArrivalDaySelection": true
}'
WHERE signup_type = 'open';

ALTER TABLE events
    DROP column signup_type;