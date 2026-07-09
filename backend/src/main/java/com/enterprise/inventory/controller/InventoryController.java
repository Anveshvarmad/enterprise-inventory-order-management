package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.InventoryReservationRequest;
import com.enterprise.inventory.dto.InventoryResponse;
import com.enterprise.inventory.dto.InventoryTransferRequest;
import com.enterprise.inventory.dto.PageResponse;
import com.enterprise.inventory.dto.StockAdjustmentRequest;
import com.enterprise.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public PageResponse<InventoryResponse> getInventory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Boolean lowStock,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<InventoryResponse> inventory = inventoryService.getInventory(
                productId,
                warehouseId,
                lowStock,
                pageable
        );

        return PageResponse.from(inventory);
    }

    @GetMapping("/{id}")
    public InventoryResponse getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id);
    }

    @GetMapping("/low-stock")
    public PageResponse<InventoryResponse> getLowStockInventory(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return PageResponse.from(inventoryService.getLowStockInventory(pageable));
    }

    @PostMapping("/adjust")
    public InventoryResponse adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return inventoryService.adjustStock(request);
    }

    @PostMapping("/reserve")
    public InventoryResponse reserveStock(@Valid @RequestBody InventoryReservationRequest request) {
        return inventoryService.reserveStock(request);
    }

    @PostMapping("/release")
    public InventoryResponse releaseReservation(@Valid @RequestBody InventoryReservationRequest request) {
        return inventoryService.releaseReservation(request);
    }

    @PostMapping("/transfer")
    public InventoryResponse transferStock(@Valid @RequestBody InventoryTransferRequest request) {
        return inventoryService.transferStock(request);
    }
}
