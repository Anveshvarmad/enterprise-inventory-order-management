package com.enterprise.inventory.dto;

public record WarehouseInventoryAnalyticsResponse(
        Long warehouseId,
        String warehouseName,
        String warehouseCode,
        Long quantityOnHand,
        Long reservedQuantity,
        Long availableQuantity
) {
}
