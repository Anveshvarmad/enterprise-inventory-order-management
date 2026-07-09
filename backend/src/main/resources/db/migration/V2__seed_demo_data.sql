INSERT INTO categories (name, description) VALUES
('Electronics', 'Consumer and enterprise electronic products'),
('Office Supplies', 'Office consumables and accessories'),
('Industrial Equipment', 'Warehouse and industrial tools'),
('Furniture', 'Office and commercial furniture'),
('Packaging', 'Shipping and packaging materials');

INSERT INTO suppliers (name, contact_email, phone, address, status) VALUES
('Global Tech Supply', 'sales@globaltech.example.com', '+1-555-100-1111', 'New York, NY', 'ACTIVE'),
('OfficePro Distributors', 'support@officepro.example.com', '+1-555-200-2222', 'Edison, NJ', 'ACTIVE'),
('WarehouseWorks Inc.', 'orders@warehouseworks.example.com', '+1-555-300-3333', 'Atlanta, GA', 'ACTIVE'),
('FurniMax Commercial', 'hello@furnimax.example.com', '+1-555-400-4444', 'Dallas, TX', 'ACTIVE');

INSERT INTO warehouses (name, code, location, capacity, status) VALUES
('East Coast Fulfillment Center', 'EAST-001', 'Jersey City, NJ', 100000, 'ACTIVE'),
('South Distribution Hub', 'SOUTH-001', 'Atlanta, GA', 85000, 'ACTIVE'),
('West Coast Fulfillment Center', 'WEST-001', 'San Jose, CA', 95000, 'ACTIVE');

INSERT INTO products (sku, name, description, category_id, supplier_id, unit_price, reorder_level, status) VALUES
('ELEC-LAP-001', 'Business Laptop 14 inch', 'Enterprise laptop for office users', 1, 1, 899.99, 25, 'ACTIVE'),
('ELEC-MON-002', '27 inch LED Monitor', 'High-resolution office monitor', 1, 1, 229.99, 40, 'ACTIVE'),
('OFF-PEN-003', 'Premium Pen Pack', 'Pack of 50 office pens', 2, 2, 14.99, 100, 'ACTIVE'),
('OFF-PPR-004', 'A4 Printer Paper Box', 'Box of 10 paper reams', 2, 2, 42.50, 80, 'ACTIVE'),
('IND-FRK-005', 'Manual Forklift Jack', 'Warehouse pallet jack', 3, 3, 499.00, 10, 'ACTIVE'),
('FUR-DSK-006', 'Standing Desk', 'Adjustable commercial standing desk', 4, 4, 349.00, 20, 'ACTIVE'),
('PKG-BOX-007', 'Shipping Box Medium', 'Medium corrugated shipping box', 5, 3, 2.50, 300, 'ACTIVE');

INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, reserved_quantity, last_restocked_at) VALUES
(1, 1, 120, 15, CURRENT_TIMESTAMP),
(1, 2, 80, 8, CURRENT_TIMESTAMP),
(2, 1, 200, 25, CURRENT_TIMESTAMP),
(2, 3, 160, 12, CURRENT_TIMESTAMP),
(3, 1, 1000, 150, CURRENT_TIMESTAMP),
(4, 2, 650, 50, CURRENT_TIMESTAMP),
(5, 2, 35, 5, CURRENT_TIMESTAMP),
(6, 3, 75, 10, CURRENT_TIMESTAMP),
(7, 1, 5000, 600, CURRENT_TIMESTAMP),
(7, 2, 4200, 350, CURRENT_TIMESTAMP);

INSERT INTO customers (first_name, last_name, email, phone, address, status) VALUES
('John', 'Smith', 'john.smith@example.com', '+1-555-777-1001', 'New York, NY', 'ACTIVE'),
('Emma', 'Johnson', 'emma.johnson@example.com', '+1-555-777-1002', 'Atlanta, GA', 'ACTIVE'),
('Michael', 'Brown', 'michael.brown@example.com', '+1-555-777-1003', 'San Jose, CA', 'ACTIVE');

INSERT INTO customer_orders (order_number, customer_id, order_status, payment_status, shipment_status, total_amount, notes) VALUES
('ORD-100001', 1, 'CONFIRMED', 'PAID', 'PROCESSING', 929.98, 'Laptop and pen order'),
('ORD-100002', 2, 'CREATED', 'PENDING', 'NOT_SHIPPED', 579.49, 'Office restock order');

INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES
(1, 1, 1, 899.99, 899.99),
(1, 3, 2, 14.99, 29.98),
(2, 5, 1, 499.00, 499.00),
(2, 4, 1, 42.50, 42.50),
(2, 7, 15, 2.50, 37.50);

INSERT INTO payments (order_id, payment_reference, payment_method, payment_status, amount, paid_at) VALUES
(1, 'PAY-100001', 'CARD', 'PAID', 929.98, CURRENT_TIMESTAMP);

INSERT INTO shipments (order_id, warehouse_id, tracking_number, carrier, shipment_status) VALUES
(1, 1, 'TRK-100001', 'UPS', 'PROCESSING');

INSERT INTO inventory_transactions (product_id, warehouse_id, transaction_type, quantity, reference_type, reference_id, notes) VALUES
(1, 1, 'RESERVE', 1, 'ORDER', 1, 'Reserved for order ORD-100001'),
(3, 1, 'RESERVE', 2, 'ORDER', 1, 'Reserved for order ORD-100001'),
(5, 2, 'RESERVE', 1, 'ORDER', 2, 'Reserved for order ORD-100002');

INSERT INTO audit_logs (entity_name, entity_id, action, performed_by, new_value) VALUES
('customer_orders', 1, 'ORDER_CREATED', 'system', 'ORD-100001 created'),
('customer_orders', 2, 'ORDER_CREATED', 'system', 'ORD-100002 created'),
('inventory', 1, 'STOCK_RESERVED', 'system', 'Reserved stock for demo order');
