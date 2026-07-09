package com.enterprise.inventory.dto;

import java.util.List;

public record AnalyticsDashboardResponse(
        List<RevenueTrendResponse> revenueTrend,
        List<OrderStatusBreakdownResponse> orderStatusBreakdown,
        List<TopProductResponse> topProducts,
        List<WarehouseInventoryAnalyticsResponse> warehouseInventory,
        List<LowStockRiskResponse> lowStockRisk
) {
}
