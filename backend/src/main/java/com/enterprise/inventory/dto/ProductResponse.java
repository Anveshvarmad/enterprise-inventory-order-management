package com.enterprise.inventory.dto;

import com.enterprise.inventory.entity.Product;
import com.enterprise.inventory.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        CategoryResponse category,
        SupplierResponse supplier,
        BigDecimal unitPrice,
        Integer reorderLevel,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory() == null ? null : CategoryResponse.from(product.getCategory()),
                product.getSupplier() == null ? null : SupplierResponse.from(product.getSupplier()),
                product.getUnitPrice(),
                product.getReorderLevel(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
