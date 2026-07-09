package com.enterprise.inventory.service;

import com.enterprise.inventory.config.CacheNames;
import com.enterprise.inventory.dto.OrderCreateRequest;
import com.enterprise.inventory.dto.OrderItemCreateRequest;
import com.enterprise.inventory.dto.OrderResponse;
import com.enterprise.inventory.entity.*;
import com.enterprise.inventory.enums.InventoryTransactionType;
import com.enterprise.inventory.enums.OrderStatus;
import com.enterprise.inventory.enums.PaymentStatus;
import com.enterprise.inventory.enums.ShipmentStatus;
import com.enterprise.inventory.exception.BusinessRuleException;
import com.enterprise.inventory.exception.ResourceNotFoundException;
import com.enterprise.inventory.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;

@Service
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public OrderService(
            CustomerOrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository,
            InventoryRepository inventoryRepository,
            InventoryTransactionRepository inventoryTransactionRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(
            String search,
            Long customerId,
            OrderStatus orderStatus,
            PaymentStatus paymentStatus,
            ShipmentStatus shipmentStatus,
            Pageable pageable
    ) {
        Specification<CustomerOrder> specification = buildSpecification(
                search,
                customerId,
                orderStatus,
                paymentStatus,
                shipmentStatus
        );

        return orderRepository.findAll(specification, pageable)
                .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        return OrderResponse.from(findOrder(id));
    }

    @CacheEvict(value = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Customer customer = findCustomer(request.customerId());

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setShipmentStatus(ShipmentStatus.NOT_SHIPPED);
        order.setNotes(request.notes());
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemCreateRequest itemRequest : request.items()) {
            Product product = findProduct(itemRequest.productId());
            Warehouse warehouse = findWarehouse(itemRequest.warehouseId());
            Inventory inventory = findInventory(product.getId(), warehouse.getId());

            if (inventory.getAvailableQuantity() < itemRequest.quantity()) {
                throw new BusinessRuleException(
                        "Insufficient stock for product " + product.getSku()
                                + " in warehouse " + warehouse.getCode()
                );
            }

            inventory.setReservedQuantity(inventory.getReservedQuantity() + itemRequest.quantity());
            inventoryRepository.save(inventory);

            BigDecimal lineTotal = product.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setWarehouse(warehouse);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getUnitPrice());
            orderItem.setLineTotal(lineTotal);

            order.addItem(orderItem);
            totalAmount = totalAmount.add(lineTotal);

            recordInventoryTransaction(
                    product,
                    warehouse,
                    InventoryTransactionType.RESERVE,
                    itemRequest.quantity(),
                    "ORDER",
                    null,
                    "Reserved stock while creating order"
            );
        }

        order.setTotalAmount(totalAmount);

        CustomerOrder savedOrder = orderRepository.save(order);

        return OrderResponse.from(savedOrder);
    }

    @CacheEvict(value = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        CustomerOrder order = findOrder(id);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Order is already cancelled");
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Shipped or delivered orders cannot be cancelled");
        }

        for (OrderItem item : order.getItems()) {
            Inventory inventory = findInventory(item.getProduct().getId(), item.getWarehouse().getId());

            if (inventory.getReservedQuantity() < item.getQuantity()) {
                throw new BusinessRuleException("Reserved stock is lower than order item quantity");
            }

            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);

            recordInventoryTransaction(
                    item.getProduct(),
                    item.getWarehouse(),
                    InventoryTransactionType.RELEASE_RESERVATION,
                    item.getQuantity(),
                    "ORDER",
                    order.getId(),
                    "Released stock after order cancellation"
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.CANCELLED);
        order.setShipmentStatus(ShipmentStatus.CANCELLED);

        return OrderResponse.from(orderRepository.save(order));
    }

    @CacheEvict(value = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    @Transactional
    public OrderResponse markOrderAsPaid(Long id) {
        CustomerOrder order = findOrder(id);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled orders cannot be marked as paid");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessRuleException("Order is already paid");
        }

        order.setPaymentStatus(PaymentStatus.PAID);

        return OrderResponse.from(orderRepository.save(order));
    }

    @CacheEvict(value = CacheNames.DASHBOARD_SUMMARY, allEntries = true)
    @Transactional
    public OrderResponse shipOrder(Long id) {
        CustomerOrder order = findOrder(id);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled orders cannot be shipped");
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Order has already been shipped or delivered");
        }

        for (OrderItem item : order.getItems()) {
            Inventory inventory = findInventory(item.getProduct().getId(), item.getWarehouse().getId());

            if (inventory.getReservedQuantity() < item.getQuantity()) {
                throw new BusinessRuleException("Reserved stock is lower than order item quantity");
            }

            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() - item.getQuantity());
            inventoryRepository.save(inventory);

            recordInventoryTransaction(
                    item.getProduct(),
                    item.getWarehouse(),
                    InventoryTransactionType.SHIP,
                    -item.getQuantity(),
                    "ORDER",
                    order.getId(),
                    "Deducted stock after order shipment"
            );
        }

        order.setOrderStatus(OrderStatus.SHIPPED);
        order.setShipmentStatus(ShipmentStatus.SHIPPED);

        return OrderResponse.from(orderRepository.save(order));
    }

    private CustomerOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private Warehouse findWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    private Inventory findInventory(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId + " and warehouse " + warehouseId
                ));
    }

    private void recordInventoryTransaction(
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

    private String generateOrderNumber() {
        return "ORD-" + Instant.now().toEpochMilli();
    }

    private Specification<CustomerOrder> buildSpecification(
            String search,
            Long customerId,
            OrderStatus orderStatus,
            PaymentStatus paymentStatus,
            ShipmentStatus shipmentStatus
    ) {
        return (root, query, criteriaBuilder) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

                Predicate orderNumberMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("orderNumber")),
                        pattern
                );

                Predicate customerEmailMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("customer").get("email")),
                        pattern
                );

                predicates.add(criteriaBuilder.or(orderNumberMatch, customerEmailMatch));
            }

            if (customerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), customerId));
            }

            if (orderStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus"), orderStatus));
            }

            if (paymentStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), paymentStatus));
            }

            if (shipmentStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("shipmentStatus"), shipmentStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
