package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.CategoryResponse;
import com.enterprise.inventory.dto.SupplierResponse;
import com.enterprise.inventory.dto.WarehouseResponse;
import com.enterprise.inventory.repository.CategoryRepository;
import com.enterprise.inventory.repository.SupplierRepository;
import com.enterprise.inventory.repository.WarehouseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LookupController {

    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;

    public LookupController(
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            WarehouseRepository warehouseRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping("/api/lookups/categories")
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @GetMapping("/api/lookups/suppliers")
    public List<SupplierResponse> getSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(SupplierResponse::from)
                .toList();
    }

    @GetMapping("/api/lookups/warehouses")
    public List<WarehouseResponse> getWarehouses() {
        return warehouseRepository.findAllByOrderByNameAsc()
                .stream()
                .map(WarehouseResponse::from)
                .toList();
    }
}
