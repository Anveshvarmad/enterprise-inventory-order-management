package com.enterprise.inventory.dto;

public record DemandForecastItemResponse(
        Long productId,
        String sku,
        String productName,
        Long warehouseId,
        String warehouseCode,
        String warehouseName,
        Integer quantityOnHand,
        Integer reservedQuantity,
        Integer availableQuantity,
        Integer reorderLevel,
        Integer predictedDemand7Days,
        Double predictedDailyDemand,
        Double estimatedDaysUntilStockout,
        Integer recommendedReorderQuantity,
        String riskLevel
) {
}
