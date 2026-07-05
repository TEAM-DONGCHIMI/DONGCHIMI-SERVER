ALTER TABLE users DROP CONSTRAINT users_email_key;

CREATE UNIQUE INDEX uq_users_email_active ON users (email) WHERE deleted_at IS NULL;
