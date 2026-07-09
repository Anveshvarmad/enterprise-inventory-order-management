CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    contact_email VARCHAR(160),
    phone VARCHAR(40),
    address TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    location VARCHAR(255),
    capacity INT NOT NULL DEFAULT 0,
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    description TEXT,
    category_id BIGINT REFERENCES categories(id),
    supplier_id BIGINT REFERENCES suppliers(id),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    reorder_level INT NOT NULL DEFAULT 10 CHECK (reorder_level >= 0),
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id) ON DELETE CASCADE,
    quantity_on_hand INT NOT NULL DEFAULT 0 CHECK (quantity_on_hand >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    available_quantity INT GENERATED ALWAYS AS (quantity_on_hand - reserved_quantity) STORED,
    last_restocked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_inventory_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT chk_reserved_lte_on_hand CHECK (reserved_quantity <= quantity_on_hand)
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(40),
    address TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(80) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    order_status VARCHAR(40) NOT NULL DEFAULT 'CREATED',
    payment_status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    shipment_status VARCHAR(40) NOT NULL DEFAULT 'NOT_SHIPPED',
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    payment_reference VARCHAR(120) UNIQUE,
    payment_method VARCHAR(60) NOT NULL,
    payment_status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    warehouse_id BIGINT REFERENCES warehouses(id),
    tracking_number VARCHAR(120) UNIQUE,
    carrier VARCHAR(80),
    shipment_status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouses(id),
    transaction_type VARCHAR(60) NOT NULL,
    quantity INT NOT NULL,
    reference_type VARCHAR(80),
    reference_id BIGINT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(120) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(80) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    performed_by VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory(warehouse_id);
CREATE INDEX idx_orders_customer ON customer_orders(customer_id);
CREATE INDEX idx_orders_status ON customer_orders(order_status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_inventory_transactions_product ON inventory_transactions(product_id);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_categories_updated_at
BEFORE UPDATE ON categories
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_suppliers_updated_at
BEFORE UPDATE ON suppliers
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_warehouses_updated_at
BEFORE UPDATE ON warehouses
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_inventory_updated_at
BEFORE UPDATE ON inventory
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_customers_updated_at
BEFORE UPDATE ON customers
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_customer_orders_updated_at
BEFORE UPDATE ON customer_orders
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_payments_updated_at
BEFORE UPDATE ON payments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_shipments_updated_at
BEFORE UPDATE ON shipments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
