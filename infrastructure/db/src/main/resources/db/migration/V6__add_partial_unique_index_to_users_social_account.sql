CREATE UNIQUE INDEX uq_users_social_account_active ON users (social_provider, social_id) WHERE deleted_at IS NULL AND social_id IS NOT NULL;
