package com.hsf.hsfproject.service.order;

import com.hsf.hsfproject.dtos.request.OrderDto;
import com.hsf.hsfproject.dtos.request.OrderRequest;
import com.hsf.hsfproject.model.Order;

import java.util.List;
import java.util.UUID;


import org.springframework.data.domain.Page;


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


    Page<Order> getOrderList(int page, int limit);

    Order updateOrderStatus(UUID orderId, String status);

    Order findById(UUID orderId);
}
