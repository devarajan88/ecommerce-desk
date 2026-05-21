package com.appsdeveloperblog.orders.web.graphql.input;

import java.util.UUID;

public record CreateOrderInput(UUID customerId, UUID productId, Integer productQuantity) {}
