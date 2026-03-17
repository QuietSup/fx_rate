-- FX pair historical OHLC and change data (from CSV)
CREATE TABLE historical (
    id          BIGSERIAL PRIMARY KEY,
    pair_id     BIGINT NOT NULL REFERENCES pairs(id),
    date        DATE NOT NULL,
    high        NUMERIC(12, 5) NOT NULL,
    low         NUMERIC(12, 5) NOT NULL,
    close       NUMERIC(12, 5) NOT NULL,
    change_pips NUMERIC(10, 2),         -- Change(Pips), can be negative
    change_pct  NUMERIC(8, 2),          -- Change(%), can be negative
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_historical_pair_id_date UNIQUE (pair_id, date)
);

CREATE INDEX idx_historical_pair_id ON historical (pair_id);
CREATE INDEX idx_historical_date ON historical (date);

COMMENT ON TABLE historical IS 'FX pair daily OHLC and change data from CSV import';
COMMENT ON COLUMN historical.pair_id IS 'FK to pairs';
COMMENT ON COLUMN historical.change_pips IS 'Price change in pips';
COMMENT ON COLUMN historical.change_pct IS 'Price change in percent';
