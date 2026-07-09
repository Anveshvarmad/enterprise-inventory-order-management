package com.enterprise.inventory.dto;

import com.enterprise.inventory.entity.Supplier;

public record SupplierResponse(
        Long id,
        String name,
        String contactEmail,
        String phone,
        String status
) {
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactEmail(),
                supplier.getPhone(),
                supplier.getStatus()
        );
    }
}
