package com.enterprise.inventory.dto;

import com.enterprise.inventory.entity.Inventory;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long productId,
        String sku,
        String productName,
        Long warehouseId,
        String warehouseName,
        String warehouseCode,
        Integer quantityOnHand,
        Integer reservedQuantity,
        Integer availableQuantity,
        Integer reorderLevel,
        Boolean lowStock,
        LocalDateTime lastRestockedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InventoryResponse from(Inventory inventory) {
        Integer available = inventory.getAvailableQuantity();
        Integer reorderLevel = inventory.getProduct().getReorderLevel();

        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getProduct().getName(),
                inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(),
                inventory.getWarehouse().getCode(),
                inventory.getQuantityOnHand(),
                inventory.getReservedQuantity(),
                available,
                reorderLevel,
                available <= reorderLevel,
                inventory.getLastRestockedAt(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}
