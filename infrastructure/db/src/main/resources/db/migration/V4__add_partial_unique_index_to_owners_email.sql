CREATE UNIQUE INDEX uq_owners_email_active ON owners (email) WHERE deleted_at IS NULL;
