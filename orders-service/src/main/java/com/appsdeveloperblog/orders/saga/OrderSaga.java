package com.appsdeveloperblog.orders.saga;

import com.appsdeveloperblog.core.dto.commands.ApproveOrderCommand;
import com.appsdeveloperblog.core.dto.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.commands.RejectOrderCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.events.OrderApprovedEvent;
import com.appsdeveloperblog.core.dto.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.dto.events.OrderRejectedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservationFailedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Saga Orchestrator for the Order workflow.
 *
 * <p>Listens to all event topics and drives the saga forward (happy path)
 * or triggers compensating transactions (failure paths) by issuing commands
 * to the appropriate participant services.
 *
 * <h2>Happy Path</h2>
 * <pre>
 * OrderCreatedEvent
 *   → ReserveProductCommand (products-commands)
 *   → ProductReservedEvent
 *   → ProcessPaymentCommand (payments-commands)
 *   → PaymentProcessedEvent
 *   → ApproveOrderCommand (orders-commands)
 *   → OrderApprovedEvent
 *   → [history: APPROVED] ✓
 * </pre>
 *
 * <h2>Compensation Path 1 — Product Reservation Fails</h2>
 * <pre>
 * ProductReservationFailedEvent
 *   → RejectOrderCommand (orders-commands)
 *   → OrderRejectedEvent
 *   → [history: REJECTED] ✓
 * </pre>
 *
 * <h2>Compensation Path 2 — Payment Fails</h2>
 * <pre>
 * PaymentFailedEvent
 *   → CancelProductReservationCommand (products-commands)   ← undo product step
 *   → ProductReservationCancelledEvent
 *   → RejectOrderCommand (orders-commands)
 *   → OrderRejectedEvent
 *   → [history: REJECTED] ✓
 * </pre>
 */
@Component
@KafkaListener(topics = {
        "${orders.events.topic.name}",
        "${products.events.topic.name}",
        "${payments.events.topic.name}"
})
public class OrderSaga {

