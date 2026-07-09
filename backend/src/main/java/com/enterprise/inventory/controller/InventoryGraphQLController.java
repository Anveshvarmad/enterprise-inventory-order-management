package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.*;
import com.enterprise.inventory.enums.OrderStatus;
import com.enterprise.inventory.enums.PaymentStatus;
import com.enterprise.inventory.enums.ProductStatus;
import com.enterprise.inventory.enums.ShipmentStatus;
import com.enterprise.inventory.repository.CategoryRepository;
import com.enterprise.inventory.repository.CustomerRepository;
import com.enterprise.inventory.repository.SupplierRepository;
import com.enterprise.inventory.repository.WarehouseRepository;
import com.enterprise.inventory.service.DashboardService;
import com.enterprise.inventory.service.InventoryService;
import com.enterprise.inventory.service.OrderService;
import com.enterprise.inventory.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class InventoryGraphQLController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final DashboardService dashboardService;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;

    public InventoryGraphQLController(
            ProductService productService,
            InventoryService inventoryService,
            OrderService orderService,
            DashboardService dashboardService,
            CategoryRepository categoryRepository,
            SupplierRepository supplierRepository,
            WarehouseRepository warehouseRepository,
            CustomerRepository customerRepository
    ) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.dashboardService = dashboardService;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.customerRepository = customerRepository;
    }

    @QueryMapping
    public DashboardSummaryResponse dashboardSummary() {
        return dashboardService.getDashboardSummary();
    }

    @QueryMapping
    public PageResponse<ProductResponse> products(
            @Argument String search,
            @Argument Long categoryId,
            @Argument Long supplierId,
            @Argument ProductStatus status,
            @Argument Integer page,
            @Argument Integer size
    ) {
        Page<ProductResponse> products = productService.getProducts(
                search,
                categoryId,
                supplierId,
                status,
                PageRequest.of(
                        safePage(page),
                        safeSize(size),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return PageResponse.from(products);
    }

    @QueryMapping
    public ProductResponse product(@Argument Long id) {
        return productService.getProductById(id);
    }

    @QueryMapping
    public PageResponse<InventoryResponse> inventory(
            @Argument Long productId,
            @Argument Long warehouseId,
            @Argument Boolean lowStock,
            @Argument Integer page,
            @Argument Integer size
    ) {
        Page<InventoryResponse> inventory = inventoryService.getInventory(
                productId,
                warehouseId,
                lowStock,
                PageRequest.of(
                        safePage(page),
                        safeSize(size),
                        Sort.by(Sort.Direction.ASC, "id")
                )
        );

        return PageResponse.from(inventory);
    }

    @QueryMapping
    public InventoryResponse inventoryItem(@Argument Long id) {
        return inventoryService.getInventoryById(id);
    }

    @QueryMapping
    public PageResponse<InventoryResponse> lowStockInventory(
            @Argument Integer page,
            @Argument Integer size
    ) {
        return PageResponse.from(
                inventoryService.getLowStockInventory(
                        PageRequest.of(
                                safePage(page),
                                safeSize(size),
                                Sort.by(Sort.Direction.ASC, "id")
                        )
                )
        );
    }

    @QueryMapping
    public PageResponse<OrderResponse> orders(
            @Argument String search,
            @Argument Long customerId,
            @Argument OrderStatus orderStatus,
            @Argument PaymentStatus paymentStatus,
            @Argument ShipmentStatus shipmentStatus,
            @Argument Integer page,
            @Argument Integer size
    ) {
        Page<OrderResponse> orders = orderService.getOrders(
                search,
                customerId,
                orderStatus,
                paymentStatus,
                shipmentStatus,
                PageRequest.of(
                        safePage(page),
                        safeSize(size),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return PageResponse.from(orders);
    }

    @QueryMapping
    public OrderResponse order(@Argument Long id) {
        return orderService.getOrderById(id);
    }

    @QueryMapping
    public List<CategoryResponse> categories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @QueryMapping
    public List<SupplierResponse> suppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(SupplierResponse::from)
                .toList();
    }

    @QueryMapping
    public List<WarehouseResponse> warehouses() {
        return warehouseRepository.findAllByOrderByNameAsc()
                .stream()
                .map(WarehouseResponse::from)
                .toList();
    }

    @QueryMapping
    public List<CustomerResponse> customers() {
        return customerRepository.findAllByOrderByFirstNameAscLastNameAsc()
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @MutationMapping
    public ProductResponse createProduct(@Argument("input") ProductCreateRequest request) {
        return productService.createProduct(request);
    }

    @MutationMapping
    public ProductResponse updateProduct(
            @Argument Long id,
            @Argument("input") ProductUpdateRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument Long id) {
        productService.deleteProduct(id);
        return true;
    }

    @MutationMapping
    public OrderResponse createOrder(@Argument("input") OrderCreateRequest request) {
        return orderService.createOrder(request);
    }

    @MutationMapping
    public OrderResponse cancelOrder(@Argument Long id) {
        return orderService.cancelOrder(id);
    }

    @MutationMapping
    public OrderResponse markOrderAsPaid(@Argument Long id) {
        return orderService.markOrderAsPaid(id);
    }

    @MutationMapping
    public OrderResponse shipOrder(@Argument Long id) {
        return orderService.shipOrder(id);
    }

    @MutationMapping
    public InventoryResponse adjustStock(@Argument("input") StockAdjustmentRequest request) {
        return inventoryService.adjustStock(request);
    }

    @MutationMapping
    public InventoryResponse reserveStock(@Argument("input") InventoryReservationRequest request) {
        return inventoryService.reserveStock(request);
    }

    @MutationMapping
    public InventoryResponse releaseReservation(@Argument("input") InventoryReservationRequest request) {
        return inventoryService.releaseReservation(request);
    }

    @MutationMapping
    public InventoryResponse transferStock(@Argument("input") InventoryTransferRequest request) {
        return inventoryService.transferStock(request);
    }

    private int safePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }

        return page;
    }

    private int safeSize(Integer size) {
        if (size == null || size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}
