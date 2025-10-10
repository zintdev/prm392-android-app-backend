-- V1__baseline_all.sql
-- Baseline schema for PRM392 CD Store (PostgreSQL)
-- Includes: enums, tables, view, indexes, store_inventory + triggers.

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
CREATE INDEX idx_artist_images_artist ON artist_images(artist_id, sort_order);

CREATE TABLE publishers (
  publisher_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  publisher_name VARCHAR(255) NOT NULL UNIQUE,
  founded_year   INT
);

CREATE TABLE categories (
  category_id  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name         VARCHAR(120) NOT NULL UNIQUE
);

-- ======================
-- PRODUCTS & IMAGES
-- ======================
CREATE TABLE products (
  product_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_name  VARCHAR(255) NOT NULL,
  description   TEXT,
  price         NUMERIC(12,2) NOT NULL CHECK (price >= 0),
  quantity      INT NOT NULL CHECK (quantity >= 0),
  image_url     TEXT,
  artist_id     INT REFERENCES artists(artist_id) ON DELETE RESTRICT,
  publisher_id  INT REFERENCES publishers(publisher_id) ON DELETE RESTRICT,
  release_date  DATE,
  category_id   INT REFERENCES categories(category_id) ON DELETE RESTRICT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product_images (
  image_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id  INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
  url         TEXT NOT NULL,
  sort_order  INT NOT NULL DEFAULT 0,
  UNIQUE (product_id, sort_order)
);

-- ======================
-- STORES & INVENTORY (M-M)
-- ======================
CREATE TABLE store_locations (
  store_location_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  latitude          NUMERIC(9,6) NOT NULL,
  longitude         NUMERIC(9,6) NOT NULL,
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
-- CARTS
-- ======================
CREATE TABLE carts (
  cart_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id     INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
  status      cart_status NOT NULL DEFAULT 'ACTIVE',
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_items (
  cart_item_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  cart_id      INT NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
  product_id   INT NOT NULL REFERENCES products(product_id) ON DELETE RESTRICT,
  quantity     INT NOT NULL CHECK (quantity > 0),
  is_selected  BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (cart_id, product_id)
);

-- ======================
-- ORDERS
-- ======================
CREATE TABLE orders (
  order_id                INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id                 INT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
  cart_id                 INT REFERENCES carts(cart_id) ON DELETE SET NULL,
  order_status            order_status NOT NULL DEFAULT 'PENDING',
  order_date              TIMESTAMPTZ NOT NULL DEFAULT now(),
  shipment_method         shipment_method NOT NULL DEFAULT 'DELIVERY',
  shipping_full_name      VARCHAR(150),
  shipping_phone          VARCHAR(20),
  shipping_address_line1  VARCHAR(255),
  shipping_address_line2  VARCHAR(255),
  shipping_city_state     VARCHAR(255)
);

CREATE TABLE order_items (
  order_item_id  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id       INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
  product_id     INT REFERENCES products(product_id) ON DELETE SET NULL,
  product_name   VARCHAR(255) NOT NULL,
  unit_price     NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
  quantity       INT NOT NULL CHECK (quantity > 0)
);

-- ======================
-- PAYMENTS (1-1 with orders)
-- ======================
CREATE TABLE payments (
  payment_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id     INT NOT NULL UNIQUE REFERENCES orders(order_id) ON DELETE CASCADE,
  method       payment_method NOT NULL,
  status       payment_status NOT NULL DEFAULT 'PENDING',
  amount       NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
  paid_at      TIMESTAMPTZ
);

-- ======================
-- NOTIFICATIONS & CHAT
-- ======================
CREATE TABLE notifications (
  notification_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id         INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
  message         TEXT NOT NULL,
  is_read         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE chat_messages (
  chat_message_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id         INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
  message         TEXT NOT NULL,
  sent_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ======================
-- BLOG
-- ======================
CREATE TABLE blog_posts (
  blog_post_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  author_user_id  INT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
  title           VARCHAR(255) NOT NULL,
  slug            VARCHAR(255) NOT NULL UNIQUE,
  status          blog_status NOT NULL DEFAULT 'DRAFT',
  content         TEXT,
  image_url       TEXT,
  published_at    TIMESTAMPTZ
);

-- ======================
-- VIEW: cart total for selected items with current prices
-- ======================
CREATE VIEW v_cart_totals_selected AS
SELECT c.cart_id,
       COALESCE(SUM(ci.quantity * p.price), 0)::NUMERIC(12,2) AS total_price_selected
FROM carts c
LEFT JOIN cart_items ci
       ON ci.cart_id = c.cart_id AND ci.is_selected = TRUE
LEFT JOIN products p
       ON p.product_id = ci.product_id
GROUP BY c.cart_id;

-- ======================
-- INDEXES
-- ======================
CREATE INDEX idx_products_category       ON products(category_id);
CREATE INDEX idx_products_publisher      ON products(publisher_id);
CREATE INDEX idx_products_artist         ON products(artist_id);
CREATE INDEX idx_cart_items_cart         ON cart_items(cart_id);
CREATE INDEX idx_orders_user_date        ON orders(user_id, order_date DESC);
CREATE INDEX idx_payments_status         ON payments(status);
CREATE INDEX idx_store_inventory_product ON store_inventory(product_id);
CREATE INDEX idx_store_inventory_store   ON store_inventory(store_location_id);
CREATE INDEX idx_blog_posts_status_pub   ON blog_posts(status, published_at DESC);

-- ======================
-- TRIGGERS: keep products.quantity = sum of per-store inventory
-- ======================
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
  RETURN NULL; -- AFTER trigger: return value ignored
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS store_inventory_aiud ON store_inventory;
CREATE TRIGGER store_inventory_aiud
AFTER INSERT OR UPDATE OR DELETE ON store_inventory
FOR EACH ROW
EXECUTE FUNCTION trg_store_inventory_sync();
