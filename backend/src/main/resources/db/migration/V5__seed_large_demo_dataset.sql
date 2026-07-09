INSERT INTO products (
    sku,
    name,
    description,
    category_id,
    supplier_id,
    unit_price,
    reorder_level,
    status
)
SELECT
    'BULK-SKU-' || LPAD(n::TEXT, 4, '0'),
    CASE
        WHEN n % 5 = 0 THEN 'Enterprise Laptop Model ' || n
        WHEN n % 5 = 1 THEN 'Warehouse Scanner Model ' || n
        WHEN n % 5 = 2 THEN 'Office Supply Kit ' || n
        WHEN n % 5 = 3 THEN 'Industrial Tool Set ' || n
        ELSE 'Packaging Material Pack ' || n
    END,
    'Large demo product generated for industry-scale testing',
    (
        SELECT id
        FROM categories
        ORDER BY id
        OFFSET ((n - 1) % (SELECT COUNT(*) FROM categories))
        LIMIT 1
    ),
    (
        SELECT id
        FROM suppliers
        ORDER BY id
        OFFSET ((n - 1) % (SELECT COUNT(*) FROM suppliers))
        LIMIT 1
    ),
    ROUND((25 + (n * 7.35))::NUMERIC, 2),
    10 + (n % 40),
    CASE
        WHEN n % 25 = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END
FROM generate_series(1, 150) AS n
ON CONFLICT (sku) DO NOTHING;

INSERT INTO inventory (
    product_id,
    warehouse_id,
    quantity_on_hand,
    reserved_quantity,
    last_restocked_at
)
SELECT
    p.id,
    w.id,
    50 + ((p.id * w.id) % 500),
    ((p.id + w.id) % 20),
    CURRENT_TIMESTAMP - (((p.id + w.id) % 30) || ' days')::INTERVAL
FROM products p
CROSS JOIN warehouses w
WHERE p.sku LIKE 'BULK-SKU-%'
ON CONFLICT (product_id, warehouse_id) DO NOTHING;

INSERT INTO customers (
    first_name,
    last_name,
    email,
    phone,
    address,
    status
)
SELECT
    CASE n % 10
        WHEN 0 THEN 'Alex'
        WHEN 1 THEN 'Jordan'
        WHEN 2 THEN 'Taylor'
        WHEN 3 THEN 'Morgan'
        WHEN 4 THEN 'Casey'
        WHEN 5 THEN 'Riley'
        WHEN 6 THEN 'Avery'
        WHEN 7 THEN 'Cameron'
        WHEN 8 THEN 'Drew'
        ELSE 'Jamie'
    END,
    'Customer' || LPAD(n::TEXT, 4, '0'),
    'demo.customer.' || LPAD(n::TEXT, 4, '0') || '@example.com',
    '+1-555-' || LPAD((7000 + n)::TEXT, 4, '0'),
    CASE n % 5
        WHEN 0 THEN 'New York, NY'
        WHEN 1 THEN 'Jersey City, NJ'
        WHEN 2 THEN 'Atlanta, GA'
        WHEN 3 THEN 'Dallas, TX'
        ELSE 'San Jose, CA'
    END,
    'ACTIVE'
FROM generate_series(1, 300) AS n
ON CONFLICT (email) DO NOTHING;

INSERT INTO customer_orders (
    order_number,
    customer_id,
    order_status,
    payment_status,
    shipment_status,
    total_amount,
    notes,
    created_at
)
SELECT
    'BULK-ORD-' || LPAD(n::TEXT, 6, '0'),
    (
        SELECT id
        FROM customers
        WHERE email LIKE 'demo.customer.%@example.com'
        ORDER BY id
        OFFSET ((n - 1) % 300)
        LIMIT 1
    ),
    CASE
        WHEN n % 20 = 0 THEN 'CANCELLED'
        WHEN n % 4 = 0 THEN 'SHIPPED'
        ELSE 'CONFIRMED'
    END,
    CASE
        WHEN n % 20 = 0 THEN 'CANCELLED'
        WHEN n % 4 = 0 THEN 'PAID'
        WHEN n % 3 = 0 THEN 'PAID'
        ELSE 'PENDING'
    END,
    CASE
        WHEN n % 20 = 0 THEN 'CANCELLED'
        WHEN n % 4 = 0 THEN 'SHIPPED'
        ELSE 'PROCESSING'
    END,
    0,
    'Large demo order generated for testing dashboards and workflows',
    CURRENT_TIMESTAMP - ((n % 90) || ' days')::INTERVAL
FROM generate_series(1, 800) AS n
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO order_items (
    order_id,
    product_id,
    warehouse_id,
    quantity,
    unit_price,
    line_total
)
SELECT
    o.id,
    p.id,
    w.id,
    item_quantity.quantity,
    p.unit_price,
    ROUND((p.unit_price * item_quantity.quantity)::NUMERIC, 2)