    private static final Logger log = LoggerFactory.getLogger(OrderSaga.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderHistoryService orderHistoryService;
    private final String productsCommandsTopicName;
    private final String paymentsCommandsTopicName;
    private final String ordersCommandsTopicName;

    public OrderSaga(KafkaTemplate<String, Object> kafkaTemplate,
                     OrderHistoryService orderHistoryService,
                     @Value("${products.commands.topic.name}") String productsCommandsTopicName,
                     @Value("${payments.commands.topic.name}") String paymentsCommandsTopicName,
                     @Value("${orders.commands.topic.name}") String ordersCommandsTopicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderHistoryService = orderHistoryService;
        this.productsCommandsTopicName = productsCommandsTopicName;
        this.paymentsCommandsTopicName = paymentsCommandsTopicName;
        this.ordersCommandsTopicName = ordersCommandsTopicName;
    }

    // =========================================================================
    // HAPPY PATH
    // =========================================================================

    /**
     * Step 1 — Order placed.
     * Record initial state in history and ask products-service to reserve stock.
     */
    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event) {
        log.info("[SAGA] OrderCreatedEvent received for orderId={}", event.getOrderId());

        orderHistoryService.add(event.getOrderId(), OrderStatus.CREATED);

        ReserveProductCommand command = new ReserveProductCommand(
                event.getProductId(),
                event.getProductQuantity(),
                event.getOrderId()
        );
        log.info("[SAGA] → Sending ReserveProductCommand for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());
        kafkaTemplate.send(productsCommandsTopicName, command);
    }

    /**
     * Step 2 — Product stock reserved.
     * Ask payments-service to process the payment.
     */
    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent event) {
        log.info("[SAGA] ProductReservedEvent received for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());

        ProcessPaymentCommand command = new ProcessPaymentCommand(
                event.getOrderId(),
                event.getProductId(),
                event.getProductPrice(),
                event.getProductQuantity()
        );
        log.info("[SAGA] → Sending ProcessPaymentCommand for orderId={}", event.getOrderId());
        kafkaTemplate.send(paymentsCommandsTopicName, command);
    }

    /**
     * Step 3 — Payment processed successfully.
     * Ask orders-service to mark the order as APPROVED.
     */
    @KafkaHandler
    public void handleEvent(@Payload PaymentProcessedEvent event) {
        log.info("[SAGA] PaymentProcessedEvent received for orderId={}, paymentId={}",
                event.getOrderId(), event.getPaymentId());

        ApproveOrderCommand command = new ApproveOrderCommand(event.getOrderId());
        log.info("[SAGA] → Sending ApproveOrderCommand for orderId={}", event.getOrderId());
        kafkaTemplate.send(ordersCommandsTopicName, command);
    }

    /**
     * Step 4 — Order approved. Saga completed successfully.
     */
    @KafkaHandler
    public void handleEvent(@Payload OrderApprovedEvent event) {
        log.info("[SAGA] OrderApprovedEvent received — saga COMPLETED for orderId={}", event.getOrderId());
        orderHistoryService.add(event.getOrderId(), OrderStatus.APPROVED);
    }

    // =========================================================================
    // COMPENSATION PATH 1 — Product Reservation Fails
    // No product was reserved, so no stock rollback needed.
    // Simply reject the order.
    // =========================================================================

    /**
     * Compensation step — product reservation failed (e.g. insufficient stock).
     * No stock was reserved, so directly reject the order.
     */
    @KafkaHandler
    public void handleEvent(@Payload ProductReservationFailedEvent event) {
        log.warn("[SAGA] ProductReservationFailedEvent received for orderId={}, productId={} — initiating rejection",
                event.getOrderId(), event.getProductId());

        RejectOrderCommand command = new RejectOrderCommand(event.getOrderId());
        log.info("[SAGA] → Sending RejectOrderCommand (no stock reserved) for orderId={}", event.getOrderId());
        kafkaTemplate.send(ordersCommandsTopicName, command);
    }

    // =========================================================================
    // COMPENSATION PATH 2 — Payment Fails
    // Stock WAS reserved — must undo that first, then reject the order.
    // =========================================================================

    /**
     * Compensation step 2a — payment failed.
     * Stock was already reserved; send a cancel command to restore it before
     * rejecting the order.
     */
    @KafkaHandler
    public void handleEvent(@Payload PaymentFailedEvent event) {
        log.warn("[SAGA] PaymentFailedEvent received for orderId={} — compensating: cancelling product reservation",
                event.getOrderId());

        CancelProductReservationCommand command = new CancelProductReservationCommand(
                event.getOrderId(),
                event.getProductId(),
                event.getProductQuantity()
        );
        log.info("[SAGA] → Sending CancelProductReservationCommand for orderId={}, productId={}",
                event.getOrderId(), event.getProductId());
        kafkaTemplate.send(productsCommandsTopicName, command);
    }

    /**
     * Compensation step 2b — product reservation cancelled after payment failure.
     * Stock is restored; now reject the order.
     */
    @KafkaHandler
    public void handleEvent(@Payload ProductReservationCancelledEvent event) {
        log.info("[SAGA] ProductReservationCancelledEvent received for orderId={} — stock restored, rejecting order",
                event.getOrderId());

        RejectOrderCommand command = new RejectOrderCommand(event.getOrderId());
        log.info("[SAGA] → Sending RejectOrderCommand (after stock rollback) for orderId={}", event.getOrderId());
        kafkaTemplate.send(ordersCommandsTopicName, command);
    }

    /**
     * Final compensation step — order has been persisted as REJECTED.
     * Record the terminal state in history to close the saga.
     */
    @KafkaHandler
    public void handleEvent(@Payload OrderRejectedEvent event) {
        log.warn("[SAGA] OrderRejectedEvent received — saga COMPENSATED for orderId={}", event.getOrderId());
        orderHistoryService.add(event.getOrderId(), OrderStatus.REJECTED);
    }

    // =========================================================================
    // SAFETY NET
    // =========================================================================

    /**
     * Default handler — catches any message type not matched by the handlers above.
     * Prevents the listener container from throwing an unhandled-message error.
     */
    @KafkaHandler(isDefault = true)
    public void handleUnknown(@Payload Object unknown) {
        log.debug("[SAGA] Received unhandled message type={}", unknown.getClass().getSimpleName());
    }
}
