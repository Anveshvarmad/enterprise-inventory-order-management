package com.enterprise.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemCreateRequest> items,

        String notes
) {
}
