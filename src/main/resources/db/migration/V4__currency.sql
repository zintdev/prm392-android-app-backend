-- Cart items
ALTER TABLE cart_items
  ALTER COLUMN currency_code TYPE varchar(3) USING trim(currency_code),
  ALTER COLUMN currency_code SET NOT NULL;

-- Order items (nếu cột này cũng đang CHAR)
ALTER TABLE order_items
  ALTER COLUMN currency_code TYPE varchar(3) USING trim(currency_code),
  ALTER COLUMN currency_code SET NOT NULL;

-- (Tuỳ chọn) Ràng buộc đúng 3 ký tự ISO-4217
DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM pg_constraint WHERE conname = 'ck_cart_items_currency_code_len'
  ) THEN
    ALTER TABLE cart_items
      ADD CONSTRAINT ck_cart_items_currency_code_len CHECK (char_length(currency_code) = 3);
  END IF;
  IF NOT EXISTS (
      SELECT 1 FROM pg_constraint WHERE conname = 'ck_order_items_currency_code_len'
  ) THEN
    ALTER TABLE order_items
      ADD CONSTRAINT ck_order_items_currency_code_len CHECK (char_length(currency_code) = 3);
  END IF;
END$$;
