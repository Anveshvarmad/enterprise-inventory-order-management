package com.enterprise.inventory.dto;

import com.enterprise.inventory.entity.CustomerOrder;
import com.enterprise.inventory.enums.OrderStatus;
import com.enterprise.inventory.enums.PaymentStatus;
import com.enterprise.inventory.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        CustomerResponse customer,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        ShipmentStatus shipmentStatus,
        BigDecimal totalAmount,
        String notes,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                CustomerResponse.from(order.getCustomer()),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getShipmentStatus(),
                order.getTotalAmount(),
                order.getNotes(),
                order.getItems()
                        .stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
