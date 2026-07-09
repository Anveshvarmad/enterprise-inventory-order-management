package com.enterprise.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryTransferRequest(

        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Source warehouse ID is required")
        Long sourceWarehouseId,

        @NotNull(message = "Target warehouse ID is required")
        Long targetWarehouseId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity,

        String notes
) {
}
