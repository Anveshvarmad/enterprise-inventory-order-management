package com.enterprise.inventory.dto;

import java.math.BigDecimal;

public record RevenueTrendResponse(
        String period,
        Long totalOrders,
        BigDecimal totalRevenue
) {
}
