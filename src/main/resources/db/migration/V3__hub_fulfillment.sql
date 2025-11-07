-- Hub-based fulfillment support
ALTER TABLE store_locations
    ADD COLUMN IF NOT EXISTS is_hub BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS order_fulfillment_sources (
    order_fulfillment_source_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    order_item_id INT NOT NULL REFERENCES order_items(order_item_id) ON DELETE CASCADE,
    store_location_id INT NOT NULL REFERENCES store_locations(store_location_id) ON DELETE RESTRICT,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
    soft_reserved_quantity INT NOT NULL CHECK (soft_reserved_quantity >= 0),
    hard_deducted_quantity INT NOT NULL DEFAULT 0 CHECK (hard_deducted_quantity >= 0),
    soft_reserved_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    hard_deducted_at TIMESTAMPTZ,
    CHECK (hard_deducted_quantity <= soft_reserved_quantity),
    UNIQUE (order_item_id, store_location_id)
);

CREATE INDEX IF NOT EXISTS idx_order_fulfillment_sources_order
    ON order_fulfillment_sources(order_id);

CREATE INDEX IF NOT EXISTS idx_order_fulfillment_sources_store_product
    ON order_fulfillment_sources(store_location_id, product_id);
