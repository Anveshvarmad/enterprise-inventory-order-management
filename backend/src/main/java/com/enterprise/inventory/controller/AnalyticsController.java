package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.AnalyticsDashboardResponse;
import com.enterprise.inventory.dto.LowStockRiskResponse;
import com.enterprise.inventory.dto.OrderStatusBreakdownResponse;
import com.enterprise.inventory.dto.RevenueTrendResponse;
import com.enterprise.inventory.dto.TopProductResponse;
import com.enterprise.inventory.dto.WarehouseInventoryAnalyticsResponse;
import com.enterprise.inventory.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/api/analytics/dashboard")
    public AnalyticsDashboardResponse getAnalyticsDashboard() {
        return analyticsService.getAnalyticsDashboard();
    }

    @GetMapping("/api/analytics/revenue-trend")
    public List<RevenueTrendResponse> getRevenueTrend() {
        return analyticsService.getRevenueTrend();
    }

    @GetMapping("/api/analytics/order-status")
    public List<OrderStatusBreakdownResponse> getOrderStatusBreakdown() {
        return analyticsService.getOrderStatusBreakdown();
    }

    @GetMapping("/api/analytics/top-products")
    public List<TopProductResponse> getTopProducts() {
        return analyticsService.getTopProducts();
    }

    @GetMapping("/api/analytics/warehouse-inventory")
    public List<WarehouseInventoryAnalyticsResponse> getWarehouseInventory() {
        return analyticsService.getWarehouseInventory();
    }

    @GetMapping("/api/analytics/low-stock-risk")
    public List<LowStockRiskResponse> getLowStockRisk() {
        return analyticsService.getLowStockRisk();
    }
}
