package com.appsdeveloperblog.orders.dao.jpa.entity;

import com.appsdeveloperblog.core.types.OrderStatus;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * @deprecated Migrated to MongoDB. Order history is now persisted as
 * {@link com.appsdeveloperblog.orders.dao.mongodb.document.OrderHistoryDocument}
 * in the {@code order_history} collection.
 * This class is kept only as a reference and is no longer mapped by JPA.
 */
@Deprecated
public class OrderHistoryEntity {
    private UUID id;
    private UUID orderId;
    private OrderStatus status;
    private Timestamp createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
