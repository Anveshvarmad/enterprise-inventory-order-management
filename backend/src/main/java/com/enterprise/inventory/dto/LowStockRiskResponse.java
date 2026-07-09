package com.enterprise.inventory.dto;

public record LowStockRiskResponse(
        Long productId,
        String sku,
        String productName,
        Long warehouseId,
        String warehouseName,
        String warehouseCode,
        Integer availableQuantity,
        Integer reorderLevel
) {
}
