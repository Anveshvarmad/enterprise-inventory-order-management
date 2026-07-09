package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.DashboardSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardSummaryResponse getDashboardSummary() {
        return new DashboardSummaryResponse(
                queryLong("SELECT COUNT(*) FROM products"),
                queryLong("SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'"),
                queryLong("""
                        SELECT COUNT(*)
                        FROM inventory i
                        JOIN products p ON p.id = i.product_id
                        WHERE i.available_quantity <= p.reorder_level
                        """),
                queryLong("SELECT COUNT(*) FROM customer_orders"),
                queryLong("SELECT COUNT(*) FROM customer_orders WHERE order_status IN ('CREATED', 'CONFIRMED')"),
                queryLong("SELECT COUNT(*) FROM customer_orders WHERE order_status = 'SHIPPED'"),
                queryLong("SELECT COUNT(*) FROM customers"),
                queryLong("SELECT COUNT(*) FROM warehouses"),
                queryLong("SELECT COALESCE(SUM(quantity_on_hand), 0) FROM inventory"),
                queryBigDecimal("SELECT COALESCE(SUM(total_amount), 0) FROM customer_orders")
        );
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
