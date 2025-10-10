-- Unique cho username/email
ALTER TABLE users
  ADD CONSTRAINT uq_users_username UNIQUE (username),
  ADD CONSTRAINT uq_users_email UNIQUE (email);

-- Index tra cứu khoá ngoại
CREATE INDEX IF NOT EXISTS idx_products_categoryid ON products(categoryid);
CREATE INDEX IF NOT EXISTS idx_carts_userid ON carts(userid);
CREATE INDEX IF NOT EXISTS idx_cartitems_cartid ON cartitems(cartid);
CREATE INDEX IF NOT EXISTS idx_cartitems_productid ON cartitems(productid);
CREATE INDEX IF NOT EXISTS idx_orders_cartid ON orders(cartid);
CREATE INDEX IF NOT EXISTS idx_orders_userid ON orders(userid);
CREATE INDEX IF NOT EXISTS idx_payments_orderid ON payments(orderid);
CREATE INDEX IF NOT EXISTS idx_notifications_userid ON notifications(userid);
CREATE INDEX IF NOT EXISTS idx_chatmessages_userid ON chatmessages(userid);
