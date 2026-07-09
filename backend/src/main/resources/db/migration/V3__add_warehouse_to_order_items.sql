ALTER TABLE order_items
ADD COLUMN warehouse_id BIGINT REFERENCES warehouses(id);

UPDATE order_items SET warehouse_id = 1 WHERE id IN (1, 2);
UPDATE order_items SET warehouse_id = 2 WHERE id IN (3, 4, 5);

ALTER TABLE order_items
ALTER COLUMN warehouse_id SET NOT NULL;

CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_order_items_warehouse ON order_items(warehouse_id);
