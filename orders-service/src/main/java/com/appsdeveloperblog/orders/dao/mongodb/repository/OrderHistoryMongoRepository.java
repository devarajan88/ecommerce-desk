package com.appsdeveloperblog.orders.dao.mongodb.repository;

import com.appsdeveloperblog.orders.dao.mongodb.document.OrderHistoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data MongoDB repository for order history documents.
 *
 * <p>{@code findByOrderId} is backed by the {@code @Indexed} field on
 * {@link OrderHistoryDocument#orderId}, so it executes as an efficient index scan.
 */
@Repository
public interface OrderHistoryMongoRepository extends MongoRepository<OrderHistoryDocument, UUID> {
    List<OrderHistoryDocument> findByOrderId(UUID orderId);
}
