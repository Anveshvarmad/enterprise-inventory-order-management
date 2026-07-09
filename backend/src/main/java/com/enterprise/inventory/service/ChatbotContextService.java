package com.enterprise.inventory.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotContextService {

    private final JdbcTemplate jdbcTemplate;

    public ChatbotContextService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String buildOperationalContext() {
        StringBuilder context = new StringBuilder();

        context.append("ENTERPRISE INVENTORY AND ORDER MANAGEMENT SYSTEM CONTEXT\n\n");

        appendSummary(context);
        appendLowStock(context);
        appendTopProducts(context);
        appendRecentOrders(context);
        appendWarehouseInventory(context);

        return context.toString();
    }

    private void appendSummary(StringBuilder context) {
        Long totalProducts = queryLong("SELECT COUNT(*) FROM products");
        Long activeProducts = queryLong("SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'");
        Long totalOrders = queryLong("SELECT COUNT(*) FROM customer_orders");
        Long totalCustomers = queryLong("SELECT COUNT(*) FROM customers");
        Long totalWarehouses = queryLong("SELECT COUNT(*) FROM warehouses");
        Long lowStockItems = queryLong("""
                SELECT COUNT(*)
                FROM inventory i
                JOIN products p ON p.id = i.product_id
                WHERE i.available_quantity <= p.reorder_level
                """);
        BigDecimal totalOrderValue = queryBigDecimal("SELECT COALESCE(SUM(total_amount), 0) FROM customer_orders");

        context.append("SUMMARY:\n");
        context.append("- Total products: ").append(totalProducts).append("\n");
        context.append("- Active products: ").append(activeProducts).append("\n");
        context.append("- Total orders: ").append(totalOrders).append("\n");
        context.append("- Total customers: ").append(totalCustomers).append("\n");
        context.append("- Total warehouses: ").append(totalWarehouses).append("\n");
        context.append("- Low-stock inventory records: ").append(lowStockItems).append("\n");
        context.append("- Total order value: $").append(totalOrderValue).append("\n\n");
    }

    private void appendLowStock(StringBuilder context) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                    p.sku,
                    p.name AS product_name,
                    w.code AS warehouse_code,
                    i.available_quantity,
                    p.reorder_level
                FROM inventory i
                JOIN products p ON p.id = i.product_id
                JOIN warehouses w ON w.id = i.warehouse_id
                WHERE i.available_quantity <= p.reorder_level
                ORDER BY i.available_quantity ASC
                LIMIT 10
                """);

        context.append("LOW STOCK ITEMS TOP 10:\n");

        if (rows.isEmpty()) {
            context.append("- No low-stock items found.\n\n");
            return;
        }

        for (Map<String, Object> row : rows) {
            context.append("- ")
                    .append(row.get("sku"))
                    .append(" | ")
                    .append(row.get("product_name"))
                    .append(" | Warehouse ")
                    .append(row.get("warehouse_code"))
                    .append(" | Available: ")
                    .append(row.get("available_quantity"))
                    .append(" | Reorder level: ")
                    .append(row.get("reorder_level"))
                    .append("\n");
        }

        context.append("\n");
    }

    private void appendTopProducts(StringBuilder context) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                    p.sku,
                    p.name AS product_name,
                    COALESCE(SUM(oi.quantity), 0) AS units_sold,
                    COALESCE(SUM(oi.line_total), 0) AS revenue
                FROM order_items oi
                JOIN products p ON p.id = oi.product_id
                JOIN customer_orders o ON o.id = oi.order_id
                WHERE o.order_status <> 'CANCELLED'
                GROUP BY p.sku, p.name
                ORDER BY units_sold DESC
                LIMIT 10
                """);

        context.append("TOP PRODUCTS BY UNITS SOLD:\n");

        if (rows.isEmpty()) {
            context.append("- No product sales data found.\n\n");
            return;
        }

        for (Map<String, Object> row : rows) {
            context.append("- ")
                    .append(row.get("sku"))
                    .append(" | ")
                    .append(row.get("product_name"))
                    .append(" | Units sold: ")
                    .append(row.get("units_sold"))
                    .append(" | Revenue: $")
                    .append(row.get("revenue"))
                    .append("\n");
        }

        context.append("\n");
    }

    private void appendRecentOrders(StringBuilder context) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                    order_number,
                    order_status,
                    payment_status,
                    shipment_status,
                    total_amount,
                    created_at
                FROM customer_orders
                ORDER BY created_at DESC
                LIMIT 10
                """);

        context.append("RECENT ORDERS TOP 10:\n");

        if (rows.isEmpty()) {
            context.append("- No recent orders found.\n\n");
            return;
        }

        for (Map<String, Object> row : rows) {
            context.append("- ")
                    .append(row.get("order_number"))
                    .append(" | Order: ")
                    .append(row.get("order_status"))
                    .append(" | Payment: ")
                    .append(row.get("payment_status"))
                    .append(" | Shipment: ")
                    .append(row.get("shipment_status"))
                    .append(" | Total: $")
                    .append(row.get("total_amount"))
                    .append("\n");
        }

        context.append("\n");
    }

    private void appendWarehouseInventory(StringBuilder context) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                    w.code,
                    w.name,
                    COALESCE(SUM(i.quantity_on_hand), 0) AS quantity_on_hand,
                    COALESCE(SUM(i.reserved_quantity), 0) AS reserved_quantity,
                    COALESCE(SUM(i.available_quantity), 0) AS available_quantity
                FROM warehouses w
                LEFT JOIN inventory i ON i.warehouse_id = w.id
                GROUP BY w.code, w.name
                ORDER BY quantity_on_hand DESC
                """);

        context.append("WAREHOUSE INVENTORY SUMMARY:\n");

        for (Map<String, Object> row : rows) {
            context.append("- ")
                    .append(row.get("code"))
                    .append(" | ")
                    .append(row.get("name"))
                    .append(" | On hand: ")
                    .append(row.get("quantity_on_hand"))
                    .append(" | Reserved: ")
                    .append(row.get("reserved_quantity"))
                    .append(" | Available: ")
                    .append(row.get("available_quantity"))
                    .append("\n");
        }

        context.append("\n");
    }

    private Long queryLong(String sql) {
        Number value = jdbcTemplate.queryForObject(sql, Number.class);
        return value == null ? 0L : value.longValue();
    }

    private BigDecimal queryBigDecimal(String sql) {
        BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return value == null ? BigDecimal.ZERO : value;
    }
}
