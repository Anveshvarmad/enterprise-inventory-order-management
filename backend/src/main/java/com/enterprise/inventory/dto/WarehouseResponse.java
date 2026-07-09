package com.enterprise.inventory.dto;

import com.enterprise.inventory.entity.Warehouse;

public record WarehouseResponse(
        Long id,
        String name,
        String code,
        String location,
        Integer capacity,
        String status
) {
    public static WarehouseResponse from(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getLocation(),
                warehouse.getCapacity(),
                warehouse.getStatus()
        );
    }
}
