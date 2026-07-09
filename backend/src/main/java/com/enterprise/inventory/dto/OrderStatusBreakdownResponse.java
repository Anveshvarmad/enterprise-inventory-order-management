package com.enterprise.inventory.dto;

public record OrderStatusBreakdownResponse(
        String status,
        Long count
) {
}
