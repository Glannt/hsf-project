package com.hsf.hsfproject.service.order;

import com.hsf.hsfproject.dtos.request.OrderDto;
import com.hsf.hsfproject.dtos.request.OrderRequest;
import com.hsf.hsfproject.model.Order;

import java.util.List;

public interface IOrderService {

    // Define methods for order management here
    // For example:
    Order createOrder(OrderRequest request);

    Order getOrderById(String orderId);

    List<Order> getOrdersByUserId(String userId);

    void cancelOrder(String orderId);

    List<Order> getAllOrders();

    Order acceptOrder(Order order, String shippingAddress, String stripeTransactionId);

    Order saveOrder(Order order);

    Order findOrderByOrderNumber(String orderNumber);
}
