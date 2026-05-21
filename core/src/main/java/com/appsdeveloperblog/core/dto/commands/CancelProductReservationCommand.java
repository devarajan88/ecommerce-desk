package com.appsdeveloperblog.core.dto.commands;

import java.util.UUID;

/**
 * Compensation command sent by the Saga Orchestrator (OrderSaga) when a payment
 * fails. Instructs the products-service to restore the previously reserved stock,
 * completing the compensating transaction for that saga step.
 */
public class CancelProductReservationCommand {

    private UUID orderId;
    private UUID productId;
    private Integer productQuantity;

    public CancelProductReservationCommand() {
    }

    public CancelProductReservationCommand(UUID orderId, UUID productId, Integer productQuantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.productQuantity = productQuantity;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }
}
