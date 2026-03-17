-- File upload tracking for CSV imports (status polling)
CREATE TABLE file_upload (
    id             BIGSERIAL PRIMARY KEY,
    file_upload_uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    status         VARCHAR(20) NOT NULL DEFAULT 'TO_PROCESS',  -- TO_PROCESS, PROCESSING, FINISHED, FAILED
    pair_id        BIGINT REFERENCES pairs(id),
    rows_loaded    INT,
    rows_skipped   INT,
    error_message  TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_file_upload_uuid UNIQUE (file_upload_uuid)
);

CREATE INDEX idx_file_upload_uuid ON file_upload (file_upload_uuid);
CREATE INDEX idx_file_upload_status ON file_upload (status);
CREATE INDEX idx_file_upload_pair_id ON file_upload (pair_id);

COMMENT ON TABLE file_upload IS 'Tracks CSV upload jobs; POST returns file_upload_uuid, GET by uuid returns status';
COMMENT ON COLUMN file_upload.status IS 'TO_PROCESS | PROCESSING | FINISHED | FAILED';
COMMENT ON COLUMN file_upload.pair_id IS 'FK to pairs (nullable)';
