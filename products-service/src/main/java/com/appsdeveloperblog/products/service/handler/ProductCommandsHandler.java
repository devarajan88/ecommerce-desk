package com.appsdeveloperblog.products.service.handler;

import com.appsdeveloperblog.core.dto.Product;
import com.appsdeveloperblog.core.dto.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservationFailedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Handles commands directed at the products-service via the products-commands topic.
 *
 * <p>Commands are issued by the {@code OrderSaga} orchestrator:
 * <ul>
 *   <li>{@link ReserveProductCommand}              — happy path: decrement stock, publish ProductReservedEvent</li>
 *   <li>{@link CancelProductReservationCommand}    — compensation: restore stock, publish ProductReservationCancelledEvent</li>
 * </ul>
 *
 * <p>If reservation fails (e.g. insufficient stock) a {@link ProductReservationFailedEvent}
 * is published so the orchestrator can start the compensation chain immediately.
 */
@Component
@KafkaListener(topics = "${products.commands.topic.name}")
public class ProductCommandsHandler {

    private static final Logger log = LoggerFactory.getLogger(ProductCommandsHandler.class);

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String productEventsTopicName;

    public ProductCommandsHandler(ProductService productService,
                                  KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${products.events.topic.name}") String productEventsTopicName) {
        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.productEventsTopicName = productEventsTopicName;
    }

    // -------------------------------------------------------------------------
    // Happy path — reserve stock
    // -------------------------------------------------------------------------

    @KafkaHandler
    public void handleCommand(@Payload ReserveProductCommand command) {
        log.info("[COMMAND] ReserveProductCommand received for orderId={}, productId={}, qty={}",
                command.getOrderId(), command.getProductId(), command.getProductQuantity());

        try {
            Product desired = new Product(command.getProductId(), command.getProductQuantity());
            Product reserved = productService.reserve(desired, command.getOrderId());

            ProductReservedEvent event = new ProductReservedEvent(
                    command.getOrderId(),
                    command.getProductId(),
                    reserved.getPrice(),
                    command.getProductQuantity()
            );
            log.info("[COMMAND] → Product reserved. Publishing ProductReservedEvent for orderId={}",
                    command.getOrderId());
            kafkaTemplate.send(productEventsTopicName, event);

        } catch (Exception e) {
            log.error("[COMMAND] Product reservation FAILED for orderId={}: {}",
                    command.getOrderId(), e.getMessage(), e);

            ProductReservationFailedEvent failedEvent = new ProductReservationFailedEvent(
                    command.getProductId(),
                    command.getOrderId(),
                    command.getProductQuantity()
            );
            kafkaTemplate.send(productEventsTopicName, failedEvent);
        }
    }

    // -------------------------------------------------------------------------
    // Compensation — restore stock (triggered after payment failure)
    // -------------------------------------------------------------------------

    @KafkaHandler
    public void handleCommand(@Payload CancelProductReservationCommand command) {
        log.info("[COMMAND] CancelProductReservationCommand received for orderId={}, productId={}, qty={}",
                command.getOrderId(), command.getProductId(), command.getProductQuantity());

        try {
            Product productToCancel = new Product(command.getProductId(), command.getProductQuantity());
            productService.cancelReservation(productToCancel, command.getOrderId());

            ProductReservationCancelledEvent event = new ProductReservationCancelledEvent(
                    command.getOrderId(),
                    command.getProductId(),
                    command.getProductQuantity()
            );
            log.info("[COMMAND] → Stock restored. Publishing ProductReservationCancelledEvent for orderId={}",
                    command.getOrderId());
            kafkaTemplate.send(productEventsTopicName, event);

        } catch (Exception e) {
            // Compensation failures must not be silently swallowed.
            // Log as ERROR so ops can intervene; the saga orchestrator will
            // still receive no ProductReservationCancelledEvent and can time-out
            // or alert via a Dead Letter Topic.
            log.error("[COMMAND] COMPENSATION FAILED — could not cancel reservation for orderId={}: {}",
                    command.getOrderId(), e.getMessage(), e);
        }
    }
}
