ALTER TABLE users
    ADD COLUMN store_location_id INTEGER,
    ADD CONSTRAINT fk_users_store_location
        FOREIGN KEY (store_location_id) REFERENCES store_locations (store_location_id);

CREATE INDEX idx_users_store_location ON users(store_location_id);

ALTER TABLE orders
    ADD COLUMN store_location_id INTEGER,
    ADD COLUMN keeping_expires_at TIMESTAMP WITH TIME ZONE,
    ADD CONSTRAINT fk_orders_store_location
        FOREIGN KEY (store_location_id) REFERENCES store_locations (store_location_id);

CREATE INDEX idx_orders_store_location ON orders(store_location_id);
CREATE INDEX idx_orders_keeping_expires_at ON orders(keeping_expires_at);

UPDATE orders
SET keeping_expires_at = NOW() + INTERVAL '3 days'
WHERE order_status = 'KEEPING' AND keeping_expires_at IS NULL;
