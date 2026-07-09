package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.AnalyticsDashboardResponse;
import com.enterprise.inventory.dto.LowStockRiskResponse;
import com.enterprise.inventory.dto.OrderStatusBreakdownResponse;
import com.enterprise.inventory.dto.RevenueTrendResponse;
import com.enterprise.inventory.dto.TopProductResponse;
import com.enterprise.inventory.dto.WarehouseInventoryAnalyticsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AnalyticsDashboardResponse getAnalyticsDashboard() {
        return new AnalyticsDashboardResponse(
                getRevenueTrend(),
                getOrderStatusBreakdown(),
                getTopProducts(),
                getWarehouseInventory(),
                getLowStockRisk()
        );
    }

    public List<RevenueTrendResponse> getRevenueTrend() {
        String sql = """
                SELECT
                    TO_CHAR(DATE_TRUNC('month', created_at), 'YYYY-MM') AS period,
                    COUNT(*) AS total_orders,
                    COALESCE(SUM(total_amount), 0) AS total_revenue
                FROM customer_orders
                WHERE order_status <> 'CANCELLED'
                GROUP BY DATE_TRUNC('month', created_at)
                ORDER BY DATE_TRUNC('month', created_at)
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new RevenueTrendResponse(
                        rs.getString("period"),
                        rs.getLong("total_orders"),
                        rs.getBigDecimal("total_revenue")
                )
        );
    }

    public List<OrderStatusBreakdownResponse> getOrderStatusBreakdown() {
        String sql = """
                SELECT
                    order_status AS status,
                    COUNT(*) AS status_count
                FROM customer_orders
                GROUP BY order_status
                ORDER BY status_count DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new OrderStatusBreakdownResponse(
                        rs.getString("status"),
                        rs.getLong("status_count")
                )
        );
    }

    public List<TopProductResponse> getTopProducts() {
        String sql = """
                SELECT
                    p.id AS product_id,
                    p.sku AS sku,
                    p.name AS product_name,
                    COALESCE(SUM(oi.quantity), 0) AS total_quantity_sold,
                    COALESCE(SUM(oi.line_total), 0) AS total_revenue
                FROM order_items oi
                JOIN products p ON p.id = oi.product_id
                JOIN customer_orders o ON o.id = oi.order_id
                WHERE o.order_status <> 'CANCELLED'
                GROUP BY p.id, p.sku, p.name
                ORDER BY total_quantity_sold DESC, total_revenue DESC
                LIMIT 10
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new TopProductResponse(
                        rs.getLong("product_id"),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getLong("total_quantity_sold"),
                        rs.getBigDecimal("total_revenue")
                )
        );
    }

    public List<WarehouseInventoryAnalyticsResponse> getWarehouseInventory() {
        String sql = """
                SELECT
                    w.id AS warehouse_id,
                    w.name AS warehouse_name,
                    w.code AS warehouse_code,
                    COALESCE(SUM(i.quantity_on_hand), 0) AS quantity_on_hand,
                    COALESCE(SUM(i.reserved_quantity), 0) AS reserved_quantity,
                    COALESCE(SUM(i.available_quantity), 0) AS available_quantity
                FROM warehouses w
                LEFT JOIN inventory i ON i.warehouse_id = w.id
                GROUP BY w.id, w.name, w.code
                ORDER BY quantity_on_hand DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new WarehouseInventoryAnalyticsResponse(
                        rs.getLong("warehouse_id"),
                        rs.getString("warehouse_name"),
                        rs.getString("warehouse_code"),
                        rs.getLong("quantity_on_hand"),
                        rs.getLong("reserved_quantity"),
                        rs.getLong("available_quantity")
                )
        );
    }

    public List<LowStockRiskResponse> getLowStockRisk() {
        String sql = """
                SELECT
                    p.id AS product_id,
                    p.sku AS sku,
                    p.name AS product_name,
                    w.id AS warehouse_id,
                    w.name AS warehouse_name,
                    w.code AS warehouse_code,
                    i.available_quantity AS available_quantity,
                    p.reorder_level AS reorder_level
                FROM inventory i
                JOIN products p ON p.id = i.product_id
                JOIN warehouses w ON w.id = i.warehouse_id
                WHERE i.available_quantity <= p.reorder_level
                ORDER BY i.available_quantity ASC, p.reorder_level DESC
                LIMIT 20
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new LowStockRiskResponse(
                        rs.getLong("product_id"),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getLong("warehouse_id"),
                        rs.getString("warehouse_name"),
                        rs.getString("warehouse_code"),
                        rs.getInt("available_quantity"),
                        rs.getInt("reorder_level")
                )
        );
    }
}
