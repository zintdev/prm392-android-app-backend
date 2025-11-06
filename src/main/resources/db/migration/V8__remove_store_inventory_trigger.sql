-- Remove automatic product quantity sync from store inventory updates
DROP TRIGGER IF EXISTS store_inventory_aiud ON store_inventory;
DROP FUNCTION IF EXISTS trg_store_inventory_sync();
DROP FUNCTION IF EXISTS sync_product_quantity(INT);