FROM customer_orders o
JOIN LATERAL (
    SELECT id, unit_price
    FROM products
    WHERE sku LIKE 'BULK-SKU-%'
    ORDER BY id
    OFFSET ((o.id * 2) % 150)
    LIMIT 1
) p ON TRUE
JOIN LATERAL (
    SELECT id
    FROM warehouses
    ORDER BY id
    OFFSET (o.id % (SELECT COUNT(*) FROM warehouses))
    LIMIT 1
) w ON TRUE
JOIN LATERAL (
    SELECT 1 + (o.id % 5) AS quantity
) item_quantity ON TRUE
WHERE o.order_number LIKE 'BULK-ORD-%'
  AND NOT EXISTS (
      SELECT 1
      FROM order_items existing
      WHERE existing.order_id = o.id
  );

INSERT INTO order_items (
    order_id,
    product_id,
    warehouse_id,
    quantity,
    unit_price,
    line_total
)
SELECT
    o.id,
    p.id,
    w.id,
    item_quantity.quantity,
    p.unit_price,
    ROUND((p.unit_price * item_quantity.quantity)::NUMERIC, 2)
FROM customer_orders o
JOIN LATERAL (
    SELECT id, unit_price
    FROM products
    WHERE sku LIKE 'BULK-SKU-%'
    ORDER BY id
    OFFSET ((o.id * 3 + 17) % 150)
    LIMIT 1
) p ON TRUE
JOIN LATERAL (
    SELECT id
    FROM warehouses
    ORDER BY id
    OFFSET ((o.id + 1) % (SELECT COUNT(*) FROM warehouses))
    LIMIT 1
) w ON TRUE
JOIN LATERAL (
    SELECT 1 + ((o.id + 2) % 4) AS quantity
) item_quantity ON TRUE
WHERE o.order_number LIKE 'BULK-ORD-%'
  AND (
      SELECT COUNT(*)
      FROM order_items existing
      WHERE existing.order_id = o.id
  ) = 1;

UPDATE customer_orders o
SET total_amount = totals.total
FROM (
    SELECT
        order_id,
        ROUND(SUM(line_total)::NUMERIC, 2) AS total
    FROM order_items
    GROUP BY order_id
) totals
WHERE o.id = totals.order_id;

INSERT INTO payments (
    order_id,
    payment_reference,
    payment_method,
    payment_status,
    amount,
    paid_at
)
SELECT
    o.id,
    'BULK-PAY-' || o.order_number,
    CASE
        WHEN o.id % 3 = 0 THEN 'ACH'
        WHEN o.id % 3 = 1 THEN 'CARD'
        ELSE 'WIRE'
    END,
    o.payment_status,
    o.total_amount,
    CASE
        WHEN o.payment_status = 'PAID' THEN o.created_at + INTERVAL '1 day'
        ELSE NULL
    END
FROM customer_orders o
WHERE o.order_number LIKE 'BULK-ORD-%'
  AND o.payment_status IN ('PAID', 'PENDING', 'CANCELLED')
ON CONFLICT (payment_reference) DO NOTHING;

INSERT INTO shipments (
    order_id,
    warehouse_id,
    tracking_number,
    carrier,
    shipment_status,
    shipped_at,
    delivered_at
)
SELECT
    o.id,
    (
        SELECT warehouse_id
        FROM order_items
        WHERE order_id = o.id
        ORDER BY id
        LIMIT 1
    ),
    'BULK-TRK-' || o.order_number,
    CASE
        WHEN o.id % 3 = 0 THEN 'UPS'
        WHEN o.id % 3 = 1 THEN 'FedEx'
        ELSE 'DHL'
    END,
    o.shipment_status,
    CASE
        WHEN o.shipment_status = 'SHIPPED' THEN o.created_at + INTERVAL '2 days'
        ELSE NULL
    END,
    CASE
        WHEN o.order_status = 'DELIVERED' THEN o.created_at + INTERVAL '5 days'
        ELSE NULL
    END
FROM customer_orders o
WHERE o.order_number LIKE 'BULK-ORD-%'
  AND o.shipment_status IN ('PROCESSING', 'SHIPPED', 'CANCELLED')
ON CONFLICT (tracking_number) DO NOTHING;

INSERT INTO inventory_transactions (
    product_id,
    warehouse_id,
    transaction_type,
    quantity,
    reference_type,
    reference_id,
    notes,
    created_at
)
SELECT
    oi.product_id,
    oi.warehouse_id,
    CASE
        WHEN o.order_status = 'CANCELLED' THEN 'RELEASE_RESERVATION'
        WHEN o.order_status = 'SHIPPED' THEN 'SHIP'
        ELSE 'RESERVE'
    END,
    CASE
        WHEN o.order_status = 'SHIPPED' THEN -oi.quantity
        ELSE oi.quantity
    END,
    'ORDER',
    o.id,
    'Generated transaction for large demo order',
    o.created_at
FROM order_items oi
JOIN customer_orders o ON o.id = oi.order_id
WHERE o.order_number LIKE 'BULK-ORD-%';

INSERT INTO audit_logs (
    entity_name,
    entity_id,
    action,
    performed_by,
    new_value,
    created_at
)
SELECT
    'customer_orders',
    id,
    'BULK_ORDER_CREATED',
    'demo-data-generator',
    order_number || ' generated',
    created_at
FROM customer_orders
WHERE order_number LIKE 'BULK-ORD-%';
