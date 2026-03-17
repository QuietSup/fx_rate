-- Currency pairs (base + quote), referenced by historical and file_upload
CREATE TABLE pairs (
    id    BIGSERIAL PRIMARY KEY,
    base  VARCHAR(10) NOT NULL,
    quote VARCHAR(10) NOT NULL,
    CONSTRAINT uq_pairs_base_quote UNIQUE (base, quote)
);

CREATE INDEX idx_pairs_base_quote ON pairs (base, quote);

COMMENT ON TABLE pairs IS 'Currency pairs (e.g. EUR/USD); referenced by historical and file_upload';
