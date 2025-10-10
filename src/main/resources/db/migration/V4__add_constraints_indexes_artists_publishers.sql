-- Unique constraints cho Publishers
ALTER TABLE publishers
  ADD CONSTRAINT uq_publishers_publisher_name UNIQUE (publisher_name);

-- Unique constraints cho Artists  
ALTER TABLE artists
  ADD CONSTRAINT uq_artists_artist_name UNIQUE (artist_name);

-- Indexes cho tra cứu
CREATE INDEX IF NOT EXISTS idx_publishers_name ON publishers(publisher_name);
CREATE INDEX IF NOT EXISTS idx_artists_name ON artists(artist_name);
CREATE INDEX IF NOT EXISTS idx_artists_type ON artists(artist_type);
