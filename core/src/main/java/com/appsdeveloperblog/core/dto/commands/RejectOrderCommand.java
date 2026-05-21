package com.appsdeveloperblog.core.dto.commands;

import java.util.UUID;

/**
 * Command sent by the Saga Orchestrator (OrderSaga) to the orders-service when a
 * saga step fails and the order must be marked as REJECTED. Triggers the final
 * compensating transaction in the orchestrator chain.
 */
public class RejectOrderCommand {

    private UUID orderId;

    public RejectOrderCommand() {
    }

    public RejectOrderCommand(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
