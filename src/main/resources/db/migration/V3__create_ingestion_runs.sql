CREATE TABLE ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(255) NOT NULL,
    feed_url TEXT NOT NULL,
    run_status VARCHAR(20) NOT NULL CHECK (run_status IN ('RUNNING', 'SUCCEEDED', 'PARTIAL', 'FAILED')),
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    total_items INTEGER NOT NULL DEFAULT 0 CHECK (total_items >= 0),
    inserted_count INTEGER NOT NULL DEFAULT 0 CHECK (inserted_count >= 0),
    duplicate_count INTEGER NOT NULL DEFAULT 0 CHECK (duplicate_count >= 0),
    invalid_count INTEGER NOT NULL DEFAULT 0 CHECK (invalid_count >= 0),
    failed_count INTEGER NOT NULL DEFAULT 0 CHECK (failed_count >= 0),
    error_code VARCHAR(50),
    error_message VARCHAR(255)
);

CREATE INDEX ingestion_runs_started_at_id_idx ON ingestion_runs (started_at DESC, id DESC);
