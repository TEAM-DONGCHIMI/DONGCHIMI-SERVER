CREATE TABLE product_import_jobs (
    job_id           VARCHAR(32) PRIMARY KEY,
    market_id        BIGINT      NOT NULL,
    owner_id         BIGINT      NOT NULL,
    excel_object_key TEXT        NOT NULL,
    status           VARCHAR(20) NOT NULL,
    attempt_count    INT         NOT NULL DEFAULT 0,
    locked_by        VARCHAR(64),
    locked_until     TIMESTAMP,
    total_count      INT,
    success_count    INT,
    fail_count       INT,
    error_code       VARCHAR(50),
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 큐 claim 전용: PENDING 또는 리스 만료된 IN_PROGRESS를 created_at 순으로 스캔한다
CREATE INDEX idx_product_import_jobs_status_locked_until_created_at ON product_import_jobs (status, locked_until, created_at);
