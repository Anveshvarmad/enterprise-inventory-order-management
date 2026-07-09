package com.enterprise.inventory.dto;

import com.enterprise.inventory.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(

        @Size(max = 80, message = "SKU cannot exceed 80 characters")
        String sku,

        @Size(max = 180, message = "Product name cannot exceed 180 characters")
        String name,

        String description,

        Long categoryId,

        Long supplierId,

        @DecimalMin(value = "0.0", inclusive = true, message = "Unit price cannot be negative")
        BigDecimal unitPrice,

        @Min(value = 0, message = "Reorder level cannot be negative")
        Integer reorderLevel,

        ProductStatus status
) {
}
