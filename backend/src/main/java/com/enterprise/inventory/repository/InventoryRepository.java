package com.enterprise.inventory.repository;

import com.enterprise.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Override
    @EntityGraph(attributePaths = {"product", "warehouse"})
    Optional<Inventory> findById(Long id);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    @EntityGraph(attributePaths = {"product", "warehouse"})
    @Query("""
            SELECT i FROM Inventory i
            WHERE (:productId IS NULL OR i.product.id = :productId)
              AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
              AND (:lowStock IS NULL OR :lowStock = false OR i.availableQuantity <= i.product.reorderLevel)
            """)
    Page<Inventory> searchInventory(Long productId, Long warehouseId, Boolean lowStock, Pageable pageable);
}
