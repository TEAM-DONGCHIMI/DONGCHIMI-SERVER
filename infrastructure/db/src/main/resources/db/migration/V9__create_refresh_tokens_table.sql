CREATE TABLE refresh_tokens (
    token_id   VARCHAR(36) PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    expires_at TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
