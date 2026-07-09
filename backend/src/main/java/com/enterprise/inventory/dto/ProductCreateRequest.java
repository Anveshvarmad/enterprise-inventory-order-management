package com.enterprise.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateRequest(

        @NotBlank(message = "SKU is required")
        @Size(max = 80, message = "SKU cannot exceed 80 characters")
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 180, message = "Product name cannot exceed 180 characters")
        String name,

        String description,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotNull(message = "Supplier ID is required")
        Long supplierId,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Unit price cannot be negative")
        BigDecimal unitPrice,

        @NotNull(message = "Reorder level is required")
        @Min(value = 0, message = "Reorder level cannot be negative")
        Integer reorderLevel
) {
}
