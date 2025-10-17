-- ======================
-- V2: Snapshot giá (no discount) + Totals + Constraints + View + Store checks
-- ======================

-- 1) CART ITEMS: snapshot giá + thuế + tiền tệ
ALTER TABLE cart_items
  ADD COLUMN unit_price    NUMERIC(12,2),
  ADD COLUMN tax_rate      NUMERIC(5,2)  NOT NULL DEFAULT 0,
  ADD COLUMN currency_code CHAR(3)       NOT NULL DEFAULT 'VND';

-- Backfill từ giá hiện tại sản phẩm
UPDATE cart_items ci
SET unit_price = p.price
FROM products p
WHERE ci.product_id = p.product_id
  AND ci.unit_price IS NULL;

ALTER TABLE cart_items
  ALTER COLUMN unit_price SET NOT NULL;

-- 2) ORDER ITEMS: đồng bộ cột thuế/tiền tệ (để copy từ cart lúc checkout)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='order_items' AND column_name='tax_rate'
  ) THEN
    ALTER TABLE order_items
      ADD COLUMN tax_rate NUMERIC(5,2) NOT NULL DEFAULT 0;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='order_items' AND column_name='currency_code'
  ) THEN
    ALTER TABLE order_items
      ADD COLUMN currency_code CHAR(3) NOT NULL DEFAULT 'VND';
  END IF;
END$$;

-- 3) CARTS & ORDERS: totals header (không dùng discount)
ALTER TABLE carts
  ADD COLUMN subtotal     NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN tax_total    NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN shipping_fee NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN grand_total  NUMERIC(14,2) NOT NULL DEFAULT 0;

ALTER TABLE orders
  ADD COLUMN subtotal     NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN tax_total    NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN shipping_fee NUMERIC(14,2) NOT NULL DEFAULT 0,
  ADD COLUMN grand_total  NUMERIC(14,2) NOT NULL DEFAULT 0;

-- 4) Giới hạn 1 giỏ ACTIVE / user
CREATE UNIQUE INDEX IF NOT EXISTS uq_active_cart_per_user
  ON carts(user_id) WHERE (status = 'ACTIVE');

-- 5) Chặn INSERT/UPDATE cart_items nếu cart không ACTIVE
CREATE OR REPLACE FUNCTION ensure_cart_active() RETURNS TRIGGER AS $$
DECLARE s cart_status;
BEGIN
  SELECT status INTO s FROM carts WHERE cart_id = NEW.cart_id;
  IF s IS DISTINCT FROM 'ACTIVE' THEN
    RAISE EXCEPTION 'Cannot modify items of non-ACTIVE cart % (status=%)', NEW.cart_id, s;
  END IF;
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS cart_items_only_active ON cart_items;
CREATE TRIGGER cart_items_only_active
BEFORE INSERT OR UPDATE ON cart_items
FOR EACH ROW EXECUTE FUNCTION ensure_cart_active();

-- 6) Tự động tính totals cho CART sau khi thay đổi cart_items
CREATE OR REPLACE FUNCTION recalc_cart_totals(p_cart_id INT) RETURNS VOID AS $$
BEGIN
  UPDATE carts c SET
    subtotal = COALESCE((
      SELECT SUM(ci.unit_price * ci.quantity)
      FROM cart_items ci
      WHERE ci.cart_id = c.cart_id AND ci.is_selected = TRUE
    ), 0),
    tax_total = COALESCE((
      SELECT SUM((ci.unit_price * ci.quantity) * (ci.tax_rate/100.0))
      FROM cart_items ci
      WHERE ci.cart_id = c.cart_id AND ci.is_selected = TRUE
    ), 0),
    grand_total = subtotal + tax_total + shipping_fee
  WHERE c.cart_id = p_cart_id;
END; $$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_recalc_cart() RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP IN ('INSERT','UPDATE') THEN
    PERFORM recalc_cart_totals(NEW.cart_id);
  ELSIF TG_OP = 'DELETE' THEN
    PERFORM recalc_cart_totals(OLD.cart_id);
  END IF;
  RETURN NULL;
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS cart_items_recalc ON cart_items;
CREATE TRIGGER cart_items_recalc
AFTER INSERT OR UPDATE OR DELETE ON cart_items
FOR EACH ROW EXECUTE FUNCTION trg_recalc_cart();

-- 7) Sửa VIEW: dùng unit_price snapshot thay vì products.price
DROP VIEW IF EXISTS v_cart_totals_selected;

CREATE VIEW v_cart_totals_selected AS
SELECT c.cart_id,
       COALESCE(SUM(ci.quantity * ci.unit_price), 0)::NUMERIC(12,2) AS total_price_selected
FROM carts c
LEFT JOIN cart_items ci
       ON ci.cart_id = c.cart_id AND ci.is_selected = TRUE
GROUP BY c.cart_id;

-- 8) Check biên độ toạ độ store
ALTER TABLE store_locations
  ADD CONSTRAINT chk_store_lat CHECK (latitude BETWEEN -90 AND 90),
  ADD CONSTRAINT chk_store_lng CHECK (longitude BETWEEN -180 AND 180);
