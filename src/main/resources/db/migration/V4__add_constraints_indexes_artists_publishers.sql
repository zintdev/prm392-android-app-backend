
-- Indexes cho tra cứu
CREATE INDEX IF NOT EXISTS idx_publishers_name ON publishers(publisher_name);
CREATE INDEX IF NOT EXISTS idx_artists_name ON artists(artist_name);
CREATE INDEX IF NOT EXISTS idx_artists_type ON artists(artist_type);
