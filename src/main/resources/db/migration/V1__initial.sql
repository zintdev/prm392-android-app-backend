-- ================================================================
-- UNIFIED BASELINE SCHEMA (V1 + V2 + Chat)
-- ================================================================

-- ======================
-- EXTENSIONS
-- ======================
CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;

-- ======================
-- ENUMs
-- ======================
CREATE TYPE user_role      AS ENUM ('ADMIN','STAFF','CUSTOMER');
CREATE TYPE cart_status    AS ENUM ('ACTIVE','CHECKED_OUT','ABANDONED');
CREATE TYPE order_status   AS ENUM ('PENDING','PAID','PROCESSING','SHIPPED','COMPLETED','CANCELLED');
CREATE TYPE payment_method AS ENUM ('VNPAY','PAYPAL','COD');
CREATE TYPE payment_status AS ENUM ('PENDING','PAID','FAILED');
CREATE TYPE shipment_method AS ENUM ('DELIVERY','PICKUP');
CREATE TYPE blog_status    AS ENUM ('DRAFT','PUBLISHED');

-- ======================
-- USERS
-- ======================
CREATE TABLE users (
  user_id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  username       VARCHAR(150) NOT NULL UNIQUE,
  email          VARCHAR(255) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  phone_number   VARCHAR(20),
  role           user_role NOT NULL DEFAULT 'CUSTOMER',
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ======================
-- ARTISTS / PUBLISHERS / CATEGORIES
-- ======================
CREATE TABLE artists (
  artist_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  artist_type    VARCHAR(50) NOT NULL,
  artist_name    VARCHAR(255) NOT NULL,
  debut_year     INT CHECK (debut_year >= 1900)
);

CREATE TABLE artist_images (
  image_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  artist_id  INT NOT NULL REFERENCES artists(artist_id) ON DELETE CASCADE,
  url        TEXT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (artist_id, sort_order)
);

CREATE TABLE publishers (
  publisher_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  publisher_name VARCHAR(255) NOT NULL UNIQUE,
  founded_year   INT
);

CREATE TABLE categories (
  category_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name        VARCHAR(120) NOT NULL UNIQUE
);

-- ======================
-- PRODUCTS & IMAGES
-- ======================
CREATE TABLE products (
  product_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_name VARCHAR(255) NOT NULL,
  description  TEXT,
  price        NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  quantity     INT NOT NULL CHECK (quantity >= 0),
  image_url    TEXT,
  artist_id    INT REFERENCES artists(artist_id) ON DELETE RESTRICT,
  publisher_id INT REFERENCES publishers(publisher_id) ON DELETE RESTRICT,
  release_date DATE,
  category_id  INT REFERENCES categories(category_id) ON DELETE RESTRICT,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product_images (
  image_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
  url        TEXT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  UNIQUE (product_id, sort_order)
);

-- ======================
-- STORES & INVENTORY (M-M)
-- ======================
CREATE TABLE store_locations (
  store_location_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  latitude          NUMERIC(9,6) NOT NULL CHECK (latitude BETWEEN -90 AND 90),
  longitude         NUMERIC(9,6) NOT NULL CHECK (longitude BETWEEN -180 AND 180),
  address           VARCHAR(255) NOT NULL
);

CREATE TABLE store_inventory (
  store_inventory_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  store_location_id  INT NOT NULL REFERENCES store_locations(store_location_id) ON DELETE CASCADE,
  product_id         INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
  quantity           INT NOT NULL CHECK (quantity >= 0),
  UNIQUE (store_location_id, product_id)
);

-- ======================
-- CARTS (Merged V1 + V2)
-- ======================
CREATE TABLE carts (
  cart_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id      INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
  status       cart_status NOT NULL DEFAULT 'ACTIVE',
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  -- V2 Columns
  subtotal     NUMERIC(14,2) NOT NULL DEFAULT 0,
  tax_total    NUMERIC(14,2) NOT NULL DEFAULT 0,
  shipping_fee NUMERIC(14,2) NOT NULL DEFAULT 0,
  grand_total  NUMERIC(14,2) NOT NULL DEFAULT 0
);

CREATE TABLE cart_items (
  cart_item_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  cart_id      INT NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
  product_id   INT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
  quantity     INT NOT NULL CHECK (quantity > 0),
  is_selected  BOOLEAN NOT NULL DEFAULT TRUE,
  -- V2 Columns
  unit_price    NUMERIC(12,2) NOT NULL,
  tax_rate      NUMERIC(5,2) NOT NULL DEFAULT 0,
  currency_code VARCHAR(3) NOT NULL DEFAULT 'VND' CHECK (char_length(currency_code) = 3),
  UNIQUE (cart_id, product_id)
);

-- ======================
-- ORDERS (Merged V1 + V2)
-- ======================
CREATE TABLE orders (
  order_id               INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id                INT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
  cart_id                INT REFERENCES carts(cart_id) ON DELETE SET NULL,
  order_status           order_status NOT NULL DEFAULT 'PENDING',
  order_date             TIMESTAMPTZ NOT NULL DEFAULT now(),
  shipment_method        shipment_method NOT NULL DEFAULT 'DELIVERY',
  shipping_full_name     VARCHAR(150),
  shipping_phone         VARCHAR(20),
  shipping_address_line1 VARCHAR(255),
  shipping_address_line2 VARCHAR(255),
  shipping_city_state    VARCHAR(255),
  -- V2 Columns
  subtotal               NUMERIC(14,2) NOT NULL DEFAULT 0,
  tax_total              NUMERIC(14,2) NOT NULL DEFAULT 0,
  shipping_fee           NUMERIC(14,2) NOT NULL DEFAULT 0,
  grand_total            NUMERIC(14,2) NOT NULL DEFAULT 0
);

CREATE TABLE order_items (
  order_item_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id      INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
  product_id    INT REFERENCES products(product_id) ON DELETE SET NULL,
  product_name  VARCHAR(255) NOT NULL,
  unit_price    NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
  quantity      INT NOT NULL CHECK (quantity > 0),
  -- V2 Columns
  tax_rate      NUMERIC(5,2) NOT NULL DEFAULT 0,
  currency_code VARCHAR(3) NOT NULL DEFAULT 'VND' CHECK (char_length(currency_code) = 3)
);

-- ======================
-- PAYMENTS (1-1 with orders)
-- ======================
CREATE TABLE payments (
  payment_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id   INT NOT NULL UNIQUE REFERENCES orders(order_id) ON DELETE CASCADE,
  method     payment_method NOT NULL,
  status     payment_status NOT NULL DEFAULT 'PENDING',
  amount     NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
  paid_at    TIMESTAMPTZ
);

-- ======================
-- NOTIFICATIONS
-- ======================
CREATE TABLE notifications (
  notification_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id         INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
  message         TEXT NOT NULL,
  is_read         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ======================
-- BLOG
-- ======================
CREATE TABLE blog_posts (
  blog_post_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  author_user_id INT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
  title          VARCHAR(255) NOT NULL,
  slug           VARCHAR(255) NOT NULL UNIQUE,
  status         blog_status NOT NULL DEFAULT 'DRAFT',
  content        TEXT,
  image_url      TEXT,
  published_at   TIMESTAMPTZ
);

-- ======================
-- CHAT (New Model - V1 'chat_messages' table removed)
-- ======================
CREATE TABLE conversations (
  id              SERIAL PRIMARY KEY,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_message_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE conversation_participants (
  id              SERIAL PRIMARY KEY,
  conversation_id INT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  user_id         INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
  joined_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(conversation_id, user_id)
);

CREATE TABLE messages (
  id              SERIAL PRIMARY KEY,
  conversation_id INT NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  sender_id       INT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
  message_type    VARCHAR(20) NOT NULL DEFAULT 'TEXT', -- TEXT, IMAGE
  content         TEXT NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  read_at         TIMESTAMPTZ DEFAULT NULL
);


-- ======================
-- VIEW: (V2 Version)
-- ======================
CREATE VIEW v_cart_totals_selected AS
SELECT c.cart_id,
       COALESCE(SUM(ci.quantity * ci.unit_price), 0)::NUMERIC(12,2) AS total_price_selected
FROM carts c
LEFT JOIN cart_items ci
       ON ci.cart_id = c.cart_id AND ci.is_selected = TRUE
GROUP BY c.cart_id;

-- ======================
-- INDEXES (Merged V1 + V2 + Chat)
-- ======================
CREATE INDEX idx_products_category   ON products(category_id);
CREATE INDEX idx_products_publisher  ON products(publisher_id);
CREATE INDEX idx_products_artist     ON products(artist_id);
CREATE INDEX idx_cart_items_cart     ON cart_items(cart_id);
CREATE INDEX idx_orders_user_date    ON orders(user_id, order_date DESC);
CREATE INDEX idx_payments_status     ON payments(status);
CREATE INDEX idx_store_inventory_product ON store_inventory(product_id);
CREATE INDEX idx_store_inventory_store   ON store_inventory(store_location_id);
CREATE INDEX idx_blog_posts_status_pub   ON blog_posts(status, published_at DESC);
CREATE INDEX idx_artist_images_artist    ON artist_images(artist_id, sort_order);

-- V2 Index
CREATE UNIQUE INDEX uq_active_cart_per_user
  ON carts(user_id) WHERE (status = 'ACTIVE');

-- Chat Indexes
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_participants_user_id ON conversation_participants(user_id);
CREATE INDEX idx_conversations_last_message_at ON conversations(last_message_at);

-- ======================
-- TRIGGERS & FUNCTIONS (Merged V1 + V2)
-- ======================

-- 1. Sync store_inventory -> products.quantity (V1)
CREATE OR REPLACE FUNCTION sync_product_quantity(pid INT)
RETURNS VOID AS $$
BEGIN
  UPDATE products p
  SET quantity = COALESCE((
    SELECT SUM(si.quantity)::INT
    FROM store_inventory si
    WHERE si.product_id = pid
  ), 0)
  WHERE p.product_id = pid;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_store_inventory_sync()
RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    PERFORM sync_product_quantity(NEW.product_id);
  ELSIF (TG_OP = 'UPDATE') THEN
    IF (NEW.product_id IS DISTINCT FROM OLD.product_id) THEN
      PERFORM sync_product_quantity(OLD.product_id);
    END IF;
    PERFORM sync_product_quantity(NEW.product_id);
  ELSIF (TG_OP = 'DELETE') THEN
    PERFORM sync_product_quantity(OLD.product_id);
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS store_inventory_aiud ON store_inventory;
CREATE TRIGGER store_inventory_aiud
AFTER INSERT OR UPDATE OR DELETE ON store_inventory
FOR EACH ROW
EXECUTE FUNCTION trg_store_inventory_sync();

-- 2. Chặn sửa cart không ACTIVE (V2)
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

-- 3. Tự động tính totals cho CART (V2)
CREATE OR REPLACE FUNCTION recalc_cart_totals(p_cart_id INT) RETURNS VOID AS $$
DECLARE
    v_subtotal    NUMERIC;
    v_tax_total   NUMERIC;
    v_shipping_fee NUMERIC;
BEGIN
    -- Lấy shipping_fee hiện tại
    SELECT shipping_fee INTO v_shipping_fee
    FROM carts
    WHERE cart_id = p_cart_id;

    -- Tính subtotal và tax_total từ cart_items
    SELECT
        COALESCE(SUM(ci.unit_price * ci.quantity), 0),
        COALESCE(SUM((ci.unit_price * ci.quantity) * (ci.tax_rate/100.0)), 0)
    INTO
        v_subtotal,
        v_tax_total
    FROM cart_items ci
    WHERE ci.cart_id = p_cart_id AND ci.is_selected = TRUE;

    -- Cập nhật bảng carts
    UPDATE carts
    SET
        subtotal = v_subtotal,
        tax_total = v_tax_total,
        grand_total = v_subtotal + v_tax_total + v_shipping_fee
    WHERE cart_id = p_cart_id;
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