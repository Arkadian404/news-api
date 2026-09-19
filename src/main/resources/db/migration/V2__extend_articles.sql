ALTER TABLE articles
    ADD COLUMN source VARCHAR(255),
    ADD COLUMN source_url TEXT,
    ADD COLUMN category VARCHAR(255),
    ADD COLUMN description TEXT,
    ADD COLUMN published_at TIMESTAMPTZ,
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD CONSTRAINT articles_source_url_unique UNIQUE (source_url);

ALTER TABLE articles ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE articles ALTER COLUMN updated_at DROP DEFAULT;

CREATE INDEX articles_published_at_id_idx ON articles (published_at DESC NULLS LAST, id DESC);
