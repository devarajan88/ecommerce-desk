package com.appsdeveloperblog.core.dto.events;

import java.util.UUID;

/**
 * Published by the products-service after it successfully executes the
 * {@code CancelProductReservationCommand} — i.e., stock has been restored.
 * The Saga Orchestrator (OrderSaga) reacts to this event by proceeding to
 * reject the order via {@code RejectOrderCommand}.
 */
public class ProductReservationCancelledEvent {

    private UUID orderId;
    private UUID productId;
    private Integer productQuantity;

    public ProductReservationCancelledEvent() {
    }

    public ProductReservationCancelledEvent(UUID orderId, UUID productId, Integer productQuantity) {
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
