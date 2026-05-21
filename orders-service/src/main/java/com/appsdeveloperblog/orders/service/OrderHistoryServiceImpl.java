package com.appsdeveloperblog.orders.service;

import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.dao.mongodb.document.OrderHistoryDocument;
import com.appsdeveloperblog.orders.dao.mongodb.repository.OrderHistoryMongoRepository;
import com.appsdeveloperblog.orders.dto.OrderHistory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderHistoryMongoRepository orderHistoryRepository;

    public OrderHistoryServiceImpl(OrderHistoryMongoRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Override
    public void add(UUID orderId, OrderStatus orderStatus) {
        OrderHistoryDocument document = new OrderHistoryDocument();
        document.setId(UUID.randomUUID());
        document.setOrderId(orderId);
        document.setStatus(orderStatus);
        document.setCreatedAt(Instant.now());
        orderHistoryRepository.save(document);
    }

    @Override
    public List<OrderHistory> findByOrderId(UUID orderId) {
        return orderHistoryRepository.findByOrderId(orderId).stream().map(doc -> {
            OrderHistory history = new OrderHistory();
            history.setId(doc.getId());
            history.setOrderId(doc.getOrderId());
            history.setStatus(doc.getStatus());
            history.setCreatedAt(doc.getCreatedAt() != null
                    ? Timestamp.from(doc.getCreatedAt())
                    : null);
            return history;
        }).toList();
    }
}
