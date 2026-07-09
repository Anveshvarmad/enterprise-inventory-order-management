package com.enterprise.inventory.dto;

import java.math.BigDecimal;

public record TopProductResponse(
        Long productId,
        String sku,
        String productName,
        Long totalQuantitySold,
        BigDecimal totalRevenue
) {
}
