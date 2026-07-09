package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.OrderCreateRequest;
import com.enterprise.inventory.dto.OrderResponse;
import com.enterprise.inventory.dto.PageResponse;
import com.enterprise.inventory.enums.OrderStatus;
import com.enterprise.inventory.enums.PaymentStatus;
import com.enterprise.inventory.enums.ShipmentStatus;
import com.enterprise.inventory.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public PageResponse<OrderResponse> getOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) ShipmentStatus shipmentStatus,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderResponse> orders = orderService.getOrders(
                search,
                customerId,
                orderStatus,
                paymentStatus,
                shipmentStatus,
                pageable
        );

        return PageResponse.from(orders);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.createOrder(request);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @PostMapping("/{id}/mark-paid")
    public OrderResponse markOrderAsPaid(@PathVariable Long id) {
        return orderService.markOrderAsPaid(id);
    }

    @PostMapping("/{id}/ship")
    public OrderResponse shipOrder(@PathVariable Long id) {
        return orderService.shipOrder(id);
    }
}
