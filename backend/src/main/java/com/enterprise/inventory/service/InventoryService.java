package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.InventoryReservationRequest;
import com.enterprise.inventory.dto.InventoryResponse;
import com.enterprise.inventory.dto.InventoryTransferRequest;
import com.enterprise.inventory.dto.StockAdjustmentRequest;
import com.enterprise.inventory.entity.Inventory;
import com.enterprise.inventory.entity.InventoryTransaction;
import com.enterprise.inventory.entity.Product;
import com.enterprise.inventory.entity.Warehouse;
import com.enterprise.inventory.enums.InventoryTransactionType;
import com.enterprise.inventory.exception.BusinessRuleException;
import com.enterprise.inventory.exception.ResourceNotFoundException;
import com.enterprise.inventory.repository.InventoryRepository;
import com.enterprise.inventory.repository.InventoryTransactionRepository;
import com.enterprise.inventory.repository.ProductRepository;
import com.enterprise.inventory.repository.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getInventory(
            Long productId,
            Long warehouseId,
            Boolean lowStock,
            Pageable pageable
    ) {
        return inventoryRepository.searchInventory(productId, warehouseId, lowStock, pageable)
                .map(InventoryResponse::from);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long id) {
        return InventoryResponse.from(findInventory(id));
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getLowStockInventory(Pageable pageable) {
        return inventoryRepository.searchInventory(null, null, true, pageable)
                .map(InventoryResponse::from);
    }

    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        if (request.quantityChange() == 0) {
            throw new BusinessRuleException("Quantity change cannot be zero");
        }

        Inventory inventory = findOrCreateInventory(request.productId(), request.warehouseId());

        int newQuantityOnHand = inventory.getQuantityOnHand() + request.quantityChange();

        if (newQuantityOnHand < inventory.getReservedQuantity()) {
            throw new BusinessRuleException("Quantity on hand cannot be less than reserved quantity");
        }

        inventory.setQuantityOnHand(newQuantityOnHand);

        if (request.quantityChange() > 0) {
            inventory.setLastRestockedAt(LocalDateTime.now());
        }

        Inventory savedInventory = inventoryRepository.save(inventory);

        recordTransaction(
                savedInventory.getProduct(),
                savedInventory.getWarehouse(),
                request.quantityChange() > 0 ? InventoryTransactionType.RESTOCK : InventoryTransactionType.STOCK_ADJUSTMENT,
                request.quantityChange(),
                "MANUAL_ADJUSTMENT",
                null,
                request.notes()
        );

        return InventoryResponse.from(savedInventory);
    }

    @Transactional
    public InventoryResponse reserveStock(InventoryReservationRequest request) {
        Inventory inventory = findInventoryByProductAndWarehouse(request.productId(), request.warehouseId());

        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new BusinessRuleException("Insufficient available stock for reservation");
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.quantity());

        Inventory savedInventory = inventoryRepository.save(inventory);

        recordTransaction(
                savedInventory.getProduct(),
                savedInventory.getWarehouse(),
                InventoryTransactionType.RESERVE,
                request.quantity(),
                request.referenceType(),
                request.referenceId(),
                request.notes()
        );

        return InventoryResponse.from(savedInventory);
    }

    @Transactional
    public InventoryResponse releaseReservation(InventoryReservationRequest request) {
        Inventory inventory = findInventoryByProductAndWarehouse(request.productId(), request.warehouseId());

        if (inventory.getReservedQuantity() < request.quantity()) {
            throw new BusinessRuleException("Release quantity cannot exceed reserved quantity");
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.quantity());

        Inventory savedInventory = inventoryRepository.save(inventory);

        recordTransaction(
                savedInventory.getProduct(),
                savedInventory.getWarehouse(),
                InventoryTransactionType.RELEASE_RESERVATION,
                request.quantity(),
                request.referenceType(),
                request.referenceId(),
                request.notes()
        );

        return InventoryResponse.from(savedInventory);
    }

    @Transactional
    public InventoryResponse transferStock(InventoryTransferRequest request) {
        if (request.sourceWarehouseId().equals(request.targetWarehouseId())) {
            throw new BusinessRuleException("Source and target warehouses must be different");
        }

        Inventory sourceInventory = findInventoryByProductAndWarehouse(
                request.productId(),
                request.sourceWarehouseId()
        );

        if (sourceInventory.getAvailableQuantity() < request.quantity()) {
            throw new BusinessRuleException("Insufficient available stock for transfer");
        }

        Inventory targetInventory = findOrCreateInventory(
                request.productId(),
                request.targetWarehouseId()
        );

        sourceInventory.setQuantityOnHand(sourceInventory.getQuantityOnHand() - request.quantity());
        targetInventory.setQuantityOnHand(targetInventory.getQuantityOnHand() + request.quantity());
        targetInventory.setLastRestockedAt(LocalDateTime.now());

        Inventory savedSource = inventoryRepository.save(sourceInventory);
        inventoryRepository.save(targetInventory);

        recordTransaction(
                savedSource.getProduct(),
                savedSource.getWarehouse(),
                InventoryTransactionType.TRANSFER_OUT,
                -request.quantity(),
                "WAREHOUSE_TRANSFER",
                targetInventory.getWarehouse().getId(),
                request.notes()
        );

        recordTransaction(
                targetInventory.getProduct(),
                targetInventory.getWarehouse(),
                InventoryTransactionType.TRANSFER_IN,
                request.quantity(),
                "WAREHOUSE_TRANSFER",
                savedSource.getWarehouse().getId(),
                request.notes()
        );

        return InventoryResponse.from(savedSource);
    }

    private Inventory findInventory(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found with id: " + id));
    }

    private Inventory findInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory record not found for product " + productId + " and warehouse " + warehouseId
                ));
    }

    private Inventory findOrCreateInventory(Long productId, Long warehouseId) {
        Product product = findProduct(productId);
        Warehouse warehouse = findWarehouse(warehouseId);

        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> {
                    Inventory inventory = new Inventory();
                    inventory.setProduct(product);
                    inventory.setWarehouse(warehouse);
                    inventory.setQuantityOnHand(0);
                    inventory.setReservedQuantity(0);
                    return inventoryRepository.save(inventory);
                });
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Warehouse findWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    private void recordTransaction(
            Product product,
            Warehouse warehouse,
            InventoryTransactionType transactionType,
            Integer quantity,
            String referenceType,
            Long referenceId,
            String notes
    ) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setWarehouse(warehouse);
        transaction.setTransactionType(transactionType);
        transaction.setQuantity(quantity);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);

        inventoryTransactionRepository.save(transaction);
    }
}
