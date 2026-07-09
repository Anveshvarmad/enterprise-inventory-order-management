package com.enterprise.inventory.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        Long totalProducts,
        Long activeProducts,
        Long lowStockItems,
        Long totalOrders,
        Long pendingOrders,
        Long shippedOrders,
        Long totalCustomers,
        Long totalWarehouses,
        Long totalInventoryUnits,
        BigDecimal totalOrderValue
) {
}
