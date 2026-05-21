package com.appsdeveloperblog.orders.dao.mongodb.document;

import com.appsdeveloperblog.core.types.OrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

/**
 * MongoDB document for saga order history.
 *
 * <p>Each status transition in the saga (CREATED → APPROVED / REJECTED) is appended
 * as a separate document, giving a full immutable audit trail per order.
 * This is a natural fit for MongoDB: the records are append-only, never updated,
 * and queried purely by orderId — no relational joins required.
 *
 * <p>Contrast with {@code orders} collection in MySQL (via JPA), which holds the
 * current mutable state of each order. This dual-store setup demonstrates the
 * polyglot-persistence pattern common in microservices architectures.
 */
@Document(collection = "order_history")
public class OrderHistoryDocument {

    @Id
    private UUID id;

    /** Indexed so findByOrderId() uses a covered query instead of a full scan. */
    @Indexed
    @Field("order_id")
    private UUID orderId;

    @Field("status")
    private OrderStatus status;

    /** Use Instant (UTC) — MongoDB stores dates as 64-bit UTC milliseconds. */
    @Field("created_at")
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
