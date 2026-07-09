package com.enterprise.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Warehouse ID is required")
        Long warehouseId,

        @NotNull(message = "Quantity change is required")
        Integer quantityChange,

        String notes
) {
}
