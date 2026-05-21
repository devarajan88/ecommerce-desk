package com.appsdeveloperblog.core.dto.events;

import java.util.UUID;

/**
 * Published by the orders-service after it processes {@code RejectOrderCommand}
 * and persists the REJECTED status. The Saga Orchestrator (OrderSaga) listens to
 * this event to record the final REJECTED state in the order history, completing
 * the saga's compensating transaction chain.
 */
public class OrderRejectedEvent {

    private UUID orderId;

    public OrderRejectedEvent() {
    }

    public OrderRejectedEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
