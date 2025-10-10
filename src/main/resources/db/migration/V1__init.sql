-- ENUMs
CREATE TYPE user_role AS ENUM ('ADMIN','STAFF','CUSTOMER');
CREATE TYPE cart_status AS ENUM ('ACTIVE','CHECKED_OUT','ABANDONED');
CREATE TYPE order_status AS ENUM ('PENDING','PAID','PROCESSING','SHIPPED','COMPLETED','CANCELLED','REFUNDED');
CREATE TYPE payment_method AS ENUM ('VNPAY','PAYPAL','COD');
CREATE TYPE payment_status AS ENUM ('PENDING','PAID','FAILED','REFUNDED');
CREATE TYPE shipment_method AS ENUM ('DELIVERY','PICKUP');

-- USERS
CREATE TABLE users (
  user_id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  username       VARCHAR(150) NOT NULL UNIQUE,
  email          VARCHAR(255) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  phone_number   VARCHAR(20),
  role           user_role NOT NULL DEFAULT 'CUSTOMER',
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ARTISTS / PUBLISHERS / CATEGORIES
CREATE TABLE artists (
  artist_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  artist_type  VARCHAR(50) NOT NULL,
  artist_name         VARCHAR(255) NOT NULL,
  debut_year   INT CHECK (debut_year >= 1900)
);

CREATE TABLE publishers (
  publisher_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  publisher_name         VARCHAR(255) NOT NULL UNIQUE,
  founded_year INT DEFAULT EXTRACT(YEAR FROM now())
);

CREATE TABLE categories (
  category_id  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name         VARCHAR(120) NOT NULL UNIQUE
);

-- PRODUCTS
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
  image_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id   INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
  url          TEXT NOT NULL,
  sort_order   INT NOT NULL DEFAULT 0
);

-- CARTS
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

-- ORDERS (không billing, city+state gộp, bỏ postal/country)
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

-- PAYMENTS (1-1 với order)
CREATE TABLE payments (
  payment_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_id     INT NOT NULL UNIQUE REFERENCES orders(order_id) ON DELETE CASCADE,
  method       payment_method NOT NULL,
  status       payment_status NOT NULL DEFAULT 'PENDING',
  amount       NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
  paid_at      TIMESTAMPTZ
);

-- OTHERS
CREATE TABLE store_locations (
  store_location_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  latitude          NUMERIC(9,6) NOT NULL,
  longitude         NUMERIC(9,6) NOT NULL,
  address           VARCHAR(255) NOT NULL
);

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

CREATE TABLE blog_posts (
  blog_post_id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  author_user_id  INT NOT NULL REFERENCES users(user_id) ON DELETE RESTRICT,
  title           VARCHAR(255) NOT NULL,
  slug            VARCHAR(255) NOT NULL UNIQUE,
  status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
  content         TEXT,
  image_url       TEXT,
  published_at    TIMESTAMPTZ
);

-- VIEW: tổng tiền giỏ chỉ tính item được chọn (giá hiện tại)
CREATE VIEW v_cart_totals_selected AS
SELECT c.cart_id,
       COALESCE(SUM(ci.quantity * p.price), 0)::NUMERIC(12,2) AS total_price_selected
FROM carts c
LEFT JOIN cart_items ci
  ON ci.cart_id = c.cart_id AND ci.is_selected = TRUE
LEFT JOIN products p
  ON p.product_id = ci.product_id
GROUP BY c.cart_id;

-- Index gợi ý
CREATE INDEX idx_products_category   ON products(category_id);
CREATE INDEX idx_products_publisher  ON products(publisher_id);
CREATE INDEX idx_products_artist     ON products(artist_id);
CREATE INDEX idx_cart_items_cart     ON cart_items(cart_id);
CREATE INDEX idx_orders_user_date    ON orders(user_id, order_date DESC);
CREATE INDEX idx_payments_status     ON payments(status);
