ALTER TABLE queued_emails
    ADD column type TEXT DEFAULT '';
ALTER TABLE queued_emails
    ADD column user_key TEXT DEFAULT '';
