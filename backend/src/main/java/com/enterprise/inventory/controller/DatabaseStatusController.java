package com.enterprise.inventory.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class DatabaseStatusController {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseStatusController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/db/status")
    public Map<String, Object> databaseStatus() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("status", "CONNECTED");
        response.put("database", "PostgreSQL");

        Map<String, Integer> tableCounts = new LinkedHashMap<>();
        tableCounts.put("categories", count("categories"));
        tableCounts.put("suppliers", count("suppliers"));
        tableCounts.put("warehouses", count("warehouses"));
        tableCounts.put("products", count("products"));
        tableCounts.put("inventory", count("inventory"));
        tableCounts.put("customers", count("customers"));
        tableCounts.put("orders", count("customer_orders"));
        tableCounts.put("orderItems", count("order_items"));
        tableCounts.put("payments", count("payments"));
        tableCounts.put("shipments", count("shipments"));
        tableCounts.put("inventoryTransactions", count("inventory_transactions"));
        tableCounts.put("auditLogs", count("audit_logs"));

        response.put("tables", tableCounts);
        return response;
    }

    private Integer count(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }
}
