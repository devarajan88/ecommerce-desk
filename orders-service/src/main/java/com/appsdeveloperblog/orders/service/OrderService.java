package com.appsdeveloperblog.orders.service;

import com.appsdeveloperblog.core.dto.Order;

import java.util.UUID;

public interface OrderService {
    Order placeOrder(Order order);
    void approveOrder(UUID orderId);
    /**
     * Compensation operation: persist REJECTED status and publish OrderRejectedEvent
     * so the saga orchestrator can close the compensating transaction chain.
     */
    void rejectOrder(UUID orderId);
    java.util.List<Order> findAllOrders();
    Order updateOrderStatus(UUID orderId, com.appsdeveloperblog.core.types.OrderStatus status);
    void deleteOrder(UUID orderId);
}
