CREATE INDEX IF NOT EXISTS idx_historical_pair_date_id_desc
    ON historical (pair_id, date DESC, id DESC);
