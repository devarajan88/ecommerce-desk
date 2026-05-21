package com.appsdeveloperblog.orders.web.graphql;

import com.appsdeveloperblog.core.dto.Order;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.dto.CreateOrderResponse;
import com.appsdeveloperblog.orders.dto.OrderHistory;
import com.appsdeveloperblog.orders.service.OrderHistoryService;
import com.appsdeveloperblog.orders.service.OrderService;
import com.appsdeveloperblog.orders.web.graphql.input.CreateOrderInput;
import org.springframework.beans.BeanUtils;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class OrdersGraphQLController {

    private final OrderService orderService;
    private final OrderHistoryService orderHistoryService;

    public OrdersGraphQLController(OrderService orderService, OrderHistoryService orderHistoryService) {
        this.orderService = orderService;
        this.orderHistoryService = orderHistoryService;
    }

    @QueryMapping
    public List<Order> orders() {
        return orderService.findAllOrders();
    }

    @QueryMapping
    public List<OrderHistory> orderHistory(@Argument UUID orderId) {
        return orderHistoryService.findByOrderId(orderId);
    }

    @MutationMapping
    public CreateOrderResponse placeOrder(@Argument CreateOrderInput input) {
        var order = new Order();
        order.setCustomerId(input.customerId());
        order.setProductId(input.productId());
        order.setProductQuantity(input.productQuantity());
        Order created = orderService.placeOrder(order);
        var response = new CreateOrderResponse();
        BeanUtils.copyProperties(created, response);
        return response;
    }

    @MutationMapping
    public Order updateOrderStatus(@Argument UUID orderId, @Argument OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @MutationMapping
    public boolean deleteOrder(@Argument UUID orderId) {
        orderService.deleteOrder(orderId);
        return true;
    }
}
