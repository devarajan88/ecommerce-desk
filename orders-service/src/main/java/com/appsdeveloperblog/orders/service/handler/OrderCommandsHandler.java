package com.appsdeveloperblog.orders.service.handler;

import com.appsdeveloperblog.core.dto.commands.ApproveOrderCommand;
import com.appsdeveloperblog.core.dto.commands.RejectOrderCommand;
import com.appsdeveloperblog.orders.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Handles commands directed at the orders-service via the orders-commands topic.
 *
 * <p>Commands are issued by the {@code OrderSaga} orchestrator:
 * <ul>
 *   <li>{@link ApproveOrderCommand} — happy path: persist APPROVED + publish OrderApprovedEvent</li>
 *   <li>{@link RejectOrderCommand}  — compensation: persist REJECTED + publish OrderRejectedEvent</li>
 * </ul>
 */
@Component
@KafkaListener(topics = "${orders.commands.topic.name}")
public class OrderCommandsHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandsHandler.class);

    private final OrderService orderService;

    public OrderCommandsHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaHandler
    public void handleCommand(@Payload ApproveOrderCommand command) {
        log.info("[COMMAND] ApproveOrderCommand received for orderId={}", command.getOrderId());
        orderService.approveOrder(command.getOrderId());
    }

    @KafkaHandler
    public void handleCommand(@Payload RejectOrderCommand command) {
        log.info("[COMMAND] RejectOrderCommand received for orderId={}", command.getOrderId());
        orderService.rejectOrder(command.getOrderId());
    }
}
