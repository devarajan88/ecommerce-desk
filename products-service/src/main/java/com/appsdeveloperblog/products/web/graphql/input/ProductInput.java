package com.appsdeveloperblog.products.web.graphql.input;

import java.math.BigDecimal;

public record ProductInput(String name, BigDecimal price, Integer quantity) {}
